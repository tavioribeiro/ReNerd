package com.example.renerd.components.player.di


import com.example.renerd.components.player.FloatingPlayerContract
import com.example.renerd.components.player.FloatingPlayerPresenter
import com.example.renerd.components.player.FloatingPlayerRepository
import org.koin.dsl.module


object FloatingPlayerModule {

    val instance = module {
        factory<FloatingPlayerContract.Repository> { FloatingPlayerRepository() }
        factory<FloatingPlayerContract.Presenter> { FloatingPlayerPresenter(get()) }
    }
}