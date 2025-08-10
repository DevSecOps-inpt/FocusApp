# 16KB Page Size Compliance Guide

## Overview
Android 15+ devices may use 16KB memory pages instead of the traditional 4KB pages. Apps with native libraries must ensure their `.so` files are properly aligned to avoid crashes.

## Current Status

### ✅ Updated Dependencies
- **SQLCipher**: Upgraded from `4.5.4` → `4.6.1` (latest with 16KB support)
- **NDK**: Set to version `26.1.10909125` for proper toolchain support

### 🔍 Dependencies to Monitor
- `androidx.biometric:biometric:1.2.0-alpha05` - May include native code
- `androidx.security:security-crypto:1.1.0-alpha06` - May include native crypto libraries

## Verification Process

### 1. Build APK
```bash
./gradlew assembleDebug
# or
./gradlew assembleRelease
```

### 2. Verify Alignment
Run the provided PowerShell script:
```powershell
.\verify_16kb_alignment.ps1 -ApkPath "app\build\outputs\apk\debug\app-debug.apk"
```

### 3. Manual Verification (if script fails)
```bash
# Extract APK
unzip app-debug.apk -d extracted/

# Check each .so file
$NDK/toolchains/llvm/prebuilt/windows-x86_64/bin/aarch64-linux-android-readelf.exe -l extracted/lib/arm64-v8a/libsqlcipher.so | grep -A1 LOAD
```

**Expected Output:**
```
LOAD           0x000000 0x... 0x... 0x... 0x... R E    0x4000
               Align                0x4000
```
The `Align` value should be `0x4000` (16384) or higher.

## Troubleshooting

### If Libraries Are Not Compliant

1. **Update Dependencies**: Check for newer versions of any AAR dependencies
2. **Contact Vendors**: For third-party libraries, open issues requesting 16KB builds
3. **Alternative Libraries**: Consider switching to compliant alternatives if available

### Common Issues

- **SQLCipher**: Versions before 4.6.0 may not be 16KB compliant
- **Custom JNI**: If you add native code, ensure CMakeLists.txt includes:
  ```cmake
  target_link_options(<target> PRIVATE "-Wl,-z,max-page-size=16384")
  ```

## Testing

### Recommended Testing
1. **Emulator**: Test on Android 15+ emulator with 16KB pages enabled
2. **Physical Device**: Test on Pixel 8/9 or other devices with 16KB page support
3. **CI/CD**: Add alignment verification to your build pipeline

### Test Commands
```bash
# Enable 16KB pages in emulator
adb shell "echo 16384 > /proc/sys/vm/page_size" # (if supported)

# Check current page size
adb shell getconf PAGESIZE
```

## Build Configuration Summary

The following has been added to `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        ndkVersion = "26.1.10909125"
    }
}

dependencies {
    // Updated for 16KB compliance
    implementation("net.zetetic:sqlcipher-android:4.6.1")
}
```

## Resources

- [Android 16KB Page Size Guide](https://developer.android.com/guide/practices/page-sizes)
- [SQLCipher Release Notes](https://github.com/sqlcipher/sqlcipher-android/releases)
- [NDK Downloads](https://developer.android.com/ndk/downloads)
