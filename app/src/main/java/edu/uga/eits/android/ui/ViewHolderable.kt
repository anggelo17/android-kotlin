package edu.uga.eits.android.ui

import android.content.Context
import edu.uga.eits.android.model.CustomCellItem


/**
 * Created by Luis on 6/2/17.
 * Interface to use configure inside RxItemListViewAdapter
 */
interface ViewHolderable{
    fun configure(item: CustomCellItem,context:Context)
}