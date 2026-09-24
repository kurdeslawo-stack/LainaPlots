param(
    [Parameter(Mandatory = $true)]
    [string]$Sheet
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$destination = Join-Path $projectRoot 'resourcepack\assets\lainaplots\textures\item'
New-Item -ItemType Directory -Path $destination -Force | Out-Null

$names = @('owner_plot', 'member_plot', 'favorite_owner_plot', 'favorite_member_plot', 'summary')
$source = [System.Drawing.Bitmap]::new($Sheet)
try {
    for ($index = 0; $index -lt $names.Count; $index++) {
        $cellLeft = [int][Math]::Floor($source.Width * $index / $names.Count)
        $cellRight = [int][Math]::Floor($source.Width * ($index + 1) / $names.Count) - 1
        $minX = $cellRight
        $minY = $source.Height - 1
        $maxX = $cellLeft
        $maxY = 0

        for ($y = 0; $y -lt $source.Height; $y++) {
            for ($x = $cellLeft; $x -le $cellRight; $x++) {
                if ($source.GetPixel($x, $y).A -gt 8) {
                    $minX = [Math]::Min($minX, $x)
                    $minY = [Math]::Min($minY, $y)
                    $maxX = [Math]::Max($maxX, $x)
                    $maxY = [Math]::Max($maxY, $y)
                }
            }
        }

        if ($maxX -lt $minX -or $maxY -lt $minY) {
            throw "Cell $index is empty"
        }

        $width = $maxX - $minX + 1
        $height = $maxY - $minY + 1
        $scale = [Math]::Min(58.0 / $width, 58.0 / $height)
        $targetWidth = [Math]::Max(1, [int][Math]::Round($width * $scale))
        $targetHeight = [Math]::Max(1, [int][Math]::Round($height * $scale))
        $targetX = [int][Math]::Floor((64 - $targetWidth) / 2)
        $targetY = 62 - $targetHeight

        $output = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($output)
            try {
                $graphics.Clear([System.Drawing.Color]::Transparent)
                $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
                $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
                $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
                $graphics.DrawImage(
                    $source,
                    [System.Drawing.Rectangle]::new($targetX, $targetY, $targetWidth, $targetHeight),
                    [System.Drawing.Rectangle]::new($minX, $minY, $width, $height),
                    [System.Drawing.GraphicsUnit]::Pixel
                )
            } finally {
                $graphics.Dispose()
            }
            $output.Save((Join-Path $destination ($names[$index] + '.png')), [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $output.Dispose()
        }
    }
} finally {
    $source.Dispose()
}

Get-ChildItem -LiteralPath $destination -Filter '*.png' | Select-Object Name, Length
