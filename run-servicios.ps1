param(
    [switch]$SkipBuild,
    [switch]$NoKeycloak
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$runDirectory = Join-Path $repoRoot ".servicios"
$composeFile = Join-Path $repoRoot "docker-compose.integration.yml"
$keycloakEnv = Join-Path $repoRoot "keycloak\.env"
$credentialsEnv = Join-Path $repoRoot "servicio-incentivos\credentials.env"
$mavenCommand = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue
if ($null -eq $mavenCommand) { $mavenCommand = Get-Command "mvn" -ErrorAction SilentlyContinue }
if ($null -eq $mavenCommand) {
    throw "No se encontró Maven. Verificá que 'mvn -version' funcione en esta terminal."
}

$services = @(
    @{ Name = "donaciones"; Module = "servicio-donaciones"; Port = 8080 },
    @{ Name = "incentivos"; Module = "servicio-incentivos"; Port = 8081 },
    @{ Name = "notificaciones"; Module = "servicio-notificaciones"; Port = 8082 },
    @{ Name = "logistica"; Module = "servicio-logistica"; Port = 8083 },
    # El cliente web va al final porque consume las APIs.
    @{ Name = "web"; Module = "servicio-web"; Port = 8085 }
)

New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null

function Read-EnvFile([string]$Path) {
    $values = @{}
    if (-not (Test-Path -LiteralPath $Path)) { return $values }
    foreach ($rawLine in [System.IO.File]::ReadAllLines($Path)) {
        $line = $rawLine.TrimEnd("`r")
        if (-not $line.Trim() -or $line.TrimStart().StartsWith("#")) { continue }
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') {
            $key = $Matches[1]
            $value = $Matches[2]
            $value = $value.Trim()
            # Las comillas exteriores delimitan el valor; no forman parte de él.
            if ($value.Length -ge 2 -and (
                ($value.StartsWith('"') -and $value.EndsWith('"')) -or
                ($value.StartsWith("'") -and $value.EndsWith("'"))
            )) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            $values[$key] = $value
        }
    }
    return $values
}

function Invoke-Compose([string[]]$Arguments, [switch]$AuthProfile) {
    $composeArgs = @()
    if (Test-Path -LiteralPath $keycloakEnv) { $composeArgs += @("--env-file", $keycloakEnv) }
    $composeArgs += @("-f", $composeFile)
    if ($AuthProfile) { $composeArgs += @("--profile", "auth") }
    & docker compose @composeArgs @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Falló docker compose $($Arguments -join ' ')." }
}

function Remove-PreviousAdminPassword {
    $lines = [System.IO.File]::ReadAllLines($keycloakEnv)
    $remaining = @($lines | Where-Object { $_ -notmatch '^\s*KEYCLOAK_ADMIN_PASSWORD_PREVIOUS\s*=' })
    if ($remaining.Count -eq $lines.Count) { return }
    $encoding = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllLines($keycloakEnv, $remaining, $encoding)
    Write-Host "Se retiró la contraseña anterior de administrador del archivo local después de la rotación."
}

