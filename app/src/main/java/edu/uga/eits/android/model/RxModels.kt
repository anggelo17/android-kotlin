package edu.uga.eits.android.model

import com.jakewharton.rx.replayingShare
import edu.uga.eits.android.extensions.onComputation
import edu.uga.eits.android.extensions.onIO
import edu.uga.eits.android.extensions.reactTo
import edu.uga.eits.android.extensions.throttle
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.set


open class RxEntity<T>(){
    val disposable = CompositeDisposable()
    val flagChangesSubject = BehaviorSubject.createDefault<Date>(Date(0))
    var focusedId = ""
    fun setFocus(id:String) {if(id.isNotBlank()){focusedId = id; flagChanges()}}
    fun clearFocus() { focusedId = ""}
    open fun pull() : Observable<List<T>> = Observable.just(emptyList())
    open fun getAll(): Flowable<List<T>> = pull().distinctUntilChanged()
            .reactTo(flagChangesSubject.throttle(200))
            .toFlowable(BackpressureStrategy.LATEST)
            .replayingShare()

    fun getAll(predicate:(item :T) -> Boolean) : Flowable<List<T>> = getAll().map{it.filter(predicate)}
    fun get(predicate:(item :T) -> Boolean) : Flowable<T> = getAll(predicate).flatMap {it.firstOrNull()?.let{Flowable.just(it)} ?: Flowable.never() }

    fun <K>getMap(predicate:(item :T) -> Boolean,keySelector:(T)-> K)= getAll(predicate).map{it.associateBy(keySelector,{it})}
    fun flagChanges() = flagChangesSubject.onNext(Date())
    companion object {
        var focusedUId = ""
        fun clearFocusUId(){ focusedUId = ""}
    }
}
open class RxRealtimeEntity<T>() : RxEntity<T>(){
    var cache :MutableMap<String,Flowable<List<T>>>  = mutableMapOf()

    open fun pull(id:String) : Observable<List<T>> = pull()

    open val fetchInterval : Long = 60L
    override fun getAll() : Flowable<List<T>> = id()
    open fun id(id:String = "") : Flowable<List<T>> {
        val rx = cache[id] ?: Observable.interval(fetchInterval,TimeUnit.SECONDS).startWith(-1)
                .switchMap { pull(id).onIO().onErrorResumeNext(Observable.just(emptyList()))}//.filterError()
                .throttle(500)
                .reactTo(flagChangesSubject.throttle(200))
                .toFlowable(BackpressureStrategy.LATEST)
                .onComputation()
                .replayingShare()
                .startWith(emptyList<T>())
                //.share()
        if(cache[id] == null)cache[id] = rx
        return rx
    }

}
enum class Relations{
    StopsInRoute,RoutesInStop;
    fun id(keyId:String) = RelationMapper.maps[this]?.get(keyId) ?: emptyList()
    fun ids(keyIds:Set<String>) = keyIds.map{RelationMapper.maps[this]?.get(it) ?: emptyList()}.flatten().distinct()
}
object RelationMapper{
    var maps : Map<Relations,Map<String,List<String>>> = emptyMap()
    fun get(relation:Relations,id:String) = maps[relation]?.get(id) ?: emptyList()
    fun set(relation: Relations,data : Map<String,List<String>>){
        maps = maps.plus(mapOf(Pair(relation, data)))
    }
}
