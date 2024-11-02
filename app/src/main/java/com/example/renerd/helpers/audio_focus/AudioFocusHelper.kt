package com.example.renerd.helpers.audio_focus

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

class AudioFocusHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> onPause.invoke()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onPause.invoke()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onDuck.invoke()
            AudioManager.AUDIOFOCUS_GAIN -> onGain.invoke()
        }
    }

    lateinit var onPause: () -> Unit
    lateinit var onDuck: () -> Unit
    lateinit var onGain: () -> Unit

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestAudioFocus() {
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
        audioManager.requestAudioFocus(audioFocusRequest!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }
}


