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
import com.example.renerd.services.AudioService2
import com.example.renerd.view_models.EpisodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheet2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var onExpandedCallback: (() -> Unit)? = null
    private var onCollapsedCallback: (() -> Unit)? = null
    private val binding: BottomSheetLayoutBinding = BottomSheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentAction = "PLAY"
    private var isPlaying = false

    private val playerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "PLAYER_ACTION") {

                log(intent.getStringExtra("action"))
                when (intent.getStringExtra("action")) {
                    "play" -> {
                        binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
                        binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_pause) // Atualiza o botão do main player também
                        isPlaying = true
                        val currentTime = intent.getStringExtra("currentTime")?.toIntOrNull() ?: 0
                        val totalTime = intent.getStringExtra("totalTime")?.toIntOrNull() ?: 0
                        updatePlayerUI(currentTime, totalTime)
                    }
                    "pause" -> {
                        binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play)
                        binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_play) // Atualiza o botão do main player também
                        isPlaying = false
                        val currentTime = intent.getStringExtra("currentTime")?.toIntOrNull() ?: 0
                        val totalTime = intent.getStringExtra("totalTime")?.toIntOrNull() ?: 0
                        updatePlayerUI(currentTime, totalTime)
                    }
                    "currentTime" -> {
                        val time = intent.getStringExtra("time")?.toIntOrNull() ?: 0
                        binding.mainPlayerCurrentTime.text = formatTime(time)
                        binding.mainPlayerSeekBar.progress = time
                    }
                    "totalTime" -> {
                        val time = intent.getStringExtra("time")?.toIntOrNull() ?: 0
                        binding.mainPlayerTotalTime.text = formatTime(time)
                        binding.mainPlayerSeekBar.max = time
                    }
                }
            }
        }
    }

    init {
        //View.inflate(context, R.layout.bottom_sheet_layout, this) -
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.setupBottomSheet {
            collapse()
        }
        this.setUpTouch()
        context.registerReceiver(playerBroadcastReceiver, IntentFilter("PLAYER_ACTION"), Context.RECEIVER_NOT_EXPORTED)
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
        //bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.peekHeight = binding.miniPlayer.layoutParams.height
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //binding.root.setBackgroundColor(resources.getColor(R.color.blue))
                        onExpandedCallback?.invoke()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //binding.root.setBackgroundColor(resources.getColor(R.color.color7))
                        onCollapsedCallback?.invoke()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                log("abertura: ${slideOffset * 100}%")
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
                if (fromUser) {
                    seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playPauseClicked() {
        val intent = Intent(context, AudioService2::class.java)

        if (isPlaying) {
            intent.action = "PAUSE"
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_play)
            currentAction = "PLAY"
            isPlaying = false
        } else {
            intent.action = "PLAY"
            binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_pause)
            currentAction = "PAUSE"
            isPlaying = true
        }

        context.startService(intent)
    }

    private fun seekTo(position: Int) {
        val intent = Intent(context, AudioService2::class.java)
        intent.action = "PLAY"
        intent.putExtra("position", position.toString())
        context.startService(intent)
    }

    fun startEpisode(episode: EpisodeViewModel) {
        val intent = Intent(context, AudioService2::class.java)
        intent.action = "PLAY"

        intent.putExtra("title", episode.title)
        intent.putExtra("artist", episode.product)
        intent.putExtra("url", episode.audioUrl)
        intent.putExtra("backgroundImageUrl", episode.imageUrl)
        intent.putExtra("position", "0") // Inicia do começo

        context.startService(intent)


        binding.miniPlayerTitle.text = episode.title
        binding.miniPlayerProduct.text = episode.product
        binding.miniPlayerPoster.load(episode.imageUrl)
        binding.mainPlayerPoster.load(episode.imageUrl)



        isPlaying = true
        binding.miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
        binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.ic_pause)
        currentAction = "PAUSE"
    }

    fun expand() {
        log("expandidooo")
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setOnExpandedCallback(callback: () -> Unit) {
        this.onExpandedCallback = callback
    }

    fun collapse() {
        log("Colapsouuu")
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun setOnCollapsedCallback(callback: () -> Unit) {
        this.onCollapsedCallback = callback
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(playerBroadcastReceiver)
    }
}