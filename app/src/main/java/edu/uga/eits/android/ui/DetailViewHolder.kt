package edu.uga.eits.android.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import edu.uga.eits.android.R
import edu.uga.eits.android.model.CategoryType
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.model.NullData
import kotlinx.android.synthetic.main.detail_view_cell.view.*

class DetailViewHolder(val view: View) : RecyclerView.ViewHolder(view) , ViewHolderable{
    override fun configure(item: CustomCellItem,context:Context){
        Log.d("--DetailViewHolder15--","~~~item~~~ => ${item}" )
        if(item is DetailViewItem){
            view.title.text = item.title
            view.detailBoldText.text = item.boldDetailText
            view.detailText.text = item.detailText
            if(item.buttonTap.second == null)view.favoriteButton.visibility = View.GONE
            view.favoriteButton.isSelected = item.favoriteState
            view.favoriteButton.text = if(item.favoriteState)"Unfavorite" else "Favorite"
            view.showOnMapButton.setOnClickListener{item.buttonTap.first()}
            view.favoriteButton.setOnClickListener{item.buttonTap.second?.invoke()}
            view.directionButton.setOnClickListener{item.buttonTap.third()}
        }
    }
}
data class DetailViewItem(override val title:String, val boldDetailText:String, val detailText:String? = null, val favoriteState:Boolean = false,
                          val buttonTap : Triple<() -> Any,(() -> Any)?,() -> Any>,
                          override val sortMatrics: Float = 0.0f, override val data: Any = NullData(),
                          override val type: CategoryType = CategoryType.DetailViewItem) : CustomCellItem{
    override fun getViewType() = R.layout.detail_view_cell
}