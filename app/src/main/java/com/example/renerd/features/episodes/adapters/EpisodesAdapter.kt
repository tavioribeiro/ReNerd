package com.example.renerd.features.episodes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.renerd.R
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.styleBackground

class EpisodesAdapter(
    private val context: Context,
    private val episodes: MutableList<EpisodeViewModel>,
    private val onClick: (EpisodeViewModel) -> Unit) :
    RecyclerView.Adapter<EpisodesAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.r_episodes_list, parent, false)
        return EpisodeViewHolder(itemView)
    }


    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val play_icon_background: LinearLayout= itemView.findViewById(R.id.play_icon_background)
        val imageView_play_icon: ImageView = itemView.findViewById(R.id.imageView_play_icon)
        val texView_name: TextView = itemView.findViewById(R.id.texView_name)
        val texView_info : TextView = itemView.findViewById(R.id.texView_info)
    }



    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.imageView.load(episodes[position].imageUrl)

        holder.play_icon_background.styleBackground(
            backgroundColor = "#191919",
            radius = 50f
        )

        holder.texView_name.text = episodes[position].title
        holder.texView_info.text = episodes[position].category.toString()

        holder.imageView_play_icon.setOnClickListener(){
            onClick(episodes[position])
        }
    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}