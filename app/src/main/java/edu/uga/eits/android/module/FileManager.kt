package edu.uga.eits.android.module

import android.content.Context
import android.support.annotation.AnyRes
import android.util.Log
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.isMainThread
import edu.uga.eits.android.extensions.onIO
import edu.uga.eits.android.model.Dataset
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.ReplaySubject
import java.io.BufferedReader
import java.io.File


class FileManager(val context: Context,val gson:Gson){
    var dataset = ReplaySubject.create<Dataset>()
    val disposables = CompositeDisposable()
    init {
        Flowable.just(Unit).onIO()
               .map{getFromResource(R.raw.dataset_v3_2) ?: Dataset()}.subscribe(dataset::onNext).addTo(disposables)
        dataset.subscribe{ Log.d("0029", "~~~~dataset mod date~~~~ ====> " + it.toString())}.addTo(disposables)
    }
    inline fun <reified T: Any> getFromResource(resourceId:Int) : T? {
        return readFromResource(resourceId)?.let{
            gson.fromJson<T>(it)
        }
    }
    fun readFromResource(@AnyRes fileName:Int?) : String? {
        return context.resources.openRawResource(fileName ?: return null)
                .bufferedReader().use(BufferedReader::readText)
    }
    fun writeToFile(fileName:String,text:String) : Boolean {
        val filePath = context.filesDir.toString()+"/$fileName"
        try {
            File(filePath).writeText(text)
            Log.d("~~~Text wrote on ", filePath)
            Log.d("FileManager50", "~~~~text being written~~~~ ====> " + text.toString())
            "writeToFile".isMainThread()
            return true
        }catch (e : Exception){
            Log.e("~~~Error writing file", filePath.toString())
            return false
        }
    }
    fun readFromFile(fileName:String,rawName:Int):String?{
        val filePath = context.filesDir.toString()+"/$fileName"
        Log.d("~~~reading~~~", " = " + filePath)
        "readFromFile".isMainThread()
        val fromFile : String? = try{
            File(filePath).readText()
        }catch (e:Exception){Log.d("--FileManager65--","~~~read file exception~~~ => ${e}" );return null}
        //Log.d("FileManager62", "~~~~fromFile~~~~ ====> ${fromFile}")
        return fromFile ?: readFromResource(rawName)
    }
}