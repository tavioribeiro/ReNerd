package com.example.renerd.features.episodes

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.renerd.databinding.ActivityEpisodesBinding
import com.example.renerd.features.episodes.adapters.EpisodesAdapter
import com.example.renerd.features.episodes.components.filters_dialog.FiltersDialog
import com.example.renerd.features.episodes.components.search_dialog.SearchDialog
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsListModel
import core.extensions.fadeInAnimationNoRepeat
import core.extensions.toast
import org.koin.android.ext.android.inject
import android.content.pm.ActivityInfo

class EpisodesActivity : AppCompatActivity(), EpisodesContract.View {

    private lateinit var binding: ActivityEpisodesBinding
    private val presenter: EpisodesContract.Presenter by inject()

    private lateinit var filtersTabsListModel: FiltersTabsListModel
    private lateinit var episodesList: List<EpisodeViewModel>
    private var currentRecyclerViewPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupListeners()

        presenter.attachView(this)
        presenter.loadData()
    }

    private fun setupUI() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.navigationBarColor = Color.parseColor(ContextManager.getColorHex(1))
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.parseColor(ContextManager.getColorHex(0))
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerviewEpisodes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerviewEpisodes.recyclerviewEpisodesMonitor()
    }

    private fun setupListeners() {
        binding.iconFilter.fadeInAnimationNoRepeat(1000) {
            binding.iconFilter.setOnClickListener {
                setUpFilterModal(filtersTabsListModel)
            }
        }
        binding.iconSearch.fadeInAnimationNoRepeat(1000) {
            binding.iconSearch.setOnClickListener {
                setUpSearchModal(episodesList)
            }
        }

        binding.iconUpdate.fadeInAnimationNoRepeat(1000) {
            binding.iconUpdate.setOnClickListener {

            }
        }
    }

    // Função de extensão para monitorar o RecyclerView
    private fun RecyclerView.recyclerviewEpisodesMonitor() {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItemPosition != currentRecyclerViewPosition) {
                    presenter.recyclerviewEpisodesCurrentPosition(firstVisibleItemPosition)
                    currentRecyclerViewPosition = firstVisibleItemPosition
                }
            }
        })
    }

    override fun setListsData(tempFiltersTabsListModel: FiltersTabsListModel?, tempEpisodesList: List<EpisodeViewModel>?) {
        tempFiltersTabsListModel?.let { filtersTabsListModel = it }
        tempEpisodesList?.let { episodesList = it }
    }

    override fun setUpFilterModal(filtersTabsListModel: FiltersTabsListModel) {
        val filterModal = FiltersDialog(
            context = this,
            filtersList = filtersTabsListModel,
            onSave = { mixedFiltersItens ->
                presenter.updateFiltersTabsItemList(mixedFiltersItens)
            }
        )
        filterModal.show(supportFragmentManager, "filterModal")
    }

    override fun setUpSearchModal(episodesList: List<EpisodeViewModel>) {
        val searchModal = SearchDialog(
            context = this,
            episodesList = episodesList,
            onClick = {
                this.goToEpisode(it)
            }
        )
        searchModal.show(supportFragmentManager, "searchModal")
    }

    override fun showEpisodes(episodes: MutableList<EpisodeViewModel>, scrollTo: Int) {
        val adapter = EpisodesAdapter(
            context = this,
            episodes = episodes,
            onClick = { episode -> goToEpisode(episode) }
        )
        binding.recyclerviewEpisodes.adapter = adapter
        binding.recyclerviewEpisodes.scrollToPosition(scrollTo)
    }

    private fun goToEpisode(episode: EpisodeViewModel) {
        binding.floatingPlayer.startEpisode(episode)
    }

    override fun onBackPressed() {
        if (false) {
            binding.floatingPlayer.collapse()
        } else {
            super.onBackPressed()
        }
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

    override fun setUpFilterButton() {
        binding.iconFilter.fadeInAnimationNoRepeat(1000) {
            binding.iconFilter.setOnClickListener {
                setUpFilterModal(filtersTabsListModel)
            }
        }
    }

    override fun setUpSearchButton() {
        binding.iconSearch.fadeInAnimationNoRepeat(1000) {
            binding.iconSearch.setOnClickListener {
                setUpSearchModal(episodesList)
            }
        }
    }

    override fun onDestroy() {
        binding.floatingPlayer.stopService()
        presenter.detachView()
        super.onDestroy()
    }
}
