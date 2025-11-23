package com.podcast.renerd.features.episodes.components.floating_player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.podcast.renerd.R
import com.podcast.renerd.core.extentions.*
import com.podcast.renerd.core.utils.convertMillisecondsToTime
import com.podcast.renerd.core.utils.log
import com.podcast.renerd.databinding.CFloatingPlayerLayoutBinding
import com.podcast.renerd.services.AudioService3
import com.podcast.renerd.view_models.EpisodeViewModel
import org.koin.java.KoinJavaComponent.inject
import core.extensions.*

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

    // Media3
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val TAG = "[RENERD_DEBUG] FloatingPlayer"

    // Controle de Loading e Sync
    private var pendingAudioUrl: String? = null

    // Timer UI
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            mediaController?.let { controller ->
                if (controller.isPlaying) {
                    val current = controller.currentPosition.toInt()
                    val total = controller.duration.toInt()
                    updatePlayerTimerUI(current, total)

                    currentEpisode.elapsedTime = current
                    presenter.updateEpisode(currentEpisode)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    private var currentEpisode = EpisodeViewModel()
    private var isUserSeeking = false

    init {
        binding.mainContainer.visibility = View.GONE
        presenter.attachView(this)
        presenter.getCurrentPlayingEpisode()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupBottomSheet { collapse() }
        setUpTouch()
        initializeMediaController()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateProgressRunnable)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, AudioService3::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                updateUiState("InitialConnection")
            } catch (e: Exception) {
                log("$TAG: Erro conexão: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateUiState("onEvents")
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            pendingAudioUrl = null
            binding.miniPlayerPlayPauseButton.showLoading(false)
            binding.mainPlayerPlayPauseButton.showLoading(false)
        }
    }

    private fun updateUiState(source: String) {
        val controller = mediaController ?: return

        val state = controller.playbackState
        val isPlaying = controller.isPlaying
        val currentMediaId = controller.currentMediaItem?.mediaId

        // --- LÓGICA DE LOADING ---
        // Só mostra loading se estiver Bufferizando OU se estivermos esperando sincronizar o ID do áudio novo
        var isLoading = state == Player.STATE_BUFFERING

        if (pendingAudioUrl != null) {
            if (currentMediaId != pendingAudioUrl) {
                isLoading = true
            } else {
                pendingAudioUrl = null
            }
        }

        binding.miniPlayerPlayPauseButton.showLoading(isLoading)
        binding.mainPlayerPlayPauseButton.showLoading(isLoading)

        if (!isLoading) {
            updateButtonsUi(isPlaying, controller.currentPosition.toInt(), controller.duration.toInt())

            if (isPlaying && state == Player.STATE_READY) {
                if (!handler.hasCallbacks(updateProgressRunnable)) handler.post(updateProgressRunnable)
            } else {
                handler.removeCallbacks(updateProgressRunnable)
            }
        } else {
            handler.removeCallbacks(updateProgressRunnable)
        }
    }

    private fun setUpTouch() {
        binding.miniPlayer.setOnClickListener { this.expand() }
        binding.miniPlayerPlayPauseButton.setOnClickListener { togglePlayPause() }
        binding.mainPlayerPlayPauseButton.setOnClickListener { togglePlayPause() }

        binding.mainPlayerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) updatePlayerTimerUI(progress, seekBar?.max ?: 0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaController?.seekTo(seekBar?.progress?.toLong() ?: 0)
            }
        })

        binding.buttomJumpTo.setOnClickListener {
            mediaController?.seekTo(currentEpisode.jumpToTime * 1000L)
        }
        binding.buttomReplay15.setOnClickListener {
            mediaController?.let { it.seekTo(it.currentPosition - 15000) }
        }
        binding.buttomFoward15.setOnClickListener {
            mediaController?.let { it.seekTo(it.currentPosition + 15000) }
        }
    }

    // --- CORREÇÃO PRINCIPAL AQUI ---
    private fun togglePlayPause() {
        val controller = mediaController ?: return

        if (controller.isPlaying) {
            // Se está tocando, pausa imediatamente
            controller.pause()
            // Atualiza UI otimista para Pause
            updateButtonsUi(false, controller.currentPosition.toInt(), controller.duration.toInt())
        } else {
            // Se está pausado, verifica se já está pronto (Resume) ou se precisa carregar
            if (controller.playbackState == Player.STATE_READY) {
                // Áudio já carregado, RESUME instantâneo
                binding.miniPlayerPlayPauseButton.showLoading(false)
                binding.mainPlayerPlayPauseButton.showLoading(false)

                // Atualiza UI otimista para Play
                updateButtonsUi(true, controller.currentPosition.toInt(), controller.duration.toInt())

                controller.play()
            } else {
                // Áudio não está pronto (Ex: Idle, Ended ou Buffering), mostra loading
                binding.miniPlayerPlayPauseButton.showLoading(true)
                binding.mainPlayerPlayPauseButton.showLoading(true)
                controller.play()
            }
        }
    }

    fun startEpisode(episode: EpisodeViewModel) {
        // Começando NOVO episódio: Trava o ID e força loading
        pendingAudioUrl = episode.audioUrl

        binding.miniPlayerPlayPauseButton.showLoading(true)
        binding.mainPlayerPlayPauseButton.showLoading(true)

        currentEpisode = episode
        presenter.setCurrentPlayingEpisodeId(episode)

        updateInfosUi(episode)
        showUi()

        val intent = Intent(context, AudioService3::class.java).apply {
            action = "PLAY_EPISODE"
            putExtra("id", episode.id)
            putExtra("title", episode.title)
            putExtra("product", episode.product)
            putExtra("audioUrl", episode.audioUrl)
            putExtra("imageUrl", episode.imageUrl)
            putExtra("elapsedTime", episode.elapsedTime.toLong())
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // ... [O restante dos métodos: showUi, updateInfosUi, updateButtonsUi, etc, permanecem iguais] ...
    override fun showUi() {
        binding.mainContainer.fadeInAnimation {
            binding.mainContainer.visibility = View.VISIBLE
        }
    }

    override fun updateInfosUi(episode: EpisodeViewModel) {
        binding.miniPlayerTitle.text = episode.title
        binding.mainPlayerTitle.text = episode.title
        binding.miniPlayerProductName.text = episode.productName
        binding.mainPlayerProductName.text = episode.productName
        binding.mainPlayerDescription.text = Html.fromHtml("${episode.description}")

        binding.miniPlayer.isSelected = true
        binding.mainPlayerTitle.isSelected = true

        binding.miniPlayerPoster.startSkeletonAnimation(20f)
        binding.mainPlayerPoster.startSkeletonAnimation(30f)

        binding.miniPlayerPoster.load(episode.imageUrl) {
            target(
                onSuccess = { drawable ->
                    binding.miniPlayerPoster.getSizes { width, height ->
                        val crop = drawable.cropCenterSection(widthDp = width, heightDp = height, resources)
                        binding.miniPlayerPoster.setImageDrawable(crop.toAllRoundedDrawable(20f))
                        binding.miniPlayerPoster.stopSkeletonAnimation()
                    }
                    binding.mainPlayerPoster.getSizes { width, height ->
                        val resize = drawable.resize(width = width, height = (width / 1.682242991).toInt(), resources)
                        binding.mainPlayerPoster.setImageDrawable(resize.toAllRoundedDrawable(16f))
                        binding.mainPlayerPoster.stopSkeletonAnimation()
                    }
                    binding.mainPlayerPoster.getPalletColors { colors ->
                        val (color1, color2) = colors
                        try {
                            binding.mainContainer.styleBackground(
                                backgroundColorsList = mutableListOf(darkenColor(color1, 97.0), darkenColor(color2, 65.0)),
                                topLeftRadius = 40f,
                                topRightRadius = 40f
                            )
                            onBackgroundCollorsChangeCallback?.invoke(darkenColor(color1, 90.0), darkenColor(color2, 70.0))
                        } catch (e: Exception) { log(e) }
                    }
                },
                onError = {
                    binding.miniPlayerPoster.setImageResource(R.drawable.background)
                    binding.mainPlayerPoster.setImageResource(R.drawable.background)
                }
            )
        }
    }

    override fun updateCurrentEpisode(episode: EpisodeViewModel) {
        currentEpisode = episode
    }

    override fun updatePlayerTimerUI(currentTime: Int, totalTime: Int) {
        binding.mainPlayerCurrentTime.text = convertMillisecondsToTime(currentTime)
        binding.mainPlayerTotalTime.text = convertMillisecondsToTime(totalTime)
        if (!isUserSeeking) {
            binding.mainPlayerSeekBar.max = totalTime
            binding.mainPlayerSeekBar.progress = currentTime
        }
    }

    override fun updateButtonsUi(isPlaying: Boolean, currentTime: Int, totalTime: Int) {
        if (isPlaying) {
            binding.miniPlayerPlayPauseButton.setIconResource(R.drawable.icon_pause)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.icon_pause)

            if (currentTime > 500) {
                if (currentEpisode.jumpToTime != 0) {
                    binding.buttomJumpTo.fadeInAnimationNoRepeat { binding.buttomJumpTo.visibility = View.VISIBLE }
                }
                binding.buttomFoward15.fadeInAnimationNoRepeat { binding.buttomFoward15.visibility = View.VISIBLE }
                binding.buttomReplay15.fadeInAnimationNoRepeat { binding.buttomReplay15.visibility = View.VISIBLE }
            }
        } else {
            binding.miniPlayerPlayPauseButton.setIconResource(R.drawable.icon_play)
            binding.mainPlayerPlayPauseButton.setIconResource(R.drawable.icon_play)
        }
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

    fun expand() { bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }
    fun setOnExpandedCallback(callback: () -> Unit) { this.onExpandedCallback = callback }
    fun collapse() { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }
    fun setOnCollapsedCallback(callback: () -> Unit) { this.onCollapsedCallback = callback }
    fun setonBackgroundCollorsChangeListener(listener: (String, String) -> Unit) { this.onBackgroundCollorsChangeCallback = listener }

    fun stopService() {
        mediaController?.stop()
        val intent = Intent(context, AudioService3::class.java)
        context.stopService(intent)
    }
}