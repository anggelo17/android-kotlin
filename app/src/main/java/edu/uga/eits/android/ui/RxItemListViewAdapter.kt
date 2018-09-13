package edu.uga.eits.android.ui

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.view.clicks
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.inflate
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.model.NullData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.title_view_cell.view.*
import layout.LegendViewHolder

/**
 * Created by Luis on 6/1/17.
 * Adapter for applying CustomCellItem to RecyclerView
 */
class RxItemListViewAdapter(val rx_itemSelected :  PublishSubject<Any> = PublishSubject.create<Any>()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mItems:  List<CustomCellItem>
    var selectedPoisition = -1
    var disposables = CompositeDisposable()
    init {
        Log.d("RxItemListViewAdapter23", "~~~init~~~ => " + this.toString())
        mItems = emptyList()
    }
    fun updateItems(items:List<CustomCellItem>){
        mItems = items
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = parent.inflate(viewType)
        view.clicks().takeUntil(RxView.detaches(parent))
                .map{view.container.tag ?: NullData()}.subscribe(rx_itemSelected::onNext).addTo(disposables)
        return when(viewType){
            R.layout.title_view_cell -> TitleViewHolder(view)
            R.layout.title_view_cell_large -> TitleViewLargeHolder(view)
            R.layout.phone_number_view_cell -> PhoneNumberViewHolder(view)
            R.layout.header_view_cell -> HeaderViewHolder(view)
            R.layout.header_view_cell_grey -> HeaderViewHolder(view)
            R.layout.blank_cell -> BlankViewHolder(view)
            R.layout.drawable_view_cell -> DrawableViewHolder(view)
            R.layout.distance_view_cell -> DistanceViewHolder(view)
            R.layout.legent_view_cell -> LegendViewHolder(view)
            R.layout.selectable_view_cell -> SelectableViewHolder(view)
            R.layout.detail_view_cell -> DetailViewHolder(view)
            R.layout.header_expandable_cell -> HeaderExpandableViewHolder(view)
            R.layout.title_view_cell_large_grey -> TitleViewLargeHolder(view)
            R.layout.selectable_view_cell_grey -> SelectableViewHolder(view)
            else -> {Log.e("Error ","~~~~~~~~~!!! id:$viewType ViewHolder Not assigned !!!~~~~~~"); TitleViewHolder(view)}
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if(holder is ViewHolderable) {
            holder.configure(mItems[position],holder.itemView.context)
        }

    }
    override fun getItemViewType(position: Int): Int {
        return mItems[position].getViewType()
    }
    override fun getItemCount() = mItems.size
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        Log.d("--RxItem..Adapter61- ", "~~~ondetached~~~ => " )
        disposables.clear()
    }


}