package com.example.renerd.features.episodes.components.search_dialog.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.renerd.R
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.cropCenterSection
import core.extensions.getSizes
import core.extensions.startSkeletonAnimation
import core.extensions.stopSkeletonAnimation
import core.extensions.styleBackground
import core.extensions.toAllRoundedDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchEpisodesAdapter(
    private val resources: Resources,
    private val episodesList: List<EpisodeViewModel>,
    private val onClick: (EpisodeViewModel) -> Unit
) : RecyclerView.Adapter<SearchEpisodesAdapter.FilterItemViewHolder>() {

    class FilterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainContainer: LinearLayout = itemView.findViewById(R.id.main_container)
        val posterImageView: ImageView = itemView.findViewById(R.id.mini_player_poster)
        val titleTextView: TextView = itemView.findViewById(R.id.mini_player_title)
        val productNameTextView: TextView = itemView.findViewById(R.id.mini_player_product_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.r_search_dialog_item_adapter, parent, false)
        return FilterItemViewHolder(itemView)
    }

    override fun onViewRecycled(holder: FilterItemViewHolder) {
        holder.posterImageView.stopSkeletonAnimation()
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: FilterItemViewHolder, position: Int) {
        val episode = episodesList[position]

        holder.titleTextView.text = episode.title
        holder.titleTextView.isSelected = true
        holder.productNameTextView.text = episode.productName

        holder.posterImageView.startSkeletonAnimation(10f)
        holder.posterImageView.load(episode.imageUrl) {
            target(
                onSuccess = { drawable ->
                    holder.posterImageView.post {
                        holder.posterImageView.getSizes { width, height ->
                            val crop = drawable.cropCenterSection(widthDp = width, heightDp = height, resources = resources)
                            holder.posterImageView.stopSkeletonAnimation()
                            holder.posterImageView.setImageDrawable(crop.toAllRoundedDrawable(20f))
                        }
                    }
                },
                onError = {
                    holder.posterImageView.setImageResource(R.drawable.background)
                    holder.posterImageView.stopSkeletonAnimation()
                }
            )
        }


        holder.itemView.setOnClickListener {
            this.simulateClickEffect(holder)
            onClick(episode)
        }
    }

    private fun simulateClickEffect(holder:FilterItemViewHolder){
        CoroutineScope(Dispatchers.Main).launch {
            holder.mainContainer.styleBackground(
                backgroundColor = ContextManager.getColorHex(2),
                radius = 12f,
                borderWidth = 2,
                borderColor = ContextManager.getColorHex(5)
            )

            delay(70)

            holder.mainContainer.styleBackground(
                backgroundColor = ContextManager.getColorHex(0),
                radius = 0f,
                borderWidth = 0,
                borderColor = ContextManager.getColorHex(0)
            )
        }

    }

    override fun getItemCount(): Int {
        return episodesList.size
    }
}