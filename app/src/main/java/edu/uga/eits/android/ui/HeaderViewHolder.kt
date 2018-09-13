package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.R
import edu.uga.eits.android.model.CategoryType
import edu.uga.eits.android.model.CustomCellItem
import kotlinx.android.synthetic.main.header_view_cell.view.*

/**
 * Created by Luis on 6/15/17.
 */

class HeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context){
        if(item is HeaderItem){
            view.title.text = item.title
            view.container.tag = item.type


        }
    }
}
class BlankViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context){
    }
}
data class HeaderItem(override val title:String, override val sortMatrics: Float = -1.0f
                      , override val type: CategoryType = CategoryType.HeaderItem, val expanded: Boolean? = null, override val data: Any = type):CustomCellItem{
    companion object{
        fun getItem(title:String) = listOf(HeaderItem(title) as CustomCellItem)
        fun getGrayItem(title:String) = listOf(HeaderItem(title,type = CategoryType.GrayHeaderItem) as CustomCellItem)
        fun getItem(predicate:Boolean,title:String) = if(predicate)listOf(HeaderItem(title)) else emptyList()
        fun getItem(type: CategoryType) = listOf(HeaderItem(type.toTitle(), type = type, expanded = false,data = type as Any))
        fun getBlank() = listOf(HeaderItem("",type = CategoryType.BlankItem) as CustomCellItem)
        fun setSelected(cell: CustomCellItem, selectedType:CategoryType) = (cell as? HeaderItem)?.copy(expanded = cell.type == selectedType) ?: cell
    }
    override fun getViewType() = when{
        expanded != null -> R.layout.header_expandable_cell
        type == CategoryType.GrayHeaderItem -> R.layout.header_view_cell_grey
        type == CategoryType.BlankItem -> R.layout.blank_cell
        else -> R.layout.header_view_cell
    }
}