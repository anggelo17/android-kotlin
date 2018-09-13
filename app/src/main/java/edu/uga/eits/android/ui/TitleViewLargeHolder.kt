package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.model.CustomCellItem
import kotlinx.android.synthetic.main.title_view_cell_large.view.*

/**
 * Created by Luis on 6/1/17.
 * Title view holder which has no subtitle but larger title size
 */


class TitleViewLargeHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context){
        if(item is TitleViewItem){
            view.title.text = item.title
            view.info.text = item.info
            view.container.tag = item.data
        }
    }
}
