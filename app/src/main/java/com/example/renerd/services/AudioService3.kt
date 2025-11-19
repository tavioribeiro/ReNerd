package com.example.renerd.services

import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.renerd.features.episodes.components.floating_player.FloatingPlayer
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.*

class AudioService3 : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private var progressUpdateJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentEpisode = EpisodeViewModel()

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player.addListener(playerListener)
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tempEpisode = EpisodeViewModel(
            id = intent?.getIntExtra("id", 0) ?: 0,
            audioUrl = intent?.getStringExtra("audioUrl") ?: "",
            title = intent?.getStringExtra("title") ?: "",
            productName = intent?.getStringExtra("productName") ?: "",
            imageUrl = intent?.getStringExtra("imageUrl") ?: "",
            elapsedTime = intent?.getIntExtra("elapsedTime", 0) ?: 0
        )

        when (intent?.action) {
            "PLAY" -> {
                val isNewEpisode = tempEpisode.id != 0 && tempEpisode.id != currentEpisode.id
                if (isNewEpisode) {
                    currentEpisode = tempEpisode
                    startPlaying()
                } else {
                    player.play()
                }
            }
            "PAUSE" -> player.pause()
            "STOP" -> stopPlaying()
            "SEEK_TO" -> player.seekTo(intent.getIntExtra("position", 0).toLong())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startProgressUpdateJob()
            } else {
                stopProgressUpdateJob()
            }
            sendPlayerStatusUpdate()
        }
    }

    private fun startPlaying() {
        val mediaItem = MediaItem.fromUri(currentEpisode.audioUrl)
        player.setMediaItem(mediaItem)
        player.seekTo(currentEpisode.elapsedTime.toLong())
        player.prepare()
        player.play()
    }

    private fun stopPlaying() {
        player.stop()
        player.clearMediaItems()
    }

    private fun startProgressUpdateJob() {
        stopProgressUpdateJob()
        progressUpdateJob = serviceScope.launch {
            while (isActive) {
                sendPlayerStatusUpdate()
                delay(1000L)
            }
        }
    }

    private fun stopProgressUpdateJob() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun sendPlayerStatusUpdate() {
        val intent = Intent(FloatingPlayer.PLAYER_STATUS_UPDATE).apply {
            putExtra(FloatingPlayer.IS_PLAYING, player.isPlaying)
            putExtra(FloatingPlayer.CURRENT_TIME, player.currentPosition.toInt())
            putExtra(FloatingPlayer.TOTAL_TIME, player.duration.toInt())
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        player.removeListener(playerListener)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}