param(
    [string]$SshHost = 'tom@tom-ROG-Strix-SCAR-18-G834JYR-G834JYR.local',
    [string]$SshKey = (Join-Path $env:USERPROFILE '.ssh\codex_tomblock_ubuntu'),
    [string]$RemoteServer = '/opt/tomblock/build-server',
    [string]$Service = 'tomblock-build.service',
    [switch]$Restart
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $projectRoot 'build\libs\TomBlock.jar'
$islandEdgeJar = Join-Path $projectRoot 'build\island-edge\TomBlock-Island-Edge.jar'
$worldMap = Join-Path $projectRoot 'src\main\resources\maps\world.png'

if (-not (Test-Path -LiteralPath $SshKey)) { throw "SSH key not found: $SshKey" }
if ($RemoteServer -notmatch '^/[A-Za-z0-9._/-]+$') { throw 'RemoteServer contains unsupported characters.' }
if ($Service -notmatch '^[A-Za-z0-9_.@-]+\.service$') { throw 'Invalid systemd service name.' }

Push-Location $projectRoot
try {
    & .\gradlew.bat test jar islandEdgeJar --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Tests or plugin packaging failed; nothing was deployed.' }
} finally {
    Pop-Location
}

$remoteNext = "$RemoteServer/plugins/TomBlock.jar.next"
$remoteIslandEdgeNext = "$RemoteServer/plugins/TomBlock-Island-Edge.jar.next"
& scp -i $SshKey -o BatchMode=yes $jar "${SshHost}:$remoteNext"
if ($LASTEXITCODE -ne 0) { throw 'JAR upload failed.' }
& scp -i $SshKey -o BatchMode=yes $islandEdgeJar "${SshHost}:$remoteIslandEdgeNext"
if ($LASTEXITCODE -ne 0) { throw 'Island Edge JAR upload failed.' }
& scp -i $SshKey -o BatchMode=yes $worldMap "${SshHost}:/tmp/tomblock-world.png"
if ($LASTEXITCODE -ne 0) { throw 'World-map upload failed.' }

# Install atomically and retain one immediately usable rollback artifact.
$install = "set -eu; cd '$RemoteServer/plugins'; test -s TomBlock.jar.next; " +
           "test -s TomBlock-Island-Edge.jar.next; " +
           "if test -f TomBlock.jar; then cp -p TomBlock.jar TomBlock.jar.rollback-next; " +
           "mv -f TomBlock.jar.rollback-next TomBlock.jar.previous; fi; " +
           "if test -f TomBlock-Island-Edge.jar; then cp -p TomBlock-Island-Edge.jar TomBlock-Island-Edge.jar.rollback-next; " +
           "mv -f TomBlock-Island-Edge.jar.rollback-next TomBlock-Island-Edge.jar.previous; fi; " +
           "mv TomBlock.jar.next TomBlock.jar; mv TomBlock-Island-Edge.jar.next TomBlock-Island-Edge.jar; " +
           "mkdir -p TomBlock/maps; " +
           "mv /tmp/tomblock-world.png TomBlock/maps/world.png"
& ssh -i $SshKey -o BatchMode=yes $SshHost $install
if ($LASTEXITCODE -ne 0) { throw 'JAR installation failed.' }

Write-Host "Deployed TomBlock.jar and TomBlock-Island-Edge.jar to ${SshHost}:$RemoteServer"
if (-not $Restart) {
    Write-Host 'Paper was not restarted. Re-run with -Restart when players can be disconnected.'
    exit 0
}

# -t permits sudo to prompt on installations that do not grant passwordless
# control of this one service.
$restartEpoch = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
& ssh -t -i $SshKey $SshHost "sudo systemctl restart '$Service'"
if ($LASTEXITCODE -ne 0) { throw 'Service restart failed. TomBlock.jar.previous remains available.' }
$healthy = $false
$startupLog = @()
for ($attempt = 1; $attempt -le 15; $attempt++) {
    Start-Sleep -Seconds 2
    & ssh -i $SshKey -o BatchMode=yes $SshHost "systemctl is-active --quiet '$Service'"
    if ($LASTEXITCODE -ne 0) { break }
    $startupLog = @(& ssh -i $SshKey -o BatchMode=yes $SshHost "journalctl -u '$Service' --since '@$restartEpoch' --no-pager")
    if ($startupLog -match 'TomBlock startup complete\.') {
        $healthy = $true
        break
    }
    if ($startupLog -match 'Error occurred while enabling TomBlock') { break }
}
if (-not $healthy) {
    Write-Warning 'TomBlock did not emit its startup-complete marker. Restoring the previous JAR.'
    $rollback = "set -eu; cd '$RemoteServer/plugins'; test -f TomBlock.jar.previous; " +
                "cp -p TomBlock.jar.previous TomBlock.jar; " +
                "if test -f TomBlock-Island-Edge.jar.previous; then " +
                "cp -p TomBlock-Island-Edge.jar.previous TomBlock-Island-Edge.jar; fi"
    & ssh -i $SshKey -o BatchMode=yes $SshHost $rollback
    & ssh -t -i $SshKey $SshHost "sudo systemctl restart '$Service'"
    throw 'Deployment failed its health check and the previous JAR was restored.'
}

$startupLog | Select-Object -Last 80
Write-Host 'Build server deployment is active.'
