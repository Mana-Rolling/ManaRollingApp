package com.fiap.manarolling.model

import kotlinx.serialization.Serializable

object ClassPresets {

    /** Nomes das classes exibidas na criação de personagem. */
    val options: List<String> = listOf("Guerreiro", "Mago", "Arqueiro", "Ladino", "Clérigo")

    /**
     * Atributos base por classe (mantidos como estavam no seu arquivo,
     * apenas removi os "..." que quebravam o código).
     * Observação: 'vida' é usada para hpMax (regra atual = hpMax = vida).
     */
    val base: Map<String, Attributes> = mapOf(
        "Guerreiro" to Attributes(
            intelligence = 8,  dexterity = 12, strength = 18, agility = 10, charisma = 8,
            vida = 32
        ),
        "Mago" to Attributes(
            intelligence = 18, dexterity = 8,  strength = 6,  agility = 8,  charisma = 10,
            vida = 14
        ),
        "Arqueiro" to Attributes(
            intelligence = 10, dexterity = 18, strength = 10, agility = 14, charisma = 8,
            vida = 22
        ),
        "Ladino" to Attributes(
            intelligence = 12, dexterity = 16, strength = 8,  agility = 16, charisma = 10,
            vida = 18
        ),
        "Clérigo" to Attributes(
            intelligence = 14, dexterity = 8,  strength = 12, agility = 8,  charisma = 14,
            vida = 24
        )
    )

    /* =========================
     *  Kit de combate por classe (arma + habilidade)
     * ========================= */

    @Serializable
    data class Loadout(
        // Arma
        val weaponName: String,
        val weaponImageRes: String,   // nome do drawable (sem extensão), ex.: "weapon_guerreiro"
        val weaponDamage: Int = 5,    // FIXO: arma causa 5 de dano

        // Habilidade
        val abilityName: String,
        val abilityImageRes: String,  // ex.: "skill_guerreiro"
        val abilityPower: Int = 10,   // 10 de dano OU 10 de cura (se healing=true)
        val manaCost: Int = 5,        // FIXO: habilidade gasta 5 de mana
        val healing: Boolean = false  // true = cura (ex.: Clérigo)
    )

    /** Mapa classe -> kit (arma + habilidade + imagens). */
    val loadouts: Map<String, Loadout> = mapOf(
        "Guerreiro" to Loadout(
            weaponName = "Espada Curta",
            weaponImageRes = "weapon_guerreiro",
            weaponDamage = 5,
            abilityName = "Golpe Giratório",
            abilityImageRes = "skill_guerreiro",
            abilityPower = 10,
            manaCost = 5,
            healing = false
        ),
        "Mago" to Loadout(
            weaponName = "Cajado Simples",
            weaponImageRes = "weapon_mago",
            weaponDamage = 5,
            abilityName = "Bola de Fogo",
            abilityImageRes = "skill_mago",
            abilityPower = 10,
            manaCost = 5,
            healing = false
        ),
        "Arqueiro" to Loadout(
            weaponName = "Arco Curto",
            weaponImageRes = "weapon_arqueiro",
            weaponDamage = 5,
            abilityName = "Flecha Tripla",
            abilityImageRes = "skill_arqueiro",
            abilityPower = 10,
            manaCost = 5,
            healing = false
        ),
        "Ladino" to Loadout(
            weaponName = "Adaga",
            weaponImageRes = "weapon_ladino",
            weaponDamage = 5,
            abilityName = "Ataque Furtivo",
            abilityImageRes = "skill_ladino",
            abilityPower = 10,
            manaCost = 5,
            healing = false
        ),
        "Clérigo" to Loadout(
            weaponName = "Maça Leve",
            weaponImageRes = "weapon_clerigo",
            weaponDamage = 5,
            abilityName = "Cura",
            abilityImageRes = "skill_clerigo",
            abilityPower = 10, // cura 10 em aliado
            manaCost = 5,
            healing = true
        )
    )

    /** Helper rápido para obter o kit pelo nome do preset. */
    fun loadoutFor(presetName: String): Loadout? = loadouts[presetName]
}
