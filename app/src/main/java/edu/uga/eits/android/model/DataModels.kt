package edu.uga.eits.android.model


import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.annotations.SerializedName
import com.jakewharton.rx.replayingShare
import edu.uga.eits.android.App
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.checkDays
import edu.uga.eits.android.extensions.isMainThread
import edu.uga.eits.android.extensions.onIO
import io.reactivex.Observable
import io.reactivex.rxkotlin.combineLatest
import khronos.Dates
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*




data class Place(override val id:String, override val name:String,val code:String,val address : String,val searchText:String,val categoriesId:String,
                 override val latitude: Double,override val longitude: Double,val st_latitude:Double,val st_longitude: Double,val st_heading: Double
): Favorable,Locateable,Serializable {
    companion object : RxEntity<Place>(){
        override fun pull() = App.dataset.map{it.places}
    }
    override fun flagChanges() {   Place.flagChanges() }
}
data class BusStop(override val id:String,override val name:String
                   ,override val latitude: Double,override val longitude: Double,
                   val isAthensBus: Boolean,val st_latitude: Double,
                   val st_longitude: Double,val st_heading: Double
) : Locateable, Favorable
{
    companion object : RxEntity<BusStop>(){
        override fun pull() = App.dataset.map{it.stops}
    }

    override fun flagChanges() {   BusStop.flagChanges() }
}

data class RouteStop(val id:String,val routeId:String, val direction:Int, val directionName:String,
                     val stopId:String,val stopSequence:Int,val stop: BusStop,val busLocations : List<Float>,override val times:List<Long>) : EtaDisplayable{
    companion object : RxRealtimeEntity<RouteStop>(){
        override val fetchInterval = 5L
        override fun pull(id:String) = App.component.getRestapi().getRoutestops(id)

    }
}
data class BusStopEtas(val id:String,val routeId: String,override val times:List<Long>) : EtaDisplayable{
    companion object :RxRealtimeEntity<BusStopEtas>(){
        override val fetchInterval = 20L
        override fun pull(id: String) = App.component.getRestapi().getStopEtas(id)
    }
    fun stopId() = id.split("__").filter{it.contains("s_")}.map{it.replace("s_","")}.first() ?: ""
    fun isAthensStop() = stopId().startsWith("c")
}
data class Parking(val lots: List<Lot> = emptyList(),val prices:List<Price> = emptyList(),val enforcements:List<Enforcement> = emptyList())
data class Lot(override val id: String, val type: String, val nearest: String, val priceColor: String, val enforceColor: String,
               @SerializedName("lat") override val latitude: Double, @SerializedName("lng") override val longitude: Double, override val name: String):Locateable,Serializable,Favorable{
    companion object : RxEntity<Lot>(){
        override fun pull() = App.dataset.map{it.parking.lots}
        val withEnforcement =
            getAll().combineLatest(Enforcement.getAll()).map { (lots,enforcements)->
                " Parking and Enforcement join ".isMainThread()
                lots.map{lot -> Pair(lot,enforcements.filter { it.enforceColor == lot.enforceColor })}
            }.replayingShare()
    }
    override fun flagChanges() {Lot.flagChanges()}
    fun enforcementText(list:List<Enforcement>) : String? {
        val date = Dates.today
        return list.find { date.after(Dates.of(-1,-1,-1,it.start.split(":").get(0).toInt(),it.start.split(":").get(1).toInt(),-1))
                && date.before(Dates.of(-1,-1,-1,it.end.split(":").get(0).toInt(),it.end.split(":").get(1).toInt(),-1))
                && Dates.checkDays(it.days,date)
        }?.displayText
    }
}
data class Price(val priceColor:String,@SerializedName("price") val cost:Cost){
    data class Cost(val monthly:String,val hourly:String)
    companion object : RxEntity<Price>(){
        override fun pull() = App.dataset.map{it.parking.prices}
    }
}
data class Enforcement(val enforceColor:String,val displayText:String,val days:String,val start:String,val end:String){
    companion object : RxEntity<Enforcement>(){
        override fun pull() = App.dataset.map{it.parking.enforcements}
    }
}

data class LotCoord(val pId: String,val pOC:List<List<LatLong>>){
   data class LatLong(val lat:Double,val lng:Double)
    companion object {
        fun getCoords(id: String)= App.component.getRestapi().getLotCoords(id)
                .onIO().onErrorResumeNext(Observable.just(LotCoord("", emptyList())))
                .map{it.pOC}.map{it.map{it.map{LatLng(it.lat,it.lng)}}}
    }
}

data class Dataset(var modified: Date = Date(-10),
                   val places: List<Place> = emptyList(), val stops: List<BusStop> = emptyList(),
                   val parking:Parking = Parking() ){
    fun postProcess(){}
}




data class NullData(val name: String = "Empty Data")





data class RouteShape(@SerializedName("route_id") val routeId:String,@SerializedName("route_shape") val points:List<Coord>){
    data class Coord(@SerializedName("shape_pt_lon")val longitude: Double,@SerializedName("shape_pt_lat")val latitude: Double)
    companion object {
        private var cache : Map<String,List<LatLng>>
        private var cacheBound : Map<String,LatLngBounds>
        init {
            val shapeLists : List<List<RouteShape>> = listOf(R.raw.route_shapes_uga_052018,R.raw.route_shape_athens_bus013118).map({
                App.component.getFileManager().getFromResource<List<RouteShape>>(it) ?: emptyList()
            })
            cache = shapeLists.flatten().associateBy({it.routeId},{it.points.map{LatLng(it.latitude,it.longitude)}})
            cacheBound = cache.mapValues { LatLngBounds.Builder().apply{it.value.forEach{include(it)}}.build()  }
        }
        fun getCoords(routeId:String) = cache[routeId] ?: emptyList()
        fun getBound(routeId:String) = cacheBound[routeId]
    }
}
