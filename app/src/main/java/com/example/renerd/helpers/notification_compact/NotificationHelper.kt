package com.example.renerd.helpers.notification_compact

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.renerd.R
import com.example.renerd.features.episodes.EpisodesActivity
import android.support.v4.media.MediaMetadataCompat
import com.example.renerd.services.AudioService3
import com.example.renerd.view_models.EpisodeViewModel

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val channelId = "media_playback_channel"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager?.createNotificationChannel(channel)
    }

    fun createNotification(
        episode: EpisodeViewModel,
        albumArtBitmap: Bitmap,
        isPlaying: Boolean,
        mediaSession: MediaSessionCompat
    ): Notification {
        val playPauseIntent = Intent(context, AudioService3::class.java).apply {
            action = if (isPlaying) "PAUSE" else "PLAY"
        }

        val playPausePendingIntent = PendingIntent.getService(
            context,
            0,
            playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationIcon = if (!isPlaying) R.drawable.ic_play else R.drawable.ic_pause
        val notificationActionTitle = if (isPlaying) "Pausar" else "Reproduzir"

        val intent = Intent(context, EpisodesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episode.product)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episode.productName)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
            .build()


        // 3. Defina o MediaMetadataCompat no MediaSession
        mediaSession.setMetadata(mediaMetadata)


        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(episode.title)
            .setContentText(episode.productName)
            .setSmallIcon(R.drawable.ic_play)
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            .addAction(NotificationCompat.Action(notificationIcon, notificationActionTitle, playPausePendingIntent))
            .apply {

            }
            .build()

    }
}
