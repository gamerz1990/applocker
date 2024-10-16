package com.maliks.applocker.xtreme.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.ui.background.BackgroundsFragment
import com.maliks.applocker.xtreme.ui.security.SecurityFragment
import com.maliks.applocker.xtreme.ui.settings.SettingsFragment

class MainPagerAdapter(context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val tabs = context.resources.getStringArray(R.array.tabs)

    override fun getItem(position: Int): Fragment {
        return when (position) {
            INDEX_SECURITY -> SecurityFragment.newInstance()
            INDEX_SETTINGS -> SettingsFragment.newInstance()
            else -> SecurityFragment.newInstance()
        }
    }

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence? = tabs[position]

    companion object {

        private const val INDEX_SECURITY = 0
        private const val INDEX_SETTINGS = 1
    }
}