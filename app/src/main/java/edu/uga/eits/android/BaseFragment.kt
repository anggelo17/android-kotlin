package edu.uga.eits.android

import android.support.v4.app.Fragment
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Luis on 6/15/17.
 */
abstract class BaseFragment : Fragment(){
    var disposables = CompositeDisposable()
    var resumableDisposables = CompositeDisposable()
    val lifecycleSubject = BehaviorSubject.create<LifecycleEvents>()

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
    override fun onPause() {
        super.onPause()
        resumableDisposables.dispose()
        lifecycleSubject.onNext(LifecycleEvents.PAUSE)
    }
    override fun onResume() {
        super.onResume()
        resumableDisposables = CompositeDisposable()
        lifecycleSubject.onNext(LifecycleEvents.RESUME)
    }
    fun<T> resumable(flowable: Flowable<T>, subscribeWork: (T) -> Unit){
        lifecycleSubject.filter{it==LifecycleEvents.RESUME}.subscribe{
            resumableDisposables = CompositeDisposable()
            flowable.subscribe{
                subscribeWork(it)
            }.addTo(resumableDisposables)
        }.addTo(disposables)
    }
}