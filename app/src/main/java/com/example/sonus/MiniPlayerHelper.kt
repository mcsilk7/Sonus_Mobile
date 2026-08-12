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
        val title = activity.findViewById<TextView>(R.id.miniPlayerTitle)
        val artist = activity.findViewById<TextView>(R.id.miniPlayerArtist)
        val cover = activity.findViewById<ImageView>(R.id.miniPlayerCover)
        val btnPlay = activity.findViewById<ImageView>(R.id.miniPlayerPlay)
        val btnPrev = activity.findViewById<ImageView>(R.id.miniPlayerPrev)
        val btnNext = activity.findViewById<ImageView>(R.id.miniPlayerNext)
        val progress = activity.findViewById<android.widget.ProgressBar>(R.id.miniPlayerProgress)

        PlayerState.setOnStateChangedListener {
            activity.runOnUiThread {
                updateUI(activity, miniPlayer, title, artist, cover, btnPlay, btnPrev, btnNext, progress)
            }
        }

        updateUI(activity, miniPlayer, title, artist, cover, btnPlay, btnPrev, btnNext, progress)
        
        // Start a recurring task to update progress
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
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
        })
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

            val coverUrl = RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
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
