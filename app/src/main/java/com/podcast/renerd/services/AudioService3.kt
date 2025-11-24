package com.podcast.renerd.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.podcast.renerd.features.episodes.EpisodesActivity

class AudioService3 : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private fun initializePlayer() {
        val player = ExoPlayer.Builder(this).build()
        exoPlayer = player

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        player.setAudioAttributes(audioAttributes, true)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when(playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {

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
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == "PLAY_EPISODE") {
            val id = intent.getIntExtra("id", 0)
            val title = intent.getStringExtra("title") ?: "Sem Titulo"
            val audioUrl = intent.getStringExtra("audioUrl") ?: ""
            val elapsedTime = intent.getLongExtra("elapsedTime", 0L)

            playEpisode(title, intent.getStringExtra("product") ?: "", audioUrl, intent.getStringExtra("imageUrl") ?: "", elapsedTime)
        }

        return START_STICKY
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
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}