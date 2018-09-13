package edu.uga.eits.android.viewmodel

import android.content.Context
import android.util.Log
import edu.uga.eits.android.BaseActivity
import edu.uga.eits.android.ListViewActivity
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.model.*
import edu.uga.eits.android.model.Constants.NEAR_BY_FOR_ETA
import edu.uga.eits.android.module.pushView
import edu.uga.eits.android.ui.HeaderItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.intentFor


/**
 * Created by Luis on 6/14/17.
 *
 */

enum class ListViewModels{

    UGABusStopList {
        init {
            rxTitle.onNext("Stops")
            rxSearchText = BehaviorSubject.createDefault("")
        }

    },
    PlaceSelectList{init{rxSearchText = BehaviorSubject.createDefault("")}},  //Delegated on MapsActivity
    MapFavoritesList{}, //Delegated on MapsActivity
    BusMapLegendList{}, //Delegated on MapsActivity
    PlaceDetailsList{}, //Deleagted on DetailViewModels
    BusStopDetailsList{}, //Delgated on DetailViewModels
    ParkingDetailList{}, //Delgated on DetailViewModels
    //Delegated on TabsViewModels
    UniversalSearchAll{},
    UniversalSearchBus{},
    UniversalSearchCampus{},
    UniversalSearchOther{},
    ParkingLotList{
        init {rxTitle.onNext("Lots") ; rxSearchText = BehaviorSubject.createDefault("")}
        override fun customCells(modelId: String,context: Context): Flowable<List<CustomCellItem>> {
            return Lot.withEnforcement.map{it.filter { it.first.type.isType(CategoryType.Parking_Lot) }}
                    .reactToTimeInterval(60).map{
                it.map{(lot,enforcement) -> lot.asTitleViewItem(enforcement).copy(info = null) as CustomCellItem}
            }.let{applySearchFilter(it)}
        }
    },
    ParkingDeckList{
        init {rxTitle.onNext( "Decks") }
        override fun customCells(modelId: String,context: Context): Flowable<List<CustomCellItem>> {
            return Lot.withEnforcement.map{it.filter { it.first.type.isType(CategoryType.Parking_Deck) }}
                    .reactToTimeInterval(60).map{
                it.map{(lot,enforcement) -> lot.asTitleViewItem(enforcement).copy(info = null) as CustomCellItem}
            }
        }
    },
    ParkingByBuildinglist{
        init {rxTitle.onNext( "By Building"); rxSearchText = BehaviorSubject.createDefault("")}
        override fun customCells(modelId: String,context: Context): Flowable<List<CustomCellItem>> {
            return Lot.withEnforcement.map{
                it.filter { it.first.type.isType(CategoryType.Parking_Lot) }.sortedBy { it.first.nearest }
            }.reactToTimeInterval(60).map{
                it.map{(lot,enforcement) -> lot.asCustomCellItem(enforcement)}
            }.let{applySearchFilter(it)}
        }
    },
    RecentList{
        init {rxTitle.onNext("Recent") }
        override fun customCells(modelId: String,context: Context): Flowable<List<CustomCellItem>> {
            return Lot.withEnforcement.map{
                it.filter {(lot,_) -> lot.isRecent(On.ParkingViewed)==true }.sortedBy {(lot,_) -> lot.getRecentRank(On.ParkingViewed)}
            }.reactToTimeInterval(60).map{
                it.map{(lot,enforcement) -> lot.asCustomCellItem(enforcement)}
            }
        }
    },
    EmptyList{
    }
    ; //to end enum items
    open fun customCells(modelId:String,context: Context) = customCellsDelegate(Pair(modelId,context))
    open fun toArgsMap(modelId: String = "") : ArgMap = mapOf(Keys.LIST_ID to this.toString())
            .let{ if(modelId.isNotBlank()) it.plus(Keys.MODEL_ID to modelId) else it}
    fun toIntent(context: Context?) = mapOf(Keys.LIST_ID to this.toString()).asParam()
            .let{context?.intentFor<ListViewActivity>(it)}
    fun startActivity(context: Context,modelId: String="",argsMap: ArgMap = mapOf()){
        this.toArgsMap(modelId).plus(argsMap).let { params = params.plus(argsMap); it.plus(params) }
                .startActivity<ListViewActivity>(context)
    }
    var customCellsDelegate: (Pair<String,Context>) -> Flowable<List<CustomCellItem>> = { Flowable.just<List<CustomCellItem>>(emptyList())}
    var onClickDelegate: (Any,Context) -> Unit = {model,context -> Log.d("--ListViewModels108--","~~~onClickDelegate~~~ => ${model}" );model.pushView(context)}
    val onClickItem : (Any,Context) -> Unit = {model,context -> onTypeSelect(model);onClickDelegate(model,context)}
    var listDiverOn : Boolean = true
    var params : ArgMap = mapOf()
    val rxTitle = BehaviorSubject.createDefault("")
    var rxSearchText : BehaviorSubject<String>? = null
    private var typeSelect = BehaviorSubject.createDefault<CategoryType>(CategoryType.None)
    private var onTypeSelect : (model:Any) -> Unit = {} //do nothing by default
    fun applySearchFilter(cells:Flowable<List<CustomCellItem>>) = CustomCellUtils.searchFilter(cells,rxSearchText)
    open fun initAccordionCells(cells : Flowable<List<CustomCellItem>>,allTypes: List<CategoryType>,topNForEach :Int? = null) : Flowable<List<CustomCellItem>> {
        typeSelect.onNext(CategoryType.None)
        onTypeSelect = {model -> if(model is CategoryType){typeSelect.onNext(model)} }
        val selection = typeSelect.scan(CategoryType.None){
            last,currunt -> if(last == currunt)CategoryType.None else currunt
        } // Toggle selection, close it if type is already opened
        return cells.map{list ->
            allTypes.fold(listOf<CustomCellItem>()) { displayList, type -> // add headers for each types
                list.filter { it.type == type }.let{
                    val subList = topNForEach?.let{n -> it.sortedBy { it.sortMatrics }.take(n)} ?: it
                    if(subList.count()==0) displayList
                    else displayList + HeaderItem.getItem(type) + subList
                }
            }
        }.toObservable().combineLatest(selection,rxSearchText ?: Observable.just("")).map{ (list,selectedType,searchText) -> //filter only selected type
            list.filter{item ->
                if(item is CategoryItemable){
                    (item.type == selectedType) || (searchText.isNotBlank()) //If All type show when there is search text present
                }else{ true }
            }.map{HeaderItem.setSelected(it,selectedType) }
        }.toFlowable(BackpressureStrategy.LATEST)

        val  HeadersParking: List<String> = listOf("Rates")


    }
    fun bindTitle(activity: BaseActivity) = rxTitle.distinctUntilChanged().onMainThread()
            .subscribe{activity.supportActionBar?.title = it}.addTo(activity.disposables)
    companion object : EnumCompanion<ListViewModels>(values(),EmptyList) {
        fun getViewModel(argsMap: ArgMap,context: Context) =
                argsMap.getValues(Keys.LIST_ID,Keys.MODEL_ID)
                        .let{ (listId,modelId) -> ListViewModels.of(listId)
                                .let{Pair(it,it.customCells(modelId,context))}
                        }
    }
}