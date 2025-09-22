package com.fiap.manarolling.model

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: Long = System.currentTimeMillis(),
    var title: String = "",
    var date: String = "",
    var content: String = ""
)

@Serializable
data class Story(
    val chapters: List<Chapter> = emptyList()
)
