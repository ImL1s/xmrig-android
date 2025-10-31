# XMRig Android Miner

**現代化的 Android Monero (XMR) 挖礦應用**

從 2018 年的 CoinHiveMiner 全面改造而來，使用最新的 Android 技術棧和 XMRig 挖礦引擎。

---

## 📊 項目狀態

### ✅ Phase 0: 準備完成
- [x] XMRig 源碼獲取
- [x] donate.h 修改（抽成降為 0%）
- [x] Android 項目結構搭建
- [x] Gradle 配置
- [x] NDK/CMake 配置
- [x] 核心數據模型

### 🚧 Phase 1: 核心功能（進行中）
- [x] 項目骨架
- [x] Native Bridge (JNI)
- [x] 數據模型 (MiningConfig, MiningStats)
- [ ] XMRigService 實現
- [ ] ConfigRepository
- [ ] StatsRepository
- [ ] 基礎 Compose UI

### ⏳ Phase 2: 監控系統（待開始）
- [ ] ThermalMonitor
- [ ] BatteryMonitor
- [ ] CpuMonitor
- [ ] Room Database

### ⏳ Phase 3: 優化與完善（待開始）
- [ ] 性能優化
- [ ] 崩潰恢復
- [ ] 錯誤處理
- [ ] ProGuard 規則

### ⏳ Phase 4: 測試與發布（待開始）
- [ ] 單元測試
- [ ] 集成測試
- [ ] 文檔完善
- [ ] APK 打包

---

## 🏗️ 技術架構

```
┌─────────────────────────────────────┐
│  Jetpack Compose UI                 │
│  ├─ MiningScreen                    │
│  ├─ ConfigScreen                    │
│  └─ StatsScreen                     │
├─────────────────────────────────────┤
│  ViewModel (StateFlow)              │
├─────────────────────────────────────┤
│  Repository (Hilt DI)               │
│  ├─ ConfigRepository (DataStore)    │
│  └─ StatsRepository (Room)          │
├─────────────────────────────────────┤
│  Service Layer                      │
│  ├─ XMRigService (ProcessBuilder)   │
│  └─ MonitorService                  │
├─────────────────────────────────────┤
│  Native Layer (NDK)                 │
│  ├─ XMRig Binary (arm64/arm32)      │
│  └─ JNI Bridge                      │
└─────────────────────────────────────┘
```

---

## 🔧 技術棧

### Android
- **Kotlin**: 1.9.20
- **Compose**: BOM 2024.01.00
- **Material 3**: 最新設計語言
- **NDK**: r26+
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)

### 依賴注入 & 架構
- **Hilt**: 2.50
- **Navigation Compose**: 2.7.6
- **Lifecycle**: 2.7.0

### 數據層
- **DataStore**: 1.0.0 (替代 SharedPreferences)
- **Room**: 2.6.1 (本地數據庫)
- **Kotlin Serialization**: 1.6.2

### 異步處理
- **Coroutines**: 1.7.3
- **Flow**: Reactive streams
- **WorkManager**: 2.9.0 (後台任務)

### 工具
- **Timber**: 5.0.1 (日誌)
- **LeakCanary**: 2.12 (內存洩漏檢測)

---

## 🎯 核心功能

### 已實現
✅ **Native Bridge**: CPU 信息檢測、版本查詢
✅ **數據模型**: 
  - `MiningConfig`: 完整的配置管理（包含 JSON 生成）
  - `MiningStats`: 挖礦統計數據
  - `MiningState`: 狀態機

### 待實現
⏳ **XMRigService**: ProcessBuilder 管理 XMRig 進程
⏳ **OutputParser**: 解析 XMRig 輸出（算力、shares）
⏳ **MonitorService**: 溫度/電量/CPU 監控
⏳ **UI Components**: Compose 界面

---

## 📁 項目結構

