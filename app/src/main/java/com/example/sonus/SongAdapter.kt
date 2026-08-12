package com.example.sonus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.sonus.network.GlideHelper
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO

class SongAdapter(
    private var songs: List<SongDTO>,
    private val onItemClick: (SongDTO) -> Unit,
    private val onAddClick: ((SongDTO) -> Unit)? = null,
    private val onFavoriteClick: ((SongDTO) -> Unit)? = null,
    private val onLongClick: ((SongDTO) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvSongTitle)
        val artist: TextView = view.findViewById(R.id.tvSongArtist)
        val duration: TextView = view.findViewById(R.id.tvSongDuration)
        val cover: ImageView = view.findViewById(R.id.imgCover)
        val btnAdd: TextView = view.findViewById(R.id.btnAddToPlaylist)
        val btnFavorite: ImageView = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        
        // Convert duration seconds to MM:SS
        val mins = (song.duration ?: 0) / 60
        val secs = (song.duration ?: 0) % 60
        holder.duration.text = String.format("%d:%02d", mins, secs)

        // Favorite state color
        val context = holder.itemView.context
        if (song.isFavorite) {
            holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        } else {
            holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, android.R.color.white))
        }

        // Load cover image using the new dedicated endpoint with authentication
        val coverUrl = RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
        val authenticatedUrl = GlideHelper.getAuthenticatedUrl(holder.itemView.context, coverUrl)

        val radius = (12 * context.resources.displayMetrics.density).toInt()

        Glide.with(holder.itemView.context)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_cover_placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(holder.cover)

        holder.itemView.setOnClickListener { onItemClick(song) }
        
        // Always show the add to playlist button
        holder.btnAdd.visibility = View.VISIBLE
        holder.btnAdd.text = if (song.isInPlaylist) "✓" else "+"
        
        holder.btnAdd.setOnClickListener {
            onAddClick?.invoke(song)
        }

        if (onFavoriteClick != null) {
            holder.btnFavorite.setOnClickListener { onFavoriteClick(song) }
        } else {
            holder.btnFavorite.setOnClickListener(null)
        }

        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onLongClick(song)
                true
            }
        } else {
            holder.itemView.setOnLongClickListener(null)
        }
    }

    override fun getItemCount() = songs.size

    fun getSongs(): List<SongDTO> = songs

    fun updateData(newSongs: List<SongDTO>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
