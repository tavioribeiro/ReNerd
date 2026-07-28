package com.podcast.renerd.core.network.di

import com.podcast.renerd.core.network.PodcastClient
import com.podcast.renerd.core.network.api.PodcastApi
import org.koin.dsl.module

object NetworkModule {
    val instance = module {
        single { PodcastClient() }
        single<PodcastApi> { get<PodcastClient>().api }
    }
}
