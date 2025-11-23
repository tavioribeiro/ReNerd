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
import com.podcast.renerd.core.utils.log // Supondo que sua função de log exista

class AudioService3 : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private val TAG = "[RENERD_DEBUG] Service"

    override fun onCreate() {
        super.onCreate()
        log("$TAG: onCreate chamado. Inicializando Player...")
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

        // Listener interno para ver o que o ExoPlayer está fazendo "no lado do servidor"
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when(playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                log("$TAG: Internal Player State Changed -> $stateName")
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                log("$TAG: Internal Player IsPlaying Changed -> $isPlaying")
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                log("$TAG: ERRO CRÍTICO NO PLAYER -> ${error.message}")
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

        log("$TAG: Player e MediaSession inicializados com sucesso.")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        log("$TAG: onGetSession solicitado por ${controllerInfo.packageName}")
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("$TAG: onStartCommand recebido. Action: ${intent?.action}")
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == "PLAY_EPISODE") {
            val id = intent.getIntExtra("id", 0)
            val title = intent.getStringExtra("title") ?: "Sem Titulo"
            val audioUrl = intent.getStringExtra("audioUrl") ?: ""
            val elapsedTime = intent.getLongExtra("elapsedTime", 0L)

            log("$TAG: Comando PLAY_EPISODE recebido. ID=$id, Title=$title, StartTime=$elapsedTime")
            log("$TAG: Audio URL=$audioUrl")

            playEpisode(title, intent.getStringExtra("product") ?: "", audioUrl, intent.getStringExtra("imageUrl") ?: "", elapsedTime)
        }

        return START_STICKY
    }

    @OptIn(UnstableApi::class)
    private fun playEpisode(title: String, artist: String, audioUrl: String, imageUrl: String, startTime: Long) {
        val player = exoPlayer ?: return

        val currentItem = player.currentMediaItem

        // Verifica se já é o mesmo episódio
        if (currentItem?.mediaId == audioUrl) {
            log("$TAG: playEpisode -> O áudio solicitado JÁ é o atual.")
            if (player.playbackState != Player.STATE_IDLE && player.playbackState != Player.STATE_ENDED) {
                if (!player.isPlaying) {
                    log("$TAG: playEpisode -> Apenas retomando (play).")
                    player.play()
                } else {
                    log("$TAG: playEpisode -> Já está tocando, nada a fazer.")
                }
                return
            }
        }

        log("$TAG: playEpisode -> Configurando novo MediaItem...")

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(Uri.parse(imageUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaId(audioUrl) // Importante para a verificação acima
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        log("$TAG: playEpisode -> prepare() chamado.")

        if (startTime > 0) {
            log("$TAG: playEpisode -> seekTo($startTime) chamado.")
            player.seekTo(startTime)
        }

        player.play()
        log("$TAG: playEpisode -> play() chamado.")
    }

    override fun onDestroy() {
        log("$TAG: onDestroy chamado. Liberando recursos.")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}