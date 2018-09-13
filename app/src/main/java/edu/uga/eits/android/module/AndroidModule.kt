package edu.uga.eits.android.module
import android.app.Application
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.gson.Gson
import com.patloew.rxlocation.RxLocation
import dagger.Module
import dagger.Provides
import edu.uga.eits.android.extensions.ForApplication
import edu.uga.eits.android.extensions.throttle
import edu.uga.eits.android.model.Locateable
import edu.uga.eits.android.model.LocateableInstance
import edu.uga.eits.android.model.Meters
import edu.uga.eits.android.model.asLocateable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import javax.inject.Singleton

/**
 * A module for Android-specific dependencies which require a [Context] or
 * [android.app.Application] to create.
 */
@Module class AndroidModule(private val application: Application) {

    /**
     * Allow the application context to be injected but require that it be annotated with
     * [@Annotation][ForApplication] to explicitly differentiate it from an activity context.
     */
    @Provides @Singleton @ForApplication
    fun provideApplicationContext(): Context {
        return application
    }
    @Provides @Singleton
    fun provideLocationManager(): LocationManager {
        return application.getSystemService(LOCATION_SERVICE) as LocationManager
    }
    @Provides @Singleton
    fun provideRxLocationManager() : RxLocationManager = RxLocationManager(application)
    @Provides @Singleton fun provideFileManager(gson: Gson) = FileManager(application,gson)
}

class RxLocationManager(val context: Context){
    val permissionSubject = ReplaySubject.create<Boolean>()
    val rxLocation = RxLocation(context)
    val locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(60000L)
    val disposables = CompositeDisposable()
    val rxUserLocation = BehaviorSubject.createDefault<Locateable>(LocateableInstance.invalidLocation)
    var lastLocation : Locateable? = null

    init {
        Log.d("--AndroidModule45- ", "~~~RxLocationManager~~~ => init ")
        //TODO: provide not available Loc if no permission or error occurs
        rxUserLocation.onNext(LocateableInstance.invalidLocation)
        permissionSubject.filter{it}.flatMap {
            getLocation().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext{t:Throwable-> Log.d("--AndroidModule58- ", "~~~error on Loc~~~ => ${t} ");Observable.never() }
        }.throttle(300).map{it.asLocateable()}
                .subscribe(rxUserLocation::onNext).addTo(disposables)
        permissionSubject.filter{!it}.map{LocateableInstance.invalidLocation}.subscribe{rxUserLocation::onNext}.addTo(disposables)
        rxUserLocation.subscribe{
            Log.d("--AndroidModule68--","~~~last location~~~ => ${it}" )
            lastLocation = if(it == LocateableInstance.invalidLocation) null else it
        }.addTo(disposables)
    }
    fun getDistanceFromUser(item:Locateable) : Meters {
        val userLoc = lastLocation ?: return Meters.MAX_VALUE
        return userLoc.distance(item)
    }
    @SuppressWarnings("MissingPermission")
    fun getLocation()=rxLocation.location().updates(locationRequest)
}