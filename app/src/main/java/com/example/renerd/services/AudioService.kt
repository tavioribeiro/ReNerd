package com.example.renerd.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.renerd.R
import com.example.renerd.core.extentions.loadBitmapFromUrl
import com.example.renerd.core.utils.log
import com.example.renerd.features.player.PlayerActivity
import kotlinx.coroutines.*
import java.io.IOException

class AudioService : Service() {

    private val context = this
    private var player: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var isPaused = false

    private var url = ""
    private var title = ""
    private var artist = ""
    private var backgroundImageUrl = ""
    private lateinit var albumArt: Bitmap

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var job: Job? = null
    private var mediaSession: MediaSessionCompat? = null

    // Notificação
    private lateinit var albumArtBitmap: Bitmap
    private var notificationIcon: Int = R.drawable.ic_play
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

    private val playPauseReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            if (isPlaying) pausePlaying() else startPlaying()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        albumArt = BitmapFactory.decodeResource(resources, R.drawable.background)
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "AudioService").apply {
            isActive = true
            setCallback(mediaSessionCallback)
        }

        registerReceiver(playPauseReceiver, IntentFilter("PLAY_PAUSE"), RECEIVER_EXPORTED)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        extractTrackInfoFromIntent(intent)
        when (intent?.action) {
            "PLAY" -> startPlaying(intent.getStringExtra("position")?.toInt() ?: 0)
            "PAUSE" -> pausePlaying()
            "STOP" -> stopPlaying()
        }
        return START_NOT_STICKY
    }

    private fun extractTrackInfoFromIntent(intent: Intent?) {
        url = intent?.getStringExtra("url") ?: url
        title = intent?.getStringExtra("title") ?: title
        artist = intent?.getStringExtra("artist") ?: artist
        backgroundImageUrl = intent?.getStringExtra("backgroundImageUrl") ?: backgroundImageUrl
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "media_playback_channel",
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPlaying(position: Int = 0) {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        requestAudioFocus()

        if (audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isPaused = false
            initializeMediaPlayer(position)
        } else {
            log("Failed to obtain audio focus")
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

    private fun initializeMediaPlayer(position: Int) {
        if (!isPlaying) {
            if (player == null) {
                player = MediaPlayer().apply {
                    try {
                        setDataSource(url)
                        setOnPreparedListener {
                            handleMediaPlayerPrepared(position)
                        }
                        prepareAsync()
                    } catch (e: IOException) {
                        log("Error setting data source")
                    }
                }
            } else {
                player!!.start()
                isPlaying = true
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, player!!.currentPosition)
                showNotification()
            }
        } else {
            player!!.seekTo(position)
        }
    }

    private fun handleMediaPlayerPrepared(position: Int) {
        player!!.start()
        sendTotalTime(player!!.duration.toString())
        startProgressUpdateJob()
        player!!.seekTo(position)
        isPlaying = true
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, position)
        showNotification()
    }


    private fun startProgressUpdateJob() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (!isPaused) {
                    val currentPosition = player!!.currentPosition
                    sendCurrentTime(currentPosition.toString())
                    sendTotalTime(player!!.duration.toString())
                    sendPlay()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition)
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
        val playPauseIntent = Intent("PLAY_PAUSE")
        playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        notificationIcon = if (!isPlaying) R.drawable.ic_play else R.drawable.ic_pause
        notificationActionTitle = if (isPlaying) "Pausar" else "Reproduzir"

        GlobalScope.launch(Dispatchers.Main) {
            albumArtBitmap = loadBitmapFromUrl(backgroundImageUrl, context) ?: albumArt
            val notification = createNotification()
            startForeground(1, notification)
        }
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPlay() {
            handlePlayAction(null)
            sendPlay()
        }
        override fun onPause() {
            pausePlaying()
            sendPause()
        }
        override fun onSkipToNext() = log("NEXT")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePlayAction(position: String?)  = startPlaying(position?.toInt() ?: 0)

    private fun createNotification(): Notification {

        val intent = Intent(this, PlayerActivity::class.java) // Substitua ActivityX pela sua Activity
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
            .build()

        // 3. Defina o MediaMetadataCompat no MediaSession
        mediaSession?.setMetadata(mediaMetadata)

        // 4. Crie a notificação
        return NotificationCompat.Builder(this, "media_playback_channel")
            .setContentTitle(title)
            .setContentText(artist)
            .setProgress(player!!.duration, player!!.currentPosition, false)
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            .addAction(NotificationCompat.Action(notificationIcon, notificationActionTitle, playPausePendingIntent))
            .build()

    }

    private fun sendTotalTime(time: String) {
        sendBroadcast(Intent("MY_ACTION").putExtra("playerTotalTime", time))
    }

    private fun sendPause() {
        sendBroadcast(Intent("MY_ACTION")
            .putExtra("paused", "")
            .putExtra("playerCurrentTime", player!!.currentPosition.toString())
            .putExtra("playerTotalTime", player!!.duration.toString()))
    }

    private fun sendPlay() {
        sendBroadcast(Intent("MY_ACTION")
            .putExtra("played", "")
            .putExtra("playerCurrentTime", player!!.currentPosition.toString())
            .putExtra("playerTotalTime", player!!.duration.toString()))
    }

    private fun sendCurrentTime(time: String) {
        sendBroadcast(Intent("MY_ACTION").putExtra("playerCurrentTime", time))
        showNotification()
    }

    private fun pausePlaying() {
        isPaused = true
        if (isPlaying && player != null) {
            player!!.pause()
            isPlaying = false
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, player!!.currentPosition)
            showNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopPlaying() {
        player?.let {
            it.release()
            player = null
            isPlaying = false
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        }

        stopForeground(true)
        mediaSession?.release()
        mediaSession = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
        unregisterReceiver(playPauseReceiver)
    }
}