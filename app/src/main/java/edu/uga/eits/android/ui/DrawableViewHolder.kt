package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.R
import edu.uga.eits.android.model.CategoryType
import edu.uga.eits.android.model.CellStyle
import edu.uga.eits.android.model.CellStyle.NORMAL
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.model.NullData
import kotlinx.android.synthetic.main.drawable_view_cell.view.*

/**
 * Created by Luis on 8/31/17.
 *
 */
class DrawableViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context:Context){
        if(item is DrawableViewItem){
            view.title.text = item.title
            view.boldSubTitle.text = item.boldSubTitle
            view.subTitle.text = item.subTitle
            view.container.tag = item.data
            view.imageView.busLocations =  item.busLocations
            view.info.text = item.info
            item.style.applyStyle(view,context)
        }
    }
}
data class DrawableViewItem(override val title:String,val boldSubTitle:String?=null,val subTitle:String?=null,val info:String?=null,val busLocations : List<Float>
                            ,override val sortMatrics: Float = 0.0f, override val data:Any= NullData()
                            ,override val type: CategoryType = CategoryType.None,var style:CellStyle = NORMAL) : CustomCellItem{
    override fun getViewType(): Int {
        return R.layout.drawable_view_cell
    }
}