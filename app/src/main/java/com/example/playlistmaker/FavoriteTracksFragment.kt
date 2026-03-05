package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.db.TrackDbConvertor
import com.example.playlistmaker.ui.TrackAdapter
import kotlinx.coroutines.launch

class FavoriteTracksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: TrackAdapter

    private lateinit var database: AppDatabase
    private val convertor = TrackDbConvertor()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorite_tracks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        database = AppDatabase.getDatabase(requireContext())

        // Настраиваем адаптер
        adapter = TrackAdapter(mutableListOf()) { track ->
            val intent = Intent(requireContext(), AudioPlayerActivity::class.java).apply {
                putExtra("EXTRA_TRACK", track)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Подписываемся на обновления из базы данных
        lifecycleScope.launch {
            database.trackDao().getFavoriteTracks().collect { trackEntities ->
                val tracks = trackEntities.map { convertor.map(it) }

                if (tracks.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateLayout.visibility = View.GONE
                    adapter.updateTracks(tracks) // Метод обновления списка в адаптере
                }
            }
        }
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}