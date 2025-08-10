# PowerShell script to verify 16KB page size alignment for Android native libraries
# Run this after building your APK to check .so alignment

param(
    [Parameter(Mandatory=$true)]
    [string]$ApkPath,
    
    [Parameter(Mandatory=$false)]
    [string]$NdkPath = "$env:ANDROID_NDK_ROOT"
)

Write-Host "Verifying 16KB alignment for native libraries..." -ForegroundColor Green

if (-not $NdkPath -or -not (Test-Path $NdkPath)) {
    Write-Host "ERROR: NDK path not found. Set ANDROID_NDK_ROOT or provide -NdkPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $ApkPath)) {
    Write-Host "ERROR: APK not found at $ApkPath" -ForegroundColor Red
    exit 1
}

# Extract APK to temp directory
$tempDir = Join-Path $env:TEMP "apk_extract_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

try {
    # Extract APK (assuming 7zip or built-in Expand-Archive)
    if (Get-Command "7z" -ErrorAction SilentlyContinue) {
        & 7z x "$ApkPath" -o"$tempDir" -y | Out-Null
    } else {
        Expand-Archive -Path $ApkPath -DestinationPath $tempDir -Force
    }
    
    # Find readelf tool
    $readelfPath = Get-ChildItem -Path $NdkPath -Recurse -Name "aarch64-linux-android-readelf.exe" | Select-Object -First 1
    if (-not $readelfPath) {
        $readelfPath = Get-ChildItem -Path $NdkPath -Recurse -Name "aarch64-linux-android-readelf" | Select-Object -First 1
    }
    
    if (-not $readelfPath) {
        Write-Host "ERROR: readelf not found in NDK" -ForegroundColor Red
        exit 1
    }
    
    $readelfFullPath = Join-Path $NdkPath $readelfPath
    
    # Check all .so files
    $soFiles = Get-ChildItem -Path $tempDir -Recurse -Filter "*.so"
    $allCompliant = $true
    
    if ($soFiles.Count -eq 0) {
        Write-Host "No .so files found in APK" -ForegroundColor Yellow
    } else {
        foreach ($soFile in $soFiles) {
            Write-Host "`nChecking: $($soFile.Name)" -ForegroundColor Cyan
            
            $output = & $readelfFullPath -l $soFile.FullName | Select-String -Pattern "LOAD|Align"
            
            $loadSegments = $output | Where-Object { $_ -match "LOAD" }
            foreach ($segment in $loadSegments) {
                $nextLine = $output | Where-Object { $output.IndexOf($_) -eq ($output.IndexOf($segment) + 1) }
                if ($nextLine -and $nextLine -match "Align\s+0x([0-9a-fA-F]+)") {
                    $alignHex = $matches[1]
                    $alignDec = [Convert]::ToInt32($alignHex, 16)
                    
                    if ($alignDec -ge 16384 -and ($alignDec % 16384) -eq 0) {
                        Write-Host "  ✓ LOAD segment aligned to $alignDec bytes (0x$alignHex)" -ForegroundColor Green
                    } else {
                        Write-Host "  ✗ LOAD segment aligned to $alignDec bytes (0x$alignHex) - NOT 16KB compliant!" -ForegroundColor Red
                        $allCompliant = $false
                    }
                }
            }
        }
    }
    
    if ($allCompliant) {
        Write-Host "`n✅ All native libraries are 16KB page size compliant!" -ForegroundColor Green
    } else {
        Write-Host "`n❌ Some libraries are NOT 16KB compliant. Update dependencies or contact vendors." -ForegroundColor Red
        exit 1
    }
    
} finally {
    # Cleanup
    Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
}
