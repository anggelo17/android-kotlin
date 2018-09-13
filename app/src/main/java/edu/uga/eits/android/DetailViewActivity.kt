package edu.uga.eits.android

import android.os.Bundle
import android.util.Log
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.ui.RxListFragment
import edu.uga.eits.android.viewmodel.DetailViewModels
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject

class DetailViewActivity : BaseActivity(),OnStreetViewPanoramaReadyCallback , OnMapReadyCallback {
    private val rxStreetview = BehaviorSubject.create<StreetViewPanorama>()
    private val rxMap = BehaviorSubject.create<GoogleMap>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }
    override fun onStreetViewPanoramaReady(streetview: StreetViewPanorama) {
        rxStreetview.onNext(streetview)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        rxMap.onNext(googleMap)
    }
    private fun init(){
        intent.getArgs(Keys.MODEL_ID,Keys.DETAIL_VIEW_ID).let{(modelId,viewId) ->
            val viewModel = DetailViewModels.of(viewId)
            viewModel.bindTitle(this)
            //Top view
            when(viewModel.params.getValue(Keys.DETAIL_VIEW_MAP_TYPE).toValues()){
                VALUES.STREET_VIEW -> {
                    setContentView(R.layout.activity_detail_street_view)
                    fragmentManager.findFragmentById(R.id.streetviewpanorama).bind(this)
                    viewModel.streetviewCoords(modelId).combineLatest(rxStreetview).onMainThread().subscribe{ (viewCoord,streetview) ->
                        streetview.setPosition(viewCoord.first)
                        streetview.animateTo(StreetViewPanoramaCamera.builder().bearing(viewCoord.second.toFloat()).build(),0)
                    }.addTo(disposables)
                }
                VALUES.MAP_VIEW -> {
                    setContentView(R.layout.activity_detail_map_view)
                    supportFragmentManager.findFragmentById(R.id.map).bind(this)
                    viewModel.maptviewShape(modelId).combineLatest(rxMap).onMainThread().subscribe({ (linesAndBound,mapview) ->
                        linesAndBound.first.forEach{mapview.addPolyline(it)}
                        mapview.moveCamera(CameraUpdateFactory.newLatLngBounds(linesAndBound.second,800,400,10))
                    },{e -> Log.d("--DetailViewActivity45--","~~~e~~~ => ${e}" );}).addTo(disposables)
                }
            }
            // List view
            viewModel.list.let{
                setFragment(RxListFragment.newInstance(it.toArgsMap(modelId)),R.id.listview)
            }
        }
    }




}
