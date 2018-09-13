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
import kotlinx.android.synthetic.main.distance_view_cell.view.*

/**
 * Created by Luis on 10/3/17.
 */

class DistanceViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context){
        if(item is DistanceViewItem){
            view.title.text = item.title
            view.subTitle.text = item.boldSubTitle
            view.distance.text = item.distance
            view.unit.text = item.unit
            view.walk.text = if(item.unit == null)"" else "walk"
            view.container.tag = item.data
        }
    }
}


data class DistanceViewItem(override val title:String, val boldSubTitle:String?=null, val distance:String?=null, val unit:String?=null
                            , override val sortMatrics: Float = 0.0f, override val data:Any= NullData()
                            , override val type: CategoryType = CategoryType.None, override val searchText: String = title) : CustomCellItem, CategoryItemable, Searchable {
    override fun getViewType(): Int {
        return R.layout.distance_view_cell
    }
}