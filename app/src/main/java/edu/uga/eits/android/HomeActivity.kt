package edu.uga.eits.android

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.GridView
import com.google.android.gms.analytics.HitBuilders
import edu.uga.eits.android.extensions.getArgsMap
import edu.uga.eits.android.model.Analytics
import edu.uga.eits.android.module.Navigator
import edu.uga.eits.android.ui.HomeGridAdaper
import edu.uga.eits.android.ui.HomeModule
import kotlinx.android.synthetic.main.activity_home_.*

class HomeActivity : BaseActivity() {
    private var gridView: GridView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_)
        val imageAdapter = HomeGridAdaper(this)


        grid?.layoutManager = GridLayoutManager(this, 4)

        grid?.adapter = imageAdapter
    }
    override fun onResume() {
        super.onResume()
        Navigator.selectedModule.onNext(Pair(HomeModule.HOME,this))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_top_bar,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Navigator.menuItemSelected(item,this )
        return super.onOptionsItemSelected(item)
    }
}
