package layout

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.ui.TitleViewItem
import edu.uga.eits.android.ui.ViewHolderable
import kotlinx.android.synthetic.main.legent_view_cell.view.*

/**
 * Created by Luis on 10/13/17.
 */

class LegendViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable {
    override fun configure(item: CustomCellItem,context: Context) {
        if(item is TitleViewItem){
            view.title.text = item.title
            view.checkBox2.buttonTintList = ColorStateList.valueOf(item.color ?: Color.GRAY)
            view.checkBox2.isChecked = item.isSelected ?: false
            view.container.tag = item.data
        }
    }
}