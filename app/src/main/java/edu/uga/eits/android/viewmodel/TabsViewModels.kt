package edu.uga.eits.android.viewmodel

import edu.uga.eits.android.App
import edu.uga.eits.android.BaseActivity
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.model.*
import edu.uga.eits.android.ui.HeaderItem
import io.reactivex.BackpressureStrategy
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zip
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Luis on 11/9/17.
 */
enum class TabsViewModels{
    UGABusHome{
        init{ tabIds = listOf(ListViewModels.UGABusStopList); rxTitle.onNext("UGA Bus")}
    },
    ParkingHome{
        init{ tabIds = listOf(ListViewModels.ParkingDeckList,ListViewModels.ParkingByBuildinglist,ListViewModels.ParkingLotList,ListViewModels.RecentList); rxTitle.onNext("Parking")}
    },
    UniversalSearch{
        init{

            val searchText = BehaviorSubject.createDefault("")
                    .apply { rxSearchText = this }
            val bus = BusStop.getAll().map{it.map{it.asCustomCellItem()}}
            val place = Place.getAll().map{it.map{it.asCustomCellItem()}}
            val parking = Lot.getAll().map{it.map{it.asCustomCellItem()}}
            val all = listOf(bus,place,parking).zip{it}.throttle(200).shareReplay()
            val result = all.combineLatest(searchText).map{(lists,text) ->
                text.takeIf { it.isBlank() }
                if(text.isBlank()){ //return recent items
                    App.preferences.getRecent(On.GlobalRecents)
                            .run{associateBy({it},{ indexOf(it) })}.let{recents ->
                        lists.map{CustomCellUtils.recentFilter(it,recents)}

                    }
                }else{ //return filtered
                    lists.map{CustomCellUtils.searchFilter(it ,text)}
                }
            }.shareReplay()
            val header = searchText.map{ if(it.isBlank())"Recent Items" else "Search Results"}
                    .map{HeaderItem.getItem(it)}.toFlowable(BackpressureStrategy.LATEST)
            ListViewModels.UniversalSearchAll.apply { rxTitle.onNext("All")
                customCellsDelegate = {_ -> result.map{it.flatten().sortedBy { it.sortMatrics }}.let{header.plus(it)}}
            }
            ListViewModels.UniversalSearchBus.apply { rxTitle.onNext("Bus")
                customCellsDelegate = {_ -> result.map{it.get(0).sortedBy { it.sortMatrics }}.let{header.plus(it)}}
            }
            ListViewModels.UniversalSearchCampus.apply { rxTitle.onNext("Campus")
                customCellsDelegate = {_ -> result.map{it.get(1).sortedBy { it.sortMatrics }}.let{header.plus(it)}}
            }
            ListViewModels.UniversalSearchOther.apply { rxTitle.onNext( "Other")
                customCellsDelegate = {_ -> result.map{it.get(2).sortedBy { it.sortMatrics }}.let{header.plus(it)}}
            }
            tabIds = listOf(ListViewModels.UniversalSearchAll,ListViewModels.UniversalSearchBus,ListViewModels.UniversalSearchCampus,ListViewModels.UniversalSearchOther)
        }
    },
    EmptyTab{}
    ;
    companion object : EnumCompanion<TabsViewModels>(values(),EmptyTab){
    }
    var tabIds = listOf<Any>()
    var params : ArgMap = mapOf()
    var rxSearchText : BehaviorSubject<String>? = null
    val rxTitle = BehaviorSubject.createDefault("")
    fun bindTitle(activity:BaseActivity) = rxTitle.distinctUntilChanged()
            .subscribe{activity.supportActionBar?.title = it}.addTo(activity.disposables)
}