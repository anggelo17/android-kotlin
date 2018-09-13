package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.model.CustomCellItem
import kotlinx.android.synthetic.main.header_expandable_cell.view.*

/**
 * Created by Luis on 11/1/17.
 */
class HeaderExpandableViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context) {
        if(item is HeaderItem){
            view.title.text = item.title
            view.container.tag = item.data
            view.expandButton.isSelected = item.expanded ?: false
        }
    }
}