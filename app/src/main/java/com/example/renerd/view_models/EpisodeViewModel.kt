package com.example.renerd.view_models



data class EpisodeViewModel(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val audioUrl: String,
    val duration: Int,
    val publishedAt: String,
    val category: Any
)