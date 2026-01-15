try {
    Add-Type -AssemblyName System.Drawing
    
    function Get-Brightness ($file) {
        $img = [System.Drawing.Bitmap]::FromFile($file)
        $totalBrightness = 0
        $count = 0
        
        # Sample pixels (every 10th to save time)
        for ($x = 0; $x -lt $img.Width; $x += 10) {
            for ($y = 0; $y -lt $img.Height; $y += 10) {
                $pixel = $img.GetPixel($x, $y)
                if ($pixel.A -gt 10) {
                    # If not transparent
                    $brightness = ($pixel.R + $pixel.G + $pixel.B) / 3
                    $totalBrightness += $brightness
                    $count++
                }
            }
        }
        $img.Dispose()
        
        if ($count -eq 0) { return 0 }
        return $totalBrightness / $count
    }

    $img0 = "C:/Users/isaac/.gemini/antigravity/brain/c60fa4b0-2d6d-471c-a85e-f496c98fc074/uploaded_image_0_1768310105488.png"
    $img1 = "C:/Users/isaac/.gemini/antigravity/brain/c60fa4b0-2d6d-471c-a85e-f496c98fc074/uploaded_image_1_1768310105488.png"

    $b0 = Get-Brightness $img0
    $b1 = Get-Brightness $img1

    Write-Host "IMG0_BRIGHTNESS:$b0"
    Write-Host "IMG1_BRIGHTNESS:$b1"
}
catch {
    Write-Host "Error: $_"
}
