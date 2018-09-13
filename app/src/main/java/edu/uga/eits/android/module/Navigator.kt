package edu.uga.eits.android.module


import android.content.Context
import android.util.Log
import android.view.MenuItem
import edu.uga.eits.android.*
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.model.*
import edu.uga.eits.android.ui.HomeModule
import edu.uga.eits.android.viewmodel.DetailViewModels
import edu.uga.eits.android.viewmodel.TabsViewModels
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.anko.browse
import org.jetbrains.anko.intentFor


object Navigator{
  //  val selectedModule = BehaviorSubject.createDefault<Pair<HomeModule,Context?>>(Pair(HomeModule.Parking,null))
    val selectedModule = BehaviorSubject.createDefault<Pair<HomeModule,Context?>>(Pair(HomeModule.HOME,null))
    val disposable = CompositeDisposable()
    fun pushView(model : Any, context: Context ){
        when(model){

            is Place -> {
                DetailViewModels.PlaceDetail.startActivity(context,model.id)
            }
            is Lot -> {                
                model.addToRecent(On.ParkingViewed)
                DetailViewModels.ParkingDetail.startActivity(context,model.id)
            }


            else -> Log.d("--Navigator12- ", "~~~push view unknown type~~~ => ${model} ")
        }
    }
    fun menuItemSelected(item: MenuItem?,context: Context,argMap:ArgMap= mapOf()){
        when(item?.itemId){


            R.id.app_info -> {context.browse("http://mobileapps.uga.edu")}
        }
    }
    init{
        selectedModule
                .doOnNext{Log.d("--Navigator72--","~~~it~~~ => ${it}" )}
                .distinctUntilChanged{(item,context)-> item}
                .subscribe{ (item,context) -> gridItemSelected(item,context) }.addTo(disposable)
    }
   fun navigationSelected(id: Int,context: Context) = HomeModule.moduleForMenu[id]?.let{ selectedModule.onNext(Pair(it,context))}
    private fun gridItemSelected(item: HomeModule,context: Context? ){

        Log.d("ittem==", item.getId()+"--"+item.getTitle())

        when(item){


            HomeModule.HOME -> context?.intentFor<HomeActivity>()
            HomeModule.Parking -> mapOf(Keys.TABS_ID to TabsViewModels.ParkingHome.toString()).asParam()
                    .let{context?.intentFor<TabsActivity>(it)}

        }?.let{it.putExtra("MODULE_ID",item.getId())}?.let{context?.startActivity(it)}
    }

}
fun Any.pushView(context: Context) = Navigator.pushView(this,context)
