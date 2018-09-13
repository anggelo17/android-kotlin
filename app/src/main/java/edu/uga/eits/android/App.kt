package edu.uga.eits.android

import android.app.Application
import edu.uga.eits.android.component.ApplicationComponent
import edu.uga.eits.android.component.DaggerApplicationComponent
import edu.uga.eits.android.extensions.KPreferences
import edu.uga.eits.android.model.Dataset
import edu.uga.eits.android.module.AndroidModule
import edu.uga.eits.android.module.NetworkModule
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.ReplaySubject
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker


/**
 * Created by Luis on 5/26/17.
 */
class App : Application() {
    var disposables = CompositeDisposable()

    lateinit var sAnalytics: GoogleAnalytics
    //private var sTracker: Tracker? = null


    companion object{
        lateinit var component : ApplicationComponent
       lateinit var dataset : ReplaySubject<Dataset>
        lateinit var preferences : KPreferences
        lateinit var sTracker: Tracker

    }
    override fun onCreate() {
        super.onCreate()
        component = initDaggerComponent()
        component.inject(this)
        dataset = component.getFileManager().dataset
        preferences = KPreferences(this, component.getGson())
        sAnalytics = GoogleAnalytics.getInstance(this);
        sTracker = sAnalytics.newTracker(R.xml.app_tracker)


    }
    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
    }
    fun initDaggerComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder().androidModule(AndroidModule(this))
                .networkModule(NetworkModule()).build()
    }


//    @Synchronized fun getDefaultTracker(): Tracker? {
//
//        if (sTracker == null) {
//            sTracker = sAnalytics?.newTracker(R.xml.app_tracker)
//        }
//
//        return sTracker
//    }


}
