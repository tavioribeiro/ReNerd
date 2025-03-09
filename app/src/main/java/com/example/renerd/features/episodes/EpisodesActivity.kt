package com.example.renerd.features.episodes


import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.features.episodes.components.filters_dialog.FiltersDialog
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.databinding.ActivityEpisodesBinding
import com.example.renerd.features.episodes.adapters.EpisodesAdapter
import com.example.renerd.view_models.EpisodeViewModel
import com.example.renerd.view_models.FiltersTabsListModel
import core.extensions.fadeInAnimationNoRepeat
import core.extensions.toast
import org.koin.android.ext.android.inject
import android.content.pm.ActivityInfo
import androidx.recyclerview.widget.RecyclerView
import com.example.renerd.core.utils.log


class EpisodesActivity: AppCompatActivity(), EpisodesContract.View{

    private lateinit var binding: ActivityEpisodesBinding
    private val presenter: EpisodesContract.Presenter by inject()

    private lateinit var filtersTabsListModel:FiltersTabsListModel


    private var originalColor1 = ContextManager.getColorHex(0)
    private var originalColor2 = ContextManager.getColorHex(0)

    private var currentRecyclerViewPoition = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.setUpUi()
        presenter.attachView(this)
        presenter.getFiltersTabsList()
        this.recyclerviewEpisodesMonitor()
    }


    private fun setUpUi(){
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        window.navigationBarColor = Color.parseColor(ContextManager.getColorHex(1))
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            statusBarColor = Color.parseColor(ContextManager.getColorHex(0))
        }
    }


    /*private fun setUpCallbacks(){
        binding.floatingPlayer.setonBackgroundCollorsChangeListener{ color1, color2 ->

        }
    }*/

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
        binding.floatingPlayer.stopService()
        presenter.detachView()
        super.onDestroy()
    }



    override fun showEpisodes(episodes: MutableList<EpisodeViewModel>, scrollTo: Int) {
        binding.recyclerviewEpisodes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = EpisodesAdapter(
            context = this,
            episodes = episodes,
            onClick = { it ->
                this.goTo(it)
            }
        )
        binding.recyclerviewEpisodes.adapter = adapter

        binding.recyclerviewEpisodes.scrollToPosition(scrollTo)


        /*binding.recyclerviewEpisodes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Obtenha a posição do item visível mais à esquerda (primeiro item visível)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // A partir da posição, obtenha o item no adapter
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)

                // Verifique se a posição é válida e o viewHolder não é nulo
                if (viewHolder != null) {
                    val item = viewHolder.itemView
                    // Supondo que o título do item seja um TextView
                    val imageView: ImageView = item.findViewById(R.id.imageView)

                    imageView.getPalletColors { colors ->
                        val (color1, color2) = colors
                        try {
                            binding.mainContainer.changeBackgroundColorWithGradient(
                                color1 = darkenColor(color1, 90.0),
                                color2 = darkenColor(color2, 70.0)
                            )

                            originalColor1 = darkenColor(color1, 90.0)
                            originalColor2 = darkenColor(color2, 90.0)
                        } catch (e: Exception) {
                            log(e)
                        }
                    }
                }
            }
        })*/

    }


    private fun recyclerviewEpisodesMonitor(){
        binding.recyclerviewEpisodes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Obtenha o LayoutManager da RecyclerView
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                // Obtenha o índice do primeiro e último item visível
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if(firstVisibleItemPosition != currentRecyclerViewPoition) {
                    presenter.recyclerviewEpisodesCurrentPosition(firstVisibleItemPosition)
                    currentRecyclerViewPoition = firstVisibleItemPosition
                }
            }
        })
    }


    private fun goTo(episode: EpisodeViewModel){
        binding.floatingPlayer.startEpisode(episode)

        /*val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("episode", episode)
        startActivity(intent)*/
    }


    override fun onBackPressed() {
        if (0 == 0) {
            binding.floatingPlayer.collapse()
        } else {
            super.onBackPressed()
        }
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
