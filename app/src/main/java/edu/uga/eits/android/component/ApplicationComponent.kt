package edu.uga.eits.android.component

import android.app.Application
import com.google.gson.Gson
import dagger.Component
import edu.uga.eits.android.BaseActivity
import edu.uga.eits.android.api.Restapi
import edu.uga.eits.android.module.AndroidModule
import edu.uga.eits.android.module.FileManager
import edu.uga.eits.android.module.NetworkModule
import edu.uga.eits.android.module.RxLocationManager
import edu.uga.eits.android.ui.RxListFragment
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(AndroidModule::class,NetworkModule::class))
interface ApplicationComponent {
    fun inject(listFragment: RxListFragment)
    fun inject(application: Application)
    fun inject(baseActivity: BaseActivity)
    fun getRestapi() : Restapi
    fun getFileManager() : FileManager
    fun getGson() : Gson
    fun getLocationManager() : RxLocationManager
}