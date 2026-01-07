# Building XMRig Miner for iOS

This guide explains how to build and run the iOS version of XMRig Miner.

> ⚠️ **Note**: Apple App Store prohibits cryptocurrency mining apps. This app can only be installed via sideloading (requires Developer Account) or self-compilation.

## Requirements

- macOS 14+ (Sonoma)
- Xcode 15+
- Apple Developer Account ($99/year for 1-year signing, or free for 7-day)
- Physical iOS device (arm64) - **Simulator won't work**

## Build Steps

### 1. Clone Repository

```bash
git clone https://github.com/ImL1s/xmrig-android.git
cd xmrig-android/ios
```

### 2. Build XMRig Static Library

```bash
cd XMRigCore/scripts
chmod +x build-ios.sh
./build-ios.sh
```

This will:
- Download XMRig 6.25.0 source
- Compile for iOS arm64
- Output `libxmrig-ios-arm64.a`

### 3. Open Xcode Project

```bash
cd ../XMRigMiner-iOS
open XMRigMiner.xcodeproj
```

### 4. Configure Signing

1. Select the project in Xcode
2. Go to "Signing & Capabilities"
3. Select your Team (Apple ID)
4. Change Bundle Identifier if needed

### 5. Build and Run

1. Connect your iPhone
2. Select your device in Xcode
3. Press `Cmd + R` to build and run

## Sideloading (Without Xcode)

### Using AltStore (Recommended)

1. Install [AltStore](https://altstore.io/) on your Mac
2. Connect iPhone to Mac
3. Install AltStore on iPhone
4. Build IPA: `xcodebuild archive ...`
5. Open IPA with AltStore

### Signing Duration

| Account Type | Signing Validity |
|--------------|-----------------|
| Free Apple ID | 7 days |
| $99 Developer | 1 year |

## Troubleshooting

### Build Errors

**"No such module 'XMRigCore'"**
- Ensure static library was built correctly
- Check library search paths in Xcode

**"Signing certificate not found"**
- Add your Apple ID in Xcode Preferences → Accounts

### Runtime Issues

**Low/Zero Hashrate**
- Only works on physical devices (arm64)
- Check CPU temperature (throttling)

## Performance

Expected hashrate on iOS devices:

| Device | Hashrate |
|--------|----------|
| iPhone 15 Pro | ~150-200 H/s |
| iPhone 14 | ~100-150 H/s |
| iPhone 12 | ~80-120 H/s |

> Note: Actual performance varies. iOS background restrictions will pause mining when app is backgrounded.

## License

GPLv3 - Same as XMRig
