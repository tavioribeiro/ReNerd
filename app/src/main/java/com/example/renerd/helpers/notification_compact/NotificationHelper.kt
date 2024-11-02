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
import com.example.renerd.features.player.PlayerActivity
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
        mediaSessionToken: MediaSessionCompat.Token?
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

        val intent = Intent(context, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(episode.title)
            .setContentText(episode.productName)
            .setSmallIcon(R.drawable.ic_play)
            .setLargeIcon(albumArtBitmap)
            .setContentIntent(pendingIntent)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSessionToken)
                    .setShowActionsInCompactView(0)
            )
            .addAction(NotificationCompat.Action(notificationIcon, notificationActionTitle, playPausePendingIntent))
            .build()
    }
}
