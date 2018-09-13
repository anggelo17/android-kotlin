package edu.uga.eits.android.ui

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import edu.uga.eits.android.viewmodel.ListViewModels

/**
 * Created by Luis on 6/12/17.
 *
 */
class RxPagerAdapter(val tabs : List<ListViewModels> , fragmentManager: FragmentManager, private val context: Context) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int)
            = RxListFragment.newInstance(tabs.map{it.toArgsMap()}.getOrNull(position))
    override fun getCount() = tabs.size
    override fun getPageTitle(position: Int)
            = tabs.map{ it.rxTitle.value }.getOrElse(position,{""})
}
