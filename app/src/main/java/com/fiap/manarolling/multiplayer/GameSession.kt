package com.fiap.manarolling.multiplayer

data class PlayerInfo(
    val id: String = "",
    val name: String = "",
    val characterId: String? = null
)

data class GameSession(
    val id: String = "",
    val masterId: String = "",
    val masterName: String = "",
    val players: Map<String, PlayerInfo> = emptyMap(),
    val state: Map<String, Any> = emptyMap(),
    val createdAt: Long = 0L
)
