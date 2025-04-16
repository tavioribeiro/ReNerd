package com.example.renerd.features.episodes.components.search_dialog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.renerd.R
import com.example.renerd.view_models.EpisodeViewModel

class SearchEpisodesAdapter(
    private val episodesList: List<EpisodeViewModel>,
    private val onClick: (EpisodeViewModel) -> Unit
) : RecyclerView.Adapter<SearchEpisodesAdapter.FilterItemViewHolder>() {

    class FilterItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImageView: ImageView = itemView.findViewById(R.id.mini_player_poster)
        val titleTextView: TextView = itemView.findViewById(R.id.mini_player_title)
        val productNameTextView: TextView = itemView.findViewById(R.id.mini_player_product_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.r_search_dialog_item_adapter, parent, false)
        return FilterItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FilterItemViewHolder, position: Int) {
        val episode = episodesList[position]

        holder.titleTextView.text = episode.title
        holder.titleTextView.isSelected = true
        holder.productNameTextView.text = episode.productName

        holder.posterImageView.load(episode.imageUrl)

        holder.itemView.setOnClickListener {
            onClick(episode)
        }
    }


    override fun getItemCount(): Int {
        return episodesList.size
    }
}