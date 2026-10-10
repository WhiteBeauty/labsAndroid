package com.karyarina.labs.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Рисует схему вязания на Canvas: слева флажки для отметки рядов,
 * в центре сама сетка (один пиксель картинки = одна клетка), справа номера рядов.
 * Поддерживает увеличение щипком и перемещение пальцем.
 */
class KnittingGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private var rowCount = 0
    private var columnCount = 0
    private var checkedRows: Set<Int> = emptySet()
    private var currentRow = 1

    var onRowToggle: ((Int) -> Unit)? = null

    private val baseCellSize = 40f
    private val checkboxColumnWidth = baseCellSize

    private var scaleFactor = 1f
    private var translateX = 0f
    private var translateY = 0f

    private val cellPaint = Paint()
    private val checkboxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.DKGRAY
    }
    private val checkmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.DKGRAY
    }
    private val overlayPaint = Paint().apply { color = Color.argb(140, 255, 255, 255) }
    private val strikePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3f
        color = Color.DKGRAY
    }
    private val currentRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.parseColor("#D9A441")
    }
    private val rowNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 28f
    }

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor = (scaleFactor * detector.scaleFactor).coerceIn(0.5f, 5f)
                invalidate()
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                translateX -= distanceX
                translateY -= distanceY
                invalidate()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }
        }
    )

    fun setData(bitmap: Bitmap?, rowCount: Int, checkedRows: Set<Int>) {
        this.bitmap = bitmap
        this.rowCount = rowCount
        this.columnCount = bitmap?.width ?: 0
        this.checkedRows = checkedRows
        invalidate()
    }

    fun setCheckedRows(checkedRows: Set<Int>) {
        this.checkedRows = checkedRows
        invalidate()
    }

    fun setCurrentRow(row: Int) {
        currentRow = row
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun handleTap(viewX: Float, viewY: Float) {
        val contentX = (viewX - translateX) / scaleFactor
        val contentY = (viewY - translateY) / scaleFactor
        if (contentX < 0f || contentX > checkboxColumnWidth) return
        val row = (contentY / baseCellSize).toInt() + 1
        if (row in 1..rowCount) {
            onRowToggle?.invoke(row)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)

        for (row in 0 until rowCount) {
            val rowNumber = row + 1
            val top = row * baseCellSize
            val bottom = top + baseCellSize
            val isChecked = rowNumber in checkedRows

            val boxPad = baseCellSize * 0.2f
            val boxRect = RectF(boxPad, top + boxPad, checkboxColumnWidth - boxPad, bottom - boxPad)
            canvas.drawRect(boxRect, checkboxPaint)
            if (isChecked) {
                canvas.drawLine(
                    boxRect.left, boxRect.top + boxRect.height() / 2,
                    boxRect.left + boxRect.width() * 0.4f, boxRect.bottom,
                    checkmarkPaint
                )
                canvas.drawLine(
                    boxRect.left + boxRect.width() * 0.4f, boxRect.bottom,
                    boxRect.right, boxRect.top,
                    checkmarkPaint
                )
            }

            for (col in 0 until columnCount) {
                val left = checkboxColumnWidth + col * baseCellSize
                cellPaint.color = bmp.getPixel(col, row)
                canvas.drawRect(left, top, left + baseCellSize, bottom, cellPaint)
            }

            val gridRight = checkboxColumnWidth + columnCount * baseCellSize
            if (isChecked) {
                canvas.drawRect(checkboxColumnWidth, top, gridRight, bottom, overlayPaint)
                canvas.drawLine(
                    checkboxColumnWidth, top + baseCellSize / 2,
                    gridRight, top + baseCellSize / 2,
                    strikePaint
                )
            }
            if (rowNumber == currentRow) {
                canvas.drawRect(checkboxColumnWidth, top, gridRight, bottom, currentRowPaint)
            }

            canvas.drawText(rowNumber.toString(), gridRight + 10f, top + baseCellSize * 0.7f, rowNumberPaint)
        }

        canvas.restore()
    }
}
