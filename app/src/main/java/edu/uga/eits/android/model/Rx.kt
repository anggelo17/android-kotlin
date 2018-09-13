package edu.uga.eits.android.model

import edu.uga.eits.android.App
import edu.uga.eits.android.extensions.onIO
import edu.uga.eits.android.extensions.reactToUserLocation
import edu.uga.eits.android.extensions.shareReplay
import edu.uga.eits.android.extensions.withKey
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.rxkotlin.zip



object Rx{
    val UGAStops = BusStop.getAll { !it.isAthensBus }.reactToUserLocation().shareReplay()





}