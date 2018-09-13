package edu.uga.eits.android.extensions



import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.os.Looper
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jakewharton.rx.replayingShare
import edu.uga.eits.android.App
import edu.uga.eits.android.BaseActivity
import edu.uga.eits.android.BaseFragment
import edu.uga.eits.android.model.CategoryType
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import khronos.*
import okhttp3.Headers
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

/**
 * Created by Luis on 6/3/17.
 *
 */
@Qualifier
annotation class ForApplication

/* Date*/
object helper{
    val headerFormatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
}
fun String?.parseHeaderDate() = this?.let{helper.headerFormatter.parse(this)} ?: Date()

fun Headers.lastModified() = this.get("Last-Modified").parseHeaderDate()
/* String */
fun String.Or(str:String) = if(this.isBlank())str else this

/* Rx */
fun <T,S> Observable<Response<S>>.of(transfrom:(data:S)->List<T>) : Observable<Pair<List<T>,Date>> {
    return this.map{Pair(it.body()?.let{transfrom(it)}?: emptyList(),it.headers().lastModified())}
}

fun <T> Observable<T>.shareReplay() = replayingShare()
fun <T> Flowable<T>.shareReplay() = replayingShare()

fun <T> Observable<T>.onComputation() = observeOn(Schedulers.computation())
fun <T> Flowable<T>.onComputation() = observeOn(Schedulers.computation())

fun <T> Observable<T>.onIO() = subscribeOn(Schedulers.io())
fun <T> Flowable<T>.onIO() = subscribeOn(Schedulers.io())

fun <T> Observable<T>.throttle(ms:Int) = throttleWithTimeout(ms.toLong(), TimeUnit.MILLISECONDS,Schedulers.computation())
fun <T> Flowable<T>.throttle(ms:Int) = throttleWithTimeout(ms.toLong(), TimeUnit.MILLISECONDS,Schedulers.computation())
fun <T> ReplaySubject<T>.throttle(ms:Int) = throttleWithTimeout(ms.toLong(), TimeUnit.MILLISECONDS,Schedulers.computation())

fun <T:Any,U:Any> Flowable<T>.reactTo(observable: Observable<U>) = combineLatest(observable.toFlowable(BackpressureStrategy.LATEST)).map{it.first}
fun <T:Any,U:Any> Observable<T>.reactTo(observable: Observable<U>) = combineLatest(observable).map{it.first}
fun <T:Any> Flowable<T>.reactToTimeInterval(seconds:Long) = combineLatest(Observable.interval(seconds,TimeUnit.SECONDS).startWith(-1)).map{it.first}
fun <T:Any> Flowable<T>.reactToUserLocation() = reactTo(App.component.getLocationManager().rxUserLocation)
fun <T:Any> Observable<T>.reactToUserLocation() = reactTo(App.component.getLocationManager().rxUserLocation)

fun <T,K> Flowable<List<T>>.withKey(keySelector:(T)-> K) = map{it.associateBy(keySelector,{it})}
fun <T,K> Collection<T>.withKey(keySelector:(T)-> K) = this.associateBy(keySelector,{it})

fun <T> PublishSubject<T>.toFlowable() = this.toFlowable(BackpressureStrategy.LATEST)
fun <T : Any, R : Any> Flowable<T>.combineLatest(publishSubject: PublishSubject<R>) = this.combineLatest(publishSubject.toFlowable(BackpressureStrategy.LATEST))
fun <T : Any, R : Any> Flowable<T>.combineLatest(subject: BehaviorSubject<R>) = this.combineLatest(subject.toFlowable(BackpressureStrategy.LATEST))
fun <T : Any, R : Any> Flowable<T>.combineLatest(observable: Observable<R>) = this.combineLatest(observable.toFlowable(io.reactivex.BackpressureStrategy.LATEST))
fun <T : Any> Flowable<List<T>>.plus(observable: Flowable<List<T>>) = this.combineLatest(observable).map{(a,b)-> a+b}

fun <T>Flowable<T>.subscribeWithPausing(activity : BaseActivity,subscribeWork: (T) -> Unit) = activity.resumable(this,subscribeWork)
fun <T>Flowable<T>.subscribeWithPausing(fragment : BaseFragment,subscribeWork: (T) -> Unit) = fragment.resumable(this,subscribeWork)

fun String.isMainThread() {
    val current = Looper.myLooper()
    if(current == Looper.getMainLooper()){ Log.d("=====", "~~~~$this~~~~ ====> is in Main Thread========" )}
    else{Log.d("=====", "~~~~$this~~~~ ====> is in Background Thread========$current" )    }

}
fun <T> Observable<T>.filterError() : Observable<T>{
    return this.onErrorResumeNext(Observable.empty())
}
fun Any.toObservable() = Observable.just(this)

