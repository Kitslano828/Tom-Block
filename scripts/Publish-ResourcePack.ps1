param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^pack-v(?:0|[1-9][0-9]*)(?:\.(?:0|[1-9][0-9]*)){0,2}$')]
    [string]$Tag,

    [string]$PackRepository,
    [string]$SshHost = 'tom@tom-ROG-Strix-SCAR-18-G834JYR-G834JYR.local',
    [string]$SshKey = (Join-Path $env:USERPROFILE '.ssh\codex_tomblock_ubuntu'),
    [switch]$Resume
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $PackRepository) {
    $PackRepository = Join-Path (Split-Path -Parent $projectRoot) 'TomBlock-Resource-Pack'
}
$packRepository = (Resolve-Path -LiteralPath $PackRepository).Path
$sourcePack = Join-Path $projectRoot 'resource-pack'
$serverProperties = '/opt/tomblock/build-server/server.properties'
$zipUrl = "https://github.com/Kitslano828/TomBlock-Resource-Pack/releases/download/$Tag/TomBlock-Resource-Pack.zip"
$protocolManifest = ConvertFrom-StringData (Get-Content -LiteralPath (Join-Path $projectRoot 'src\main\resources\hud-protocol.properties') -Raw)
$packId = $protocolManifest['pack.id']
if ($packId -notmatch '^[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$') {
    throw 'HUD protocol pack.id is missing or invalid.'
}

function Invoke-Git([string[]]$Arguments) {
    & git -C $packRepository @Arguments
    if ($LASTEXITCODE -ne 0) { throw "git $($Arguments -join ' ') failed" }
}

if (-not (Test-Path -LiteralPath (Join-Path $packRepository '.git'))) {
    throw "Not a Git repository: $packRepository"
}
if (-not (Test-Path -LiteralPath $SshKey)) { throw "SSH key not found: $SshKey" }
$existingChanges = @(& git -C $packRepository status --porcelain)
if ($existingChanges.Count -gt 0 -and -not $Resume) {
    throw 'The public pack repository has uncommitted changes. Review or commit them before publishing. Use -Resume only after a previous publishing attempt copied the current source pack and stopped at a safety check.'
}
if ($Resume) {
    $unsafeChanges = @($existingChanges | Where-Object {
        $path = $_.Substring(3).Replace('\\', '/')
        $path -ne 'pack.mcmeta' -and -not $path.StartsWith('assets/') -and -not $path.StartsWith('licenses/')
    })
    if ($unsafeChanges.Count -gt 0) {
        throw "Cannot resume because the public pack contains changes outside managed pack files: $($unsafeChanges -join ', ')"
    }
}
Invoke-Git @('fetch', 'origin', 'main', '--tags')
if ((& git -C $packRepository rev-list --count 'HEAD..origin/main') -ne '0') {
    throw 'The public pack repository is behind origin/main. Pull those changes before publishing.'
}
if ((& git -C $packRepository tag --list $Tag) -eq $Tag) { throw "Tag already exists: $Tag" }

# Copy only the actual resource-pack contents.
Copy-Item -LiteralPath (Join-Path $sourcePack 'pack.mcmeta') -Destination $packRepository -Force
$assetRoot = Join-Path $sourcePack 'assets'
$sourceAssetPaths = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
Get-ChildItem -LiteralPath $assetRoot -File -Recurse | ForEach-Object {
    $relativePath = $_.FullName.Substring($assetRoot.Length + 1)
    [void]$sourceAssetPaths.Add($relativePath)
    $target = Join-Path (Join-Path $packRepository 'assets') $relativePath
    New-Item -ItemType Directory -Path (Split-Path -Parent $target) -Force | Out-Null
    Copy-Item -LiteralPath $_.FullName -Destination $target -Force
}
$publishedAssetRoot = Join-Path $packRepository 'assets'
$staleFiles = @(Get-ChildItem -LiteralPath $publishedAssetRoot -File -Recurse | Where-Object {
    -not $sourceAssetPaths.Contains($_.FullName.Substring($publishedAssetRoot.Length + 1))
})
if ($staleFiles.Count -gt 0) {
    Write-Host "Removing source-deleted pack files: $($staleFiles.FullName -join ', ')"
    $staleFiles | ForEach-Object { Remove-Item -LiteralPath $_.FullName }
}
$sourceLicenseRoot = Join-Path $sourcePack 'licenses'
$publishedLicenseRoot = Join-Path $packRepository 'licenses'
New-Item -ItemType Directory -Path $publishedLicenseRoot -Force | Out-Null
$sourceLicenseNames = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
Get-ChildItem -LiteralPath $sourceLicenseRoot -File | ForEach-Object {
    [void]$sourceLicenseNames.Add($_.Name)
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $publishedLicenseRoot $_.Name) -Force
}
$staleLicenses = @(Get-ChildItem -LiteralPath $publishedLicenseRoot -File | Where-Object {
    -not $sourceLicenseNames.Contains($_.Name)
})
if ($staleLicenses.Count -gt 0) {
    Write-Host "Removing source-deleted licence files: $($staleLicenses.FullName -join ', ')"
    $staleLicenses | ForEach-Object { Remove-Item -LiteralPath $_.FullName }
}
Invoke-Git @('add', '--', 'pack.mcmeta', 'assets', 'licenses')
& git -C $packRepository diff --cached --quiet
if ($LASTEXITCODE -eq 0) {
    throw 'The public pack repository already matches the source pack; there is no new pack to publish.'
}

