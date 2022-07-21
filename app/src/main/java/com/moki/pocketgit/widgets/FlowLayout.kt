package com.moki.pocketgit.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

@SuppressLint("ResourceType")
class FlowLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    companion object {
        const val DEFAULT_HORIZONTAL_SPACING = 5
        const val DEFAULT_VERTICAL_SPACING = 5
    }

    private val horizontalSpacing: Int
    private val verticalSpacing: Int
    private var currentRows: List<RowMeasurement> = emptyList()

    private val layoutChildren: List<View> =
        IntRange(0, childCount).map { getChildAt(it) }.filter { it.visibility != View.GONE }

    private val horizontalPadding = paddingLeft + paddingRight

    private val verticalPadding = paddingTop + paddingBottom

    init {
        val typed = context.obtainStyledAttributes(
            attrs,
            com.google.android.material.R.styleable.FlowLayout
        )
        horizontalSpacing = typed.getDimensionPixelSize(0, DEFAULT_HORIZONTAL_SPACING)
        verticalSpacing = typed.getDimensionPixelSize(1, DEFAULT_VERTICAL_SPACING)
        typed.recycle()
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val mMeasuredWidth = measuredWidth - paddingRight
        var mPaddingLeft = paddingLeft
        var mPaddingTop = paddingTop
        val it = currentRows.iterator()

        var rowMeasurement = it.next()
        for (view in layoutChildren) {
            val subMeasureWidth = view.measuredWidth
            val subMeasurementHeight = view.measuredHeight
            if (mPaddingLeft + subMeasureWidth > mMeasuredWidth) {
                mPaddingLeft = paddingLeft
                mPaddingTop = rowMeasurement.height + verticalSpacing

                if (it.hasNext()) rowMeasurement = it.next()
            }

            view.layout(
                mPaddingLeft,
                mPaddingTop,
                mPaddingLeft + subMeasureWidth,
                mPaddingTop + subMeasurementHeight
            )
            mPaddingLeft += horizontalSpacing + subMeasureWidth
        }
    }

    private fun createChildMeasureSpec(width: Int, size: Int, mode: Int): Int {
        val flags = 0x40000000
        return when (width) {
            1 -> MeasureSpec.makeMeasureSpec(size, flags)
            2 -> MeasureSpec.makeMeasureSpec(size, if (mode == 0) 0 else Int.MIN_VALUE)
            else -> MeasureSpec.makeMeasureSpec(width, flags)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - horizontalPadding
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) - verticalPadding
        val rows = mutableListOf<RowMeasurement>()
        var rowMeasurement = RowMeasurement(widthSize, widthMode, horizontalSpacing)
        rows.add(rowMeasurement)
        for (view in layoutChildren) {
            val params = view.layoutParams
            view.measure(
                createChildMeasureSpec(params.width, widthSize, widthMode),
                createChildMeasureSpec(params.height, heightSize, heightMode)
            )
            val mMeasuredWidth = view.measuredWidth
            val mMeasuredHeight = view.measuredHeight
            if (rowMeasurement.wouldExceedMax(mMeasuredWidth)) {
                rowMeasurement = RowMeasurement(widthSize, widthMode, horizontalSpacing)
                rows.add(rowMeasurement)
            }
            rowMeasurement.addChildDimensions(mMeasuredWidth, mMeasuredHeight)
        }

        var mWidth = 0
        var mHeight = 0
        for (i in 0..rows.size) {
            val mRow = rows[i]
            mHeight += mRow.height
            if (i < rows.size - 1) {
                mHeight += verticalSpacing
            }
            mWidth = max(mWidth, mRow.width)
        }
        setMeasuredDimension(
            if (widthMode == 1073741824) MeasureSpec.getSize(widthMeasureSpec) else horizontalPadding + mWidth,
            if (heightMode == 1073741824) MeasureSpec.getSize(heightMeasureSpec) else verticalPadding + mHeight
        )
        currentRows = rows
    }

    private class RowMeasurement(
        private val maxWidth: Int,
        private val widthMode: Int,
        private val spacing: Int,
    ) {
        var height: Int = 0
            private set
        var width: Int = 0
            private set

        private fun getNewWidth(max: Int): Int {
            return if (width == 0) max else max + width + spacing
        }

        fun addChildDimensions(w: Int, h: Int) {
            width = getNewWidth(w)
            height = max(height, h)
        }

        fun wouldExceedMax(max: Int): Boolean {
            return widthMode != 0 && getNewWidth(max) > maxWidth
        }
    }
}