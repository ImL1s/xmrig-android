# CPU Monitoring Fix

## Problem
The app was getting `EACCES (Permission denied)` errors when trying to read `/proc/stat` to monitor CPU usage. On Android, regular apps don't have permission to read system-wide CPU statistics.

## Solution
Changed the CPU monitoring approach to only use the process's own statistics from `/proc/[pid]/stat`, which is accessible to the app, and calculate CPU usage based on wall-clock time instead of system CPU time.

### Key Changes

**Before:**
```kotlin
// Tried to read /proc/stat (permission denied)
val cpuInfo = File("/proc/stat").readLines()[0]
val currentSystemTime = cpuInfo.slice(1..7).sumOf { it.toLongOrNull() ?: 0L }
```

**After:**
```kotlin
// Only read own process stats (accessible)
val statFile = File("/proc/$pid/stat")
if (statFile.exists() && statFile.canRead()) {
    val stat = statFile.readText().split(" ")
    // Use wall-clock time for calculation
    val currentWallTime = System.currentTimeMillis()
    
    // Calculate: (CPU time / Wall time) * 100
    val cpuUsage = (cpuMillis.toDouble() / wallTimeDelta * 100).toFloat()
}
```

## How It Works

1. **Process CPU Time**: Read from `/proc/[pid]/stat` (fields 13-14: utime + stime)
   - This gives CPU time in clock ticks (usually 1/100 second)

2. **Wall-Clock Time**: Use `System.currentTimeMillis()` 
   - This gives actual elapsed time

3. **Calculate Usage**:
   ```
   CPU Usage % = (CPU Time Delta / Wall Time Delta) × 100
   ```

4. **Multi-Core Support**: The result can exceed 100% on multi-core systems
   - Limited to: `0% to (CPU_CORES × 100%)`

## Testing

Build and install:
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The app should now show CPU usage without permission errors.

## Expected Behavior

- CPU usage updates every 5 seconds
- Values range from 0% to (cores × 100%)
- For XMRig mining on 8 cores, expect 300-400% CPU usage (using 3-4 cores at 100%)
- No more "Permission denied" errors in logcat

## Files Modified

- `app/src/main/java/com/iml1s/xmrigminer/service/MiningWorker.kt`
  - `monitorCpuUsage()` method