Write-Host 'Pack changes to publish:'
Invoke-Git @('diff', '--cached', '--stat')
$confirmation = Read-Host "Publish these pack files publicly as $Tag and configure the build server? Type PUBLISH"
if ($confirmation -cne 'PUBLISH') { throw 'Publishing cancelled. Copied files remain staged for review.' }

Invoke-Git @('commit', '-m', "Publish resource pack $Tag")
Invoke-Git @('tag', $Tag)
Invoke-Git @('push', 'origin', 'main')
Invoke-Git @('push', 'origin', $Tag)

# The existing GitHub Actions workflow creates a ZIP release from this tag.
$download = Join-Path $env:TEMP "TomBlock-Resource-Pack-$Tag.zip"
try {
    $available = $false
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        try {
            Invoke-WebRequest -Uri $zipUrl -OutFile $download -MaximumRedirection 10 -ErrorAction Stop | Out-Null
            $available = $true
            break
        } catch {
            Start-Sleep -Seconds 5
        }
    }
    if (-not $available) { throw "Release ZIP did not become available. Check GitHub Actions, then configure the server manually: $zipUrl" }
    $zip = [System.IO.Compression.ZipFile]::OpenRead($download)
    try {
        if (-not ($zip.Entries | Where-Object FullName -eq 'pack.mcmeta')) { throw 'Release ZIP lacks pack.mcmeta at its root.' }
    } finally { $zip.Dispose() }
    $sha1 = (Get-FileHash -LiteralPath $download -Algorithm SHA1).Hash.ToLowerInvariant()
} finally {
    Remove-Item -LiteralPath $download -ErrorAction SilentlyContinue
}

# Run a short Python program remotely, with values supplied only from the validated tag and computed hash.
$remoteScript = @'
from pathlib import Path
import os
import sys
import tempfile

path = Path(sys.argv[1])
url = sys.argv[2]
sha1 = sys.argv[3]
updates = {
    'require-resource-pack': 'true',
    'resource-pack': url.replace(':', r'\:'),
    'resource-pack-sha1': sha1,
    'resource-pack-id': sys.argv[4],
}
original = path.read_text()
lines = original.splitlines()
seen = set()
result = []
for line in lines:
    key = line.split('=', 1)[0]
    if key in updates:
        result.append(f'{key}={updates[key]}')
        seen.add(key)
    else:
        result.append(line)
for key in updates.keys() - seen:
    result.append(f'{key}={updates[key]}')
backup = path.with_name(path.name + '.tomblock-backup')
backup.write_text(original)
os.chmod(backup, path.stat().st_mode)
fd, temporary = tempfile.mkstemp(prefix='server.properties.', dir=path.parent)
try:
    os.chmod(temporary, path.stat().st_mode)
    with os.fdopen(fd, 'w') as output:
        output.write('\n'.join(result) + '\n')
    os.replace(temporary, path)
finally:
    if os.path.exists(temporary):
        os.unlink(temporary)
'@
$remoteScript | & ssh -i $SshKey -o BatchMode=yes $SshHost "python3 - '$serverProperties' '$zipUrl' '$sha1' '$packId'"
if ($LASTEXITCODE -ne 0) { throw 'Release published, but Ubuntu server.properties update failed.' }

Write-Host "Published $Tag and configured the Ubuntu build server to require it."
Write-Host "SHA-1: $sha1"
Write-Host 'Restart the build server when convenient: ssh -i <your key> tom@<Ubuntu host> "sudo systemctl restart tomblock-build.service"'
