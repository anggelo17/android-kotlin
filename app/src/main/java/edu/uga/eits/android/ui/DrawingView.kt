package edu.uga.eits.android.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import edu.uga.eits.android.extensions.drawFlatMapBuses

/**
 * Created by Luis on 9/13/17.
 */

class DrawingView(context: Context, attrs: AttributeSet?) : View(context,attrs){
    constructor(context: Context) : this(context, null)
    var busLocations : List<Float> = listOf()
    var showBus : Int = 10
    set(value){
         if(value < 2){field = value; invalidate()}
    }
    val showBusAnimator = ValueAnimator.ofInt(10,0,10).apply { duration = 5000; }
    init {}
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showBusAnimator.apply{addUpdateListener  { showBus = it.animatedValue as Int };start()}
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        showBusAnimator.apply { cancel();removeAllUpdateListeners() } // because of reuse, this should not be called
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawFlatMapBuses(busLocations,width.toFloat(),height.toFloat(),showBus>0)
        //!!Do not put,  invalidate() // Never put invalidate inside onDraw
    }
}