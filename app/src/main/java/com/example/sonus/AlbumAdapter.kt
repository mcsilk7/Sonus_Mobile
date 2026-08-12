package com.example.sonus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.AlbumDTO

class AlbumAdapter(
    private var albums: List<AlbumDTO>,
    private val onItemClick: (AlbumDTO) -> Unit,
    private val onAddClick: ((AlbumDTO) -> Unit)? = null
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvAlbumName)
        val artist: TextView = view.findViewById(R.id.tvAlbumArtist)
        val cover: ImageView = view.findViewById(R.id.imgAlbumCover)
        val btnAdd: TextView = view.findViewById(R.id.btnAddAlbumToLibrary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.title.text = album.title
        holder.artist.text = album.artist

        // TODO: Load cover image using Glide
        // holder.cover.setImageResource(...)

        holder.itemView.setOnClickListener { onItemClick(album) }
        
        // Always show the add to library button for consistency with songs
        holder.btnAdd.visibility = View.VISIBLE
        holder.btnAdd.text = if (album.isSaved) "✓" else "+"
        
        holder.btnAdd.setOnClickListener {
            onAddClick?.invoke(album)
        }
    }

    override fun getItemCount() = albums.size

    fun updateData(newAlbums: List<AlbumDTO>) {
        albums = newAlbums
        notifyDataSetChanged()
    }
}
