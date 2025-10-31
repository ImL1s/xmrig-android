package com.iml1s.xmrigminer.data.repository

import com.iml1s.xmrigminer.data.model.MiningStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 簡化版 Stats Repository
 * 後續可以接入 Room Database 持久化
 */
@Singleton
class StatsRepository @Inject constructor() {

    private val _stats = MutableStateFlow(MiningStats())
    val stats: Flow<MiningStats> = _stats.asStateFlow()

    fun updateHashrate(hashrate10s: Double, hashrate60s: Double = 0.0, hashrate15m: Double = 0.0) {
        _stats.update { 
            it.copy(
                hashrate = hashrate10s,  // Use 10s as main hashrate
                hashrate10s = hashrate10s,
                hashrate60s = hashrate60s,
                hashrate15m = hashrate15m
            ) 
        }
    }

    fun incrementAccepted() {
        _stats.update { it.copy(acceptedShares = it.acceptedShares + 1) }
    }

    fun incrementRejected() {
        _stats.update { it.copy(rejectedShares = it.rejectedShares + 1) }
    }

    fun updateCpuUsage(usage: Float) {
        _stats.update { it.copy(cpuUsage = usage) }
    }

    fun updateTemperature(temp: Float) {
        _stats.update { it.copy(temperature = temp) }
    }

    fun updateBatteryLevel(level: Int) {
        _stats.update { it.copy(batteryLevel = level) }
    }

    fun updateChargingState(isCharging: Boolean) {
        _stats.update { it.copy(isCharging = isCharging) }
    }

    fun updateDifficulty(difficulty: Long) {
        _stats.update { it.copy(difficulty = difficulty) }
    }

    fun reset() {
        _stats.value = MiningStats()
    }
}
