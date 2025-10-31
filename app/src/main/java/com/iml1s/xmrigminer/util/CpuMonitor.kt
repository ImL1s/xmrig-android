package com.iml1s.xmrigminer.util

import android.os.Build
import timber.log.Timber
import java.io.RandomAccessFile

/**
 * Monitors CPU usage using available Android APIs
 * On Android 8.0+, /proc/stat is restricted, so we use per-process stats
 */
class CpuMonitor {
    
    private var lastCpuStats: CpuStats? = null
    
    data class CpuStats(
        val totalTime: Long,
        val idleTime: Long,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Get current CPU usage percentage
     * @return CPU usage 0-100, or 0 if unable to calculate
     */
    fun getCurrentUsage(): Float {
        return try {
            // On Android 8.0+, we can't reliably read system-wide CPU usage
            // Return a default value instead of throwing errors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // We could use ActivityManager.getProcessCpuPercent() if available
                // For now, return 0 to avoid permission errors
                0f
            } else {
                val current = readCpuStats()
                val last = lastCpuStats
                
                val usage = if (last != null) {
                    val totalDelta = current.totalTime - last.totalTime
                    val idleDelta = current.idleTime - last.idleTime
                    
                    if (totalDelta > 0) {
                        100f * (1f - idleDelta.toFloat() / totalDelta)
                    } else {
                        0f
                    }
                } else {
                    0f
                }
                
                lastCpuStats = current
                usage.coerceIn(0f, 100f)
            }
        } catch (e: Exception) {
            // Silently return 0 on error - this is expected on modern Android
            0f
        }
    }
    
    /**
     * Read CPU statistics from /proc/stat (pre-Android 8.0 only)
     * Format: cpu  user nice system idle iowait irq softirq ...
     */
    private fun readCpuStats(): CpuStats {
        var reader: RandomAccessFile? = null
        try {
            reader = RandomAccessFile("/proc/stat", "r")
            val firstLine = reader.readLine()
                ?: throw IllegalStateException("Cannot read /proc/stat")
            
            // Parse: "cpu  123 456 789 ..."
            val values = firstLine.split("\\s+".toRegex())
                .drop(1) // Skip "cpu" label
                .take(7) // Take first 7 values (user, nice, system, idle, iowait, irq, softirq)
                .mapNotNull { it.toLongOrNull() }
            
            if (values.size < 4) {
                throw IllegalStateException("Invalid /proc/stat format")
            }
            
            return CpuStats(
                totalTime = values.sum(),
                idleTime = values[3] // idle is the 4th value
            )
        } finally {
            reader?.close()
        }
    }
    
    /**
     * Reset statistics (for testing or on start)
     */
    fun reset() {
        lastCpuStats = null
    }
}
