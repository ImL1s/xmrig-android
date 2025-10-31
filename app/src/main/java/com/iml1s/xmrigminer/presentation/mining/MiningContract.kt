package com.iml1s.xmrigminer.presentation.mining

import com.iml1s.xmrigminer.data.model.MiningStats

/**
 * MVI Pattern: Single UI State
 */
data class MiningUiState(
    val stats: MiningStats = MiningStats(),
    val isRunning: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val logs: List<String> = emptyList()
)

/**
 * MVI Pattern: User Events
 */
sealed interface MiningEvent {
    data object StartMining : MiningEvent
    data object StopMining : MiningEvent
    data object ClearError : MiningEvent
    data object ClearLogs : MiningEvent
}

/**
 * MVI Pattern: One-time UI Effects
 */
sealed interface MiningEffect {
    data class ShowToast(val message: String) : MiningEffect
    data class NavigateToConfig(val reason: String) : MiningEffect
}
