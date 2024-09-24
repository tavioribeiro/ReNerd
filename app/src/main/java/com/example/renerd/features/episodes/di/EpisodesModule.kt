package com.example.renerd.features.episodes.di



import com.example.renerd.features.episodes.EpisodesContract
import com.example.renerd.features.episodes.EpisodesPresenter
import com.example.renerd.features.episodes.EpisodesRepository
import org.koin.dsl.module

object EpisodesModule {

//    val instance = module {
//
//        //Activity
//        factory<FiltersContract.Presenter> { (view: FiltersContract.View) ->
//            FiltersPresenter(
//                view = view
//            )
//        }
//
//        //Fragment
//        factory<FiltersFragmentContract.Presenter> { (view: FiltersFragmentContract.View) ->
//            FiltersFragmentPresenter(
//                view = view
//            )
//        }
//    }



    val instance = module {
        factory<EpisodesContract.Repository> { EpisodesRepository() }
        factory<EpisodesContract.Presenter> { EpisodesPresenter(get()) }
    }
}