package com.example.renerd.features.episodes.adapters

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
import com.example.renerd.R
import com.example.renerd.components.buttons.IconButtonSmall
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.core.utils.log
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.cropCenterSection
import core.extensions.darkenColor
import core.extensions.fadeInAnimation
import core.extensions.fadeInAnimationNoRepeat
import core.extensions.getPalletColors
import core.extensions.getSizes
import core.extensions.resize
import core.extensions.startSkeletonAnimation
import core.extensions.stopSkeletonAnimation
import core.extensions.styleBackground
import core.extensions.toAllRoundedDrawable
import core.extensions.toTopRoundedDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        val bottom_info: LinearLayout = itemView.findViewById(R.id.bottom_info)
        val play_icon_background: LinearLayout= itemView.findViewById(R.id.play_icon_background)
        val playButton: IconButtonSmall = itemView.findViewById(R.id.imageView_play_icon)
        val texView_name: TextView = itemView.findViewById(R.id.texView_name)
        val texView_info : TextView = itemView.findViewById(R.id.texView_info)

        var imageColor1: String = ContextManager.getColorHex(2)
        var imageColor2: String = ContextManager.getColorHex(2)
    }



    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]

        holder.bottom_info.setBackgroundColor(Color.TRANSPARENT)
        holder.imageView.setImageDrawable(null)
        holder.texView_name.text = ""
        holder.texView_info.text = ""

        holder.bottom_info.visibility = View.INVISIBLE

        holder.imageColor1 = ""
        holder.imageColor2 = ""

        holder.texView_name.isSelected = true
        holder.texView_name.text = episode.title
        holder.texView_info.text = episode.productName

        holder.imageView.load(episode.imageUrl) {
            placeholder(R.drawable.media_cover)
            error(R.drawable.background)

            target(
                onSuccess = { drawable ->
                    holder.imageView.setImageDrawable(drawable.toTopRoundedDrawable(36f))

                    CoroutineScope(Dispatchers.IO).launch {
                        holder.imageView.getPalletColors { colors ->
                            val (color1, color2) = colors

                            holder.imageColor1 = color1
                            holder.imageColor2 = color2

                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    holder.bottom_info.styleBackground(
                                        backgroundColorsList = mutableListOf(
                                            darkenColor(color1, 85.0),
                                            darkenColor(color2, 65.0)
                                        ),
                                        bottomLeftRadius = 36f,
                                        bottomRightRadius = 36f
                                    )
                                    holder.bottom_info.fadeInAnimationNoRepeat()
                                } catch (e: Exception) {
                                    log(e)
                                    holder.bottom_info.setBackgroundColor(Color.DKGRAY)
                                    holder.bottom_info.fadeInAnimationNoRepeat()
                                }
                            }
                        }
                    }
                },
                onError = {
                    holder.bottom_info.setBackgroundColor(Color.DKGRAY)
                    holder.bottom_info.fadeInAnimationNoRepeat()
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