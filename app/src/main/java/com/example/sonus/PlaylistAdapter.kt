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
        
        val songCount = playlist.songCount ?: playlist.songs?.size ?: 0
        holder.count.text = "$songCount utworów"
        
        // TODO: Load cover image using Glide or similar if available
        // holder.cover.setImageResource(...)

        holder.itemView.setOnClickListener { onItemClick(playlist) }
    }

    override fun getItemCount() = playlists.size

    fun updateData(newPlaylists: List<PlaylistDTO>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}
