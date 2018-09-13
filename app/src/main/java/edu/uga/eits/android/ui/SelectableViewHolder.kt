package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.module.Navigator
import kotlinx.android.synthetic.main.selectable_view_cell.view.*

/**
 * Created by Luis on 8/31/17.
 *
 */
class SelectableViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable {
    override fun configure(item: CustomCellItem,context: Context){
        if(item is TitleViewItem){
            view.title.text = item.title
            view.checkbox.isChecked = item.isSelected ?: false
            view.container.tag = item.data
            view.infoOnRight.text = item.info
            if(!item.showDetailButton)view.detailButton.visibility = View.INVISIBLE
            view.detailButton.setOnClickListener { Navigator.pushView(item.data,view.container.context) }
        }
    }
}
