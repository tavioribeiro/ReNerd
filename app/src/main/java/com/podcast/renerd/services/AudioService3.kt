package com.podcast.renerd.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.podcast.renerd.features.episodes.EpisodesActivity

class AudioService3 : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            val player = exoPlayer ?: return
            if (player.isPlaying) {
                sendProgressBroadcast()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val seekBackCommand = SessionCommand("seek_back", Bundle.EMPTY)
    private val seekForwardCommand = SessionCommand("seek_forward", Bundle.EMPTY)

    private val sessionCallback = object : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                .buildUpon()
                .add(seekBackCommand)
                .add(seekForwardCommand)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setCustomLayout(
                    listOf(
                        CommandButton.Builder(CommandButton.ICON_SKIP_BACK_15)
                            .setSessionCommand(seekBackCommand)
                            .setDisplayName("Retroceder 15s")
                            .build(),
                        CommandButton.Builder(CommandButton.ICON_SKIP_FORWARD_15)
                            .setSessionCommand(seekForwardCommand)
                            .setDisplayName("Avançar 15s")
                            .build()
                    )
                )
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            val player = session.player
            when (customCommand.customAction) {
                "seek_back" -> player.seekTo(maxOf(0, player.currentPosition - 15000))
                "seek_forward" -> player.seekTo(minOf(player.duration, player.currentPosition + 15000))
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private fun initializePlayer() {
        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(15000)
            .setSeekForwardIncrementMs(15000)
            .build()
        exoPlayer = player

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        player.setAudioAttributes(audioAttributes, true)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                sendProgressBroadcast()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                sendProgressBroadcast()
                if (isPlaying) {
                    handler.post(progressRunnable)
                } else {
                    handler.removeCallbacks(progressRunnable)
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                error.printStackTrace()
            }
        })

        val intent = Intent(this, EpisodesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(sessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            "PLAY_EPISODE" -> {
                val id = intent.getIntExtra("id", 0)
                val title = intent.getStringExtra("title") ?: "Sem Titulo"
                val audioUrl = intent.getStringExtra("audioUrl") ?: ""
                val imageUrl = intent.getStringExtra("imageUrl") ?: ""
                val product = intent.getStringExtra("product") ?: ""
                val elapsedTime = intent.getLongExtra("elapsedTime", 0L)

                if (audioUrl.isNotEmpty()) {
                    playEpisode(title, product, audioUrl, imageUrl, elapsedTime)
                }
            }

            "PLAY" -> {
                val position = intent.getStringExtra("position")
                if (position != null) {
                    exoPlayer?.seekTo(position.toLong())
                }
                exoPlayer?.play()
            }

            "PAUSE" -> {
                exoPlayer?.pause()
            }
        }

        return START_STICKY
    }

    private fun sendProgressBroadcast() {
        val player = exoPlayer ?: return

        val totalTime = player.duration.toInt()
        val currentTime = player.currentPosition.toInt()
        val isPlaying = player.isPlaying

        val broadcastIntent = Intent("MY_ACTION").apply {
            putExtra("playerTotalTime", totalTime.toString())
            putExtra("playerCurrentTime", currentTime.toString())
            if (isPlaying) {
                putExtra("played", "true")
            } else {
                putExtra("paused", "true")
            }
        }
        sendBroadcast(broadcastIntent)
    }

    @OptIn(UnstableApi::class)
    private fun playEpisode(title: String, artist: String, audioUrl: String, imageUrl: String, startTime: Long) {
        val player = exoPlayer ?: return

        val currentItem = player.currentMediaItem

        if (currentItem?.mediaId == audioUrl) {
            if (player.playbackState != Player.STATE_IDLE && player.playbackState != Player.STATE_ENDED) {
                if (!player.isPlaying) {
                    player.play()
                }
                return
            }
            player.prepare()
            if (startTime > 0 && player.currentPosition < 1000) {
                player.seekTo(startTime)
            }
            player.play()
            return
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(Uri.parse(imageUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaId(audioUrl)
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()

        if (startTime > 0) {
            player.seekTo(startTime)
        }

        player.play()
    }

    override fun onDestroy() {
        handler.removeCallbacks(progressRunnable)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
