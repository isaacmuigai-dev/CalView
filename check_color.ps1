try {
    Add-Type -AssemblyName System.Drawing
    
    function Get-Color ($file) {
        $img = [System.Drawing.Bitmap]::FromFile($file)
        
        # Find first non-transparent pixel
        for ($x = $img.Width / 2; $x -lt $img.Width; $x++) {
            for ($y = $img.Height / 2; $y -lt $img.Height; $y++) {
                $pixel = $img.GetPixel($x, $y)
                if ($pixel.A -gt 200) {
                    $img.Dispose()
                    return "R=$($pixel.R) G=$($pixel.G) B=$($pixel.B)"
                }
            }
        }
        $img.Dispose()
        return "No opaque pixel found"
    }

    $img0 = "C:/Users/isaac/.gemini/antigravity/brain/c60fa4b0-2d6d-471c-a85e-f496c98fc074/uploaded_image_0_1768310105488.png"
    $img1 = "C:/Users/isaac/.gemini/antigravity/brain/c60fa4b0-2d6d-471c-a85e-f496c98fc074/uploaded_image_1_1768310105488.png"

    $c0 = Get-Color $img0
    $c1 = Get-Color $img1

    Write-Host "IMG0_COLOR:$c0"
    Write-Host "IMG1_COLOR:$c1"
}
catch {
    Write-Host "Error: $_"
}
