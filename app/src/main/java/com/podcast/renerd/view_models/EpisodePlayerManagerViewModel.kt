package com.podcast.renerd.view_models

data class EpisodePlayerManagerViewModel(
    val url: String ?= "",
    val totalTime: Long ?= null,
    val elapsedTime: Long ?= null,
)
