package com.fiap.manarolling.model

import kotlinx.serialization.Serializable

@Serializable
data class Attributes(
    var intelligence: Int = 5,
    var dexterity: Int = 5,
    var strength: Int = 5,
    var agility: Int = 5,
    var charisma: Int = 5
)

@Serializable
data class Character(
    val id: Long = System.currentTimeMillis(),
    var name: String = "",
    var region: String = "",
    var age: Int = 0,
    var clazz: String = "",
    var level: Int = 1,
    var availablePoints: Int = 10,

    var photoUri: String? = null,

    var attributes: Attributes = Attributes(),
    var story: Story = Story()
)
