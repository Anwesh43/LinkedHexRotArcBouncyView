package com.anwesh.uiprojects.hexrotarcbouncyview

/**
 * Created by anweshmishra on 11/02/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val arcs : Int = 6
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#4527A0")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBouncyRotArc(i : Int, scale : Float, size : Float, paint : Paint) {
    val deg : Float = 360f / arcs
    val sf : Float = scale.sinify().divideScale(i, arcs)
    save()
    rotate(deg * i + deg * sf)
    drawArc(RectF(-size, -size, size, size), -deg / 4, (deg / 2), false, paint)
    restore()
}

fun Canvas.drawBouncyRotArcs(scale : Float, size : Float, paint : Paint) {
    val scDiv : Double = 1.0 / arcs
    val k : Int = Math.floor(scale.sinify() / scDiv).toInt()
    for (j in 0..k) {
        drawBouncyRotArc(j, scale, size, paint)
    }
}

fun Canvas.drawHRABNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    paint.style = Paint.Style.STROKE
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    val size : Float = gap / sizeFactor
    save()
    translate(w / 2, gap * (i + 1))
    drawBouncyRotArcs(scale, size, paint)
    restore()
}
