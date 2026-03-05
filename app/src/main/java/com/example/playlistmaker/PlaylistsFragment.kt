package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.db.AppDatabase
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var newPlaylistButton: Button

    private lateinit var adapter: PlaylistAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        newPlaylistButton = view.findViewById(R.id.new_playlist_button)

        database = AppDatabase.getDatabase(requireContext())
        adapter = PlaylistAdapter(emptyList())

        // Настраиваем сетку
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        newPlaylistButton.setOnClickListener {
            val intent = Intent(requireContext(), CreatePlaylistActivity::class.java)
            startActivity(intent)
        }

        // Слушаем изменения в базе данных
        lifecycleScope.launch {
            database.playlistDao().getPlaylists().collect { playlists ->
                if (playlists.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateLayout.visibility = View.GONE
                    adapter.updatePlaylists(playlists)
                }
            }
        }
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}