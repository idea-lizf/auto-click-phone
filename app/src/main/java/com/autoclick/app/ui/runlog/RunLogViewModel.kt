package com.autoclick.app.ui.runlog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclick.app.AutoClickApp
import com.autoclick.app.data.entity.RunLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RunLogViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AutoClickApp.instance.runLogRepository

    val runLogs: StateFlow<List<RunLogEntity>> = repo.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch { repo.deleteAll() }
    }

    fun deleteRun(id: Long) {
        viewModelScope.launch { repo.deleteRun(id) }
    }
}
