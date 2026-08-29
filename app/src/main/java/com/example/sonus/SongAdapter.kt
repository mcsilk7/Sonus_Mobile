package com.example.sonus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
    private val onLongClick: ((SongDTO) -> Unit)? = null,
    private val onDownloadClick: ((SongDTO) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var activeSongId: Long? = null
    private var downloadProgressMap = mapOf<Long, Int>()

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val title: TextView = view.findViewById(R.id.tvSongTitle)
        val artist: TextView = view.findViewById(R.id.tvSongArtist)
        val duration: TextView = view.findViewById(R.id.tvSongDuration)
        val cover: ImageView = view.findViewById(R.id.imgCover)
        val btnAdd: TextView = view.findViewById(R.id.btnAddToPlaylist)
        val btnFavorite: ImageView = view.findViewById(R.id.btnFavorite)
        val btnDownload: ImageView = view.findViewById(R.id.btnDownload)
        val progressBar: ProgressBar = view.findViewById(R.id.downloadProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context
        
        holder.title.text = song.title
        holder.artist.text = song.artist
        
        // Highlight active song
        if (song.id == activeSongId) {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.studio_amber))
            holder.root.setBackgroundResource(R.drawable.bg_settings_item)
        } else {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.studio_text))
            holder.root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        // Convert duration seconds to MM:SS
        val mins = (song.duration ?: 0) / 60
        val secs = (song.duration ?: 0) % 60
        holder.duration.text = String.format(java.util.Locale.getDefault(), "%d:%02d", mins, secs)

        // Favorite state color and icon
        if (song.isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite)
            holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.studio_red))
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            holder.btnFavorite.setColorFilter(ContextCompat.getColor(context, R.color.studio_text_dim))
        }

        // Download State
        val progress = downloadProgressMap[song.id]
        val isDownloaded = DownloadManager.isSongDownloaded(context, song.id)
        val isOnline = NetworkHelper.isNetworkAvailable(context)
        
        val isUnavailable = !isOnline && !isDownloaded
        
        if (isUnavailable) {
            holder.root.alpha = 0.4f
            holder.itemView.isClickable = false
            holder.itemView.isFocusable = false
        } else {
            holder.root.alpha = 1.0f
            holder.itemView.isClickable = true
            holder.itemView.isFocusable = true
        }

        if (progress != null) {
            if (holder.progressBar.visibility == View.GONE) {
                holder.btnDownload.visibility = View.GONE
                holder.progressBar.visibility = View.VISIBLE
                holder.progressBar.alpha = 0f
                holder.progressBar.animate().alpha(1f).setDuration(200).start()
            }
            
            // Smooth progress animation
            val animator = android.animation.ObjectAnimator.ofInt(holder.progressBar, "progress", holder.progressBar.progress, progress)
            animator.duration = 400
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        } else {
            if (holder.progressBar.visibility == View.VISIBLE) {
                holder.progressBar.animate().alpha(0f).setDuration(200).withEndAction {
                    holder.progressBar.visibility = View.GONE
                    holder.btnDownload.visibility = View.VISIBLE
                    holder.btnDownload.alpha = 0f
                    holder.btnDownload.animate().alpha(1f).setDuration(200).start()
                }.start()
            } else {
                holder.progressBar.visibility = View.GONE
                holder.btnDownload.visibility = View.VISIBLE
            }
            
            if (isDownloaded) {
                holder.btnDownload.setImageResource(R.drawable.ic_play)
                holder.btnDownload.setColorFilter(ContextCompat.getColor(context, R.color.studio_green))
            } else {
                holder.btnDownload.setImageResource(R.drawable.ic_arrow_down)
                holder.btnDownload.setColorFilter(ContextCompat.getColor(context, R.color.studio_text_dim))
            }
        }

        holder.btnDownload.setOnClickListener { 
            if (isOnline) onDownloadClick?.invoke(song) 
        }

        // Load cover image
        val coverUrl = if (song.coverPath?.startsWith("http") == true) {
            song.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/songs/${song.id}/cover"
        }
        val authenticatedUrl = GlideHelper.getAuthenticatedUrl(context, coverUrl)
        val placeholder = GlideHelper.getBlurHashPlaceholder(context, song.blurHash) ?: ContextCompat.getDrawable(context, R.drawable.bg_cover_placeholder)

        val radius = (12 * context.resources.displayMetrics.density).toInt()

        Glide.with(context)
            .load(authenticatedUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(placeholder)
            .error(R.drawable.bg_cover_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(holder.cover)

        holder.itemView.setOnClickListener { 
            if (!isUnavailable) onItemClick(song) 
        }
        
        holder.btnAdd.visibility = View.VISIBLE
        holder.btnAdd.text = if (song.isInPlaylist) "✓" else "+"
        
        holder.btnAdd.setOnClickListener {
            if (!isUnavailable) onAddClick?.invoke(song)
        }

        if (onFavoriteClick != null) {
            holder.btnFavorite.setOnClickListener { 
                if (!isUnavailable) onFavoriteClick(song) 
            }
        } else {
            holder.btnFavorite.setOnClickListener(null)
        }

        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                if (!isUnavailable) {
                    onLongClick(song)
                    true
                } else false
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

    fun setDownloadProgress(progressMap: Map<Long, Int>) {
        this.downloadProgressMap = progressMap
        notifyDataSetChanged()
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val mutableSongs = songs.toMutableList()
        val song = mutableSongs.removeAt(fromPosition)
        mutableSongs.add(toPosition, song)
        songs = mutableSongs
        notifyItemMoved(fromPosition, toPosition)
    }

    fun setActiveSongId(id: Long?) {
        activeSongId = id
        notifyDataSetChanged()
    }
}
