[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [ValidateSet("donaciones", "incentivos", "notificaciones", "logistica")]
    [string[]]$Service = @("donaciones", "incentivos", "notificaciones", "logistica")
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$runDirectory = Join-Path $repoRoot ".servicios"
$modules = @{
    donaciones = "servicio-donaciones"
    incentivos = "servicio-incentivos"
    notificaciones = "servicio-notificaciones"
    logistica = "servicio-logistica"
}
$failed = $false

foreach ($name in $Service) {
    $pidFile = Join-Path $runDirectory ($name + ".pid")
    if (-not (Test-Path -LiteralPath $pidFile)) {
        Write-Host "$name no tiene un proceso registrado."
        continue
    }

    $serviceProcessId = 0
    $storedPid = (Get-Content -LiteralPath $pidFile -Raw).Trim()
    if (-not [int]::TryParse($storedPid, [ref]$serviceProcessId) -or $serviceProcessId -le 0) {
        Write-Warning "PID invalido para $name. No se detuvo ningun proceso."
        $failed = $true
        continue
    }

    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $serviceProcessId"
    if ($null -eq $process) {
        if ($PSCmdlet.ShouldProcess($pidFile, "Eliminar PID de proceso finalizado")) {
            Remove-Item -LiteralPath $pidFile
        }
        Write-Host "$name ya estaba detenido."
        continue
    }

    # Evita detener un programa ajeno si Windows reutilizo un PID antiguo.
    $pomPath = Join-Path $repoRoot ($modules[$name] + "\pom.xml")
    if (-not $process.CommandLine -or
        $process.CommandLine.IndexOf($pomPath, [StringComparison]::OrdinalIgnoreCase) -lt 0 -or
        $process.CommandLine.IndexOf("spring-boot:run", [StringComparison]::OrdinalIgnoreCase) -lt 0) {
        Write-Warning "El PID $serviceProcessId no corresponde al arranque de $name. No se detuvo."
        $failed = $true
        continue
    }

    if ($PSCmdlet.ShouldProcess("$name (PID $serviceProcessId)", "Terminar Maven y sus procesos hijos")) {
        # /T incluye Java; /F permite cerrar los procesos de consola ocultos.
        & taskkill.exe /PID $serviceProcessId /T /F
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "No se pudo detener $name. Se conserva su archivo PID."
            $failed = $true
            continue
        }
        Remove-Item -LiteralPath $pidFile
        Write-Host "$name detenido."
    }
}

if ($failed) {
    throw "No se pudieron detener todos los servicios solicitados. Revisa los avisos anteriores."
}
