package com.example.sonus.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.sonus.R
import com.example.sonus.SettingsManager
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TapeReelAdapter(
    private var songs: List<SongDTO>,
    private val onItemClick: (SongDTO) -> Unit
) : RecyclerView.Adapter<TapeReelAdapter.TapeViewHolder>() {

    class TapeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCover: ImageView = view.findViewById(R.id.imgTapeCover)
        val tvTitle: TextView = view.findViewById(R.id.tvTapeTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvTapeArtist)
        val tvReelId: TextView = view.findViewById(R.id.tvReelId)
        val tvDate: TextView = view.findViewById(R.id.tvTapeDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TapeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tape_reel, parent, false)
        return TapeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TapeViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context
        val settingsManager = SettingsManager(context)
        val isTechnical = settingsManager.getThemeId() == 0

        holder.tvTitle.text = if (isTechnical) song.title.uppercase() else song.title
        holder.tvArtist.text = if (isTechnical) "SRC: ${song.artist.uppercase()}" else song.artist
        
        // Technical labels
        holder.tvReelId.text = if (isTechnical) {
            context.getString(R.string.reel_id_prefix, song.id.toString().padStart(3, '0'))
        } else {
            context.getString(R.string.reel_id_label_norm, song.id.toString())
        }
        
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val dateStr = sdf.format(Date())
        holder.tvDate.text = if (isTechnical) {
            context.getString(R.string.log_date_prefix, dateStr)
        } else {
            context.getString(R.string.log_date_label_norm, dateStr)
        }

        val coverUrl = if (song.coverPath?.startsWith("http") == true) {
            song.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(holder.itemView.context, coverUrl)
        val placeholder = com.example.sonus.network.GlideHelper.getBlurHashPlaceholder(holder.itemView.context, song.blurHash) ?: androidx.core.content.ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_cover_placeholder)

        Glide.with(holder.itemView.context)
            .load(authenticatedUrl)
            .placeholder(placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(CenterCrop())
            .into(holder.imgCover)

        val isDownloaded = com.example.sonus.DownloadManager.isSongDownloaded(context, song.id)
        val isOnline = com.example.sonus.NetworkHelper.isNetworkAvailable(context)
        val isUnavailable = !isOnline && !isDownloaded
        
        if (isUnavailable) {
            holder.itemView.alpha = 0.4f
        } else {
            holder.itemView.alpha = 1.0f
        }

        holder.itemView.setOnClickListener { 
            if (!isUnavailable) onItemClick(song) 
        }
    }

    override fun getItemCount() = songs.size

    fun updateData(newSongs: List<SongDTO>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
