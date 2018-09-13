package edu.uga.eits.android.model

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.google.android.gms.analytics.HitBuilders
import edu.uga.eits.android.App

import edu.uga.eits.android.extensions.VALUES
import edu.uga.eits.android.extensions.className
import edu.uga.eits.android.extensions.toValues
import edu.uga.eits.android.module.Navigator
import edu.uga.eits.android.ui.HomeModule
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import java.util.*



typealias Meters = Float
interface Locateable{
    val latitude: Double
    val longitude : Double
    val name: String
    fun distance(other:Locateable):Meters{
        var results = FloatArray(1)
        Location.distanceBetween( latitude,longitude,other.latitude,other.longitude,results)
        return results.firstOrNull() ?: Meters.MAX_VALUE
    }
    fun distanceFromUser() = App.component.getLocationManager().getDistanceFromUser(this)
    fun openMapDirections(context: Context){
        //Uri.parse("http://maps.google.com/maps?&daddr=${latitude},${longitude}&travelmode=walking")
        val ana=Analytics()
        ana.sendEvent("Directions","Directions for= "+ uId()+"|"+name)

        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${latitude},${longitude}&travelmode=walking")
                .let{ Intent(Intent.ACTION_VIEW,it)}.apply { setPackage("com.google.android.apps.maps") }
                .let{startActivity(context,it,null)}
    }
}

data class LocateableInstance(override val latitude: Double,override val longitude : Double,
                              override val name: String) :Locateable{
    companion object {
        val invalidLocation = LocateableInstance(0.0,0.0,"invalid")
    }
}

fun Location.asLocateable() = LocateableInstance(latitude,longitude,"fromLocation")

/*Meters*/
fun Meters.miles() = this * 0.000621371
fun Meters.ft() = this * 3.28084
//fun Meters.isWalkable() = this < Constants.WALKABLE_MATRIX_CUT_OFF
fun Meters.displayText() : String {
    val miles = miles()
    return  when{
        miles < 0.18 -> "%.0f feet".format(ft())
        miles < 100 -> "%.1f miles".format(miles)
        else -> ""
    }
}
fun Meters.distInfo() = when{
    this < 290 -> Pair("%.0f".format(ft()),"feet")
    this<16093.4 -> Pair("%.1f".format(miles()),"miles")
    else -> Pair("","")
}
fun Meters.isWalkable()  = this < Constants.WALKABLE_DISTANCE
fun Meters.isNear() = this < Constants.NEAR_BY_CUT_OFF
/*Eta displayable*/
interface EtaDisplayable{
    val times : List<Long>
    fun etaShortTitle(omitPassedSeconds : Long = 30L) = this.etaDisplays().firstOrNull() ?: ""
    fun etaDisplays(omitPassedSeconds : Long = 30L):List<String>{
        val now = Date().time
        val list  = times.map{( it - now) / 1000}
                .filter{it > -omitPassedSeconds}
                .map{(it / 60).toInt()}
                .map{if(it < 1)  "< 1 min" else  "${it} min"}
        return list
    }
    fun firstEta() = times.firstOrNull()
}


/* SelectionPersistable */
typealias UID = String

enum class On{
    UniversalMap,DisplayedRoutesOnUniversalMap,Favorites,
    GlobalRecents,SearchRecents,ParkingViewed,RouteDetails
;
    val recentStringValue = "RecentOn."+this.toString()
    val stringValue = "SelectedOn."+this.toString()
    val focusStringValue = "FocusOn."+this.toString()
    val recentListSize: Int by lazy {(if(this == GlobalRecents)30 else 10)}
}
interface Identifiable{
    val id:String
    fun uId() : UID = (this.className() +"|"+ id)
}
interface SelectionPersistable : Identifiable{
    fun isFocused()  = (RxEntity.focusedUId == uId())
    fun isSelected(on: On) = App.preferences.contains(uId(),on)
    fun isRecent(on: On) = App.preferences.getRecent(on).contains(uId())
    fun getRecentRank(on:On) = App.preferences.getRecent(on).indexOf(uId())
    fun addToRecent(on: On) = App.preferences.addToRecent(uId(),on).let{flagChanges()}
    fun select(on:On,value:Boolean = true) = App.preferences.setSelected(uId(),value,on).let{flagChanges()}
    fun deSelect(on:On) = App.preferences.setSelected(uId(),false,on).let{flagChanges()}
    fun toggleSelection(on:On) = isSelected(on).let{ App.preferences.setSelected(uId(),!it,on);flagChanges();!it}
    fun focus(on:On) { RxEntity.focusedUId = uId(); select(on,true) }
    fun flagChanges()
}
fun Any.isFocused() = if(this is SelectionPersistable) this.isFocused() else false
fun Any.isSelected(on:On) = if(this is SelectionPersistable) this.isSelected(on) else false
interface Favorable :SelectionPersistable {
    fun isFavored() = isSelected(On.Favorites)
    fun toggleFavor() {
        toggleSelection(On.Favorites)

        val ana=Analytics()
        ana.sendEvent("Favorites","Add favorte id= "+ uId())

    }
}

enum class Stores{
    WasAthensBusMap
    ;
    fun store(str:String){App.preferences.setString(this.toString(),str)}
    fun store(str:VALUES){App.preferences.setString(this.toString(),str.toString())}
    fun load() : String {return App.preferences.getString(this.toString())}
    fun loadValues() : VALUES = load().toValues()
    fun loadBoolean() : Boolean = (load().toValues() == VALUES.TRUE)
}
fun Any.Id() = (this as? Identifiable)?.id
fun Any.uId() = (this as? Identifiable)?.uId()