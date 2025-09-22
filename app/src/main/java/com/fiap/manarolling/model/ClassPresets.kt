package com.fiap.manarolling.model

object ClassPresets {
    // Mantido exatamente como no seu projeto
    val options = listOf("Guerreiro", "Mago", "Arqueiro", "Ladino", "Clérigo")

    // Acrescentei apenas o campo 'vida' em cada preset, mantendo os demais atributos iguais.
    // Valores abaixo do cap (ex.: 50) e coerentes com a ordem de “quem tem mais vida”.
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
}
