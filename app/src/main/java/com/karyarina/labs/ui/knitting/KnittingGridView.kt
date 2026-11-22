package com.karyarina.labs.ui.knitting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.karyarina.labs.R
import kotlin.math.roundToInt

class KnittingGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private var rowCount = 0
    private var columnCount = 0
    private var checkedRows: Set<Int> = emptySet()
    private var currentRow = 1
    private var reversed = false
    private var showRowNumbers = true
    private var showGridLines = true

    var onRowToggle: ((Int) -> Unit)? = null

    private var baseCellSize = dpToPx(20f)
    private val checkboxColumnWidth = dpToPx(30f)
    private val contentPadding = dpToPx(16f)

    private var scaleFactor = 1f
    private var translateX = 0f
    private var translateY = 0f

    private fun dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    private val cellPaint = Paint()
    private val checkboxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.5f)
        color = context.getColor(R.color.stitch_on_surface_variant)
    }
    private val checkedBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.stitch_primary)
    }
    private val checkmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2f)
        color = context.getColor(R.color.stitch_on_primary)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val overlayPaint = Paint().apply { color = context.getColor(R.color.stitch_completed_overlay) }
    private val strikePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dpToPx(1.5f)
        color = context.getColor(R.color.stitch_on_surface)
    }
    private val currentRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2f)
        color = context.getColor(R.color.stitch_secondary)
    }
    private val rowNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.stitch_on_surface)
        textSize = spToPx(10f)
        typeface = ResourcesCompat.getFont(context, R.font.stitch_sans)
    }
    private val currentRowNumberPaint = Paint(rowNumberPaint).apply {
        color = context.getColor(R.color.stitch_secondary)
    }
    private val gridLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(0.5f)
        color = context.getColor(R.color.stitch_outline)
    }

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val oldScale = scaleFactor
                scaleFactor = (scaleFactor * detector.scaleFactor).coerceIn(0.5f, 8f)
                val change = scaleFactor / oldScale
                translateX = detector.focusX - contentPadding -
                    (detector.focusX - contentPadding - translateX) * change
                translateY = detector.focusY - contentPadding -
                    (detector.focusY - contentPadding - translateY) * change
                invalidate()
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

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
        fitGrid()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitGrid()
    }

    private fun fitGrid() {
        if (columnCount == 0 || rowCount == 0 || width == 0 || height == 0) return
        val numberWidth = if (showRowNumbers) dpToPx(24f) else 0f
        val availableWidth = width - contentPadding * 2 - checkboxColumnWidth - numberWidth
        val availableHeight = height - contentPadding * 2
        if (availableWidth <= 0 || availableHeight <= 0) return
        baseCellSize = minOf(availableWidth / columnCount, availableHeight / rowCount)
        rowNumberPaint.textSize = minOf(spToPx(10f), baseCellSize * 0.8f)
        currentRowNumberPaint.textSize = rowNumberPaint.textSize
        checkmarkPaint.strokeWidth = minOf(dpToPx(2f), baseCellSize * 0.1f)
        scaleFactor = 1f
        translateX = 0f
        translateY = 0f
    }

    fun setCheckedRows(checkedRows: Set<Int>) {
        this.checkedRows = checkedRows
        invalidate()
    }

    fun setCurrentRow(row: Int) {
        currentRow = row
        invalidate()
    }

    fun setReversed(reversed: Boolean) {
        this.reversed = reversed
        invalidate()
    }

    fun setShowRowNumbers(show: Boolean) {
        showRowNumbers = show
        invalidate()
    }

    fun setShowGridLines(show: Boolean) {
        showGridLines = show
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(
            event.actionMasked != MotionEvent.ACTION_UP &&
                event.actionMasked != MotionEvent.ACTION_CANCEL
        )
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun handleTap(viewX: Float, viewY: Float) {
        val contentX = (viewX - contentPadding - translateX) / scaleFactor
        val contentY = (viewY - contentPadding - translateY) / scaleFactor
        if (contentX < 0f || contentX > checkboxColumnWidth || contentY < 0f) return
        val physicalRowNumber = (contentY / baseCellSize).toInt() + 1
        if (physicalRowNumber in 1..rowCount) {
            onRowToggle?.invoke(physicalRowNumber)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        canvas.save()
        canvas.translate(contentPadding + translateX, contentPadding + translateY)
        canvas.scale(scaleFactor, scaleFactor)

        for (row in 0 until rowCount) {
            val physicalRowNumber = row + 1
            val rowNumber = if (reversed) rowCount - row else physicalRowNumber
            val top = row * baseCellSize
            val bottom = top + baseCellSize
            val isChecked = physicalRowNumber in checkedRows

            val boxSize = minOf(dpToPx(14f), baseCellSize * 0.75f)
            val boxTop = top + (baseCellSize - boxSize) / 2
            val boxRect = RectF(dpToPx(4f), boxTop, dpToPx(4f) + boxSize, boxTop + boxSize)
            if (isChecked) {
                canvas.drawRect(boxRect, checkedBoxPaint)
                canvas.drawLine(
                    boxRect.left + boxSize * 0.2f, boxRect.centerY(),
                    boxRect.left + boxSize * 0.43f, boxRect.top + boxSize * 0.75f,
                    checkmarkPaint
                )
                canvas.drawLine(
                    boxRect.left + boxSize * 0.43f, boxRect.top + boxSize * 0.75f,
                    boxRect.right - boxSize * 0.15f, boxRect.top + boxSize * 0.25f,
                    checkmarkPaint
                )
            } else {
                val paint = if (rowNumber == currentRow) currentRowPaint else checkboxPaint
                canvas.drawRect(boxRect, paint)
            }

            val cellTop = top.roundToInt().toFloat()
            val cellBottom = bottom.roundToInt().toFloat()
            val imageRow = row
            for (col in 0 until columnCount) {
                val cellLeft = (checkboxColumnWidth + col * baseCellSize).roundToInt().toFloat()
                val cellRight = (checkboxColumnWidth + (col + 1) * baseCellSize).roundToInt().toFloat()
                cellPaint.color = bmp.getPixel(col, imageRow)
                canvas.drawRect(cellLeft, cellTop, cellRight, cellBottom, cellPaint)
                if (showGridLines) {
                    canvas.drawRect(cellLeft, cellTop, cellRight, cellBottom, gridLinePaint)
                }
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

            if (showRowNumbers) {
                val paint = if (rowNumber == currentRow) currentRowNumberPaint else rowNumberPaint
                val baseline = top + baseCellSize / 2 - (paint.ascent() + paint.descent()) / 2
                canvas.drawText(rowNumber.toString(), gridRight + dpToPx(6f), baseline, paint)
            }
        }

        canvas.restore()
    }
}
