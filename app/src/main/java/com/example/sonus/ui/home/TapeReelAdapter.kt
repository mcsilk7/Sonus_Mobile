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
        holder.tvTitle.text = song.title.uppercase()
        holder.tvArtist.text = "SRC: ${song.artist.uppercase()}"
        
        // Technical labels
        holder.tvReelId.text = "REEL_#${song.id.toString().padStart(3, '0')}"
        
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        holder.tvDate.text = "LOG_DATE: ${sdf.format(Date())}"

        val coverUrl = if (song.coverPath?.startsWith("http") == true) {
            song.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(holder.itemView.context, coverUrl)

        Glide.with(holder.itemView.context)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_cover_placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(CenterCrop())
            .into(holder.imgCover)

        holder.itemView.setOnClickListener { onItemClick(song) }
    }

    override fun getItemCount() = songs.size

    fun updateData(newSongs: List<SongDTO>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
