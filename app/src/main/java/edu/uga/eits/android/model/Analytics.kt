package edu.uga.eits.android.model

import android.util.Log
import com.google.android.gms.analytics.HitBuilders
import edu.uga.eits.android.App
import edu.uga.eits.android.extensions.getArgsMap


class Analytics{

    fun sendScreen(name:String){

        val mTracker = App.sTracker

        Log.i("analytics", "Setting screen name: " + name)
        mTracker.setScreenName("Image~" + name)
        mTracker.send(HitBuilders.ScreenViewBuilder().build())

    }

    fun sendEvent(category:String,action:String){

        val mTracker = App.sTracker
        Log.i("analytics","sending event "+ action)
        mTracker.send(HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build())

    }

}