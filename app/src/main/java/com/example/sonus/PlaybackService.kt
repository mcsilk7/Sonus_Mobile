package com.example.sonus

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.sonus.network.RetrofitClient

class PlaybackService : Service() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.sonus.ACTION_PLAY_PAUSE"
        const val ACTION_PREVIOUS = "com.example.sonus.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.sonus.ACTION_NEXT"
        const val ACTION_STOP = "com.example.sonus.ACTION_STOP"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        PlayerState.addStateListener(playerListener)
    }

    private val playerListener = object : PlayerState.PlayerStateListener {
        override fun onStateChanged() {
            showNotification()
        }
    }

    override fun onDestroy() {
        PlayerState.removeStateListener(playerListener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Call showNotification immediately to ensure startForeground is called
        showNotification()
        
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                PlayerState.togglePlayPause(this)
            }
            ACTION_PREVIOUS -> {
                PlayerState.playPrevious(this)
            }
            ACTION_NEXT -> {
                PlayerState.playNext(this)
            }
            ACTION_STOP -> {
                stopPlayback()
            }
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopPlayback()
    }

    private fun stopPlayback() {
        PlayerState.stop()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showNotification() {
        val song = PlayerState.currentSong

        val activityIntent = Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIntent = Intent(this, PlaybackService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, PlaybackService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val prevPendingIntent = PendingIntent.getService(
            this, 3, prevIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent = Intent(this, PlaybackService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 4, nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIcon = if (PlayerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseText = if (PlayerState.isPlaying) "Pause" else "Play"

        val builder = NotificationCompat.Builder(this, SonusApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(song?.title ?: "Sonus")
            .setContentText(song?.artist ?: "Przygotowywanie...")
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(PlayerState.isPlaying)
            .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))

        // First, call startForeground with placeholder to avoid ANR/Crash
        updateNotification(builder.build())

        if (song != null) {
            val coverUrl = if (song.coverPath?.startsWith("http") == true) {
                song.coverPath
            } else {
                RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
            }
            val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(this, coverUrl)

            Glide.with(this)
                .asBitmap()
                .load(authenticatedUrl)
                .placeholder(R.drawable.bg_cover_placeholder)
                .error(R.drawable.bg_cover_placeholder)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        builder.setLargeIcon(resource)
                        updateNotification(builder.build())
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        builder.setLargeIcon(null as Bitmap?)
                        updateNotification(builder.build())
                    }
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        updateNotification(builder.build())
                    }
                })
        }
    }

    private fun updateNotification(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}
