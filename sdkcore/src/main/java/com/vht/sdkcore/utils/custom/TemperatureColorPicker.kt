package com.vht.sdkcore.utils.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class TemperatureColorPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val size: Float
        get() = min(measuredWidth.toFloat(), measuredHeight.toFloat())
    private val commonStrokeWidth
        get() = size * 0.008F
    private val dimmerSize
        get() = size * 0.076F
    private val guidelineSize
        get() = size * 0.064F
    private val temperaturePointerRadius
        get() = size * 0.04F

    var maxDimmer = 65535
        private set
    var minTemp = 0
        private set
    var maxTemp = 65535
        private set
    val rangeTemperature
        get() = maxTemp - minTemp

    private var dimmer = maxDimmer
        set(value) {
            field = if (value < 0) 0
            else if (value > maxDimmer) maxDimmer
            else value

            invokeDimmerChanged(false)
        }
    private var temperaturePointerX = 0F
    private var temperature = rangeTemperature
        set(value) {
            field = if (value < 0) 0
            else if (value > rangeTemperature) rangeTemperature
            else value

            invokeTemperatureChanged(false)
        }

    private val backgroundDimmerDrawable = GradientDrawable()
    private val backgroundDimmerDrawable2 = GradientDrawable()

    private val progressDimmerPaint = Paint().apply {
        isAntiAlias = true
    }

    private val dimmerPointerFillPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#EC8122")
        style = Paint.Style.FILL
    }

    private val dimmerPointerStrokePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val centerCirclePaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#F5F5F5")
    }

    private val innerPointerFillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val innerPointerStrokePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val temperatureColors = intArrayOf(
        Color.parseColor("#FFA957"),
        Color.parseColor("#FFC489"),
        Color.parseColor("#FFD1A3"),
        Color.parseColor("#FFDDBE"),
        Color.parseColor("#FFE4CE"),
        Color.parseColor("#FFECE0"),
        Color.parseColor("#FFF3EF"),
        Color.parseColor("#FFF9FD")
    )

    private val temperatureGradient = GradientDrawable().apply {
        gradientType = GradientDrawable.LINEAR_GRADIENT
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
        shape = GradientDrawable.OVAL
        colors = temperatureColors
    }

    private var colorWheelRadius = 0F

    private val colorWheelStatusOffPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#CECECE")
    }

    private enum class TouchState {
        NONE, DIMMER, TEMPERATURE
    }

    private var touchState: TouchState = TouchState.NONE

    enum class State {
        ON, OFF
    }

    var state: State = State.ON
        set(value) {
            field = value
            onStateUpdated(value)
        }

    private var onDimmerChanged: ((Boolean, Int) -> Unit)? = null
    private var onTemperatureChanged: ((Boolean, Int) -> Unit)? = null

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)
        )
        val backgroundColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#DEDEDE")
        )
        backgroundDimmerDrawable.apply {
            setBounds(
                (0 + commonStrokeWidth).toInt(),
                (0 + commonStrokeWidth).toInt(),
                (size.toInt() - commonStrokeWidth).toInt(),
                (size.toInt() - commonStrokeWidth).toInt()
            )
            colors = backgroundColors
            orientation = GradientDrawable.Orientation.TL_BR
            shape = GradientDrawable.OVAL
            gradientType = GradientDrawable.LINEAR_GRADIENT
            setStroke(commonStrokeWidth.toInt() / 2, Color.parseColor("#CDCDCD"))
        }

        backgroundDimmerDrawable2.apply {
            setBounds(
                (0 + dimmerSize + commonStrokeWidth / 2).toInt(),
                (0 + dimmerSize + commonStrokeWidth / 2).toInt(),
                (size.toInt() - dimmerSize - commonStrokeWidth / 2).toInt(),
                (size.toInt() - dimmerSize - commonStrokeWidth / 2).toInt()
            )
            shape = GradientDrawable.OVAL
            setStroke(commonStrokeWidth.toInt(), Color.parseColor("#CDCDCD"))
        }

        val progressColors = intArrayOf(
            Color.parseColor("#FFA800"),
            Color.parseColor("#FFC74A"),
            Color.parseColor("#FDE39C"),
            Color.parseColor("#FFFFFF")
        )
        progressDimmerPaint.shader = SweepGradient(
            size / 2F, size / 2F, progressColors, null
        )
        colorWheelRadius = size / 2F - commonStrokeWidth - dimmerSize - guidelineSize

        dimmerPointerStrokePaint.strokeWidth = commonStrokeWidth
        innerPointerStrokePaint.strokeWidth = commonStrokeWidth
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) return true

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (state == State.OFF) return false

                val touchPoint = PointF(event.x, event.y)
                val distanceToCenter = calculateDistance(touchPoint, PointF(size / 2F, size / 2F))
                touchState = if (distanceToCenter <= colorWheelRadius + temperaturePointerRadius) {
                    TouchState.TEMPERATURE
                } else if (distanceToCenter <= size / 2F) {
                    TouchState.DIMMER
                } else {
                    TouchState.NONE
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (touchState) {
                    TouchState.DIMMER -> {
                        onDraggingDimmer(event.x, event.y)
                    }

                    TouchState.TEMPERATURE -> {
                        onDraggingTemperature(event.x, event.y)
                    }

                    TouchState.NONE -> {

                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when (touchState) {
                    TouchState.DIMMER -> {
                        invokeDimmerChanged(true)
                    }

                    TouchState.TEMPERATURE -> {
                        invokeTemperatureChanged(true)
                    }

                    TouchState.NONE -> {

                    }
                }
                touchState = TouchState.NONE
            }
        }

        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (canvas != null) {
            drawDimmerBackground(canvas)
            drawDimmerProgress(canvas)
            drawCenterCircle(canvas)
            drawDimmerPointer(canvas)
            drawColorWheel(canvas)
            drawTemperaturePointer(canvas)
        }
    }

    private fun drawDimmerBackground(canvas: Canvas) {
        backgroundDimmerDrawable.draw(canvas)
        backgroundDimmerDrawable2.draw(canvas)
    }

    private fun drawDimmerProgress(canvas: Canvas) {
        if (state == State.ON) {
            canvas.save()
            canvas.rotate(270F, size / 2F, size / 2F)
            val degrees = (dimmer.toFloat() / maxDimmer) * 360
            canvas.drawArc(
                0F + commonStrokeWidth - 1,
                0F + commonStrokeWidth - 1,
                size - commonStrokeWidth + 1,
                size - commonStrokeWidth + 1,
                0F,
                degrees,
                true,
                progressDimmerPaint
            )
            canvas.restore()
        }
    }

    private fun drawCenterCircle(canvas: Canvas) {
        val radius = colorWheelRadius + guidelineSize
        canvas.drawCircle(size / 2F, size / 2F, radius, centerCirclePaint)
    }

    private fun drawDimmerPointer(canvas: Canvas) {
        canvas.save()
        val degrees = if (state == State.ON) (dimmer.toFloat() / maxDimmer) * 360 else 0F
        canvas.rotate(degrees, size / 2F, size / 2F)
        val width = size * 0.02F
        val height = dimmerSize + 2 * commonStrokeWidth
        canvas.drawRect(
            size / 2F - width / 2, 0F, size / 2F + width / 2, height, dimmerPointerFillPaint
        )
        canvas.drawRect(
            size / 2F - width / 2, 0F, size / 2F + width / 2, height, dimmerPointerStrokePaint
        )
        canvas.restore()
    }

    private fun drawColorWheel(canvas: Canvas) {
        if (state == State.ON) {
            temperatureGradient.setBounds(
                (size / 2 - colorWheelRadius).toInt(),
                (size / 2 - colorWheelRadius).toInt(),
                (size / 2 + colorWheelRadius).toInt(),
                (size / 2 + colorWheelRadius).toInt()
            )
            temperatureGradient.draw(canvas)
        } else {
            canvas.drawOval(
                size / 2 - colorWheelRadius,
                size / 2 - colorWheelRadius,
                size / 2 + colorWheelRadius,
                size / 2 + colorWheelRadius,
                colorWheelStatusOffPaint
            )
        }
    }

    private fun drawTemperaturePointer(canvas: Canvas) {
        if (state == State.ON) {
            val startPointY = size / 2F - colorWheelRadius
            val cy = (temperature.toFloat() / rangeTemperature) * colorWheelRadius * 2 + startPointY
            val h = max(minOf(abs(size / 2 - cy), colorWheelRadius), 0F)
            val w = sqrt(colorWheelRadius.pow(2) - h.pow(2))
            val minX = size / 2 - w
            val maxX = size / 2 + w
            temperaturePointerX = if (temperaturePointerX < minX) minX
            else if (temperaturePointerX > maxX) maxX
            else temperaturePointerX

            val position = temperature.toFloat() / rangeTemperature
            val red =
                calculateLinearGradientColor(Color.red(temperatureColors[0]), Color.red(temperatureColors[1]), position)
            val green =
                calculateLinearGradientColor(
                    Color.green(temperatureColors[0]),
                    Color.green(temperatureColors[1]),
                    position
                )
            val blue = calculateLinearGradientColor(
                Color.blue(temperatureColors[0]),
                Color.blue(temperatureColors[1]),
                position
            )

            innerPointerFillPaint.color = Color.rgb(red, green, blue)

            canvas.drawCircle(temperaturePointerX, cy, temperaturePointerRadius, innerPointerFillPaint)
            canvas.drawCircle(temperaturePointerX, cy, temperaturePointerRadius, innerPointerStrokePaint)
        }
    }

    private fun onDraggingDimmer(x: Float, y: Float) {
        fun calculateAngle(
            centerPointX: Float,
            centerPointY: Float,
            pointX: Float,
            pointY: Float
        ): Float {
            val dx = pointX - centerPointX
            val dy = pointY - centerPointY
            val rawAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 90F
            return (if (rawAngle <= 0) 360F + rawAngle else rawAngle).toFloat()
        }

        val angle = calculateAngle(size / 2F, size / 2F, x, y)
        var newDimmer = ((angle / 360) * maxDimmer).toInt()

        newDimmer = if (dimmer > maxDimmer * 0.985 && newDimmer < maxDimmer * 0.015) maxDimmer
        else if (dimmer < maxDimmer * 0.015 && newDimmer > maxDimmer * 0.985) 0
        else if (abs(newDimmer - dimmer) > maxDimmer * 0.15) return
        else newDimmer

        dimmer = newDimmer

        invalidate()
    }

    private fun onDraggingTemperature(x: Float, y: Float) {
        val minY = size / 2F - colorWheelRadius
        val maxY = size / 2F + colorWheelRadius

        val touchY = if (y < minY) minY
        else if (y > maxY) maxY
        else y

        temperature = (((touchY - minY) / (colorWheelRadius * 2)) * rangeTemperature).toInt()

        val cy = (temperature.toFloat() / rangeTemperature) * colorWheelRadius * 2 + minY
        val h = max(minOf(abs(size / 2 - cy), colorWheelRadius), 0F)
        val w = sqrt(colorWheelRadius.pow(2) - h.pow(2))
        val minX = size / 2 - w
        val maxX = size / 2 + w
        temperaturePointerX = if (temperaturePointerX < minX) minX
        else if (temperaturePointerX > maxX) maxX
        else x
        invalidate()
    }

    private fun calculateDistance(point1: PointF, point2: PointF): Double {
        return sqrt(
            (point1.x - point2.x).toDouble().pow(2.0) + (point1.y - point2.y).toDouble().pow(2.0)
        )
    }

    fun setOnDimmerChangedListener(action: ((Boolean, Int) -> Unit)) {
        this.onDimmerChanged = action
    }

    fun setOnTemperatureChangedListener(action: ((Boolean, Int) -> Unit)) {
        this.onTemperatureChanged = action
    }

    fun setColor(dimmer: Int, temperature: Int) {
        this.dimmer = dimmer
        this.temperature = temperature - minTemp
        invalidate()
    }

    fun getDimTemperature(): Pair<Int, Int> {
        return dimmer to temperature + minTemp
    }

    fun getDim() = dimmer

    fun setRangeTemp(min: Int, max: Int) {
        minTemp = min
        maxTemp = max
        invalidate()
    }

    private fun onStateUpdated(state: State) {
        when (state) {
            State.ON -> {
                dimmerPointerFillPaint.apply {
                    color = Color.parseColor("#EC8122")
                }
            }

            State.OFF -> {
                dimmerPointerFillPaint.apply {
                    color = Color.parseColor("#8E8E8E")
                }
            }
        }
        invalidate()
    }

    private fun calculateLinearGradientColor(startColor: Int, endColor: Int, position: Float): Int {
        val min = min(startColor, endColor)
        val max = max(startColor, endColor)

        return ((max - min) * position + min).toInt()
    }

    private fun invokeDimmerChanged(fromUser: Boolean) {
        onDimmerChanged?.invoke(fromUser, dimmer)
    }

    private fun invokeTemperatureChanged(fromUser: Boolean) {
        onTemperatureChanged?.invoke(fromUser, temperature + minTemp)
    }
}