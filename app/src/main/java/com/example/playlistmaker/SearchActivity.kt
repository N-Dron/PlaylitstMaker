package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.network.RetrofitClient
import com.example.playlistmaker.network.TrackMapper
import com.example.playlistmaker.network.SearchResponse
import com.example.playlistmaker.ui.TrackAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var searchField: EditText
    private lateinit var clearButton: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var recycler: RecyclerView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var historyBlock: LinearLayout
    private lateinit var clearHistoryBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorGroup: LinearLayout
    private lateinit var emptyResultGroup: LinearLayout
    private lateinit var refreshButton: Button
    private lateinit var errorText: TextView
    private lateinit var emptyText: TextView

    private lateinit var prefs: SharedPreferences
    private lateinit var searchHistory: SearchHistory

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var tracksList: MutableList<Track> = mutableListOf()
    private var query: String = ""
    private var lastQuery: String = ""

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(searchField.text.toString()) }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupAdapters()
        setupListeners()
        updateHistory()
        updateHistoryVisibility(true)
    }

    private fun initViews() {
        prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(this)

        searchField = findViewById(R.id.search_field)
        clearButton = findViewById(R.id.clear_button)
        backButton = findViewById(R.id.back_button)
        recycler = findViewById(R.id.recycler_view)
        historyBlock = findViewById(R.id.history_block)
        clearHistoryBtn = findViewById(R.id.clear_history_btn)
        historyRecycler = findViewById(R.id.history_recycler)

        progressBar = findViewById(R.id.progress_bar)
        errorGroup = findViewById(R.id.error_group)
        emptyResultGroup = findViewById(R.id.empty_result_group)
        refreshButton = findViewById(R.id.refresh_button)
        errorText = findViewById(R.id.error_text)
        emptyText = findViewById(R.id.empty_text)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun setupAdapters() {
        adapter = TrackAdapter(tracksList) { track ->
            if (clickDebounce()) {
                searchHistory.saveTrack(track)
                updateHistory()
                openAudioPlayer(track)
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            if (clickDebounce()) {
                searchHistory.saveTrack(track)
                updateHistory()
                openAudioPlayer(track)
            }
        }

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }

        clearHistoryBtn.setOnClickListener {
            searchHistory.clear()
            updateHistory()
            updateHistoryVisibility(true)
        }

        searchField.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility(hasFocus)
        }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString() ?: ""

                clearButton.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE

                if (query.isEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    showHistory()
                } else {
                    hideHistory()
                    searchDebounce()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clearButton.setOnClickListener {
            searchField.setText("")
            hideKeyboard()
            handler.removeCallbacks(searchRunnable)
            showHistory()
        }

        searchField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                performSearch(searchField.text.toString())
                true
            } else {
                false
            }
        }

        refreshButton.setOnClickListener {
            if (lastQuery.isNotEmpty()) {
                performSearch(lastQuery)
            }
        }
    }

    private fun performSearch(searchTerm: String) {
        if (searchTerm.isEmpty()) return

        lastQuery = searchTerm
        hideKeyboard()

        showLoading()

        RetrofitClient.apiService.searchSongs(searchTerm).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    val tracks = searchResponse?.results?.map { TrackMapper.map(it) } ?: emptyList()

                    if (tracks.isEmpty()) {
                        showEmptyResults()
                    } else {
                        showResults(tracks)
                    }
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                showError()
            }
        })
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java).apply {
            putExtra("EXTRA_TRACK", track)
        }
        startActivity(intent)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recycler.visibility = View.GONE
        errorGroup.visibility = View.GONE
        emptyResultGroup.visibility = View.GONE
        historyBlock.visibility = View.GONE
    }

    private fun showResults(tracks: List<Track>) {
        progressBar.visibility = View.GONE
        recycler.visibility = View.VISIBLE
        errorGroup.visibility = View.GONE
        emptyResultGroup.visibility = View.GONE
        historyBlock.visibility = View.GONE

        tracksList.clear()
        tracksList.addAll(tracks)
        adapter.notifyDataSetChanged()
    }

    private fun showEmptyResults() {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        errorGroup.visibility = View.GONE
        emptyResultGroup.visibility = View.VISIBLE
        historyBlock.visibility = View.GONE

        tracksList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        errorGroup.visibility = View.VISIBLE
        emptyResultGroup.visibility = View.GONE
        historyBlock.visibility = View.GONE

        tracksList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun showHistory() {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        errorGroup.visibility = View.GONE
        emptyResultGroup.visibility = View.GONE

        val hasHistory = searchHistory.getHistory().isNotEmpty()
        historyBlock.visibility = if (hasHistory) View.VISIBLE else View.GONE
    }

    private fun hideHistory() {
        historyBlock.visibility = View.GONE
    }

    private fun updateHistory() {
        val list = searchHistory.getHistory()
        historyAdapter.updateTracks(list)
    }

    private fun updateHistoryVisibility(hasFocus: Boolean) {
        val emptyQuery = searchField.text.isEmpty()
        val hasHistory = searchHistory.getHistory().isNotEmpty()

        if (hasFocus && emptyQuery && hasHistory) {
            historyBlock.visibility = View.VISIBLE
        } else {
            historyBlock.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchField.windowToken, 0)
    }
}