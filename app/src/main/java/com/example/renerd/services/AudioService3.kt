package com.example.renerd.services

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import com.example.renerd.R
import com.example.renerd.components.player.FloatingPlayer
import com.example.renerd.core.extentions.loadBitmapFromUrl
import com.example.renerd.core.utils.log
import com.example.renerd.helpers.audio_focus.AudioFocusHelper
import com.example.renerd.helpers.media_session.MediaSessionHelper
import com.example.renerd.helpers.notification_compact.NotificationHelper
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.*

class AudioService3 : Service() {

    private var player: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var job: Job? = null

    private val currentEpisode = EpisodeViewModel()
    private lateinit var albumArt: Bitmap
    private lateinit var albumArtBitmap: Bitmap

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var audioFocusHelper: AudioFocusHelper
    private lateinit var mediaSessionHelper: MediaSessionHelper

    private var isPaused = false


    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        albumArt = BitmapFactory.decodeResource(resources, R.drawable.background)

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        audioFocusHelper = AudioFocusHelper(this)
        audioFocusHelper.onPause = ::pausePlaying
        audioFocusHelper.onDuck = { player?.setVolume(0.2f, 0.2f) }
        audioFocusHelper.onGain = {
            player?.setVolume(1.0f, 1.0f)
            if (!isPlaying && !isPaused) player?.start()
        }

        mediaSessionHelper = MediaSessionHelper(this)
        val mediaSession = mediaSessionHelper.createMediaSession()
        mediaSession.setCallback(mediaSessionCallback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val tempEpisode = EpisodeViewModel(
            id = intent?.getIntExtra("id", 0) ?: currentEpisode.id,
            audioUrl = intent?.getStringExtra("audioUrl") ?: currentEpisode.audioUrl,
            title = intent?.getStringExtra("title") ?: currentEpisode.title,
            productName = intent?.getStringExtra("productName") ?: currentEpisode.productName,
            imageUrl = intent?.getStringExtra("imageUrl") ?: currentEpisode.imageUrl,
            elapsedTime = intent?.getIntExtra("elapsedTime", 0) ?: 0
        )


        when (intent?.action) {
            "PLAY" -> {
                if(tempEpisode.id != currentEpisode.id && tempEpisode.id != 0){
                    this.extractTrackInfoFromIntent(intent)
                    this.stopPlaying()
                    this.startPlaying()
                }
                else{
                    this.resumePlaying()
                }
            }
            "PAUSE" -> this.pausePlaying()
            "STOP" -> this.stopPlaying()
            "SEEK_TO" -> this.seekTo(intent.getIntExtra("position", 0))
        }
        return START_NOT_STICKY
    }


    private fun extractTrackInfoFromIntent(intent: Intent?) {
        currentEpisode.id = intent?.getIntExtra("id", 0) ?: currentEpisode.id
        currentEpisode.audioUrl = intent?.getStringExtra("audioUrl") ?: currentEpisode.audioUrl
        currentEpisode.title = intent?.getStringExtra("title") ?: currentEpisode.title
        currentEpisode.productName = intent?.getStringExtra("productName") ?: currentEpisode.productName
        currentEpisode.imageUrl = intent?.getStringExtra("imageUrl") ?: currentEpisode.imageUrl
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPlaying() {
        audioFocusHelper.requestAudioFocus()

        if (player != null) {
            player?.stop()
            player?.reset()
            player?.release()
        }

        player = MediaPlayer().apply {
            try {
                setDataSource(currentEpisode.audioUrl)
                setOnPreparedListener {
                    // Use a posição salva em currentEpisode.elapsedTime
                    it.seekTo(currentEpisode.elapsedTime)
                    it.start()
                    this@AudioService3.isPlaying = true
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, currentEpisode.elapsedTime)
                    showNotification()
                    startProgressUpdateJob()
                    sendPlayerStatusUpdate()
                }
                prepareAsync()
            } catch (e: Exception) {
                // Lidar com erro
            }
        }
    }





    private fun pausePlaying() {
        if (isPlaying && player != null) {
            currentEpisode.elapsedTime = player!!.currentPosition // Salva a posição atual
            player?.pause()
            isPlaying = false
            isPaused = true
            player?.currentPosition?.let { updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, it) }
            showNotification()
            sendPlayerStatusUpdate()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun resumePlaying() {
        if (player == null) {
            // Se o player for nulo, inicie uma nova reprodução
            startPlaying()
            return
        }

        if (isPaused) {
            // Garante que a posição correta seja mantida
            val savedPosition = currentEpisode.elapsedTime
            player?.seekTo(savedPosition)
            player?.start()
            isPaused = false
            isPlaying = true
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, savedPosition)
            showNotification()
            sendPlayerStatusUpdate()
            startProgressUpdateJob()
        }
    }




    private fun seekTo(position: Int) {
        if (player == null) return
        player?.seekTo(position)
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, position)
        sendPlayerStatusUpdate()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopPlaying() {
        player?.let {
            it.stop()
            it.reset()
            isPlaying = false
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED, 0)
            showNotification()
        }
        stopProgressUpdateJob()
        audioFocusHelper.abandonAudioFocus()
        stopForeground(true)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
        mediaSessionHelper.release()
    }


    private fun startProgressUpdateJob() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (isPlaying) {
                    currentEpisode.elapsedTime = player?.currentPosition ?: 0
                    sendPlayerStatusUpdate()
                }
                delay(1000L)
            }
        }
    }


    private fun stopProgressUpdateJob() {
        job?.cancel()
    }


    private fun sendPlayerStatusUpdate() {
        val intent = Intent(FloatingPlayer.PLAYER_STATUS_UPDATE).apply {
            putExtra(FloatingPlayer.IS_PLAYING, isPlaying)
            putExtra(FloatingPlayer.CURRENT_TIME, player?.currentPosition ?: 0)
            putExtra(FloatingPlayer.TOTAL_TIME, player?.duration ?: 10000)
        }
        sendBroadcast(intent)
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun showNotification() {
        GlobalScope.launch(Dispatchers.Main) {
            //albumArtBitmap = currentEpisode.imageUrl?.let { loadBitmapFromUrl(it, this@AudioService3) } ?: albumArt
            albumArtBitmap = withContext(Dispatchers.IO) {
                currentEpisode.imageUrl?.let { loadBitmapFromUrl(it, this@AudioService3) } ?: albumArt
            }
            val notification = mediaSessionHelper.mediaSession?.let {
                notificationHelper.createNotification(
                    currentEpisode,
                    albumArtBitmap,
                    isPlaying,
                    it
                )
            }
            startForeground(1, notification)
        }
    }


    private fun updatePlaybackState(state: Int, position: Int) {
        mediaSessionHelper.updatePlaybackState(state, position)
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPlay() {
            startPlaying()
        }

        override fun onPause() {
            pausePlaying()
        }
    }
}
