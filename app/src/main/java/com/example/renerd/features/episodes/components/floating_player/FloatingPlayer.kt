package com.example.renerd.features.episodes.components.floating_player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.example.renerd.R
import com.example.renerd.core.extentions.fadeInAnimation
import com.example.renerd.core.extentions.fadeInAnimationNoRepeat
import com.example.renerd.core.extentions.fadeOutAnimationNoRepeat
import com.example.renerd.core.extentions.getSizes
import com.example.renerd.core.extentions.styleBackground
import com.example.renerd.core.utils.convertMillisecondsToTime
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.CFloatingPlayerLayoutBinding
import com.example.renerd.services.AudioService3
import com.example.renerd.view_models.EpisodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import core.extensions.cropCenterSection
import core.extensions.darkenColor
import core.extensions.getPalletColors
import core.extensions.getSizes
import core.extensions.resize
import core.extensions.startSkeletonAnimation
import core.extensions.stopSkeletonAnimation
import core.extensions.toAllRoundedDrawable
import org.koin.java.KoinJavaComponent.inject


class FloatingPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), FloatingPlayerContract.View {

    private val binding: CFloatingPlayerLayoutBinding = CFloatingPlayerLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val presenter: FloatingPlayerContract.Presenter by inject(clazz = FloatingPlayerContract.Presenter::class.java)


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var onExpandedCallback: (() -> Unit)? = null
    private var onCollapsedCallback: (() -> Unit)? = null

    private var onBackgroundCollorsChangeCallback: ((String, String) -> Unit)? = null


    private var isPlaying = false

    private var currentEpisode = EpisodeViewModel()

    init {
        binding.mainContainer.visibility = View.GONE

        presenter.attachView(this)
        presenter.getCurrentPlayingEpisode()
    }


    override fun showUi(){
        binding.mainContainer.fadeInAnimation {
            binding.mainContainer.visibility = View.VISIBLE
        }
    }

    override fun updateInfosUi(episode: EpisodeViewModel){
        binding.miniPlayerTitle.text = episode.title
        binding.mainPlayerTitle.text = episode.title


        binding.miniPlayerProductName.text = episode.productName
        binding.mainPlayerProductName.text = episode.productName


        binding.mainPlayerDescription.text = Html.fromHtml("${episode.description}")


        binding.miniPlayer.isSelected = true
        binding.mainPlayerTitle.isSelected = true



        binding.miniPlayerPoster.startSkeletonAnimation(20f)
        binding.mainPlayerPoster.startSkeletonAnimation(30f)


        binding.miniPlayerPoster.load(episode.imageUrl){
            target(
                onSuccess = { drawable ->
                    //Define a imagem com borda curva e para o skeleton
                    binding.miniPlayerPoster.getSizes { width, height ->
                        val crop = drawable.cropCenterSection(widthDp = width, heightDp = height, resources)

                        binding.miniPlayerPoster.setImageDrawable(crop.toAllRoundedDrawable(20f))
                        binding.miniPlayerPoster.stopSkeletonAnimation()
                    }



                    //Define a imagem com borda curva e para o skeleton
                    binding.mainPlayerPoster.getSizes{ width, height ->
                        val resize = drawable.resize(width = width, height = (width / 1.682242991).toInt() , resources)

                        binding.mainPlayerPoster.setImageDrawable(resize.toAllRoundedDrawable(16f))
                        binding.mainPlayerPoster.stopSkeletonAnimation()
                    }




                    //Obter a paleta de cores da imagem
                    binding.mainPlayerPoster.getPalletColors { colors ->
                        val (color1, color2) = colors
                        try {
//                            binding.mainContainer.changeBackgroundColorWithGradient(
//                                color1 = darkenColor(color1, 90.0),
//                                color2 = darkenColor(color2, 70.0)
//                            )

                            binding.mainContainer.styleBackground(
                                backgroundColorsList = mutableListOf(darkenColor(color1, 97.0), darkenColor(color2, 65.0)),
                                topLeftRadius = 40f,
                                topRightRadius = 40f
                            )

                            onBackgroundCollorsChangeCallback?.invoke(darkenColor(color1, 90.0), darkenColor(color2, 70.0))
                        } catch (e: Exception) {
                            log(e)
                        }
                    }
                },
                onError = {
                    binding.miniPlayerPoster.setImageResource(R.drawable.background)
                    binding.mainPlayerPoster.setImageResource(R.drawable.background)
                }
            )
        }
    }

