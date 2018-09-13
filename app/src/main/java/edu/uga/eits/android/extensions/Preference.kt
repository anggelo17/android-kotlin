package edu.uga.eits.android.extensions

import android.content.Context
import android.content.SharedPreferences
import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import edu.uga.eits.android.App
import edu.uga.eits.android.model.On
import edu.uga.eits.android.model.UID

/**
 * Created by Luis on 10/10/17.
 */
class KPreferences(val context: Context,val gson: Gson = Gson()){
    val FILE_NAME = "edu.uga.eits.android.preferences"
    val preferences : SharedPreferences = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)
    fun setString(key: String, value:String) =
            preferences.edit().apply{ putString(key,value);apply() }
    fun set( value:Set<String>, on :On) =
        preferences.edit().apply{ putStringSet(on.stringValue,value);apply() }
    fun getString(key: String) = preferences.getString(key,"")
    fun get(on: On) : Set<String> = preferences.getStringSet(on.stringValue, setOf()).toSet()
    fun getRecent(on: On) : List<String> = preferences.getString(on.recentStringValue,"[]").toList()
    fun addToRecent(id:UID,on: On) = listOf(on,On.GlobalRecents)
            .map{ target -> //store to Both recent and GlobalRecent
                getRecent(target).filter{it!=id}
                        .let{if(it.count() >= target.recentListSize)it.dropLast(1)else it}
                        .let{ listOf(id).plus(it).toString() }
                        .let{ setString(target.recentStringValue,it)}
            }
    fun contains(id:UID,on:On) = get(on).contains(id)
    fun setSelected(id:UID,value:Boolean,on: On) {
        get(on).let{ if(value)it.plus(setOf(id))else it.minus(id) }
                .let{ set(it, on) }
        if(value)addToRecent(id,on) //add to recent
    }

    inline fun <reified T: Any> set(key: String, obj:T) = setString(key,gson.toJson(obj))
    inline fun <reified T: Any> get(key:String) : T?{
        return gson.fromJsonOrNull<T>(getString(key))
    }
    inline fun <reified T: Any> toJson(obj:T) = gson.toJson(obj)
    inline  fun <reified T: Any> fromJson(str:String) : T?{
        return gson.fromJsonOrNull<T>(str)
    }
}

/* Kpreferences Extenstions*/
inline fun <reified T: Any> Gson.fromJsonOrNull(json: String): T? = try{fromJson(json, typeToken<T>())}catch(t:Throwable){null}
fun Any.toJsonString() = App.preferences.toJson(this)
inline  fun <reified T: Any> String.fromJson() = App.preferences.fromJson<T>(this)