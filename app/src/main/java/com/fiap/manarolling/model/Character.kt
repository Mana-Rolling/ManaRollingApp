package com.fiap.manarolling.model

import kotlinx.serialization.Serializable

@Serializable
data class Attributes(
    var intelligence: Int = 0,
    var dexterity: Int = 0,
    var strength: Int = 0,
) {
    fun total() = intelligence + dexterity + strength
}

@Serializable
data class Character (
    val id: Long = System.currentTimeMillis(),
    var name: String = "",
    var region: String = "",
    var age: Int = 0,
    var clazz: String = "",
    var level: Int = 1,
    var availablePoints: Int = 10,
    var avatarEmoji: String = "🧙", // placeholder
    var attributes: Attributes = Attributes(5, 5, 5)
)