package com.fiap.manarolling.model

import kotlinx.serialization.Serializable

@Serializable
data class Attributes(
    var intelligence: Int = 5,
    var dexterity: Int = 5,
    var strength: Int = 5,
    var agility: Int = 5,
    var charisma: Int = 5,
    /** Atributo base de Vida (usado para calcular HP Máx na criação) */
    var vida: Int = 10
)

@Serializable
data class Vitals(
    /** HP Máx deve refletir o atributo de vida definido na criação/edição */
    val hpMax: Int = 0,
    /** Mana Máx é sempre 20 */
    val manaMax: Int = 20
)

@Serializable
data class RuntimeVitals(
    /** Valores atuais durante a sessão */
    val hp: Int = 0,
    val mana: Int = 20
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
    /** Story/Chapter devem continuar definidos nos seus arquivos próprios */
    var story: Story = Story(),
    val ownerUid: String = "",

    // VITAIS com defaults (compat c/ JSONs antigos)
    var vitals: Vitals = Vitals(),
    var runtime: RuntimeVitals = RuntimeVitals()
)
