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
import com.example.sonus.network.SongDTO

class PlaylistAdapter(
    private var playlists: List<PlaylistDTO>,
    private val onItemClick: (PlaylistDTO) -> Unit,
    private val onLongClick: ((PlaylistDTO) -> Unit)? = null
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvPlaylistName)
        val count: TextView = view.findViewById(R.id.tvPlaylistCount)
        val loadLabel: TextView = view.findViewById(R.id.tvPlaylistLoad)
        val covers = listOf<ImageView>(
            view.findViewById(R.id.imgPlaylistCover1),
            view.findViewById(R.id.imgPlaylistCover2),
            view.findViewById(R.id.imgPlaylistCover3),
            view.findViewById(R.id.imgPlaylistCover4)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        val context = holder.itemView.context
        val settingsManager = SettingsManager(context)
        val isTechnical = settingsManager.getThemeId() == 0

        holder.name.text = if (isTechnical) {
            context.getString(R.string.unit_id_prefix, playlist.id?.toString(16)?.uppercase() ?: "00")
        } else {
            playlist.name
        }
        
        val count = playlist.songCount ?: playlist.songs?.size ?: 0
        holder.count.text = if (isTechnical) {
            context.getString(R.string.log_prefix, "${playlist.name.uppercase()} (${formatSongCount(count, context)})")
        } else {
            formatSongCount(count, context)
        }

        holder.loadLabel.text = LabelProvider.getLabel(context, "library_load")
        
        val songs = playlist.songs ?: emptyList()
        
        // Load up to 4 covers
        for (i in 0 until 4) {
            val imageView = holder.covers[i]
            if (i < songs.size) {
                val song = songs[i]
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
                    .into(imageView)
            } else if (songs.isNotEmpty()) {
                // If we have some songs but less than 4, reuse the first one or show placeholder
                // Spotify style: if 1 song, show 1 large. If < 4, often just shows the first.
                // For simplicity in a 2x2 grid, let's load the first song in all slots if only 1-3 songs exist
                // Or just leave placeholders for the empty slots.
                // Let's reuse the first song to make it look "full" if at least one exists.
                val song = songs[0]
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
                    .into(imageView)
            } else {
                // No songs at all
                imageView.setImageResource(R.drawable.bg_cover_placeholder)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(playlist) }

        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onLongClick.invoke(playlist)
                true
            }
        }
    }

    private fun formatSongCount(count: Int, context: android.content.Context): String {
        val settingsManager = SettingsManager(context)
        val isTechnical = settingsManager.getThemeId() == 0

        return if (isTechnical) {
            context.getString(R.string.data_slots_prefix, count)
        } else {
            context.getString(R.string.tracks_count_norm, count)
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
