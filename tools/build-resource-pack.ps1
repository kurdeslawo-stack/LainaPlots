$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$source = Join-Path $projectRoot 'resourcepack'
$destination = Join-Path $projectRoot 'src\main\resources\LainaPlots-Icons.zip'

if (-not (Test-Path -LiteralPath (Join-Path $source 'pack.mcmeta') -PathType Leaf)) {
    throw "Missing resource-pack source: $source"
}

if (Test-Path -LiteralPath $destination) {
    Remove-Item -LiteralPath $destination -Force
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stream = [System.IO.File]::Open($destination, [System.IO.FileMode]::CreateNew)
$writer = New-Object System.IO.Compression.ZipArchive(
    $stream,
    [System.IO.Compression.ZipArchiveMode]::Create,
    $false
)
try {
    Get-ChildItem -LiteralPath $source -File -Recurse | ForEach-Object {
        $relativePath = $_.FullName.Substring($source.Length).TrimStart('\', '/')
        $entryName = $relativePath.Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $writer,
            $_.FullName,
            $entryName,
            [System.IO.Compression.CompressionLevel]::Optimal
        ) | Out-Null
    }
} finally {
    $writer.Dispose()
    $stream.Dispose()
}

$archive = [System.IO.Compression.ZipFile]::OpenRead($destination)
try {
    $required = @(
        'pack.mcmeta',
        'assets/minecraft/items/paper.json',
        'assets/lainaplots/textures/item/owner_plot.png',
        'assets/lainaplots/textures/item/member_plot.png',
        'assets/lainaplots/textures/item/favorite_owner_plot.png',
        'assets/lainaplots/textures/item/favorite_member_plot.png',
        'assets/lainaplots/textures/item/summary.png'
    )
    # Windows PowerShell 5.1 zapisuje separatory katalogów jako backslashe,
    # podczas gdy PowerShell 7 używa slashy.
    $names = @($archive.Entries | ForEach-Object { $_.FullName.Replace('\', '/') })
    foreach ($entry in $required) {
        if ($entry -notin $names) {
            throw "Built pack is missing $entry"
        }
    }
} finally {
    $archive.Dispose()
}

Get-Item -LiteralPath $destination | Select-Object FullName, Length, LastWriteTime
