package com.example.sonus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient

class PlaylistAdapter(
    private var playlists: List<PlaylistDTO>,
    private val onItemClick: (PlaylistDTO) -> Unit,
    private val onLongClick: ((PlaylistDTO) -> Unit)? = null
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
        
        // Playlist cover: use first song's coverPath if it's a full URL, otherwise use dedicated endpoint
        val firstSong = playlist.songs?.firstOrNull()
        val coverUrl = if (firstSong?.coverPath?.startsWith("http") == true) {
            firstSong.coverPath
        } else {
            firstSong?.let { RetrofitClient.BASE_URL + "api/songs/${it.id}/cover" }
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(holder.itemView.context, coverUrl)

        val radius = (12 * holder.itemView.context.resources.displayMetrics.density).toInt()

        Glide.with(holder.itemView.context)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_cover_placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(holder.cover)

        holder.itemView.setOnClickListener { onItemClick(playlist) }

        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onLongClick.invoke(playlist)
                true
            }
        }
    }

    private fun formatSongCount(count: Int): String {
        return "DATA_SLOTS: $count"
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
