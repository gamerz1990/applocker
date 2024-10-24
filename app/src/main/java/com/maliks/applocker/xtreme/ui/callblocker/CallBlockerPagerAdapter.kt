package com.maliks.applocker.xtreme.ui.callblocker

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.BlackListFragment
import com.maliks.applocker.xtreme.ui.callblocker.log.CallLogFragment
import com.maliks.applocker.xtreme.ui.security.SecurityFragment

class CallBlockerPagerAdapter(context: Context, manager: FragmentManager) : FragmentPagerAdapter(manager) {

    private val tabs = context.resources.getStringArray(R.array.call_blocker_tabs)

    override fun getItem(position: Int): Fragment {
        return when (position) {
            INDEX_BLACK_LIST -> BlackListFragment.newInstance()
            INDEX_LOGS -> CallLogFragment.newInstance()
            else -> SecurityFragment.newInstance()
        }
    }

    override fun getCount(): Int = tabs.size

    override fun getPageTitle(position: Int): CharSequence? = tabs[position]

    companion object {

        private const val INDEX_BLACK_LIST = 0
        private const val INDEX_LOGS = 1
    }
}