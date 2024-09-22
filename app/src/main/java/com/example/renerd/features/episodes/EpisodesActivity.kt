package com.example.renerd.features.episodes

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.databinding.ActivityEpisodesBinding
import com.example.renerd.features.episodes.adapters.EpisodesAdapter
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.styleBackground
import core.extensions.toast
import org.koin.android.ext.android.inject

class EpisodesActivity: AppCompatActivity(), EpisodesContract.View{

    private lateinit var binding: ActivityEpisodesBinding
    private val presenter: EpisodesContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#191919")

        presenter.attachView(this)
        presenter.loadEpisodes()
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun showEpisodes(episodes: MutableList<EpisodeViewModel>) {
        binding.recyclerviewEpisodes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = EpisodesAdapter(this, episodes)
        binding.recyclerviewEpisodes.adapter = adapter
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun showLoading() {
        binding.progressIndicator.visibility = View.VISIBLE
        binding.recyclerviewEpisodes.visibility = View.GONE
    }

    override fun hideLoading() {
        binding.progressIndicator.visibility = View.GONE
        binding.recyclerviewEpisodes.visibility = View.VISIBLE
    }
}
