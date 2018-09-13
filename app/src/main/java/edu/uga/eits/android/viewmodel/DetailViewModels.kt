package edu.uga.eits.android.viewmodel

import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import edu.uga.eits.android.BaseActivity
import edu.uga.eits.android.DetailViewActivity
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.model.*
import edu.uga.eits.android.model.CategoryType.Companion.displayOnParkingDetailTypes
import edu.uga.eits.android.model.CategoryType.Companion.displayOnPlaceDetailTypes
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.subjects.BehaviorSubject



enum class DetailViewModels{
    PlaceDetail{
        init{
            rxTitle.onNext("Place Detail")
            params = mapOf(Keys.DETAIL_VIEW_MAP_TYPE to VALUES.STREET_VIEW.toString())
            list = ListViewModels.PlaceDetailsList
            list.customCellsDelegate = {(modelId,context) ->
                val detailCell = Place.get{ modelId == it.id }.map{listOf(it.asDetailViewItem(context))}
                val nearBy = Place.get{ modelId == it.id }.combineLatest(BusStop.getAll(),Place.getAll())
                        .map { (model,stops,places )->
                                    stops.map { it.asDistanceCellItem(it.distance(model)) }
                                    .plus(places.map { it.asDistanceCellItem(it.distance(model)) })
                }
                detailCell.plus(list.initAccordionCells(nearBy,displayOnPlaceDetailTypes,topNForEach = 5))
                        .map{it.addBlank()}
            }
        }
        override fun streetviewCoords(modelId: String) = Place.get{it.id==modelId}.map{Pair(LatLng(it.st_latitude,it.st_longitude),it.st_heading)}
    },

    ParkingDetail{
        init {
            rxTitle.onNext("Parking Detail")
            params = mapOf(Keys.DETAIL_VIEW_MAP_TYPE to VALUES.MAP_VIEW.toString())
            list = ListViewModels.ParkingDetailList
            list.customCellsDelegate = { (modelId,context) ->
                val detailCell = Lot.get{it.id == modelId}.flatMap { lot ->
                    Enforcement.getAll{lot.enforceColor == it.enforceColor}
                            .reactToTimeInterval(60)
                            .map{listOf(lot.asDetailViewItem(it,context))}
                }
                val nearBy = Lot.get{it.id == modelId}.combineLatest(BusStop.getAll(),Place.getAll())
                        .map{(model,stops,places) ->
                            stops.map { it.asDistanceCellItem(it.distance(model)) }
                                    .plus(places.map { it.asDistanceCellItem(it.distance(model)) })
                        }
                val rates = Lot.get{it.id == modelId}.flatMap{lot ->
                    Price.get{it.priceColor==lot.priceColor}
                            .map{
                                it.cost.hourly.split(",")
                                .plus(it.cost.monthly.trim().let{if(it!="NA") "Montly $${it}" else ""})}
                            .map{it.filter{it.isNotBlank()}}
                            .map{it.map{it.asCustomCellItem(CategoryType.Parking_Rates)}}
                }
                detailCell.plus(list.initAccordionCells(rates.plus(nearBy),displayOnParkingDetailTypes,10)) //topN should be large to display rate options
            }
        }
        override fun maptviewShape(modelId: String) = LotCoord.getCoords(modelId).toFlowable(BackpressureStrategy.LATEST).map{
            val bounds = LatLngBounds.Builder()
            val shapes = it.map{
                bounds.apply{it.forEach{include(it)}}
                PolylineOptions().color(Color.RED).addAll(it)
            }
            Pair(shapes,bounds.build())
        }
    },
    EmptyDetail{}
    ;
    private fun toArgsMap(modelId: String) : ArgMap = mapOf(Keys.DETAIL_VIEW_ID to this.toString(),Keys.MODEL_ID to modelId)
    fun startActivity(context: Context,modelId:String) = this.toArgsMap(modelId).startActivity<DetailViewActivity>(context)
    open fun streetviewCoords(modelId: String) : Flowable<Pair<LatLng,Double>> = Flowable.never()
    open fun maptviewShape(modelId: String) = Flowable.never<Pair<List<PolylineOptions>,LatLngBounds>>()
    var list = ListViewModels.EmptyList
    var params : ArgMap = mapOf()
    val rxTitle = BehaviorSubject.createDefault("")
    fun bindTitle(activity: BaseActivity) = rxTitle.distinctUntilChanged()
            .subscribe{activity.supportActionBar?.title = it}.addTo(activity.disposables)
    companion object : EnumCompanion<DetailViewModels>(DetailViewModels.values(), EmptyDetail){}

}
