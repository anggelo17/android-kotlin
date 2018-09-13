package edu.uga.eits.android.api

import edu.uga.eits.android.model.*
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*


interface Restapi {

    @GET("v3_2/cache/routeTracking_{id}")
    fun getRoutestops(@Path("id") id : String) : Observable<List<RouteStop>>
    @GET("v3_2/cache/stop_{id}")
    fun getStopEtas(@Path("id") id : String) : Observable<List<BusStopEtas>>
    @GET("v3_2/staticjsons/parkingCoords/{id}.json")
    fun  getLotCoords(@Path("id") id: String): Observable<LotCoord>




}