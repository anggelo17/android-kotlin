package edu.uga.eits.android.model



import android.content.Context
import android.os.Build
import android.view.View
import edu.uga.eits.android.R.color.focus_cell_color
import edu.uga.eits.android.R.color.no_color
import edu.uga.eits.android.extensions.Or
import edu.uga.eits.android.extensions.combineLatest
import edu.uga.eits.android.extensions.toType
import edu.uga.eits.android.ui.DetailViewItem
import edu.uga.eits.android.ui.DistanceViewItem
import edu.uga.eits.android.ui.DrawableViewItem
import edu.uga.eits.android.ui.TitleViewItem
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


interface CustomCellItem{
    val title : String
    fun getViewType() : Int
    val type : CategoryType
    val sortMatrics : Float
    val data:Any
}
interface Searchable {
    val searchText : String
}
enum class CellStyle{
    NORMAL, HIGH_LIGHT, FOCUSED{init{backgroundColor=focus_cell_color}}
    ;
    var backgroundColor : Int? = no_color
    fun applyStyle(view : View,context: Context){
        backgroundColor?.let{
            val color = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){context.getColor(it)}else{context.resources.getColor(it)}
            view.setBackgroundColor(color)
        }
    }
}
fun Place.asDetailViewItem(context: Context) : CustomCellItem {
    return DetailViewItem(name,address.Or(categoriesId.toType().toTitle()),
            code.takeIf{it.isNotBlank()}?.let{"Building Code : ${it}"} ,isFavored(),
            Triple({focus(On.UniversalMap);openMapDirections(context = context)},
                    {toggleFavor()},
                    {openMapDirections(context)}))
}

fun Place.asTitleViewItem() : TitleViewItem {
    val type = CategoryType.of(categoriesId)
    val info = if(type == CategoryType.Building) "# ${this.code}" else type.toTitle()
    return TitleViewItem(name,type = type, info = info,data = this,searchText = searchText)
}
fun Place.asCustomCellItem() : CustomCellItem = asTitleViewItem()
fun Lot.asSelectionCellItem(on:On,newCategoryType: CategoryType? = null ) : CustomCellItem {
    return TitleViewItem(name,type = newCategoryType ?: CategoryType.of(this.type), isSelected = this.isSelected(on),data = this)
}
fun BusStop.asTitleViewItem() : TitleViewItem {
    val type = if(isAthensBus) CategoryType.Athens_Transit_Stop else CategoryType.UGA_Bus_Stop
    return TitleViewItem(name,type = type,info = type.toTitle(), data = this,searchText = name)
}
fun TitleViewItem.asSelectionCellItem(on:On): CustomCellItem {
    return copy(isSelected =  this.data.isSelected(on))
}
fun BusStop.asCustomCellItem() : CustomCellItem {
    return asTitleViewItem()
}
fun BusStop.asDetailViewItem(context:Context): CustomCellItem {
    return DetailViewItem(name,distanceFromUser().displayText().takeIf{it.isNotBlank()}?.let{"${it} walking"} ?: "",
            "" ,isFavored(),
            Triple({focus(On.UniversalMap);openMapDirections(context)},
                    {toggleFavor()},
                    {openMapDirections(context)}))
}

fun Place.asDistanceCellItem(distance: Meters) : CustomCellItem {
    val (dist,unit) = distance.distInfo()
    return DistanceViewItem(name,distance = dist,unit = unit,sortMatrics = distance,data = this,type = categoriesId.toType())
}
fun BusStop.asDistanceCellItem(distance: Meters = distanceFromUser(),etasMap : Map<String,String> = mapOf(),activeMap:Map<String,String> = mapOf()) : CustomCellItem {
    val (dist,unit) = distanceFromUser().distInfo()
    return DistanceViewItem(name,etasMap[id]?: activeMap[id] ?: "",dist,unit,sortMatrics = distance,data = this ,
            type = if(isAthensBus) CategoryType.Athens_Transit_Stop else CategoryType.UGA_Bus_Stop )
}



fun Lot.asTitleViewItem(list:List<Enforcement> = listOf()) : TitleViewItem {
    return TitleViewItem(if (type.toType()==CategoryType.Parking_Lot) this.id + " (${this.nearest}) "  else this.id + " (${this.name})" , enforcementText(list),info = type.toType().toTitle(), data = this)
}
fun Lot.asCustomCellItem(list:List<Enforcement> = listOf()):CustomCellItem{
    return asTitleViewItem(list)
}
fun Lot.asDetailViewItem(list:List<Enforcement>,context:Context) : CustomCellItem {
    return DetailViewItem(name,enforcementText(list) ?: "","",false, Triple({focus(On.UniversalMap);openMapDirections(context)},
            {toggleFavor()},
            {openMapDirections(context)}))
}
fun String.asCustomCellItem(type:CategoryType = CategoryType.None):CustomCellItem{
    return TitleViewItem(this.trim(),type=type)
}


object CustomCellUtils{
    fun searchFilter(cells: Flowable<List<CustomCellItem>>, search: BehaviorSubject<String>? ) : Flowable<List<CustomCellItem>> {
        return cells.combineLatest(search ?: Observable.just(""))
                .map{(list,text) -> searchFilter(list,text)}
    }
    fun searchFilter(list : List<CustomCellItem>,text:String) : List<CustomCellItem> {
        return list.filter{(it as? Searchable)?.searchText?.contains(text,true) ?: true}
    }
    fun recentFilter(list : List<CustomCellItem>,recentRanks:Map<String,Int>):  List<CustomCellItem>{
        return list.filter{recentRanks.containsKey(it.data.uId())}.map{(it as? TitleViewItem)?.copy(sortMatrics = recentRanks[it.data.uId()]?.toFloat() ?: Float.MAX_VALUE) ?: it}
    }
}

fun List<CustomCellItem>.addBlank() = this.plus(edu.uga.eits.android.ui.HeaderItem.getBlank())