function Start-Keycloak {
    if (-not (Test-Path -LiteralPath $keycloakEnv)) { return }

    $keycloakValues = Read-EnvFile $keycloakEnv
    foreach ($key in @(
        "KEYCLOAK_ADMIN_PASSWORD", "KEYCLOAK_DB_PASSWORD", "KEYCLOAK_DB_ROOT_PASSWORD"
    )) {
        if (-not $keycloakValues.ContainsKey($key) -or -not $keycloakValues[$key] -or $keycloakValues[$key].StartsWith("definir-")) {
            throw "Falta configurar $key en keycloak\.env; no se inicia Keycloak."
        }
    }
    foreach ($mapping in @(
        @{ Key = "KEYCLOAK_WEB_CLIENT_SECRET"; Module = "servicio-web" },
        @{ Key = "KEYCLOAK_DONACIONES_CLIENT_SECRET"; Module = "servicio-donaciones" },
        @{ Key = "KEYCLOAK_INCENTIVOS_CLIENT_SECRET"; Module = "servicio-incentivos" }
    )) {
        $clientSecret = if ($keycloakValues.ContainsKey($mapping.Key)) { $keycloakValues[$mapping.Key] } else { "" }
        if (-not $clientSecret) {
            $serviceValues = Read-EnvFile (Join-Path $repoRoot ($mapping.Module + "\.env"))
            if ($serviceValues.ContainsKey("KEYCLOAK_CLIENT_SECRET")) { $clientSecret = $serviceValues["KEYCLOAK_CLIENT_SECRET"] }
        }
        $clientSecret = [string]$clientSecret
        if (-not $clientSecret -or $clientSecret -notmatch '^[A-Za-z0-9._~-]{16,}$' -or $clientSecret.StartsWith("definir-") -or $clientSecret.StartsWith("pegar-")) {
            throw "Configurá un KEYCLOAK_CLIENT_SECRET válido para $($mapping.Module) en keycloak\.env o su .env local."
        }
    }

    Write-Host "Levantando Keycloak..."
    Invoke-Compose -Arguments @("up", "-d", "keycloak") -AuthProfile

    Write-Host "Esperando a que Keycloak esté sano (hasta 3 minutos)..."
    $health = ""
    for ($i = 0; $i -lt 90; $i++) {
        $health = (& docker inspect --format '{{.State.Health.Status}}' donatrack-keycloak 2>$null | Out-String).Trim()
        if ($health -eq "healthy") { break }
        if ($health -eq "unhealthy") { throw "Keycloak quedó unhealthy. Revisá: docker logs donatrack-keycloak --tail 100" }
        Start-Sleep -Seconds 2
    }
    if ($health -ne "healthy") { throw "Keycloak no quedó healthy a tiempo. Revisá: docker logs donatrack-keycloak --tail 100" }

    Write-Host "Aplicando configuración del realm donatrack..."
    Invoke-Compose -Arguments @("run", "--rm", "--no-deps", "keycloak-config") -AuthProfile
    Remove-PreviousAdminPassword
}

if (-not (Test-Path -LiteralPath $credentialsEnv)) {
    Write-Host "No existe $credentialsEnv; se crea vacío (completarlo si el workflow requiere credenciales de Discord/Sheets)."
    New-Item -ItemType File -Path $credentialsEnv -Force | Out-Null
}

Write-Host "Levantando infraestructura (RabbitMQ, MySQL, Mongo, Adminer, n8n y Mailpit)..."
Invoke-Compose -Arguments @("up", "-d")
# El webhook de n8n aparece después de importar y publicar el workflow.
Write-Host "Esperando a que n8n registre el webhook (hasta 60s)..."
for ($i = 0; $i -lt 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:5678/webhook/events/insignia-otorgada" -Method Post -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -ne 404) { break }
    } catch {
        $statusCode = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        if ($statusCode -ne 404 -and $statusCode -ne 0) { break }
    }
    Start-Sleep -Seconds 2
}

$authEnabled = (Test-Path -LiteralPath $keycloakEnv) -and -not $NoKeycloak
if ($authEnabled) {
    Start-Keycloak
    $keycloakValues = Read-EnvFile $keycloakEnv
} elseif ($NoKeycloak) {
    Write-Host "Modo sin Keycloak; se desactiva la seguridad de las APIs para este arranque."
} else {
    Write-Host "No se encontró keycloak\.env; se levantará el sistema sin autenticación."
}

if (-not $SkipBuild) {
    Write-Host "Compilando los módulos..."
    & $mavenCommand.Source "-f" (Join-Path $repoRoot "pom.xml") "-DskipTests" "package"
    if ($LASTEXITCODE -ne 0) { throw "La compilación falló. Revisá la salida de Maven antes de levantar los servicios." }
}

# Aislar las variables de cada .env: un secret de Donaciones no debe filtrarse
# al siguiente proceso. Cada Start-Process hereda solo su configuración.
$environmentFiles = @($keycloakEnv) + @($services | ForEach-Object { Join-Path $repoRoot ($_.Module + "\.env") })
$managedKeys = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
foreach ($file in $environmentFiles) {
    foreach ($key in (Read-EnvFile $file).Keys) { [void]$managedKeys.Add($key) }
}
[void]$managedKeys.Add("KEYCLOAK_CLIENT_SECRET")
[void]$managedKeys.Add("SEGURIDAD_HABILITADA")
$baselineEnvironment = @{}
foreach ($key in $managedKeys) {
    $baselineEnvironment[$key] = [System.Environment]::GetEnvironmentVariable($key, "Process")
}

