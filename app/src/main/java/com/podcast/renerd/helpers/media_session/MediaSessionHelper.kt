package com.podcast.renerd.helpers.media_session

import android.graphics.Bitmap
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.podcast.renerd.view_models.EpisodeViewModel

class MediaSessionHelper(private val context: Context) {

    var mediaSession: MediaSessionCompat? = null

    fun createMediaSession(): MediaSessionCompat {
        mediaSession = MediaSessionCompat(context, "AudioService").apply {
            isActive = true
        }
        return mediaSession!!
    }

    fun updateMetadata(episode: EpisodeViewModel, albumArtBitmap: Bitmap) {
        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episode.productName)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
            .build()
        mediaSession?.setMetadata(mediaMetadata)
    }

    fun updatePlaybackState(state: Int, position: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE
            )
            .setState(state, position.toLong(), 1f)
        mediaSession?.setPlaybackState(playbackStateBuilder.build())
    }

    fun release() {
        mediaSession?.release()
    }
}