fun <T> Flowable<T>.onMainThread() = this.observeOn(AndroidSchedulers.mainThread())
fun <T> Observable<T>.onMainThread() = this.observeOn(AndroidSchedulers.mainThread()).toFlowable(BackpressureStrategy.LATEST)
/* intent */


/* Context */



/* Collection */
fun Any.toList() = listOf(this)
/* Etc */
fun String.toMap() = this.removeSurrounding("{","}").split(",").map{ it.split("=")}.associateBy({it.getOrNull(0)?.trim()},{it.getOrNull(1)?.trim() })
fun String.toList() = this.removeSurrounding("[","]").split(",").map{it.trim()}



fun Dates.checkDays(days: String, date: Date): Boolean {

    var startint = 100
    var endint = 0

    if (days.equals("monTofri")) {
        startint = 1
        endint = 5
    }
    if (days.equals("monTothurs")) {
        startint = 1
        endint = 4
    }
    if (days.equals("fri")) {
        startint = 5
        endint = 5
    }

    var day=0
    if (date.isMonday()) day=1
    if (date.isTuesday()) day=2
    if (date.isWednesday()) day=3
    if (date.isThursday()) day=4
    if (date.isFriday()) day=5
    if (date.isSaturday()) day=6
    if (date.isSunday()) day=7

    if ( day>=startint && day<=endint){
        return true
    }
    else{
        return false
    }


}

/*extensions*/

const val INTENT_MODEL = "MODEL"




inline fun <reified T: Activity> ArgMap.startActivity(context: Context) = context.startActivity<T>(ARG_MAP to this.asString())
inline fun <reified T: Activity> ArgMap.intentFor(context: Context) = context.intentFor<T>(ARG_MAP to this.asString())
fun ArgMap.asParam() = (ARG_MAP to this.asString())

infix fun <T : Any> Boolean.ifelse(ifelse:Pair<T,T>): T = if(this) ifelse.first else ifelse.second
inline fun <T> T.doIf(predicate: (T) -> Boolean): T? = if (predicate(this)) this else null

/*extensions*/
const val ARG_MAP = "ARG_MAP"

fun Map<String?,String?>.plusString(key: Keys,value:String) = this.plus(Pair(key.toString(),value))
fun Map<Keys,String>.toStringMap() : Map<String,String> = this.mapKeys { it.key.toString() }
fun ArgMap.asString() = this.toStringMap().toString()

enum class Keys{
    ARG_MAP,LIST_ID,TABS_ID, FOCUS_ID,DETAIL_VIEW_ID,WEBVIEW_ID,DETAIL_VIEW_MAP_TYPE,NULL,MODEL_ID,CLASSNAME,LAT,LONG,MODEL,MENU_ID,MENU_LAYOUT;
    companion object : EnumCompanion<Keys>(values(), NULL) {val emptyMap : ArgMap = mapOf()}
}
enum class VALUES{
    STREET_VIEW,MAP_VIEW,NULL,TRUE,FALSE
    ;
    companion object : EnumCompanion<VALUES>(VALUES.values(),NULL)
}
fun String.toValues() = VALUES.of(this)
typealias ArgMap = Map<Keys,String>
fun Map<String?,String?>.toArgMap() : ArgMap = this?.mapKeys { Keys.of(it.key ?:"") }
        ?.mapValues { it.value ?: "" }
fun Intent.getArgsMap() : ArgMap = this.getStringExtra(ARG_MAP)?.toMap()?.toArgMap() ?: mapOf()
fun ArgMap.getArg(key:Keys):String = this.get(key) ?: ""
fun ArgMap.getValues(key1:Keys,key2:Keys) = Pair(this.getArg(key1),this.getArg(key2))
fun ArgMap.getValues(key1:Keys,key2:Keys,key3:Keys) = Triple(this.getArg(key1),this.getArg(key2),this.getArg(key3))
fun Intent.getArg(key:Keys) = this.getArgsMap().getArg(key)
fun Intent.getArgs(key1:Keys,key2:Keys) = this.getArgsMap().getValues(key1,key2)
fun Intent.getArgs(key1:Keys,key2:Keys,key3:Keys) = this.getArgsMap().getValues(key1,key2,key3)
fun ArgMap.plus(pair:Pair<Keys,VALUES>) = this.plus(mapOf(Pair(pair.first,pair.second.toString())))

@SuppressLint("NewApi")
fun Bundle.putArgsMap(argsMap : ArgMap): Bundle {
    this.putString(ARG_MAP,argsMap.asString());return this
}
@SuppressLint("NewApi")
fun Bundle.getArgsMap() : ArgMap = this.getString(ARG_MAP)?.toMap()?.toArgMap() ?: mapOf()

