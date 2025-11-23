package com.podcast.renerd.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.podcast.renerd.features.episodes.components.floating_player.FloatingPlayer
import com.podcast.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.*

class AudioService3 : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private var progressUpdateJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentEpisode = EpisodeViewModel()

    companion object {
        const val NOTIFICATION_ID = 123
        const val NOTIFICATION_CHANNEL_ID = "media_playback_channel"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this).build()
        player.addListener(playerListener)
        mediaSession = MediaSession.Builder(this, player).build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(NOTIFICATION_ID)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .build()
        setMediaNotificationProvider(notificationProvider)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

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

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.isPlaying) {
            stopSelf()
        }
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
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(currentEpisode.title)
            .setArtist(currentEpisode.productName)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(currentEpisode.audioUrl)
            .setMediaId(currentEpisode.id.toString())
            .setMediaMetadata(mediaMetadata)
            .build()

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
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