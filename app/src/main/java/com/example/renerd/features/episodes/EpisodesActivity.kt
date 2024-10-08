package com.example.renerd.features.episodes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.ActivityEpisodesBinding
import com.example.renerd.features.episodes.adapters.EpisodesAdapter
import com.example.renerd.features.player.PlayerActivity
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.toast
import org.koin.android.ext.android.inject


class EpisodesActivity: AppCompatActivity(), EpisodesContract.View{

    private lateinit var binding: ActivityEpisodesBinding
    private val presenter: EpisodesContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setUpUi()
        presenter.attachView(this)
        presenter.loadEpisodes()
    }


    private fun setUpUi(){
        window.statusBarColor = Color.parseColor("#191919")
        binding.swipeRefreshLayout.isEnabled = false
        binding.swipeRefreshLayout.setOnRefreshListener {
            presenter.loadLastEpisodes()
        }
    }


    private fun allowSwipeRefreshLayout(){
        binding.swipeRefreshLayout.isEnabled = true
        binding.swipeRefreshLayout.isRefreshing = false
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }


    override fun showEpisodes(episodes: MutableList<EpisodeViewModel>) {
        binding.recyclerviewEpisodes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = EpisodesAdapter(
            context = this,
            episodes = episodes,
            onClick = { it ->
                this.goTo(it)
            }
        )
        binding.recyclerviewEpisodes.adapter = adapter

        this.allowSwipeRefreshLayout()
    }


    private fun goTo(episode: EpisodeViewModel){
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("episode", episode)
        startActivity(intent)
    }


    override fun showError(message: String) {
        toast(message)
        binding.swipeRefreshLayout.isRefreshing = false
        this.allowSwipeRefreshLayout()
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
