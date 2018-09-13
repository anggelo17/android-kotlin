package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.CategoryItemable
import edu.uga.eits.android.model.CategoryType
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.model.NullData
import edu.uga.eits.android.model.Searchable
import kotlinx.android.synthetic.main.title_view_cell.view.*
/**
 * Created by Luis on 6/1/17.
 *
 * ViewHolder for TitleView Cell
 */
class TitleViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context){
        if(item is TitleViewItem){
            view.title.text = item.title
            view.subTitle.text = item.subTitle
            view.info.text = item.info
            view.container.tag = item.data
        }
    }
}
data class TitleViewItem(override val title:String, val subTitle:String?=null
                         , val info:String?=null, override val sortMatrics: Float = 0.0f
                         , val isSelected:Boolean?=null, val isLegend:Boolean=false,val isGrayedOut:Boolean = false,
                         val color: Int?=null, override val data:Any= NullData(), val showDetailButton : Boolean = true,
                         override val type: CategoryType = CategoryType.None, override val searchText: String = title) : CustomCellItem, CategoryItemable, Searchable {
    override fun getViewType()= when{
            isLegend -> R.layout.legent_view_cell
            isSelected != null -> if(isGrayedOut) R.layout.selectable_view_cell_grey else R.layout.selectable_view_cell
            subTitle == null -> R.layout.title_view_cell_large
            isGrayedOut -> R.layout.title_view_cell_large_grey
            type == CategoryType.PhoneNumber -> R.layout.phone_number_view_cell
            else -> R.layout.title_view_cell
        }
}
