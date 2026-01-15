try {
    Add-Type -AssemblyName System.Drawing
    $path = "C:/Users/isaac/.gemini/antigravity/brain/c60fa4b0-2d6d-471c-a85e-f496c98fc074/uploaded_image_1768310011684.png"
    $img = [System.Drawing.Bitmap]::FromFile($path)
    
    $pixel00 = $img.GetPixel(0, 0)
    Write-Host "Pixel(0,0) Alpha: $($pixel00.A)"
    
    $pixel1010 = $img.GetPixel(10, 10)
    Write-Host "Pixel(10,10) Alpha: $($pixel1010.A)"

    if ($pixel00.A -lt 255 -or $pixel1010.A -lt 255) {
        Write-Host "RESULT: TRANSPARENT"
    }
    else {
        Write-Host "RESULT: OPAQUE"
    }
    $img.Dispose()
}
catch {
    Write-Host "Error: $_"
}
