package com.example.sonus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QueueBottomSheet : BottomSheetDialogFragment() {

    private lateinit var adapter: SongAdapter
    private var isDragging = false

    private val playerListener = object : PlayerState.PlayerStateListener {
        override fun onStateChanged() {
            // Don't refresh the whole list if we are currently dragging
            if (isDragging) return
            
            activity?.runOnUiThread {
                adapter.setActiveSongId(PlayerState.currentSong?.id)
                adapter.updateData(PlayerState.currentPlaylist)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_queue_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvQueue = view.findViewById<RecyclerView>(R.id.rvQueue)
        adapter = SongAdapter(
            songs = PlayerState.currentPlaylist,
            onItemClick = { song ->
                PlayerState.play(requireContext(), song, PlayerState.currentPlaylist)
            }
        )
        adapter.setActiveSongId(PlayerState.currentSong?.id)

        rvQueue.layoutManager = LinearLayoutManager(requireContext())
        rvQueue.adapter = adapter

        view.findViewById<TextView>(R.id.tvQueueTitle).text = LabelProvider.getLabel(requireContext(), "queue_title")
        view.findViewById<TextView>(R.id.tvQueueDesc).text = LabelProvider.getLabel(requireContext(), "queue_desc")

        PlayerState.addStateListener(playerListener)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                
                // 1. Move in local adapter list (smooth animation)
                adapter.moveItem(fromPos, toPos)
                // 2. Move in real PlayerState
                PlayerState.moveSong(fromPos, toPos)
                
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                // Detect when drag starts/ends to disable global UI refreshes
                isDragging = actionState == ItemTouchHelper.ACTION_STATE_DRAG
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(rvQueue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerState.removeStateListener(playerListener)
    }
}