```
XMRigMiner/
├── app/
│   ├── src/main/
│   │   ├── cpp/
│   │   │   └── native-bridge.cpp          # JNI 橋接
│   │   ├── java/com/iml1s/xmrigminer/
│   │   │   ├── XMRigApplication.kt        # 應用入口
│   │   │   ├── data/
│   │   │   │   ├── model/                 # 數據模型 ✅
│   │   │   │   ├── repository/            # 數據倉庫 ⏳
│   │   │   │   └── local/                 # DataStore/Room ⏳
│   │   │   ├── domain/
│   │   │   │   └── usecase/               # 業務邏輯 ⏳
│   │   │   ├── presentation/
│   │   │   │   ├── mining/                # 挖礦界面 ⏳
│   │   │   │   ├── config/                # 配置界面 ⏳
│   │   │   │   └── stats/                 # 統計界面 ⏳
│   │   │   ├── service/
│   │   │   │   ├── XMRigService.kt        # 核心服務 ⏳
│   │   │   │   └── MonitorService.kt      # 監控服務 ⏳
│   │   │   ├── native/
│   │   │   │   └── XMRigBridge.kt         # JNI 接口 ✅
│   │   │   └── di/                        # Hilt 模組 ⏳
│   │   ├── jniLibs/
│   │   │   ├── arm64-v8a/                 # ARM64 二進制 ⏳
│   │   │   └── armeabi-v7a/               # ARM32 二進制 ⏳
│   │   └── AndroidManifest.xml            # ✅
│   ├── build.gradle.kts                   # ✅
│   └── CMakeLists.txt                     # ✅
├── build.gradle.kts                       # ✅
├── settings.gradle.kts                    # ✅
└── gradle.properties                      # ✅
```

---

## 🔐 抽成移除驗證

### 修改位置
**文件**: `/tmp/xmrig/src/donate.h`

```cpp
// ❌ 原始（1% 強制抽成）
constexpr const int kDefaultDonateLevel = 1;
constexpr const int kMinimumDonateLevel = 1;

// ✅ 已修改（0% 抽成）
constexpr const int kDefaultDonateLevel = 0;
constexpr const int kMinimumDonateLevel = 0;
```

### 額外檢查點
- `src/net/strategies/DonateStrategy.cpp` - 捐贈策略
- 配置文件強制 `"donate-level": 0`

---

## 📝 下一步工作

### 立即執行（Phase 1 繼續）
1. **XMRigService** - 核心挖礦服務實現
2. **ConfigRepository** - 使用 DataStore 保存配置
3. **基礎 UI** - MainActivity + MiningScreen

### 編譯 XMRig 二進制（關鍵）
```bash
# 需要執行（在有 Android NDK 的環境）
cd /tmp/xmrig
./build_android.sh  # 生成 arm64-v8a 和 armeabi-v7a 二進制
```

**輸出產物**:
- `build/android-arm64/xmrig` → 複製到 `app/src/main/jniLibs/arm64-v8a/`
- `build/android-arm32/xmrig` → 複製到 `app/src/main/jniLibs/armeabi-v7a/`

---

## ⚖️ 法律聲明

### ⚠️ 重要提示
- 本項目僅供**教育和學習**目的
- 手機挖礦收益**極低**（每天 < $0.05）
- 會導致**耗電**和**發熱**
- 請勿在未經同意的設備上使用
- 遵守 GPL-3.0 開源協議

### 用戶須知
使用本應用即表示您同意：
1. ✅ 明確知曉挖礦會消耗電量和產生熱量
2. ✅ 自願在自己的設備上運行
3. ✅ 收益微乎其微（主要為學習目的）
4. ✅ 遵守當地法律法規

---

## 📚 參考資源

- [XMRig 官方](https://github.com/xmrig/xmrig)
- [Monero 官方](https://www.getmonero.org/)
- [Android NDK 文檔](https://developer.android.com/ndk/guides)
- [Jetpack Compose 文檔](https://developer.android.com/jetpack/compose)

---

## 🔄 變更日誌

### 2025-10-30
- ✅ 初始化項目結構
- ✅ 配置 Gradle + CMake
- ✅ 實現 Native Bridge
- ✅ 創建核心數據模型
- ✅ 修改 XMRig donate.h (0% 抽成)

---

**License**: GPL-3.0  
**Author**: ImL1s  
**Base Project**: [CoinHiveMiner (2018)](https://github.com/ImL1s/CoinHiveMiner)  
**Mining Engine**: [XMRig](https://github.com/xmrig/xmrig)
