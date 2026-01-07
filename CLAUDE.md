# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Cross-platform Monero (XMR) mining application with three targets:
- **Android**: Native Kotlin/Compose app with XMRig 6.21.0 (C++ via NDK)
- **Web**: Browser-based RandomX.js miner with Vite
- **iOS**: SwiftUI native miner (sideload-only)

## Build Commands

### Android
```bash
./gradlew assembleDebug           # Debug APK
./gradlew assembleRelease         # Release APK
./gradlew testDebugUnitTest       # Unit tests
./gradlew testDebugUnitTest --tests "*.MiningConfigTest"  # Single test class
./gradlew lintDebug               # Code analysis
./gradlew clean build             # Full clean build
```

### Web Miner
```bash
cd web && npm install             # Install dependencies
cd web && npm run dev             # Dev server (port 5173)
cd web && npm run build           # Production build
cd web/proxy && npm start         # WebSocket-to-Stratum proxy
```

### iOS
```bash
cd ios/XMRigCore/scripts && ./build-ios.sh  # Build XMRig static library
# Then open ios/XMRigMiner-iOS.xcodeproj in Xcode
```

## Architecture

### Android - Clean Architecture + MVVM + MVI

```
Presentation (Jetpack Compose)
    ↓
ViewModel (MVI: State, Event, Effect)
    ↓
Repository (DataStore, Flow)
    ↓
Native Layer (JNI → C++ XMRig)
```

**Key architectural decisions:**
- **KSP** over KAPT for 3-4x faster compilation
- **WorkManager** for background mining (not Service)
- **DataStore** for preferences (not SharedPreferences)
- **Kotlin Flow** for reactive streams
- **MVI pattern** for unidirectional data flow

### Directory Structure

```
app/src/main/java/com/iml1s/xmrigminer/
├── data/
│   ├── model/          # MiningConfig, MiningStats, MiningState, Pool
│   └── repository/     # ConfigRepository, PoolRepository, StatsRepository
├── presentation/
│   ├── mining/         # MiningScreen, MiningViewModel, MiningContract
│   ├── config/         # ConfigScreen, ConfigViewModel, ConfigContract
│   ├── theme/          # Material Design 3 theming
│   └── navigation/     # Navigation setup
├── service/
│   ├── MiningWorker.kt    # Background mining (WorkManager)
│   └── MonitorWorker.kt   # CPU/temp monitoring
├── native/
│   └── XMRigBridge.kt     # JNI bridge to C++ XMRig
├── util/               # CpuMonitor, NetworkMonitor, NotificationHelper
└── di/                 # Hilt dependency injection modules
```

### Native Code

- C++ JNI bridge: `app/src/main/cpp/native-bridge.cpp`
- Pre-compiled XMRig binary: `app/src/main/assets/xmrig_arm64`
- CMake config: `app/CMakeLists.txt`

## Dependencies

Dependencies managed via Version Catalog at `gradle/libs.versions.toml`.

Key versions:
- Kotlin 1.9.21, Compose BOM 2024.10.00
- Android SDK 34, NDK 26.3.11579264
- Hilt 2.50, Room 2.6.1, WorkManager 2.9.0

## Testing

Unit tests location: `app/src/test/java/com/iml1s/xmrigminer/`

Uses JUnit 4 + Turbine for Flow testing + kotlinx-coroutines-test.

E2E tests via Maestro in `.maestro/` directory.

## MVI Contract Pattern

Each screen follows this pattern:
```kotlin
// Contract file defines the MVI contract
object MiningContract {
    data class State(...)      // UI state
    sealed class Event { ... } // User actions
    sealed class Effect { ... } // One-shot effects (navigation, snackbar)
}

// ViewModel implements the contract
class MiningViewModel : ViewModel() {
    val state: StateFlow<State>
    fun onEvent(event: Event)
    val effect: SharedFlow<Effect>
}
```

## CI/CD

GitHub Actions workflows in `.github/workflows/`:
- `android-ci.yml` - Build, test, lint on push/PR
- `web-miner-ci.yml` - Web miner builds
- `release.yml` - Auto-release on version tags

## Development Requirements

- Android Studio Hedgehog+ with JDK 17
- NDK 26.3.11579264, CMake 3.22.1
- Node.js 20+ (for web miner)
- Xcode 15+ on macOS 14+ (for iOS)
