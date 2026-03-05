package com.example.playlistmaker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediaLibraryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // У нас всего 2 вкладки
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            FavoriteTracksFragment.newInstance() // Вкладка 0 - Избранное
        } else {
            PlaylistsFragment.newInstance()      // Вкладка 1 - Плейлисты
        }
    }
}