foreach ($service in $services) {
    $pidFile = Join-Path $runDirectory ($service.Name + ".pid")
    if (Test-Path -LiteralPath $pidFile) {
        $existingPid = Get-Content -LiteralPath $pidFile -ErrorAction SilentlyContinue
        if ($existingPid -and (Get-Process -Id $existingPid -ErrorAction SilentlyContinue)) {
            Write-Host ($service.Name + " ya está iniciado (PID " + $existingPid + ").")
            continue
        }
    }

    foreach ($key in $managedKeys) {
        [System.Environment]::SetEnvironmentVariable($key, $baselineEnvironment[$key], "Process")
    }
    $serviceEnvFile = Join-Path $repoRoot ($service.Module + "\.env")
    $serviceValues = Read-EnvFile $serviceEnvFile
    foreach ($key in $serviceValues.Keys) {
        [System.Environment]::SetEnvironmentVariable($key, $serviceValues[$key], "Process")
    }
    foreach ($key in @(
        "KEYCLOAK_ADMIN_USERNAME", "KEYCLOAK_ADMIN_PASSWORD", "KEYCLOAK_ADMIN_PASSWORD_PREVIOUS",
        "KEYCLOAK_DB_PASSWORD", "KEYCLOAK_DB_ROOT_PASSWORD", "KEYCLOAK_WEB_CLIENT_SECRET",
        "KEYCLOAK_DONACIONES_CLIENT_SECRET", "KEYCLOAK_INCENTIVOS_CLIENT_SECRET",
        "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET"
    )) {
        [System.Environment]::SetEnvironmentVariable($key, $null, "Process")
    }

    if ($authEnabled) {
        if (-not $serviceValues.ContainsKey("SEGURIDAD_HABILITADA") -or -not $serviceValues["SEGURIDAD_HABILITADA"]) {
            [System.Environment]::SetEnvironmentVariable("SEGURIDAD_HABILITADA", "true", "Process")
        }
        $secretKey = switch ($service.Module) {
            "servicio-web" { "KEYCLOAK_WEB_CLIENT_SECRET" }
            "servicio-donaciones" { "KEYCLOAK_DONACIONES_CLIENT_SECRET" }
            "servicio-incentivos" { "KEYCLOAK_INCENTIVOS_CLIENT_SECRET" }
            default { $null }
        }
        if ($secretKey) {
            if ($keycloakValues.ContainsKey($secretKey) -and $keycloakValues[$secretKey]) {
                [System.Environment]::SetEnvironmentVariable("KEYCLOAK_CLIENT_SECRET", $keycloakValues[$secretKey], "Process")
            }
        } else {
            [System.Environment]::SetEnvironmentVariable("KEYCLOAK_CLIENT_SECRET", $null, "Process")
        }
    } else {
        [System.Environment]::SetEnvironmentVariable("SEGURIDAD_HABILITADA", "false", "Process")
        [System.Environment]::SetEnvironmentVariable("KEYCLOAK_CLIENT_SECRET", $null, "Process")
    }

    $pomPath = Join-Path $repoRoot ($service.Module + "\pom.xml")
    $stdoutPath = Join-Path $runDirectory ($service.Name + ".out.log")
    $stderrPath = Join-Path $runDirectory ($service.Name + ".err.log")
    $process = Start-Process `
        -FilePath $mavenCommand.Source `
        -ArgumentList @("-f", $pomPath, "-Dspring-boot.run.arguments=--server.port=$($service.Port)", "spring-boot:run") `
        -WorkingDirectory $repoRoot `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -WindowStyle Hidden `
        -PassThru

    Set-Content -LiteralPath $pidFile -Value $process.Id
    Write-Host ($service.Name + " iniciado en el puerto " + $service.Port + " (PID " + $process.Id + ").")
}

foreach ($key in $managedKeys) {
    [System.Environment]::SetEnvironmentVariable($key, $baselineEnvironment[$key], "Process")
}

Write-Host ""
Write-Host "Swagger:"
Write-Host "  Donaciones:     http://localhost:8080/swagger-ui.html"
Write-Host "  Incentivos:     http://localhost:8081/swagger-ui/index.html"
Write-Host "  Notificaciones: http://localhost:8082/swagger-ui/index.html"
Write-Host "  Logística:      http://localhost:8083/swagger-ui/index.html"
Write-Host ""
Write-Host "Cliente web:     http://localhost:8085"
Write-Host "Keycloak:        http://localhost:8180"
Write-Host ""
Write-Host ("Logs disponibles en " + $runDirectory)
Write-Host "n8n: http://localhost:5678  |  RabbitMQ: http://localhost:15672  |  Adminer: http://localhost:8090  |  Mailpit: http://localhost:8025"
Write-Host "Para detener los servicios: .\stop-servicios.ps1"
