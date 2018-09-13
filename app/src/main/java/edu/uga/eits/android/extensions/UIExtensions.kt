package edu.uga.eits.android.extensions

import android.app.Fragment
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.LayoutRes
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker
import edu.uga.eits.android.R
import edu.uga.eits.android.model.CategoryType

/**
 * Created by Luis on 6/1/17.
 * Extensions for UI
 */

interface CategoryItemable{val type: CategoryType }

/* inflate layout */
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}


/* Paint */
fun Paint(color:Int = Color.BLACK, style: Paint.Style =  Paint.Style.FILL) : Paint {
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color
    paint.style = style
  return paint
}

/** Button **/
/*Fragment*/

fun Fragment.bind(callback: OnStreetViewPanoramaReadyCallback) = (this as? StreetViewPanoramaFragment)?.let{it.getStreetViewPanoramaAsync(callback)}
fun android.support.v4.app.Fragment.bind(callback: OnMapReadyCallback) = (this as? SupportMapFragment)?.let{it.getMapAsync(callback)}



/* google Map */
fun GoogleMap.focus(marker:Marker){marker.showInfoWindow();this.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position,16f) )}
         /*** My Locaiton Button Layout https://stackoverflow.com/a/39816232 */
fun SupportMapFragment.sendMyLocationButtonToBottom() = (this.view?.findViewWithTag<View>("GoogleMapMyLocationButton")
        ?.layoutParams as? RelativeLayout.LayoutParams)?.apply{
    addRule(RelativeLayout.ALIGN_PARENT_TOP,0)
    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
    setMargins(0,0,50,50)
}
fun GoogleMap.getBounds() = this.projection.visibleRegion.latLngBounds



/**
 * Created by Yasuhiro Suzuki on 2017/07/15.
 *
 * http://stackoverflow.com/questions/40176244/how-to-disable-bottomnavigationview-shift-mode
 */

fun BottomNavigationView.disableShiftModeAndSetIconSize() {
    val menuView = getChildAt(0) as BottomNavigationMenuView
    try {
        menuView.javaClass.getDeclaredField("mShiftingMode").also { shiftMode ->
            shiftMode.isAccessible = true
            shiftMode.setBoolean(menuView, false)
            shiftMode.isAccessible = false
        }
        val iconHeight = resources.getDimension(R.dimen.bottom_navigation_icon_dp).toInt()
        for (i in 0 until menuView.childCount) {
            (menuView.getChildAt(i) as BottomNavigationItemView).also { item ->
                item.setShiftingMode(false)
                item.setChecked(item.itemData.isChecked)
                item.findViewById<View>(android.support.design.R.id.icon)?.layoutParams
                ?.apply { height=iconHeight; width = iconHeight}
            }
        }
    } catch (e: NoSuchFieldException) {
        Log.e("BottomNavigationHelper", "Unable to get shift mode field", e)
    } catch (e: IllegalAccessException) {
        Log.e("BottomNavigationHelper", "Unable to change value of shift mode", e)
    }
}


/* Snackbar*/
fun Snackbar.toTop() : Snackbar {
    (view.layoutParams as? FrameLayout.LayoutParams)?.gravity = Gravity.TOP;return this
}