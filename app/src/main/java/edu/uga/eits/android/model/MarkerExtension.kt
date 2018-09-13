package edu.uga.eits.android.model

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.IconRenderer
import edu.uga.eits.android.module.pushView



fun Place.asMarker(context:Context) : MarkerOptions {
    return MarkerOptions().position(LatLng(this.latitude,this.longitude)).title(this.name).alpha(0.5f).icon(IconRenderer.getSVGIcon(R.drawable.ic_pin_and_shadow,context))
}
fun Lot.asMarker(context:Context) : MarkerOptions {
    return MarkerOptions().position(LatLng(this.latitude,this.longitude)).title(this.name).alpha(0.5f).icon(IconRenderer.getSVGIcon(R.drawable.ic_pin_and_shadow,context))
}
fun Marker.markerSelect() = when(this.tag){
   // is Bus -> {true} //Do nothing, no focusing
    is BusStop -> {false}
    is Place -> {false}
    is Lot -> {false}
    else -> {
        Log.d("--MarkerExtension23--","~~~markerSelect unkown type~~~ => ${this}" );false}
}