    override fun updateCurrentEpisode(episode: EpisodeViewModel){
        currentEpisode = episode
    }

    private val playerStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == PLAYER_STATUS_UPDATE) {
                val isPlaying = intent.getBooleanExtra(IS_PLAYING, false)
                val currentTime = intent.getIntExtra(CURRENT_TIME, 0)
                val totalTime = intent.getIntExtra(TOTAL_TIME, 0)

                updateButtonsUi(isPlaying, currentTime, totalTime)

                updateDatabase(isPlaying, currentTime, totalTime)

                updatePlayerTimerUI(currentTime, totalTime)

                //log("\n\nFloating Player playerStatusReceiver currentEpisode: ${currentEpisode.title} | ${currentEpisode.elapsedTime}")
            }
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.setupBottomSheet {
            collapse()
        }
        this.setUpTouch()

        val intentFilter = IntentFilter(PLAYER_STATUS_UPDATE)
        context.registerReceiver(playerStatusReceiver, intentFilter, Context.RECEIVER_EXPORTED)
    }



    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(playerStatusReceiver)
    }



    override fun updatePlayerTimerUI(currentTime: Int, totalTime: Int) {
        binding.mainPlayerCurrentTime.text = convertMillisecondsToTime(currentTime)
        binding.mainPlayerTotalTime.text = convertMillisecondsToTime(totalTime)
        binding.mainPlayerSeekBar.max = totalTime
        binding.mainPlayerSeekBar.progress = currentTime
    }



    private fun setupBottomSheet(onInitialized: () -> Unit) {
        bottomSheetBehavior = BottomSheetBehavior.from(this)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = binding.miniPlayer.layoutParams.height
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> onExpandedCallback?.invoke()
                    BottomSheetBehavior.STATE_COLLAPSED -> onCollapsedCallback?.invoke()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.linearLayoutMiniPlayer.alpha = 1 - slideOffset
                binding.mainPlayer.alpha = slideOffset
                binding.linearLayoutArrowDown.alpha = slideOffset
            }
        })
        onInitialized()
    }



    fun setonBackgroundCollorsChangeListener(listener: (String, String) -> Unit) {
        this.onBackgroundCollorsChangeCallback = listener
    }




    private fun setUpTouch() {
        binding.miniPlayer.setOnClickListener {
            this.expand()
        }

        binding.miniPlayerPlayPauseButton.setOnClickListener {
            playPauseClicked()

            binding.miniPlayerPlayPauseButton.showLoading(isPlaying)
        }

        binding.mainPlayerPlayPauseButton.setOnClickListener {
            playPauseClicked()

            binding.mainPlayerPlayPauseButton.showLoading(isPlaying)
        }

        binding.mainPlayerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        binding.buttomJumpTo.setOnClickListener(){
            this.seekTo(currentEpisode.jumpToTime * 1000)
        }

        binding.buttomReplay15.setOnClickListener(){
            this.seekTo(currentEpisode.elapsedTime - 15000)
        }

        binding.buttomFoward15.setOnClickListener() {
            this.seekTo(currentEpisode.elapsedTime + 15000)
        }
    }



    private fun playPauseClicked() {
        val intent = Intent(context, AudioService3::class.java)
        if (isPlaying) {
            intent.action = "PAUSE"
            isPlaying = false
        } else {
            intent.action = "PLAY"
            isPlaying = true
        }


        //log("")
        //log("--------------------------")
        //log("\n\nFloating Player playPauseClicked currentEpisode: ${currentEpisode.title} | ${currentEpisode.elapsedTime}")

        log(context.packageName)
        intent.putExtra("id", currentEpisode.id)
        intent.putExtra("title", currentEpisode.title)
        intent.putExtra("product", currentEpisode.product)
        intent.putExtra("audioUrl", currentEpisode.audioUrl)
        intent.putExtra("imageUrl", currentEpisode.imageUrl)
        intent.putExtra("elapsedTime", currentEpisode.elapsedTime)

        context.startService(intent)
    }



    private fun seekTo(position: Int) {
        val intent = Intent(context, AudioService3::class.java)
        intent.action = "SEEK_TO"
        intent.putExtra("position", position)
        context.startService(intent)
    }



    fun startEpisode(episode: EpisodeViewModel) {
        binding.miniPlayerPlayPauseButton.showLoading(true)
        binding.mainPlayerPlayPauseButton.showLoading(true)

        currentEpisode = episode

        presenter.setCurrentPlayingEpisodeId(episode)


        this.updateButtonsUi(isPlaying, episode.elapsedTime.toInt(), episode.duration.toInt())
        this.showUi()


        val intent = Intent(context, AudioService3::class.java)
        intent.action = "PLAY"
        intent.putExtra("id", episode.id)
        intent.putExtra("title", episode.title)
        intent.putExtra("product", episode.product)
        intent.putExtra("audioUrl", episode.audioUrl)
        intent.putExtra("imageUrl", episode.imageUrl)
        intent.putExtra("elapsedTime", episode.elapsedTime)

//        log("")
//        log("--------------------------")
//        log("\n\nFloating Player startEpisode currentEpisode: ${currentEpisode.title} | ${currentEpisode.elapsedTime}")

        context.startService(intent)

        // Atualizar a UI do floating_player
        updateInfosUi(episode)

        isPlaying = true
    }


    private fun updateDatabase(isPlaying: Boolean, currentTime: Int, totalTime: Int) {
        this.isPlaying = isPlaying
        if(isPlaying) {
            currentEpisode.elapsedTime = currentTime
            currentEpisode.duration = totalTime
            presenter.updateEpisode(currentEpisode)
        }
    }


     override fun updateButtonsUi(isPlaying: Boolean, currentTime: Int, totalTime: Int) {
        this.isPlaying = isPlaying
        if (isPlaying) {
            binding.miniPlayerPlayPauseButton.setIconResource(R.drawable.icon_pause)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.icon_pause)
            //binding.mainPlayerPlayPauseButton.setLabel("Pause")

            binding.miniPlayerPlayPauseButton.showLoading(false)
            binding.mainPlayerPlayPauseButton.showLoading(false)

            if(currentTime > 500){
                if(currentEpisode.jumpToTime != 0){
                    binding.buttomJumpTo.fadeInAnimationNoRepeat {
                        binding.buttomJumpTo.visibility = View.VISIBLE
                    }
                }

                binding.buttomFoward15.fadeInAnimationNoRepeat {
                    binding.buttomFoward15.visibility = View.VISIBLE
                }

                binding.buttomReplay15.fadeInAnimationNoRepeat {
                    binding.buttomReplay15.visibility = View.VISIBLE
                }
            }else{
                if(currentEpisode.jumpToTime != 0) {
                    binding.buttomJumpTo.fadeOutAnimationNoRepeat {
                        binding.buttomJumpTo.visibility = View.GONE
                    }
                }

                binding.buttomFoward15.fadeOutAnimationNoRepeat {
                    binding.buttomFoward15.visibility = View.GONE
                }


                binding.buttomReplay15.fadeOutAnimationNoRepeat {
                    binding.buttomReplay15.visibility = View.GONE
                }
            }
        } else {
            binding.miniPlayerPlayPauseButton.setIconResource(R.drawable.icon_play)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.icon_play)
            //binding.mainPlayerPlayPauseButton.setLabel("Play")

            binding.miniPlayerPlayPauseButton.showLoading(false)
            binding.mainPlayerPlayPauseButton.showLoading(false)

            if(currentEpisode.jumpToTime != 0) {
                binding.buttomJumpTo.fadeOutAnimationNoRepeat {
                    binding.buttomJumpTo.visibility = View.GONE
                }
            }

            binding.buttomFoward15.fadeOutAnimationNoRepeat {
                binding.buttomFoward15.visibility = View.GONE
            }


            binding.buttomReplay15.fadeOutAnimationNoRepeat {
                binding.buttomReplay15.visibility = View.GONE
            }
        }
    }










    fun expand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setOnExpandedCallback(callback: () -> Unit) {
        this.onExpandedCallback = callback
    }

    fun collapse() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun setOnCollapsedCallback(callback: () -> Unit) {
        this.onCollapsedCallback = callback
    }






    fun stopService() {
        val intent = Intent(context, AudioService3::class.java)
        intent.action = "STOP"
        context.startService(intent)
    }


    companion object {
        const val PLAYER_STATUS_UPDATE = "com.example.renerd.components.floating_player.PLAYER_STATUS_UPDATE"
        const val IS_PLAYING = "isPlaying"
        const val CURRENT_TIME = "currentTime"
        const val TOTAL_TIME = "totalTime"
    }
}