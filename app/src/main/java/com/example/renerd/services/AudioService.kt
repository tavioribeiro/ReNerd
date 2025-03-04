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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.LinkedList

class AudioService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val mediaPlayerDispatcher = Dispatchers.IO // Dedicated dispatcher for MediaPlayer operations
    private val context = this
    private var player: MediaPlayer? = null
    private var playbackState: PlaybackState = PlaybackState.IDLE // Using state machine
    private val playbackStateMutex = Mutex() // Mutex for state transitions

    private var url = ""
    private var title = ""
    private var artist = ""
    private var backgroundImageUrl = ""
    private lateinit var albumArt: Bitmap

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var mediaSession: MediaSessionCompat? = null

    // Notificação
    private lateinit var albumArtBitmap: Bitmap
    private var notificationIcon: Int = R.drawable.icon_play
    private lateinit var notificationActionTitle: String
    private lateinit var playPausePendingIntent: PendingIntent

    // Audio Focus Queue (Implementação da fila de AudioFocus sugerida)
    private val audioFocusQueue = LinkedList<AudioFocusRequest>()
    private var currentFocusRequest: AudioFocusRequest? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> onAudioFocusLoss()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onAudioFocusLossTransient()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onAudioFocusLossTransientCanDuck()
            AudioManager.AUDIOFOCUS_GAIN -> onAudioFocusGain()
        }
    }

    private val playPauseReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            when (playbackState) {
                PlaybackState.PLAYING -> pausePlaying()
                PlaybackState.PAUSED, PlaybackState.PREPARED -> startPlaying()
                else -> startPlaying() // Handle other states if needed
            }
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
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        extractTrackInfoFromIntent(intent)
        when (intent?.action) {
            "PLAY" -> startPlaying(intent.getStringExtra("position")?.toIntOrNull() ?: 0)
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
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPlaying(position: Int = 0) {
        serviceScope.launch {
            if (requestAudioFocus()) {
                initializeMediaPlayer(position)
            } else {
                log("Failed to obtain audio focus")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun requestAudioFocus(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                continuation.resume(true, null)
            } else {
                continuation.resume(false, null)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun initializeMediaPlayer(position: Int) {
        playbackStateMutex.withLock {
            if (playbackState == PlaybackState.IDLE || playbackState == PlaybackState.STOPPED || playbackState == PlaybackState.ERROR) {
                player?.reset() // Reset if reusing MediaPlayer
                player = MediaPlayer().apply {
                    try {
                        setDataSource(url)
                        setAudioAttributes(AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                        setOnPreparedListener {
                            serviceScope.launch(Dispatchers.Main) { handleMediaPlayerPrepared(position) }
                        }
                        setOnErrorListener { _, _, _ ->
                            serviceScope.launch(Dispatchers.Main) { handleMediaPlayerError() }
                            true // Indicate that we handled the error
                        }
                        playbackState = PlaybackState.PREPARING
                        prepareAsync() // Prepare asynchronously in IO dispatcher
                    } catch (e: IOException) {
                        playbackState = PlaybackState.ERROR
                        log("Error setting data source: ${e.message}")
                        handleMediaPlayerError() // Handle error state
                    }
                }
            } else if (playbackState == PlaybackState.PAUSED || playbackState == PlaybackState.PREPARED) {
                startMediaPlayerPlayback(position)
            } else if (playbackState == PlaybackState.PLAYING) {
                seekMediaPlayer(position)
            }
        }
    }


    private suspend fun handleMediaPlayerPrepared(position: Int) {
        playbackStateMutex.withLock {
            if (playbackState == PlaybackState.PREPARING) {
                playbackState = PlaybackState.PREPARED
                startMediaPlayerPlayback(position)
            }
        }
    }

    private suspend fun startMediaPlayerPlayback(position: Int) {
        withContext(mediaPlayerDispatcher) {
            player?.apply {
                start()
                seekTo(position)
            }
        }
        playbackStateMutex.withLock {
            playbackState = PlaybackState.PLAYING
        }
        sendTotalTimeOnUiThread(player?.duration?.toString() ?: "0")
        startProgressUpdateJob()
        updatePlaybackStateCompat(PlaybackStateCompat.STATE_PLAYING, position)
        showNotificationOnUiThread()
    }


    private suspend fun seekMediaPlayer(position: Int) {
        withContext(mediaPlayerDispatcher) {
            player?.seekTo(position)
        }
        updatePlaybackStateCompat(PlaybackStateCompat.STATE_PLAYING, position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleMediaPlayerError() {
        playbackStateMutex.withLock {
            playbackState = PlaybackState.ERROR
        }
        stopPlayingInternal()
    }


    private fun startProgressUpdateJob() {
        serviceScope.launch(mediaPlayerDispatcher) { // Run on IO dispatcher for background tasks
            while (isActive && playbackState == PlaybackState.PLAYING) { // Check state for thread-safety
                player?.let { p ->
                    if (p.isPlaying) { // Double check isPlaying
                        val currentPosition = p.currentPosition
                        sendCurrentTimeOnUiThread(currentPosition.toString())
                        sendTotalTimeOnUiThread(p.duration.toString())
                        sendPlayOnUiThread()
                        updatePlaybackStateCompat(PlaybackStateCompat.STATE_PLAYING, currentPosition)
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun updatePlaybackStateCompat(state: Int, position: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, position.toLong(), 1f)

        mediaSession?.setPlaybackState(playbackStateBuilder.build())
    }


    private fun showNotificationOnUiThread() {
        serviceScope.launch { // Ensure notification updates are on Main thread
            showNotification()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showNotification() {
        val playPauseIntent = Intent("PLAY_PAUSE")
        playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        notificationIcon = when (playbackState) {
            PlaybackState.PLAYING -> R.drawable.icon_pause
            else -> R.drawable.icon_play
        }
        notificationActionTitle = when (playbackState) {
            PlaybackState.PLAYING -> "Pausar"
            else -> "Reproduzir"
        }


        GlobalScope.launch(Dispatchers.Main) { // Use GlobalScope carefully, consider serviceScope if possible for bitmap loading as well.
            albumArtBitmap = loadBitmapFromUrl(backgroundImageUrl, context) ?: albumArt
            val notification = createNotification()
            startForeground(1, notification)
        }
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPlay() {
            serviceScope.launch { startPlaying() }
            sendPlayOnUiThread()
        }
        override fun onPause() {
            pausePlaying()
            sendPauseOnUiThread()
        }
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onStop() {
            stopPlaying()
        }
        override fun onSkipToNext() = log("NEXT")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePlayAction(position: String?)  = startPlaying(position?.toIntOrNull() ?: 0)

    private fun createNotification(): Notification {

        val intent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
            .build()

        mediaSession?.setMetadata(mediaMetadata)

        return NotificationCompat.Builder(this, "media_playback_channel")
            .setContentTitle(title)
            .setContentText(artist)
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

    private fun sendTotalTimeOnUiThread(time: String) {
        serviceScope.launch(Dispatchers.Main) { sendTotalTime(time) }
    }
    private fun sendPauseOnUiThread() {
        serviceScope.launch(Dispatchers.Main) { sendPause() }
    }
    private fun sendPlayOnUiThread() {
        serviceScope.launch(Dispatchers.Main) { sendPlay() }
    }
    private fun sendCurrentTimeOnUiThread(time: String) {
        serviceScope.launch(Dispatchers.Main) { sendCurrentTime(time) }
    }


    private fun sendTotalTime(time: String) {
        sendBroadcast(Intent("MY_ACTION").putExtra("playerTotalTime", time))
    }

    private fun sendPause() {
        player?.let {
            sendBroadcast(Intent("MY_ACTION")
                .putExtra("paused", "")
                .putExtra("playerCurrentTime", it.currentPosition.toString())
                .putExtra("playerTotalTime", it.duration.toString()))
        }
    }

    private fun sendPlay() {
        player?.let {
            sendBroadcast(Intent("MY_ACTION")
                .putExtra("played", "")
                .putExtra("playerCurrentTime", it.currentPosition.toString())
                .putExtra("playerTotalTime", it.duration.toString()))
        }
    }

    private fun sendCurrentTime(time: String) {
        sendBroadcast(Intent("MY_ACTION").putExtra("playerCurrentTime", time))
        showNotificationOnUiThread() // Consider if notification on every time update is needed.
    }


    private fun pausePlaying() {
        serviceScope.launch {
            pausePlayingInternal()
        }
    }

    private suspend fun pausePlayingInternal() {
        playbackStateMutex.withLock {
            if (playbackState == PlaybackState.PLAYING) {
                withContext(mediaPlayerDispatcher) {
                    player?.pause()
                }
                playbackState = PlaybackState.PAUSED
                updatePlaybackStateCompat(PlaybackStateCompat.STATE_PAUSED, player?.currentPosition ?: 0)
                showNotificationOnUiThread()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopPlaying() {
        serviceScope.launch(Dispatchers.Main) { // Stop needs to update foreground service etc, so run on Main
            stopPlayingInternal()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun stopPlayingInternal() {
        playbackStateMutex.withLock {
            if (playbackState != PlaybackState.IDLE && playbackState != PlaybackState.STOPPED) {
                withContext(mediaPlayerDispatcher) {
                    player?.apply {
                        stop()
                        release()
                    }
                    player = null
                }
                playbackState = PlaybackState.STOPPED
                abandonAudioFocus()
                stopForeground(true)
                mediaSession?.release()
                mediaSession = null
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        serviceScope.launch {
            stopPlayingInternal()
            serviceScope.cancel()
        }
        unregisterReceiver(playPauseReceiver)
        super.onDestroy()
    }


    // Audio Focus Handling Methods
    private fun onAudioFocusLoss() {
        pausePlaying()
    }

    private fun onAudioFocusLossTransient() {
        pausePlaying()
    }

    private fun onAudioFocusLossTransientCanDuck() {
        player?.setVolume(0.2f, 0.2f)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onAudioFocusGain() {
        player?.setVolume(1.0f, 1.0f)
        serviceScope.launch {
            playbackStateMutex.withLock {
                if (playbackState == PlaybackState.PAUSED || playbackState == PlaybackState.PREPARED) {
                    startMediaPlayerPlayback(player?.currentPosition ?: 0)
                } else if (playbackState == PlaybackState.STOPPED){
                    startPlaying() // Restart if stopped after focus loss. Adapt logic as needed.
                }
            }
        }
    }


    // Playback State Enum
    private enum class PlaybackState {
        IDLE,
        PREPARING,
        PREPARED,
        PLAYING,
        PAUSED,
        STOPPED,
        ERROR
    }
}