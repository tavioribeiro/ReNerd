package com.example.renerd.components.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.example.renerd.R
import com.example.renerd.core.utils.formatTime
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.BottomSheetLayoutBinding
import com.example.renerd.services.AudioService3
import com.example.renerd.view_models.EpisodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.java.KoinJavaComponent.inject


class FloatingPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), FloatingPlayerContract.View {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var onExpandedCallback: (() -> Unit)? = null
    private var onCollapsedCallback: (() -> Unit)? = null

    private val binding: BottomSheetLayoutBinding = BottomSheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val presenter: FloatingPlayerContract.Presenter by inject(clazz = FloatingPlayerContract.Presenter::class.java)

    private var isPlaying = false

    private var currentEpisode = EpisodeViewModel()

    init {
        // View.inflate(context, R.layout.bottom_sheet_layout, this)
    }


    private val playerStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == PLAYER_STATUS_UPDATE) {
                val isPlaying = intent.getBooleanExtra(IS_PLAYING, false)
                val currentTime = intent.getIntExtra(CURRENT_TIME, 0)
                val totalTime = intent.getIntExtra(TOTAL_TIME, 0)

                updateUi(isPlaying, currentTime, totalTime)

                //log(currentTime)
                //log(totalTime)

                updateDatabase(isPlaying, currentTime, totalTime)
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

        val intentFilter = IntentFilter(FloatingPlayer.PLAYER_STATUS_UPDATE)
        context.registerReceiver(playerStatusReceiver, intentFilter, Context.RECEIVER_EXPORTED)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(playerStatusReceiver)
    }



    private fun updatePlayerUI(currentTime: Int, totalTime: Int) {
        binding.mainPlayerCurrentTime.text = formatTime(currentTime)
        binding.mainPlayerTotalTime.text = formatTime(totalTime)
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
                binding.miniPlayer.alpha = 1 - slideOffset
                binding.mainPlayer.alpha = slideOffset
            }
        })
        onInitialized()
    }



    private fun setUpTouch() {
        binding.miniPlayer.setOnClickListener {
            this.expand()
        }

        binding.miniPlayerPlayPauseButton.setOnClickListener {
            playPauseClicked()
        }

        binding.mainPlayerPlayPauseButton.setOnClickListener {
            playPauseClicked()
        }

        binding.mainPlayerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
        context.startService(intent)
    }



    private fun seekTo(position: Int) {
        val intent = Intent(context, AudioService3::class.java)
        intent.action = "SEEK_TO"
        intent.putExtra("position", position)
        context.startService(intent)
    }



    fun startEpisode(episode: EpisodeViewModel) {
        // Pare o serviço se estiver tocando outro episódio
        //this.stopService()

        currentEpisode = episode
        this.updateUi(isPlaying, episode.elapsedTime.toInt(), episode.duration.toInt())

        val intent = Intent(context, AudioService3::class.java)
        intent.action = "PLAY"
        intent.putExtra("id", episode.id)
        intent.putExtra("title", episode.title)
        intent.putExtra("product", episode.product)
        intent.putExtra("audioUrl", episode.audioUrl)
        intent.putExtra("imageUrl", episode.imageUrl)
        intent.putExtra("elapsedTime", episode.elapsedTime)


        context.startService(intent)

        // Atualizar a UI do player
        binding.miniPlayerTitle.text = episode.title
        binding.miniPlayerProduct.text = episode.product
        binding.miniPlayerPoster.load(episode.imageUrl)
        binding.mainPlayerPoster.load(episode.imageUrl)

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


    fun updateUi(isPlaying: Boolean, currentTime: Int, totalTime: Int) {
        this.isPlaying = isPlaying
        if (isPlaying) {
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_pause)
        } else {
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_play)
        }
        updatePlayerUI(currentTime, totalTime)
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
        const val PLAYER_STATUS_UPDATE = "com.example.renerd.components.player.PLAYER_STATUS_UPDATE"
        const val IS_PLAYING = "isPlaying"
        const val CURRENT_TIME = "currentTime"
        const val TOTAL_TIME = "totalTime"
    }
}