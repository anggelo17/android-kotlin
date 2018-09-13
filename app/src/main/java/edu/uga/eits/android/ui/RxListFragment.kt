package edu.uga.eits.android.ui

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChanges
import com.jakewharton.rxbinding2.view.clicks
import edu.uga.eits.android.BaseFragment
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.*
import edu.uga.eits.android.model.CellStyle
import edu.uga.eits.android.model.CustomCellItem
import edu.uga.eits.android.viewmodel.ListViewModels
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.list_fragment.*

/**
 * Created by Luis on 6/12/17.
 *
 */

class RxListFragment() : BaseFragment()  {
    val listDataSubject = ReplaySubject.create<List<CustomCellItem>>()

    val rx_itemSelected = PublishSubject.create<Any>()
    var scrolledToFocusId = false
    private var argsMap : ArgMap = mapOf(Keys.LIST_ID to ListViewModels.EmptyList.toString())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getArgsMap()?.let{argsMap = it}
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        disposables = CompositeDisposable()
        return container?.inflate(R.layout.list_fragment)
    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val (list,cells) = ListViewModels.getViewModel(argsMap,context)
        searchbox.visibility = list.rxSearchText?.let{rxSearchText ->
            searchbox.queryTextChanges().map{it.toString()}.throttle(300).doOnNext{
                Log.d("--RxListFragment51--","~~~input~~~ => ${it}" )
            }.subscribe(rxSearchText::onNext).addTo(disposables)
            searchbox.clicks().subscribe{searchbox.isIconified = false }.addTo(disposables)
            View.VISIBLE
        } ?: View.GONE
        rx_itemSelected.subscribe{list.onClickItem(it,context)}.addTo(disposables)
        cells.subscribe(listDataSubject::onNext).addTo(disposables)
        Log.d("--RxListFragment45--","~~~argsMap~~~ => ${argsMap}" )

        val rxAdapter = RxItemListViewAdapter(rx_itemSelected)
                .apply{listDataSubject.throttle(300).onMainThread()
                        .subscribeWithPausing(this@RxListFragment){
                            this.updateItems(it)
                            val index = it.indexOfLast{(it as? DrawableViewItem)?.style == CellStyle.FOCUSED}
                            Log.d("--RxListFragment53--","~~~index~~~ => ${index}" )
                            rxlist.layoutManager?.let{it as? LinearLayoutManager}?.apply{
                                if(index>0 && !scrolledToFocusId){  scrollToPositionWithOffset(index,height/2);scrolledToFocusId = true  }
                            }
                        }}
        with(rxlist){
            setHasFixedSize(true)
            adapter = rxAdapter
            LinearLayoutManager(context).let{
                layoutManager = it
                if(list.listDiverOn){addItemDecoration(DividerItemDecoration(context,it.orientation))}
                //val focusID = argsMap.get(Keys.FOCUS_ID)?.takeIf{it?.isNotBlank()}
                //it.scrollToPositionWithOffset(10,550)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
        rxlist.adapter = null
    }
    companion object {
        fun newInstance(argsMap : ArgMap?): RxListFragment{
            val args = Bundle().putArgsMap(argsMap ?: ListViewModels.EmptyList.toArgsMap())
            return RxListFragment().let{it.arguments = args;it}
        }
    }

}