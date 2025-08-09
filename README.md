# FocusLock

A production-ready Android app for app blocking, focus mode, and digital wellbeing. Stay focused by blocking distracting apps and implementing scheduled focus sessions.

## Features

- 🔒 **App Blocking**: Block access to distracting apps with PIN/biometric unlock
- ⏰ **Focus Mode**: Quick and scheduled focus sessions with app whitelisting
- 🌐 **Internet Blocking**: Optional VPN-based internet blocking during focus sessions
- 📊 **Usage History**: Track blocked attempts and focus session analytics
- 🔐 **Privacy-First**: All data stored locally with encryption, no network access
- 🎨 **Material Design 3**: Modern, beautiful UI following Google's design guidelines

## Privacy & Security

- **Offline-Only**: No internet permissions, all data stays on your device
- **Encrypted Storage**: SQLCipher database encryption with Android Keystore
- **Secure Authentication**: PIN and biometric authentication options
- **No Tracking**: Zero analytics, telemetry, or user tracking

## Requirements

- **Android API 26+** (Android 8.0 Oreo and above)
- **Required Permissions**:
  - Usage Access (for app detection)
  - Display Over Other Apps (for lock overlays)
  - Accessibility Service (fallback detection)
  - Battery Optimization Exemption (for reliability)

## Technical Architecture

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Navigation Compose  
- **Architecture**: MVVM + Clean Architecture (presentation, domain, data)
- **Async**: Coroutines + Flow
- **DI**: Hilt
- **Database**: Room + SQLCipher (encrypted)
- **Background**: WorkManager + AlarmManager + ForegroundService
- **Authentication**: BiometricPrompt + Android Keystore

### Core Components
- **App Detection**: UsageStatsManager (primary) + AccessibilityService (fallback)
- **Lock Overlay**: System overlay Activity with authentication
- **Focus Enforcement**: Foreground service with persistent notification
- **VPN Service**: Optional per-app internet blocking
- **Encryption**: All sensitive data encrypted using Android Keystore

## Build Instructions

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17 or newer
- Android SDK with API 34

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/focuslock.git
   cd focuslock
   ```

2. Open in Android Studio and sync project

3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

### Debug vs Release
- **Debug builds** use unencrypted database for easier testing
- **Release builds** use SQLCipher encryption for production security

## Development Status

This project contains the complete foundational architecture:

✅ **Completed**
- Project structure and build configuration
- Room database with encrypted storage (entities, DAOs, converters)
- Hilt dependency injection setup
- Security manager with Android Keystore integration
- Material Design 3 theming and resources
- Android manifest with all required permissions

🚧 **In Progress**
- Data layer (repositories and data sources)
- Domain layer (use cases)
- Permission management and onboarding flow
- App-open detection engine
- Lock overlay with authentication
- Focus mode implementation
- Background services and WorkManager
- Compose UI screens

## Permissions Explained

### Usage Access
Required to monitor which apps are opened and trigger blocking when necessary. This is the primary detection mechanism.

### Display Over Other Apps
Allows the app to show lock screens over blocked apps. Essential for the blocking functionality.

### Accessibility Service
Backup detection method for devices where Usage Access might not work reliably (some OEM customizations).

### Battery Optimization Exemption
Prevents the system from killing the app's background services, ensuring reliable blocking.

### VPN Service (Optional)
Only used if internet blocking is enabled during focus sessions. Allows per-app internet restrictions.

## Contributing

This is currently a foundational implementation. Future contributions welcome for:
- Additional focus mode features
- UI/UX improvements
- Performance optimizations
- Testing coverage
- Documentation

## License

[Add your license here]

## Disclaimer

This app is designed to help with digital wellbeing but should not be relied upon as the sole method for restricting access to apps or content. Users remain responsible for their device usage. 