package com.example.sonus

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sonus.network.RetrofitClient

class PlayerActivity : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val playerListener = object : PlayerState.PlayerStateListener {
        override fun onStateChanged() {
            runOnUiThread {
                updatePlayPauseIcon(findViewById(R.id.btnPlayPause))
                updateRepeatIcon(findViewById(R.id.btnRepeat))
                updateShuffleIcon(findViewById(R.id.btnShuffle))
                updateSongInfo()
            }
        }
    }

    private val updateProgressAction = object : Runnable {
        override fun run() {
            if (PlayerState.isPlaying) {
                val current = PlayerState.getCurrentPosition()
                val total = PlayerState.getDuration()
                if (total > 0) {
                    seekBar.max = total
                    seekBar.progress = current
                    tvCurrentTime.text = formatTime(current)
                    tvTotalTime.text = formatTime(total)
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val btnPlayPause = findViewById<ImageView>(R.id.btnPlayPause)
        val btnPrevious = findViewById<ImageView>(R.id.btnPrevious)
        val btnNext = findViewById<ImageView>(R.id.btnNext)
        val btnRepeat = findViewById<TextView>(R.id.btnRepeat)
        val btnShuffle = findViewById<TextView>(R.id.btnShuffle)
        val btnQueue = findViewById<TextView>(R.id.btnQueue)
        seekBar = findViewById(R.id.seekBarPlayer)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)

        PlayerState.addStateListener(playerListener)

        btnPlayPause.setOnClickListener {
            PlayerState.togglePlayPause(this)
        }

        btnPrevious.setOnClickListener {
            PlayerState.playPrevious(this)
        }

        btnNext.setOnClickListener {
            PlayerState.playNext(this)
        }

        btnRepeat.setOnClickListener {
            PlayerState.toggleRepeat()
        }

        btnShuffle.setOnClickListener {
            PlayerState.toggleShuffle()
        }

        btnQueue.setOnClickListener {
            val bottomSheet = QueueBottomSheet()
            bottomSheet.show(supportFragmentManager, "QueueBottomSheet")
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { PlayerState.seekTo(it.progress) }
            }
        })

        updatePlayPauseIcon(btnPlayPause)
        updateRepeatIcon(btnRepeat)
        updateShuffleIcon(btnShuffle)
        updateSongInfo()

        // Powrót
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        handler.post(updateProgressAction)
    }

    private fun updateSongInfo() {
        val song = PlayerState.currentSong
        val songId = if (song != null) song.id else intent.getLongExtra("SONG_ID", -1)
        val title = if (song != null) song.title else intent.getStringExtra("SONG_TITLE") ?: "Nieznany tytuł"
        val artist = if (song != null) song.artist else intent.getStringExtra("SONG_ARTIST") ?: "Nieznany artysta"

        findViewById<TextView>(R.id.tvPlayerSongTitle).text = title
        findViewById<TextView>(R.id.tvPlayerArtist).text = artist

        val imgCover = findViewById<ImageView>(R.id.imgPlayerCover)
        val coverUrl = if (song?.coverPath?.startsWith("http") == true) {
            song.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/songs/$songId/cover"
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(this, coverUrl)

        val radius = (24 * resources.displayMetrics.density).toInt()

        com.bumptech.glide.Glide.with(this)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_cover_placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
            .into(imgCover)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerState.removeStateListener(playerListener)
        handler.removeCallbacks(updateProgressAction)
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun updatePlayPauseIcon(btn: ImageView) {
        if (PlayerState.isPlaying) {
            btn.setImageResource(R.drawable.ic_pause)
            btn.setColorFilter(ContextCompat.getColor(this, R.color.app_background))
        } else {
            btn.setImageResource(R.drawable.ic_play)
            btn.setColorFilter(ContextCompat.getColor(this, R.color.app_background))
        }
    }

    private fun updateRepeatIcon(btn: TextView) {
        val indicator = findViewById<View>(R.id.repeatIndicator)
        if (PlayerState.isRepeatEnabled) {
            indicator.visibility = View.VISIBLE
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_dark)) // Keep it dark
        } else {
            indicator.visibility = View.INVISIBLE
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        }
    }

    private fun updateShuffleIcon(btn: TextView) {
        val indicator = findViewById<View>(R.id.shuffleIndicator)
        if (PlayerState.isShuffleEnabled) {
            indicator.visibility = View.VISIBLE
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        } else {
            indicator.visibility = View.INVISIBLE
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
        }
    }
}
