package com.example.renerd.view_models

data class EpisodeViewModel(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val audioUrl: String = "",
    val duration: Int = 0,
    val publishedAt: String = "",
    val category: Any = Any()
)