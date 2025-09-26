package com.fiap.manarolling.model

import android.util.Log
import kotlinx.serialization.Serializable
import kotlin.math.max

/* ========== ATRIBUTOS ========== */
@Serializable
data class Attributes(
    var intelligence: Int = 5,
    var dexterity: Int = 5,
    var strength: Int = 5,
    var agility: Int = 5,
    var charisma: Int = 5,
    /** Atributo base de Vida (usado para calcular HP Máx) */
    var vida: Int = 10
)

/* ========== VITAIS (MÁXIMOS) ========== */
@Serializable
data class Vitals(
    /** Regra atual do projeto: hpMax deriva de 'vida' (na criação/edição). */
    val hpMax: Int = 10,
    /** Mana Máx é sempre 20 (fixo nas regras atuais). */
    val manaMax: Int = 20
) {
    companion object {
        fun from(attributes: Attributes): Vitals {
            val hp = max(1, attributes.vida)
            return Vitals(hpMax = hp, manaMax = 20)
        }
    }
}

/* ========== VITAIS (CORRENTES / RUNTIME) ========== */
@Serializable
data class RuntimeVitals(
    /** Valores atuais durante a sessão/jogo. */
    val hp: Int = 0,
    val mana: Int = 20
)

/* ========== PERSONAGEM ========== */
@Serializable
data class Character(
    val id: Long = System.currentTimeMillis(),

    var name: String = "",
    var region: String = "",
    var age: Int = 0,
    var clazz: String = "",

    var level: Int = 1,
    /** Pontos disponíveis para distribuir na criação/edição. */
    var availablePoints: Int = 10,

    var photoUri: String? = null,

    var attributes: Attributes = Attributes(),

    /** História/capítulos (definidos em Story.kt). */
    var story: Story = Story(),

    /** Dono do personagem (usado no multiplayer). */
    val ownerUid: String = "",

    // VITAIS com defaults (compatíveis com JSONs antigos).
    var vitals: Vitals = Vitals(),
    var runtime: RuntimeVitals = RuntimeVitals()
) {
    /**
     * Recalcula vitais a partir dos atributos e ajusta runtime aos novos limites.
     * Útil após alterar 'attributes' ou 'level' na criação/edição.
     */
    fun recomputeVitalsAndCoerceRuntime(): Character {
        val newVitals = Vitals.from(attributes)
        val newHp = runtime.hp.coerceIn(0, max(1, newVitals.hpMax))
        val newMana = runtime.mana.coerceIn(0, newVitals.manaMax)
        return copy(vitals = newVitals, runtime = RuntimeVitals(hp = newHp, mana = newMana))
    }

    /** Ajusta apenas o HP atual, respeitando limites. */
    fun withHp(newHp: Int): Character {
        val hpCoerced = newHp.coerceIn(0, max(1, vitals.hpMax))
        return copy(runtime = runtime.copy(hp = hpCoerced))
    }

    /** Ajusta apenas a Mana atual, respeitando limites. */
    fun withMana(newMana: Int): Character {
        val manaCoerced = newMana.coerceIn(0, vitals.manaMax)
        return copy(runtime = runtime.copy(mana = manaCoerced))
    }

    /* ========= Helpers de acesso ao kit da classe (NÃO serializados) ========= */

    /** Kit (arma + habilidade) da classe atual, conforme ClassPresets. */
    fun loadout(): ClassPresets.Loadout? = ClassPresets.loadoutFor(clazz)

    /** Conveniências para UI (não entram na serialização) */
    val weaponName: String? get() = loadout()?.weaponName
    val weaponImageRes: String? get() = loadout()?.weaponImageRes
    val weaponDamage: Int? get() = loadout()?.weaponDamage            // 5

    val abilityName: String? get() = loadout()?.abilityName
    val abilityImageRes: String? get() = loadout()?.abilityImageRes
    val abilityPower: Int? get() = loadout()?.abilityPower            // 10 (ou cura=10)
    val abilityManaCost: Int? get() = loadout()?.manaCost             // 5
    val abilityIsHealing: Boolean get() = loadout()?.healing == true  // Clérigo = true
}
