package com.iml1s.xmrigminer.presentation.mining

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.iml1s.xmrigminer.data.repository.ConfigRepository
import com.iml1s.xmrigminer.data.repository.StatsRepository
import com.iml1s.xmrigminer.service.MiningWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * MVI ViewModel
 * 2025 Best Practice: Single StateFlow + Event Channel
 */
@HiltViewModel
class MiningViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val statsRepository: StatsRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiningUiState())
    val uiState: StateFlow<MiningUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MiningEffect>(Channel.BUFFERED)
    val effects: Flow<MiningEffect> = _effects.receiveAsFlow()

    init {
        observeStats()
        observeWorkInfo()
    }

    private fun observeStats() {
        viewModelScope.launch {
            statsRepository.stats.collect { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
        }
    }

    private fun observeWorkInfo() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(MiningWorker.WORK_NAME)
                .collect { workInfos ->
                    val isRunning = workInfos.any { it.state == WorkInfo.State.RUNNING }
                    _uiState.update { it.copy(isRunning = isRunning) }
                }
        }
    }

    fun onEvent(event: MiningEvent) {
        when (event) {
            is MiningEvent.StartMining -> startMining()
            is MiningEvent.StopMining -> stopMining()
            is MiningEvent.ClearError -> clearError()
            is MiningEvent.ClearLogs -> clearLogs()
        }
    }

    private fun startMining() {
        viewModelScope.launch {
            val config = configRepository.getConfig().first()
            
            Timber.i("Starting mining with config - Wallet: ${config.walletAddress.take(10)}..., Pool: ${config.poolUrl}")
            
            if (!config.isValid()) {
                val errorMsg = when {
                    config.walletAddress.isBlank() -> "配置無效：錢包地址未設置"
                    config.poolUrl.isBlank() -> "配置無效：礦池地址未設置"
                    else -> "配置無效，請檢查設置"
                }
                Timber.w(errorMsg)
                _uiState.update { it.copy(error = errorMsg) }
                _effects.send(MiningEffect.NavigateToConfig(errorMsg))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Cancel any existing work first
                Timber.i("Cancelling existing work...")
                workManager.cancelUniqueWork(MiningWorker.WORK_NAME)
                workManager.cancelUniqueWork(com.iml1s.xmrigminer.service.MonitorWorker.WORK_NAME)
                
                // Wait a bit for cancellation to complete
                kotlinx.coroutines.delay(500)
                
                // Create WorkRequest for Mining
                val miningConstraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val miningRequest = OneTimeWorkRequestBuilder<MiningWorker>()
                    .setConstraints(miningConstraints)
                    .addTag("mining")
                    .build()

                // Create WorkRequest for Monitoring
                val monitorRequest = PeriodicWorkRequestBuilder<com.iml1s.xmrigminer.service.MonitorWorker>(
                    15, // Repeat every 15 minutes
                    java.util.concurrent.TimeUnit.MINUTES
                ).build()

                // Enqueue both workers with REPLACE policy
                Timber.i("Enqueueing MiningWorker...")
                workManager.enqueueUniqueWork(
                    MiningWorker.WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    miningRequest
                )

                Timber.i("Enqueueing MonitorWorker...")
                workManager.enqueueUniquePeriodicWork(
                    com.iml1s.xmrigminer.service.MonitorWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    monitorRequest
                )

                Timber.i("Mining and monitoring work enqueued")
                
                // Debug: Check work status after a short delay
                kotlinx.coroutines.delay(1000)
                val workInfos = workManager.getWorkInfosForUniqueWork(MiningWorker.WORK_NAME).await()
                Timber.i("MiningWorker status: ${workInfos.firstOrNull()?.state}")
                
                _effects.send(MiningEffect.ShowToast("挖礦已啟動"))
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to start mining")
                _uiState.update { it.copy(error = "啟動失敗: ${e.message}") }
                _effects.send(MiningEffect.ShowToast("啟動失敗"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun stopMining() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Cancel both mining and monitoring
                workManager.cancelUniqueWork(MiningWorker.WORK_NAME)
                workManager.cancelUniqueWork(com.iml1s.xmrigminer.service.MonitorWorker.WORK_NAME)
                statsRepository.reset()
                
                Timber.i("Mining and monitoring stopped")
                _effects.send(MiningEffect.ShowToast("挖礦已停止"))
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to stop mining")
                _uiState.update { it.copy(error = "停止失敗: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }
}
