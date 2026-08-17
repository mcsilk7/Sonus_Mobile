package com.example.sonus.ui.player

import android.Manifest
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sonus.*
import com.example.sonus.network.RetrofitClient

class PlayerFragment : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private val handler = Handler(Looper.getMainLooper())
    
    private lateinit var retroReels: RetroReelView

    private val playerListener = object : PlayerState.PlayerStateListener {
        override fun onStateChanged() {
            activity?.runOnUiThread {
                view?.let { v ->
                    updatePlayPauseIcon(v.findViewById(R.id.btnPlayPause))
                    updateRepeatIcon(v.findViewById(R.id.btnRepeat))
                    updateShuffleIcon(v.findViewById(R.id.btnShuffle))
                    updateSongInfo(v)
                    updateVisuals()
                }
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
                    
                    // Update Reels Tape Level
                    if (::retroReels.isInitialized) {
                        retroReels.progress = current.toFloat() / total.toFloat()
                    }
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPlayPause = view.findViewById<ImageView>(R.id.btnPlayPause)
        val btnPrevious = view.findViewById<ImageView>(R.id.btnPrevious)
        val btnNext = view.findViewById<ImageView>(R.id.btnNext)
        val btnRepeat = view.findViewById<TextView>(R.id.btnRepeat)
        val btnShuffle = view.findViewById<TextView>(R.id.btnShuffle)
        val btnQueue = view.findViewById<TextView>(R.id.btnQueue)
        seekBar = view.findViewById(R.id.seekBarPlayer)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)

        retroReels = view.findViewById(R.id.retroReels)

        PlayerState.addStateListener(playerListener)

        btnPlayPause.setOnClickListener {
            PlayerState.togglePlayPause(requireContext())
        }

        btnPrevious.setOnClickListener {
            PlayerState.playPrevious(requireContext())
        }

        btnNext.setOnClickListener {
            PlayerState.playNext(requireContext())
        }

        btnRepeat.setOnClickListener {
            PlayerState.toggleRepeat()
        }

        btnShuffle.setOnClickListener {
            PlayerState.toggleShuffle()
        }

        btnQueue.setOnClickListener {
            val bottomSheet = QueueBottomSheet()
            bottomSheet.show(childFragmentManager, "QueueBottomSheet")
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
        updateSongInfo(view)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        handler.post(updateProgressAction)
        updateVisuals()
    }

    private fun updateSongInfo(view: View) {
        val song = PlayerState.currentSong
        val songId = song?.id ?: -1L
        val title = song?.title ?: "Nieznany tytuł"
        val artist = song?.artist ?: "Nieznany artysta"

        view.findViewById<TextView>(R.id.tvPlayerSongTitle).text = title
        view.findViewById<TextView>(R.id.tvPlayerArtist).text = artist

        val imgCover = view.findViewById<ImageView>(R.id.imgPlayerCover)
        val coverUrl = if (song?.coverPath?.startsWith("http") == true) {
            song.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/songs/$songId/cover"
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(requireContext(), coverUrl)

        val radius = resources.getDimensionPixelSize(R.dimen.studio_radius)

        com.bumptech.glide.Glide.with(this)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_cover_placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
            .into(imgCover)
    }

    private fun updateVisuals() {
        if (::retroReels.isInitialized) {
            retroReels.isSpinning = PlayerState.isPlaying
            
            // Set initial progress
            val total = PlayerState.getDuration()
            if (total > 0) {
                retroReels.progress = PlayerState.getCurrentPosition().toFloat() / total.toFloat()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
            btn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.studio_bg))
        } else {
            btn.setImageResource(R.drawable.ic_play)
            btn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.studio_bg))
        }
    }

    private fun updateRepeatIcon(btn: TextView) {
        val indicator = view?.findViewById<View>(R.id.repeatIndicator)
        if (PlayerState.isRepeatEnabled) {
            indicator?.visibility = View.VISIBLE
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.studio_text)) 
        } else {
            indicator?.visibility = View.INVISIBLE
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.studio_text_dim))
        }
    }

    private fun updateShuffleIcon(btn: TextView) {
        val indicator = view?.findViewById<View>(R.id.shuffleIndicator)
        if (PlayerState.isShuffleEnabled) {
            indicator?.visibility = View.VISIBLE
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.studio_text))
        } else {
            indicator?.visibility = View.INVISIBLE
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.studio_text_dim))
        }
    }
}
