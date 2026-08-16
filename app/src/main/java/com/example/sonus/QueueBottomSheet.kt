package com.example.sonus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QueueBottomSheet : BottomSheetDialogFragment() {

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
        val adapter = SongAdapter(
            songs = PlayerState.currentPlaylist,
            onItemClick = { song ->
                PlayerState.play(requireContext(), song, PlayerState.currentPlaylist)
                dismiss()
            }
        )

        rvQueue.layoutManager = LinearLayoutManager(requireContext())
        rvQueue.adapter = adapter
    }
}
