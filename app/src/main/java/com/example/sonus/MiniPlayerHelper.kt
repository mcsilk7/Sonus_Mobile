package com.example.sonus

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO

object MiniPlayerHelper {
    fun setupMiniPlayer(activity: AppCompatActivity) {
        val miniPlayer = activity.findViewById<View>(R.id.miniPlayer) ?: return

        // Clean up old listener if it exists to prevent leaks
        (miniPlayer.getTag(R.id.mini_player_listener) as? PlayerState.PlayerStateListener)?.let {
            PlayerState.removeStateListener(it)
        }

        val title = activity.findViewById<TextView>(R.id.miniPlayerTitle)
        val artist = activity.findViewById<TextView>(R.id.miniPlayerArtist)
        val cover = activity.findViewById<ImageView>(R.id.miniPlayerCover)
        val btnPlay = activity.findViewById<ImageView>(R.id.miniPlayerPlay)
        val btnPrev = activity.findViewById<ImageView>(R.id.miniPlayerPrev)
        val btnNext = activity.findViewById<ImageView>(R.id.miniPlayerNext)
        val progress = activity.findViewById<android.widget.ProgressBar>(R.id.miniPlayerProgress)

        val listener = object : PlayerState.PlayerStateListener {
            override fun onStateChanged() {
                activity.runOnUiThread {
                    updateUI(activity, miniPlayer, title, artist, cover, btnPlay, btnPrev, btnNext, progress)
                }
            }
        }
        
        PlayerState.addStateListener(listener)
        miniPlayer.setTag(R.id.mini_player_listener, listener)

        updateUI(activity, miniPlayer, title, artist, cover, btnPlay, btnPrev, btnNext, progress)
        
        // Start a recurring task to update progress
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val progressRunnable = object : Runnable {
            override fun run() {
                // Only run if the activity is not finishing/destroyed
                if (activity.isFinishing || activity.isDestroyed) return
                
                if (PlayerState.isPlaying) {
                    val current = PlayerState.getCurrentPosition()
                    val total = PlayerState.getDuration()
                    if (total > 0) {
                        progress?.max = total
                        progress?.progress = current
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(progressRunnable)
    }

    fun onDestroy(activity: AppCompatActivity) {
        val miniPlayer = activity.findViewById<View>(R.id.miniPlayer) ?: return
        (miniPlayer.getTag(R.id.mini_player_listener) as? PlayerState.PlayerStateListener)?.let {
            PlayerState.removeStateListener(it)
        }
    }

    private fun updateUI(
        activity: AppCompatActivity,
        miniPlayer: View,
        title: TextView?,
        artist: TextView?,
        cover: ImageView?,
        btnPlay: ImageView?,
        btnPrev: ImageView?,
        btnNext: ImageView?,
        progress: android.widget.ProgressBar?
    ) {
        val song = PlayerState.currentSong
        if (song == null) {
            miniPlayer.visibility = View.GONE
        } else {
            miniPlayer.visibility = View.VISIBLE
            title?.text = song.title
            artist?.text = song.artist
            
            if (PlayerState.isPlaying) {
                btnPlay?.setImageResource(R.drawable.ic_pause)
                btnPlay?.setColorFilter(ContextCompat.getColor(activity, R.color.text_primary))
            } else {
                btnPlay?.setImageResource(R.drawable.ic_play)
                btnPlay?.setColorFilter(ContextCompat.getColor(activity, R.color.text_primary))
            }
            
            btnPlay?.setOnClickListener {
                PlayerState.togglePlayPause(activity)
            }

            btnPrev?.setOnClickListener {
                PlayerState.playPrevious(activity)
            }

            btnNext?.setOnClickListener {
                PlayerState.playNext(activity)
            }
            
            // Initial progress update
            val current = PlayerState.getCurrentPosition()
            val total = PlayerState.getDuration()
            if (total > 0) {
                progress?.max = total
                progress?.progress = current
            }

            val coverUrl = if (song.coverPath?.startsWith("http") == true) {
                song.coverPath
            } else {
                RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
            }
            val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(activity, coverUrl)

            val radius = (8 * activity.resources.displayMetrics.density).toInt()

            cover?.let {
                com.bumptech.glide.Glide.with(activity)
                    .load(authenticatedUrl)
                    .placeholder(R.drawable.bg_cover_placeholder)
                    .error(R.drawable.bg_cover_placeholder)
                    .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
                    .into(it)
            }

            miniPlayer.setOnClickListener {
                val intent = Intent(activity, PlayerActivity::class.java)
                intent.putExtra("SONG_ID", song.id)
                intent.putExtra("SONG_TITLE", song.title)
                intent.putExtra("SONG_ARTIST", song.artist)
                intent.putExtra("SONG_COVER", song.coverPath)
                activity.startActivity(intent)
            }
        }
    }
}
