package edu.uga.eits.android

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChanges
import com.tbruyelle.rxpermissions2.RxPermissions
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.model.Analytics
import edu.uga.eits.android.model.Rx
import edu.uga.eits.android.model.uId
import edu.uga.eits.android.module.Navigator
import edu.uga.eits.android.ui.HomeModule
import edu.uga.eits.android.viewmodel.TabsViewModels
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.element_bottom_navigation.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse

/**
 * Created by Luis on 5/26/17.
 */


enum class LifecycleEvents{
    CREATE,
    START,
    RESUME,
    PAUSE,
    STOP,
    DESTROY
}

abstract class BaseActivity : AppCompatActivity() {
    private var rxPermissions : RxPermissions? = null

    var disposables = CompositeDisposable()
    var resumableDisposables = CompositeDisposable()
    val lifecycleSubject = BehaviorSubject.create<LifecycleEvents>()
    fun getRxPermission(): RxPermissions? {   rxPermissions = rxPermissions ?: RxPermissions(this);return rxPermissions  }
    fun checkUserLocationPermission(){
        getRxPermission()?.requestEach(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION)
            ?.doOnNext{ Log.d("--BaseActivity- ", "~~~granted~~~ => ${it} ")}
                ?.map{it.granted}?.subscribe(App.component.getLocationManager().permissionSubject::onNext)?.addTo(disposables)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar?.apply{
            val hideBack = intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) ?: false
            setDisplayHomeAsUpEnabled(!hideBack)
            setHomeButtonEnabled(!hideBack)
            setDisplayHomeAsUpEnabled(!hideBack)
        }
        //Announcement


        // Perform injection so that when this call returns all dependencies will be available for use.
        App.component.inject(this)


    }


    override fun onDestroy() {
        disposables.dispose() //disposables.clear()
        super.onDestroy()
        lifecycleSubject.onNext(LifecycleEvents.DESTROY)
    }
    override fun onPause() {
        super.onPause()
        overridePendingTransition(0,0)
        resumableDisposables.dispose()
        lifecycleSubject.onNext(LifecycleEvents.PAUSE)
    }
    override fun onResume() {
        super.onResume()
        resumableDisposables = CompositeDisposable()
        lifecycleSubject.onNext(LifecycleEvents.RESUME)

        val ana = Analytics()

        if (intent.getArgsMap().toString()!="{}") {

            ana.sendScreen(intent.getArgsMap().toString())
        }
        else{
            ana.sendScreen(this.className())
        }
    }

    override fun onStart() {
        super.onStart()
        //set up bottom navigation
        bottom_navigation?.apply {
            disableShiftModeAndSetIconSize()
            setOnNavigationItemSelectedListener{
                Log.d("--BaseActivity86--","~~~it~~~ => ${it}" )
                Navigator.navigationSelected(it.itemId,this@BaseActivity); true
            }
        }
        Navigator.selectedModule.distinctUntilChanged{(item,context) -> item}.subscribe{(module,context) ->
            HomeModule.naviagtionMenu[module]?.let{bottom_navigation?.selectedItemId = it}
        }.addTo(disposables)
        lifecycleSubject.onNext(LifecycleEvents.START)
    }
    override fun onStop() {
        super.onStop()
        lifecycleSubject.onNext(LifecycleEvents.STOP)
    }
    fun<T> resumable(flowable: Flowable<T>,subscribeWork: (T) -> Unit){
        lifecycleSubject.filter{it==LifecycleEvents.RESUME}.subscribe{
            resumableDisposables = CompositeDisposable()
            flowable.subscribe{
                subscribeWork(it)
            }.addTo(resumableDisposables)
        }.addTo(disposables)
    }
    protected fun setFragment(f: Fragment, id: Int) {
        val ft = supportFragmentManager.beginTransaction();
        ft.replace(id, f);
        ft.commit();
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        intent.getArg(Keys.MENU_LAYOUT).takeIf{it.isNotBlank()}?.let{it.toInt()}?.
                let{ try{menuInflater.inflate(it,menu)}catch (t:Throwable){
                    Log.d("----","~~~Menu Layout error~~~ => ${t}" )}
                    when(it){
                        R.menu.app_search_top_bar -> {menu?.findItem(R.id.action_search)?.apply {
                            expandActionView()
                            TabsViewModels.UniversalSearch.rxSearchText?.let{
                                (actionView as? SearchView)?.queryTextChanges()
                                ?.map{it.toString()}?.subscribe(it::onNext)?.addTo(disposables)
                            }
                        }}
                        else -> {}
                    }
                }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Navigator.menuItemSelected(item,this,intent.getArgsMap())
        Log.d("--BaseActivity113--","~~~item~~~ => ${item}" )
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        overridePendingTransition(0,0)
        return super.onSupportNavigateUp()
    }
}
