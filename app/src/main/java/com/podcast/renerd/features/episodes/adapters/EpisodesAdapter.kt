package com.podcast.renerd.features.episodes.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.podcast.renerd.R
import com.podcast.renerd.components.buttons.IconButtonSmall
import com.podcast.renerd.core.extentions.styleBackground
import com.podcast.renerd.core.singletons.ColorsManager
import com.podcast.renerd.core.utils.log
import com.podcast.renerd.view_models.EpisodeViewModel
import core.extensions.darkenColor
import core.extensions.getPalletColors
import core.extensions.startSkeletonAnimation
import core.extensions.stopSkeletonAnimation
import core.extensions.toTopRoundedDrawable

class EpisodesAdapter(
    private val context: Context,
    private val episodes: MutableList<EpisodeViewModel>,
    private val onClick: (EpisodeViewModel) -> Unit
) : RecyclerView.Adapter<EpisodesAdapter.EpisodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.r_episodes_list, parent, false)
        return EpisodeViewHolder(itemView)
    }

    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val bottom_info: LinearLayout = itemView.findViewById(R.id.bottom_info)
        val play_icon_background: LinearLayout = itemView.findViewById(R.id.play_icon_background)
        val playButton: IconButtonSmall = itemView.findViewById(R.id.imageView_play_icon)
        val texView_name: TextView = itemView.findViewById(R.id.texView_name)
        val texView_info: TextView = itemView.findViewById(R.id.texView_info)

        var boundEpisodeId: Int = -1
    }

    override fun onViewRecycled(holder: EpisodeViewHolder) {
        holder.boundEpisodeId = -1
        holder.imageView.stopSkeletonAnimation()
        holder.imageView.setImageDrawable(null)
        holder.bottom_info.visibility = View.INVISIBLE
        holder.bottom_info.setBackgroundColor(Color.TRANSPARENT)
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]
        val episodeId = episode.id

        holder.boundEpisodeId = episodeId

        holder.texView_name.isSelected = true
        holder.texView_name.text = episode.title
        holder.texView_info.text = episode.productName

        holder.bottom_info.styleBackground(
            backgroundColor = ColorsManager.getColorHex(2),
            bottomLeftRadius = 36f,
            bottomRightRadius = 36f
        )
        holder.bottom_info.visibility = View.VISIBLE

        holder.imageView.setImageDrawable(null)
        holder.imageView.startSkeletonAnimation(36f)

        holder.imageView.load(episode.imageUrl) {
            target(
                onSuccess = { drawable ->
                    if (holder.boundEpisodeId != episodeId) return@target

                    holder.imageView.stopSkeletonAnimation()
                    holder.imageView.setImageDrawable(drawable.toTopRoundedDrawable(36f))

                    holder.imageView.getPalletColors { colors ->
                        if (holder.boundEpisodeId != episodeId) return@getPalletColors
                        val (color1, color2) = colors
                        try {
                            holder.bottom_info.styleBackground(
                                backgroundColorsList = mutableListOf(
                                    darkenColor(color1, 85.0),
                                    darkenColor(color2, 65.0)
                                ),
                                bottomLeftRadius = 36f,
                                bottomRightRadius = 36f
                            )
                        } catch (e: Exception) {
                            log(e)
                        }
                    }
                },
                onError = {
                    if (holder.boundEpisodeId != episodeId) return@target
                    holder.imageView.stopSkeletonAnimation()
                    holder.imageView.setImageResource(R.drawable.background)
                }
            )
        }

        holder.playButton.setOnClickListener {
            onClick(episode)
        }
    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}
