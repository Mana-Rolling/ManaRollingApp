package com.fiap.manarolling.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("manarolling_settings")

enum class UserRole { PLAYER, MASTER }

class SettingsStore(private val context: Context) {
    companion object {
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_PLAYER_ID = longPreferencesKey("player_id")
    }

    val roleFlow: Flow<UserRole?> = context.dataStore.data.map { p ->
        when (p[KEY_ROLE]) {
            "PLAYER" -> UserRole.PLAYER
            "MASTER" -> UserRole.MASTER
            else -> null
        }
    }

    val playerIdFlow: Flow<Long?> = context.dataStore.data.map { p ->
        p[KEY_PLAYER_ID]
    }

    suspend fun setRole(role: UserRole) {
        context.dataStore.edit { it[KEY_ROLE] = role.name }
    }

    suspend fun setPlayerId(id: Long?) {
        context.dataStore.edit {
            if (id == null) it.remove(KEY_PLAYER_ID) else it[KEY_PLAYER_ID] = id
        }
    }
}
