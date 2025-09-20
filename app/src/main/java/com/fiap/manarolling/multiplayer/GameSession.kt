package com.fiap.manarolling.multiplayer

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PlayerInfo(
    val id: String = "",
    val name: String = "",
    val online: Boolean = true,
    val lastSeen: Long = 0L,
    val selectedCharacterId: Long? = null
)

@IgnoreExtraProperties
data class GameSession(
    val id: String = "",
    val masterId: String = "",
    val masterName: String = "",
    val players: Map<String, PlayerInfo> = emptyMap(),
    val state: Map<String, Any?> = emptyMap(),
    val createdAt: Long = 0L
)
