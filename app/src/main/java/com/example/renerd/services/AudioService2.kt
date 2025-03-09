package com.example.renerd.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.renerd.R
import com.example.renerd.components.floating_player.FloatingPlayer
import com.example.renerd.core.extentions.loadBitmapFromUrl
import com.example.renerd.core.utils.log
import com.example.renerd.features.player.PlayerActivity
import com.example.renerd.view_models.EpisodeViewModel
import kotlinx.coroutines.*
import java.io.IOException

class AudioService2 : Service() {

    private val context = this
    private var player: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var isPaused = false

    private val currentEpisode = EpisodeViewModel()

    private lateinit var albumArt: Bitmap

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var job: Job? = null
    private var mediaSession: MediaSessionCompat? = null

    // Notificação
    private lateinit var albumArtBitmap: Bitmap
    private var notificationIcon: Int = R.drawable.icon_play
    private lateinit var notificationActionTitle: String
    private lateinit var playPausePendingIntent: PendingIntent

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> pausePlaying()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlaying()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                player?.setVolume(1.0f, 1.0f)
                if (!isPlaying && !isPaused) player?.start()
            }
        }
    }


    //override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        albumArt = BitmapFactory.decodeResource(resources, R.drawable.background)
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "AudioService").apply {
            isActive = true
            setCallback(mediaSessionCallback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val newEpisodeId = intent?.getIntExtra("id", 0) ?: 0
        if (newEpisodeId != currentEpisode.id) {
            if (isPlaying) {
                stopPlaying()
            }
            extractTrackInfoFromIntent(intent)
        }

        when (intent?.action) {
            "PLAY" -> startPlaying(intent.getIntExtra("position", 2))
            "PAUSE" -> pausePlaying()
            "STOP" -> stopPlaying()
            "SEEK_TO" -> seekTo(intent.getIntExtra("position", 0))
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
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "media_playback_channel",
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPlaying(position: Int = 1) {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        requestAudioFocus()

        if (player == null || !player!!.isPlaying) {
            player?.reset()
            player = MediaPlayer().apply {
                try {
                    setDataSource(currentEpisode.audioUrl)
                    setOnPreparedListener {
                        handleMediaPlayerPrepared(position)
                    }
                    prepareAsync()
                } catch (e: IOException) {
                    log("Erro ao definir o data source")
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocus() {
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
    }


    private fun handleMediaPlayerPrepared(position: Int) {
        player?.let {
            it.start()
            it.seekTo(position)
            isPlaying = true
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, position)
            showNotification()
            startProgressUpdateJob()
            sendPlayerStatusUpdate()
        }
    }


    private fun startProgressUpdateJob() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (!isPaused) {
                    player?.let {
                        val currentPosition = it.currentPosition
                        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition)
                        sendPlayerStatusUpdate()
                    }
                }

                delay(1000L)
            }
        }
    }

    private fun updatePlaybackState(state: Int, position: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE
            )
            .setState(state, position.toLong(), 1f)

        mediaSession?.setPlaybackState(playbackStateBuilder.build())
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun showNotification() {
        val playPauseIntent = Intent(this, AudioService2::class.java).apply {
            action = if (isPlaying) "PAUSE" else "PLAY"
        }

        playPausePendingIntent = PendingIntent.getService(
            this,
            0,
            playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationIcon = if (!isPlaying) R.drawable.icon_play else R.drawable.icon_pause
        notificationActionTitle = if (isPlaying) "Pausar" else "Reproduzir"

        GlobalScope.launch(Dispatchers.Main) {
            albumArtBitmap = currentEpisode.imageUrl?.let { loadBitmapFromUrl(it, context) } ?: albumArt
            val notification = createNotification()
            startForeground(1, notification)
        }
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPlay() {
            handlePlayAction(null)
        }
        override fun onPause() {
            pausePlaying()
        }
        override fun onSkipToNext() = log("NEXT")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePlayAction(position: String?)  = startPlaying(position?.toIntOrNull() ?: 0)

    private fun createNotification(): Notification {

        val intent = Intent(this, PlayerActivity::class.java) // Substitua ActivityX pela sua Activity
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentEpisode.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentEpisode.productName)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
            .build()

        // 3. Defina o MediaMetadataCompat no MediaSession
        mediaSession?.setMetadata(mediaMetadata)

        // 4. Crie a notificação
        return NotificationCompat.Builder(this, "media_playback_channel")
            .setContentTitle(currentEpisode.title)
            .setContentText(currentEpisode.productName)
            .setSmallIcon(R.drawable.icon_play)
            .setContentIntent(pendingIntent)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            .addAction(NotificationCompat.Action(notificationIcon, notificationActionTitle, playPausePendingIntent))
            .apply {
                player?.let {
                    setProgress(it.duration, it.currentPosition, false)
                }
            }
            .build()

    }


    private fun pausePlaying() {
        isPaused = true
        if (isPlaying && player != null) {
            player?.pause()
            isPlaying = false
            player?.currentPosition?.let { updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, it) }
            showNotification()
            sendPlayerStatusUpdate()
        }
    }

    private fun seekTo(position: Int) {
        player?.seekTo(position)
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, position)
        sendPlayerStatusUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopPlaying() {
        player?.let {
            it.stop()
            it.reset() // Reseta o floating_player sem liberar
            isPlaying = false
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED, 0)
            showNotification() // Atualiza a notificação sem pará-la
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
    }

    private fun sendPlayerStatusUpdate() {
        val intent = Intent(FloatingPlayer.PLAYER_STATUS_UPDATE).apply {
            putExtra(FloatingPlayer.IS_PLAYING, isPlaying)
            putExtra(FloatingPlayer.CURRENT_TIME, player?.currentPosition ?: 0)
            putExtra(FloatingPlayer.TOTAL_TIME, player?.duration ?: 10000)
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }
}