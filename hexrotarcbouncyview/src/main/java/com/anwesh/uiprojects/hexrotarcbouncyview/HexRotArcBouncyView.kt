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
val delay : Long = 20

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

class HexRotArcBouncyView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class HRABNode(var i : Int, val state : State = State()) {

        private var next : HRABNode? = null
        private var prev : HRABNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = HRABNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawHRABNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : HRABNode {
            var curr : HRABNode? = next
            if (dir == -1) {
                curr = prev
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class HexBouncyRotArc(var i : Int) {

        private val root : HRABNode = HRABNode(0)
        private var curr : HRABNode = root
        private var dir : Int =1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : HexRotArcBouncyView) {

        private val animator : Animator = Animator(view)
        private val hrab : HexBouncyRotArc = HexBouncyRotArc(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            hrab.draw(canvas, paint)
            animator.animate {
                hrab.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            hrab.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : HexRotArcBouncyView {
            val view : HexRotArcBouncyView = HexRotArcBouncyView(activity)
            activity.setContentView(view)
            return view
        }
    }
}