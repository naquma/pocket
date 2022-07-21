package com.moki.pocketgit.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import org.eclipse.jgit.revplot.PlotLane

class PlotLaneView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    private var mPassing: MutableSet<Int> = hashSetOf()
    private val mChildren: MutableList<Int> = arrayListOf()
    private val mParent: MutableList<Int> = arrayListOf()

    private var mLane = 0

    companion object {
        const val RADIUS: Float = 3.0f
        private val COLORS: Array<Int> = arrayOf(
            Color.parseColor("#0099CC"),
            Color.parseColor("#9933CC"),
            Color.parseColor("#669900"),
            Color.parseColor("#FF8800"),
            Color.parseColor("#CC0000")
        )
        private val PAINT = Paint().apply { style = Paint.Style.FILL }
    }

    override fun onDraw(canvas: Canvas?) {
        val flag = TypedValue.COMPLEX_UNIT_DIP;
        val dimension = TypedValue.applyDimension(flag, RADIUS, resources.displayMetrics)
        PAINT.strokeWidth = dimension
        val dimension2 =
            TypedValue.applyDimension(flag, (mLane + 1) * RADIUS * 2, resources.displayMetrics)
        val mHeight: Float = height / 2.0f
        for (num in mChildren) {
            var shadowNum = num
            if (mPassing.contains(shadowNum)) {
                shadowNum = mLane
            }
            PAINT.color = COLORS[shadowNum % 5]
            canvas?.drawLine(
                dimension2,
                mHeight,
                TypedValue.applyDimension(
                    flag,
                    (shadowNum + 1) * RADIUS * 2,
                    resources.displayMetrics
                ),
                0.0f,
                PAINT
            )
        }
        for (num in mParent) {
            var shadowNum = num
            if (mPassing.contains(shadowNum)) {
                shadowNum = mLane
            }
            PAINT.color = COLORS[shadowNum % 5]
            canvas?.drawLine(
                dimension2,
                mHeight,
                TypedValue.applyDimension(
                    flag,
                    (shadowNum + 1) * RADIUS * 2,
                    resources.displayMetrics
                ),
                height.toFloat(),
                PAINT
            )
        }
        for (num in mPassing) {
            PAINT.color = COLORS[num % 5]
            val dimension3 =
                TypedValue.applyDimension(flag, (num + 1) * RADIUS * 2, resources.displayMetrics)
            canvas?.drawLine(dimension3, 0.0f, dimension3, height.toFloat(), PAINT)
        }
        PAINT.color = COLORS[mLane % 5]
        canvas?.drawCircle(dimension2, mHeight, dimension, PAINT)
    }

    fun setLane(lane: Int) {
        mLane = lane
    }

    fun addChildLane(child: Int) {
        mChildren.add(0, child)
    }

    fun addParentLane(parent: Int) {
        mParent.add(0, parent)
    }

    fun clearLanes() {
        mChildren.clear()
        mParent.clear()
    }

    fun setPassing(plots: ArrayList<PlotLane>) {
        mPassing.clear()
        plots.forEach { mPassing.add(it.position) }
    }
}