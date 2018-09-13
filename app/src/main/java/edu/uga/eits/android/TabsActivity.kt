package edu.uga.eits.android

import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.provider.SyncStateContract
import android.support.design.widget.TabLayout
import android.util.Log
import android.widget.Toast
import com.google.android.gms.analytics.HitBuilders
import edu.uga.eits.android.extensions.Keys
import edu.uga.eits.android.extensions.getArg
import edu.uga.eits.android.extensions.getArgsMap
import edu.uga.eits.android.model.Constants
import edu.uga.eits.android.ui.RxPagerAdapter
import edu.uga.eits.android.viewmodel.ListViewModels
import edu.uga.eits.android.viewmodel.TabsViewModels
import kotlinx.android.synthetic.main.activity_tabs.*
import org.jetbrains.anko.support.v4.onPageChangeListener

/**
 * Created by Luis on 11/1/17.
 */
class TabsActivity : BaseActivity(){

    lateinit var sharedPreferences:SharedPreferences

    lateinit var title:String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        //Ask for location permission
        checkUserLocationPermission()
        val viewmodel = TabsViewModels.of(intent.getArg(Keys.TABS_ID))
        val tabs = viewmodel.tabIds.map{ it as? ListViewModels}.filterNotNull()

        Log.d("tabid","--"+viewmodel.rxTitle.value)

        title = viewmodel.rxTitle.value

        Log.d("--TabsActivity22--","~~~tabs~~~ => ${tabs}" )
        viewpager.adapter = RxPagerAdapter(tabs,supportFragmentManager, this)

        var currentTab = 0
        if (title.equals("UGA Bus")) currentTab = sharedPreferences.getInt(Constants.CURRENTTABBUS,0)
        if (title.equals("Parking")) currentTab = sharedPreferences.getInt(Constants.CURRENTTABPARKING,0)

        viewpager.setCurrentItem(currentTab,true)
        sliding_tabs.setupWithViewPager(viewpager)
        sliding_tabs.tabMode = TabLayout.MODE_FIXED
        viewmodel.bindTitle(this)
    }

    override fun onPause() {
        super.onPause()
        Log.d("tabid","current "+viewpager.currentItem)
        if (title.equals("UGA Bus"))
            sharedPreferences.edit().putInt(Constants.CURRENTTABBUS,viewpager.currentItem).apply()
        if (title.equals("Parking"))
            sharedPreferences.edit().putInt(Constants.CURRENTTABPARKING,viewpager.currentItem).apply()


    }



}