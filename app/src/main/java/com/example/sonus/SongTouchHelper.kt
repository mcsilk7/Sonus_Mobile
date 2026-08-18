package com.example.sonus

import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object SongTouchHelper {

    fun attach(
        recyclerView: RecyclerView,
        adapter: SongAdapter,
        userId: Long,
        scope: CoroutineScope,
        onFavoriteToggled: (SongDTO) -> Unit
    ) {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val song = adapter.getSongs()[position]

                if (direction == ItemTouchHelper.RIGHT) {
                    // Add to Queue
                    PlayerState.addSongToQueue(song)
                    val msg = recyclerView.context.getString(R.string.toast_added_to_queue, song.title)
                    Toast.makeText(recyclerView.context, msg, Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Toggle Favorite
                    scope.launch {
                        try {
                            val response = com.example.sonus.network.RetrofitClient.favoriteApi.toggleFavorite(userId, song.id)
                            if (response.isSuccessful) {
                                val added = response.body()?.get("added") ?: false
                                song.isFavorite = added
                                onFavoriteToggled(song)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(recyclerView.context, recyclerView.context.getString(R.string.toast_favorite_error), Toast.LENGTH_SHORT).show()
                        }
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }
}
