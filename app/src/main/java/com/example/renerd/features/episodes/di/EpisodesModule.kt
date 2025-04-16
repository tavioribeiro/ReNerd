package com.example.renerd.features.episodes.di



import com.example.renerd.features.episodes.EpisodesContract
import com.example.renerd.features.episodes.EpisodesPresenter
import com.example.renerd.features.episodes.EpisodesRepository
import com.example.renerd.features.episodes.components.search_dialog.SearchDialogContract
import com.example.renerd.features.episodes.components.search_dialog.SearchDialogPresenter
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
        factory<EpisodesContract.Repository> {
            EpisodesRepository()
        }
        factory<EpisodesContract.Presenter> {
            EpisodesPresenter(get())
        }

        factory<SearchDialogContract.Presenter> {
            SearchDialogPresenter(
                repository = get()
            )
        }
    }
}