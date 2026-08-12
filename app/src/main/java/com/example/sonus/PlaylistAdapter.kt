package com.example.sonus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.PlaylistDTO

class PlaylistAdapter(
    private var playlists: List<PlaylistDTO>,
    private val onItemClick: (PlaylistDTO) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvPlaylistName)
        val count: TextView = view.findViewById(R.id.tvPlaylistCount)
        val cover: ImageView = view.findViewById(R.id.imgPlaylistCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.name.text = playlist.name
        
        val count = playlist.songCount ?: playlist.songs?.size ?: 0
        holder.count.text = formatSongCount(count)
        
        // TODO: Load cover image using Glide or similar if available
        // holder.cover.setImageResource(...)

        holder.itemView.setOnClickListener { onItemClick(playlist) }
    }

    private fun formatSongCount(count: Int): String {
        return when {
            count == 1 -> "1 utwór"
            count % 10 in 2..4 && (count % 100 !in 12..14) -> "$count utwory"
            else -> "$count utworów"
        }
    }

    override fun getItemCount() = playlists.size

    fun getData(): List<PlaylistDTO> = playlists

    fun updateData(newPlaylists: List<PlaylistDTO>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    fun updateItem(index: Int, updatedPlaylist: PlaylistDTO) {
        if (index in playlists.indices) {
            val mutableList = playlists.toMutableList()
            mutableList[index] = updatedPlaylist
            playlists = mutableList
            notifyItemChanged(index)
        }
    }
}
