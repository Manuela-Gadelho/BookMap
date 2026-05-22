Add-Type -AssemblyName System.Drawing
$genres = @("Ação e Aventura", "Autoajuda", "Biografia", "Contos", "Crimes Verdadeiros", "Crônicas", "Distopia", "Ensaios", "Fantasia", "Ficção Científica", "Filosofia", "História", "Horror _ Terror", "Humor", "Infantil", "Jovem Adulto (YA)", "Literatura Brasileira", "Literatura Clássica", "Literatura Estrangeira", "Mangás e Quadrinhos", "Mistério", "Negócios e Finanças", "Poesia", "Policial", "Psicologia", "Religião e Espiritualidade", "Romance", "Suspense", "Tecnologia", "Outros")
$outDir = "app\src\main\assets\covers"
if (!(Test-Path $outDir)) { New-Item -ItemType Directory -Force -Path $outDir | Out-Null }

$colors = @("DarkRed", "DarkBlue", "DarkGreen", "DarkOrange", "Indigo", "Teal", "SaddleBrown", "DarkSlateGray", "Maroon", "MidnightBlue")

foreach ($i in 0..($genres.Length-1)) {
    $genre = $genres[$i]
    $colorName = $colors[$i % $colors.Length]
    $color = [System.Drawing.Color]::FromName($colorName)
    $filename = $genre -replace '[^\w\s]', '' -replace '\s+', '_'
    $filename = $filename.ToLower() + ".jpg"
    $path = Join-Path $outDir $filename
    
    $bmp = New-Object System.Drawing.Bitmap(400, 600)
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    $brush = New-Object System.Drawing.SolidBrush($color)
    $graphics.FillRectangle($brush, 0, 0, 400, 600)
    
    $font = New-Object System.Drawing.Font("Arial", 28, [System.Drawing.FontStyle]::Bold)
    $textBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    
    $rect = New-Object System.Drawing.RectangleF(20, 20, 360, 560)
    $graphics.DrawString($genre.Replace(" _ ", "/"), $font, $textBrush, $rect, $format)
    
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Jpeg)
    
    $graphics.Dispose()
    $bmp.Dispose()
    $brush.Dispose()
    $font.Dispose()
    $textBrush.Dispose()
    $format.Dispose()
}
