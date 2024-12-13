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
        val imageView_play_icon: ImageView = itemView.findViewById(R.id.imageView_play_icon)
        val texView_name: TextView = itemView.findViewById(R.id.texView_name)
        val texView_info : TextView = itemView.findViewById(R.id.texView_info)
    }



    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {

        holder.bottom_info.visibility = View.INVISIBLE

        // Parar animação de skeleton e resetar imagem
        holder.imageView.stopSkeletonAnimation()
        holder.imageView.setImageResource(0) // Limpa a imagem para evitar flickering

        // Resetar estilos de background
        holder.bottom_info.styleBackground(
            backgroundColor = ContextManager.getColorHex(0),
            bottomLeftRadius = 0f,
            bottomRightRadius = 0f
        )
        holder.bottom_info.alpha = 1f // Resetar a transparência, caso tenha sido alterada pela animação

        holder.play_icon_background.styleBackground(
            backgroundColor = ContextManager.getColorHex(0),
            radius = 50f
        )

        // Resetar texto (caso não seja necessário, pode remover)
        holder.texView_name.text = ""
        holder.texView_info.text = ""




        // **FIM DO RESET**

        holder.texView_name.isSelected = true

        holder.imageView.startSkeletonAnimation(36f)

        holder.imageView.load(episodes[position].imageUrl){
            target(
                onSuccess = { drawable ->
                    //Define a imagem com borda curva e para o skeleton
                    holder.imageView.getSizes{ width, height ->
                        val resize = drawable.resize(width = width, height = (width / 1.682242991).toInt() , context.resources)

                        holder.imageView.setImageDrawable(resize.toTopRoundedDrawable(36f))
                        holder.imageView.stopSkeletonAnimation()
                    }

                    //Obter a paleta de cores da imagem
                    holder.imageView.getPalletColors { colors ->
                        val (color1, color2) = colors
                        try {
                            holder.bottom_info.styleBackground(
                                backgroundColorsList = mutableListOf(darkenColor(color1, 85.0), darkenColor(color2, 65.0)),
                                bottomLeftRadius = 36f,
                                bottomRightRadius = 36f
                            )
                        } catch (e: Exception) {
                            log(e)
                        }
                    }
                    holder.bottom_info.fadeInAnimationNoRepeat()
                },
                onError = {
                    holder.imageView.setImageResource(R.drawable.background)

                }
            )
        }

        holder.play_icon_background.styleBackground(
            backgroundColor = ContextManager.getColorHex(1),
            radius = 50f
        )

        holder.texView_name.text = episodes[position].title
        holder.texView_info.text = episodes[position].productName

        holder.imageView_play_icon.setOnClickListener(){
            onClick(episodes[position])
        }
    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}