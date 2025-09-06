package com.fiap.manarolling.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.manarolling.data.SettingsStore
import com.fiap.manarolling.data.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsStore(app)

    val role = store.roleFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val playerId = store.playerIdFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun pickRole(r: UserRole) = viewModelScope.launch { store.setRole(r) }
    fun setPlayerId(id: Long?) = viewModelScope.launch { store.setPlayerId(id) }
}
