param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$runDirectory = Join-Path $repoRoot ".servicios"
$mavenCommand = Get-Command "mvn.cmd" -ErrorAction SilentlyContinue

if ($null -eq $mavenCommand) {
    $mavenCommand = Get-Command "mvn" -ErrorAction SilentlyContinue
}

if ($null -eq $mavenCommand) {
    throw "No se encontró Maven. Verificá que 'mvn -version' funcione en esta terminal."
}

$services = @(
    @{ Name = "donaciones"; Module = "servicio-donaciones"; Port = 8080 },
    @{ Name = "incentivos"; Module = "servicio-incentivos"; Port = 8081 },
    @{ Name = "notificaciones"; Module = "servicio-notificaciones"; Port = 8082 },
    @{ Name = "logistica"; Module = "servicio-logistica"; Port = 8083 }
)

New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null

if (-not $SkipBuild) {
    Write-Host "Compilando los modulos..."
    & $mavenCommand.Source "-f" (Join-Path $repoRoot "pom.xml") "-DskipTests" "package"
    if ($LASTEXITCODE -ne 0) {
        throw "La compilación falló. Revisá la salida de Maven antes de levantar los servicios."
    }
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

    $pomPath = Join-Path $repoRoot ($service.Module + "\pom.xml")
    $stdoutPath = Join-Path $runDirectory ($service.Name + ".out.log")
    $stderrPath = Join-Path $runDirectory ($service.Name + ".err.log")

    $process = Start-Process `
        -FilePath $mavenCommand.Source `
        -ArgumentList @("-f", $pomPath, "spring-boot:run") `
        -WorkingDirectory $repoRoot `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -WindowStyle Hidden `
        -PassThru

    Set-Content -LiteralPath $pidFile -Value $process.Id
    Write-Host ($service.Name + " iniciado en el puerto " + $service.Port + " (PID " + $process.Id + ").")
}

Write-Host ""
Write-Host "Swagger:"
Write-Host "  Donaciones:     http://localhost:8080/swagger-ui.html"
Write-Host "  Incentivos:     http://localhost:8081/swagger-ui/index.html"
Write-Host "  Notificaciones: http://localhost:8082/swagger-ui/index.html"
Write-Host "  Logística:      http://localhost:8083/swagger-ui/index.html"
Write-Host ""
Write-Host ("Logs disponibles en " + $runDirectory)
Write-Host "Para detener los servicios: .\stop-servicios.ps1"
