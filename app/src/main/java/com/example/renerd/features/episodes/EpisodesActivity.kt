package com.example.renerd.features.episodes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.components.filters_dialog.FiltersDialog
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.ActivityEpisodesBinding
import com.example.renerd.features.episodes.adapters.EpisodesAdapter
import com.example.renerd.features.player.PlayerActivity
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsListModel
import core.extensions.fadeInAnimationNoRepeat
import core.extensions.toast
import org.koin.android.ext.android.inject


class EpisodesActivity: AppCompatActivity(), EpisodesContract.View{

    private lateinit var binding: ActivityEpisodesBinding
    private val presenter: EpisodesContract.Presenter by inject()

    private lateinit var filtersTabsListModel:FiltersTabsListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setUpUi()
        presenter.attachView(this)
        presenter.getFiltersTabsList()
    }


    private fun setUpUi(){
        window.statusBarColor = Color.parseColor(ContextManager.getColorHex(0))
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }


    override fun showActionButtons(tempFiltersTabsListModel:FiltersTabsListModel) {
        filtersTabsListModel = tempFiltersTabsListModel


        binding.iconFilter.fadeInAnimationNoRepeat(1000) {
            this.setUpActionButtons()
        }
        binding.iconSearch.fadeInAnimationNoRepeat(1000) {  }

        presenter.loadEpisodes()
    }




    private fun setUpActionButtons(){
        binding.iconFilter.setOnClickListener(){
            this.setUpFilterModal(filtersTabsListModel)
        }

        binding.iconSearch.setOnClickListener(){}
    }


    override fun setUpFilterModal(filtersTabsListModel:FiltersTabsListModel){
        val filterModal = FiltersDialog(
            context = this,
            filtersList = filtersTabsListModel,
            onSave = { mixedFiltersItens ->
                presenter.updateFiltersTabsItemList(mixedFiltersItens)
            }
        )
        filterModal.show(supportFragmentManager, "filterModal")
    }


    private fun allowSwipeRefreshLayout(){
       // binding.swipeRefreshLayout.isEnabled = true
       // binding.swipeRefreshLayout.isRefreshing = false
    }


    override fun onDestroy() {
        binding.customBottomSheet.stopService()
        presenter.detachView()
        super.onDestroy()
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

        //this.allowSwipeRefreshLayout()
    }


    private fun goTo(episode: EpisodeViewModel){
        binding.customBottomSheet.startEpisode(episode)

        /*val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("episode", episode)
        startActivity(intent)*/
    }



    override fun onBackPressed() {
        //trocar isso para um dialog de confirmação customizado
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Você tem certeza que quer sair?")
            .setCancelable(false)
            .setPositiveButton("Sim") { dialog, id ->
                super.onBackPressed()
            }
            .setNegativeButton("Não") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }


    override fun showError(message: String) {
        toast(message)
       // binding.swipeRefreshLayout.isRefreshing = false
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
