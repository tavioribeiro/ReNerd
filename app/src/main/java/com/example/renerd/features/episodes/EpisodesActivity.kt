package com.example.renerd.features.episodes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.renerd.databinding.ActivityEpisodesBinding
import com.example.renerd.view_models.EpisodeViewModel
import org.koin.android.ext.android.inject

class EpisodesActivity: AppCompatActivity(), EpisodesContract.View{

    private lateinit var binding: ActivityEpisodesBinding
    private val presenter: EpisodesContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter.attachView(this)
        presenter.loadEpisodes()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun showEpisodes(episodes: List<EpisodeViewModel>) {
        // Atualize a UI com a lista de episódios
    }

    override fun showError(message: String) {
        // Exiba uma mensagem de erro
    }

    override fun showLoading() {
        // Exiba um indicador de carregamento
    }

    override fun hideLoading() {
        // Oculte o indicador de carregamento
    }
}
