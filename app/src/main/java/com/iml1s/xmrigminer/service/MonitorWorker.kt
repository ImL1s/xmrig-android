package com.iml1s.xmrigminer.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber
import com.iml1s.xmrigminer.data.repository.StatsRepository
import androidx.work.WorkManager
import com.iml1s.xmrigminer.util.CpuMonitor
import com.iml1s.xmrigminer.util.NetworkMonitor

/**
 * Monitors device conditions (temperature, battery, network, CPU)
 * Auto-pauses mining on critical conditions
 */
@HiltWorker
class MonitorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val statsRepository: StatsRepository,
    private val workManager: WorkManager,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "monitor_work"
        const val CHECK_INTERVAL = 5000L // 5 seconds
        
        // Thresholds
        const val MAX_TEMPERATURE = 45f  // Celsius
        const val MIN_BATTERY_LEVEL = 20 // Percent
    }

    private val cpuMonitor = CpuMonitor()

    override suspend fun doWork(): Result {
        Timber.i("MonitorWorker started")
        
        try {
            while (!isStopped) {
                updateBatteryStats()
                updateThermalStats()
                updateCpuStats()
                updateNetworkStats()
                checkCriticalConditions()
                delay(CHECK_INTERVAL)
            }
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e, "MonitorWorker failed")
            return Result.retry()
        }
    }

    private fun updateBatteryStats() {
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            
            val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            val isCharging = isDeviceCharging()
            
            statsRepository.updateBatteryLevel(level)
            statsRepository.updateChargingState(isCharging)
            
            Timber.v("Battery: $level%, Charging: $isCharging")
        } catch (e: Exception) {
            Timber.w(e, "Failed to update battery stats")
        }
    }

    private fun updateThermalStats() {
        try {
            val temperature = getBatteryTemperature()
            statsRepository.updateTemperature(temperature)
            
            Timber.v("Temperature: ${temperature}°C")
        } catch (e: Exception) {
            Timber.w(e, "Failed to update thermal stats")
        }
    }

    private fun updateCpuStats() {
        try {
            val cpuUsage = cpuMonitor.getCurrentUsage()
            statsRepository.updateCpuUsage(cpuUsage)
            
            // Only log if we have actual CPU data (will be 0 on Android 8.0+)
            if (cpuUsage > 0) {
                Timber.v("CPU Usage: ${cpuUsage.toInt()}%")
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to update CPU stats")
        }
    }

    private fun updateNetworkStats() {
        try {
            val isConnected = networkMonitor.isConnected()
            val networkType = networkMonitor.getNetworkTypeString()
            
            Timber.v("Network: $networkType (Connected: $isConnected)")
            
            // Could store network status in repository if needed
            // For now, just log it
        } catch (e: Exception) {
            Timber.w(e, "Failed to update network stats")
        }
    }

    private fun checkCriticalConditions() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) ?: 100
        val temp = getBatteryTemperature()
        val isCharging = isDeviceCharging()
        val isConnected = networkMonitor.isConnected()

        // Check high temperature
        if (temp > MAX_TEMPERATURE) {
            Timber.w("Critical temperature: ${temp}°C")
            pauseMining("Temperature too high (${temp}°C)")
            return
        }

        // Check low battery (not charging)
        if (level < MIN_BATTERY_LEVEL && !isCharging) {
            Timber.w("Low battery: $level%")
            pauseMining("Battery too low ($level%)")
            return
        }

        // Check network connectivity
        if (!isConnected) {
            Timber.w("Network disconnected")
            pauseMining("No network connection")
            return
        }
    }

    private fun isDeviceCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun getBatteryTemperature(): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f // Convert from tenths of degree
    }

    private fun pauseMining(reason: String) {
        Timber.w("Pausing mining: $reason")
        // Cancel mining worker
        workManager.cancelUniqueWork(MiningWorker.WORK_NAME)
        
        // Send notification
        com.iml1s.xmrigminer.util.NotificationHelper.showWarning(context, reason)
    }
}
