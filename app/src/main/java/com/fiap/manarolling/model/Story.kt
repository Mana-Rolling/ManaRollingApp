package com.fiap.manarolling.model

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: Long = System.currentTimeMillis(),
    var title: String = "",
    // Data em ISO simples para não precisar de libs extras (ex.: "2025-09-03")
    var date: String = "",
    var content: String = ""
)

@Serializable
data class Story(
    val chapters: List<Chapter> = emptyList()
)
