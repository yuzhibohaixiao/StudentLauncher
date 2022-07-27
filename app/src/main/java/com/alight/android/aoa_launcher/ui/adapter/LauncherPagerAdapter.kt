package com.alight.android.aoa_launcher.ui.adapter

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alight.android.aoa_launcher.common.base.BaseFragment
import com.alight.android.aoa_launcher.common.fragment.AppListFragment
import com.alight.android.aoa_launcher.common.fragment.AppSelectFragment
import com.alight.android.aoa_launcher.common.fragment.MainFragment

class LauncherPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments: SparseArray<BaseFragment> = SparseArray()

    init {
        fragments.put(PAGE_MAIN, MainFragment())
        fragments.put(PAGE_APP_SELECT, AppSelectFragment())
        fragments.put(PAGE_APP_LIST, AppListFragment())
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size()
    }

    companion object {

        const val PAGE_MAIN = 0
        const val PAGE_APP_SELECT = 1
        const val PAGE_APP_LIST = 2

    }
}