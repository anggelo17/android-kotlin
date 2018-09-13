package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.R
import edu.uga.eits.android.model.CustomCellItem
import kotlinx.android.synthetic.main.phone_number_view_cell.view.*

/**
 * Created by Luis on 6/1/17.
 * Title view holder which has no subtitle but larger title size
 */


class PhoneNumberViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context: Context){
        if(item is TitleViewItem){
            view.title.text = item.title
            view.subTitle.text = item.subTitle
            view.imageView.setImageResource(R.drawable.ic_phone)
            view.container.tag = item.data
        }
    }
}