/* Class name*/
fun Any.className() = this.javaClass.simpleName

/* EnumCompanion */
open class EnumCompanion<V>(private val list:Array<V>,private val default:V ){
    private var map:Map<String,V>
    init {
        map = list.associateBy { it.toString() }
    }
    fun of(str:String?) = map[str] ?: default
}
fun String.toType() = CategoryType.of(this)
fun String.isType(type:CategoryType) = (this == type.toString())
/*Canvas*/
fun Canvas.drawFlatMapBuses(busLocations:List<Float>,width:Float,height:Float,showBus:Boolean){
    val center = width * 0.5f;val circleRadius = center*0.32f;
    this.apply {
        drawLine(center, 0f, width * 0.5f, height * 1.0f, Paint(Color.LTGRAY).apply { strokeWidth = 10f })
        drawCircle(center, center, center*0.32f * 1.05f, Paint(Color.RED))
        drawCircle(center, center, center*0.32f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL})
        if(showBus)busLocations.forEach {
            val location = center +  height * 0.6f * it
            drawArrow(center, location)
        }
    }
}


private fun Canvas.drawArrow(center: Float, y: Float = center,scale: Float = center * 0.0075f, color: Int = Color.BLACK) {

    drawCircle(center, y, scale*40f * 1.12f, Paint(Color.DKGRAY, Paint.Style.STROKE).apply { alpha = 100 })
    drawCircle(center, y, scale*40f * 1.10f, Paint(Color.WHITE).apply { alpha = 100 })
    drawCircle(center, y, scale*40f * 0.9f, Paint(color).apply { alpha = 200 })
    drawPath(Path().apply {
        moveTo(center, 25f * scale + y)
        lineTo(center + 20f * scale, -25f * scale + y)
        lineTo(center, -15f * scale + y)
        lineTo(center - 20f * scale, -25f * scale + y)
        lineTo(center, +25f * scale + y)
        close()
    }, Paint(Color.WHITE))
}

/*BitmapDescriptorFactory cache*/
object IconRenderer{

    var scale = 0.3f
    var bitmapCache: MutableMap<String,BitmapDescriptor> = mutableMapOf()
    fun getBusArrow(color:Int) : BitmapDescriptor {
        val id = Pair(scale,color).toString()
        val icon = bitmapCache[id] ?: BitmapDescriptorFactory
                .fromBitmap(Bitmap.createBitmap((200 * scale).toInt(), (200 * scale).toInt(), Bitmap.Config.ARGB_8888).apply {
                    "getBusArrow Bitmap create".isMainThread()
                    Canvas(this).apply { drawArrow(width * 0.5f,scale = width*0.012f, color = color); }
                })
        bitmapCache[id] = icon
        return icon
    }
    fun getBusStop(colors:List<Int>) : BitmapDescriptor {
        val id = Pair(scale,colors).toString()
        val size  = (scale * 60 * if(colors.count() == 1){1.4f}else{colors.count()*0.2f+1.4f}).toInt()
        val icon = bitmapCache[id] ?: BitmapDescriptorFactory
                .fromBitmap(Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                    "getBusStop Bitmap create".isMainThread()
                    val rect = RectF(0f,0f,width.toFloat(),width.toFloat())
                    val segment:Float = 360f / colors.count()
                    Canvas(this).apply {
                        colors.withIndex().forEach {(index,color) ->
                            drawArc(rect,index*segment,segment,true, Paint(color))
                            drawArc(rect,index*segment,segment,true, Paint(Color.WHITE,Paint.Style.STROKE).apply { strokeWidth = 3f })
                        }
                        drawCircle(width * 0.5f,width*0.5f ,width*0.2f, Paint(Color.WHITE))
                    }
                })
        bitmapCache[id] = icon
        return icon
    }
    fun getSVGIcon(iconId:Int,context: Context) : BitmapDescriptor? {
        val id = Pair(iconId, scale).toString()
        val icon = bitmapCache[id] ?: vectorToBitmap(iconId, context.resources,scale)
        return icon?.apply{bitmapCache[id] = this}
    }
    private fun vectorToBitmap(@DrawableRes id: Int, res: Resources?,scale: Float): BitmapDescriptor? {
        return res?.let{
            val vectorDrawable = ResourcesCompat.getDrawable(res,id,null)
            val size = (scale * 200).toInt()
            val bitmap = Bitmap.createBitmap(size,size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable?.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            //DrawableCompat.setTint(vectorDrawable, color)
            vectorDrawable?.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }

    }

}


/* try catch*/
inline fun catchAll(errorMessage:String,action: () -> Unit){
    try{action()}catch (t: Throwable){Log.d("--UtilExtensions274--","~~~Falied to => ${t}" )}
}



