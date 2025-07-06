package com.example.renerd.features.episodes.di



import com.example.renerd.features.episodes.EpisodesContract
import com.example.renerd.features.episodes.EpisodesPresenter
import com.example.renerd.features.episodes.EpisodesRepository
import com.example.renerd.features.episodes.components.last_episodes_dialog.LastEpisodesDialogContract
import com.example.renerd.features.episodes.components.last_episodes_dialog.LastEpisodesDialogPresenter
import com.example.renerd.features.episodes.components.last_episodes_dialog.LastEpisodesDialogRepository
import com.example.renerd.features.episodes.components.search_dialog.SearchDialogContract
import com.example.renerd.features.episodes.components.search_dialog.SearchDialogPresenter
import org.koin.dsl.module

object EpisodesModule {

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


        factory<LastEpisodesDialogContract.Repository> {
            LastEpisodesDialogRepository()
        }


        factory<LastEpisodesDialogContract.Presenter> {
            LastEpisodesDialogPresenter(repository = get())
        }

        factory<LastEpisodesDialogContract.Presenter> {
            LastEpisodesDialogPresenter(
                repository = get()
            )
        }
    }
}