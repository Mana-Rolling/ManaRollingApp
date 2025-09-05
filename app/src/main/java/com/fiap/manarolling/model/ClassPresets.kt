package com.fiap.manarolling.model

object ClassPresets {
    val options = listOf("Guerreiro", "Mago", "Arqueiro", "Ladino", "Clérigo")


    val base: Map<String, Attributes> = mapOf(
        "Guerreiro" to Attributes(
            intelligence = 8,  dexterity = 12, strength = 18, agility = 10, charisma = 8
        ),
        "Mago" to Attributes(
            intelligence = 18, dexterity = 8,  strength = 6,  agility = 8,  charisma = 10
        ),
        "Arqueiro" to Attributes(
            intelligence = 10, dexterity = 18, strength = 10, agility = 14, charisma = 8
        ),
        "Ladino" to Attributes(
            intelligence = 12, dexterity = 16, strength = 8,  agility = 16, charisma = 10
        ),
        "Clérigo" to Attributes(
            intelligence = 14, dexterity = 8,  strength = 12, agility = 8,  charisma = 14
        )
    )
}
