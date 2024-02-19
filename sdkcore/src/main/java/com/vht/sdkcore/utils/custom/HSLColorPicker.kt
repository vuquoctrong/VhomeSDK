package com.vht.sdkcore.utils.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class HSLColorPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val size: Float
        get() = min(measuredWidth.toFloat(), measuredHeight.toFloat())
    private val commonStrokeWidth
        get() = size * 0.008F
    private val lightingSize
        get() = size * 0.076F
    private val guidelineSize
        get() = size * 0.064F
    private val huePointerRadius
        get() = size * 0.04F

    private var lighting = maxLighting
        set(value) {
            field = if (value < 0) 0
            else if (value > maxLighting) maxLighting
            else value

            invokeColorChanged(false)
        }
    private var hue = maxHue
        set(value) {
            field = if (value < 0) 0
            else if (value > maxHue) maxHue
            else value

            invokeColorChanged(false)
        }
    private var saturation = maxSaturation
        set(value) {
            field = if (value < 0) 0
            else if (value > maxSaturation) maxSaturation
            else value

            invokeColorChanged(false)
        }

    private val backgroundLightingDrawable = GradientDrawable()
    private val backgroundLightingDrawable2 = GradientDrawable()

    private val progressLightingPaint = Paint().apply {
        isAntiAlias = true
    }

    private val lightingPointerFillPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#EC8122")
        style = Paint.Style.FILL
    }

    private val lightingPointerStrokePaint = Paint().apply {
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

    private val hueColors = intArrayOf(
        Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
    )

    private val saturationColors = intArrayOf(
        Color.WHITE, Color.TRANSPARENT
    )

    private val hueGradient = GradientDrawable().apply {
        gradientType = GradientDrawable.SWEEP_GRADIENT
        shape = GradientDrawable.OVAL
        colors = hueColors
    }

    private val saturationGradient = GradientDrawable().apply {
        gradientType = GradientDrawable.RADIAL_GRADIENT
        shape = GradientDrawable.OVAL
        colors = saturationColors
    }

    private var colorWheelRadius = 0F

    private val colorWheelStatusOffPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#CECECE")
    }

    private enum class TouchState {
        NONE, LIGHTING, HUE_SATURATION
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

    private var onColorChanged: ((Boolean, Int, Int, Int) -> Unit)? = null

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        val backgroundColors = IntArray(2).apply {
            set(0, Color.parseColor("#FFFFFF"))
            set(1, Color.parseColor("#DEDEDE"))
        }
        backgroundLightingDrawable.apply {
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

        backgroundLightingDrawable2.apply {
            setBounds(
                (0 + lightingSize + commonStrokeWidth / 2).toInt(),
                (0 + lightingSize + commonStrokeWidth / 2).toInt(),
                (size.toInt() - lightingSize - commonStrokeWidth / 2).toInt(),
                (size.toInt() - lightingSize - commonStrokeWidth / 2).toInt()
            )
            shape = GradientDrawable.OVAL
            setStroke(commonStrokeWidth.toInt(), Color.parseColor("#CDCDCD"))
        }

        val progressColors = IntArray(2).apply {
            set(0, Color.parseColor("#FFA800"))
            set(1, Color.parseColor("#FFFFFF"))
        }
        progressLightingPaint.shader = SweepGradient(
            size / 2F, size / 2F, progressColors, null
        )
        colorWheelRadius = size / 2F - commonStrokeWidth - lightingSize - guidelineSize

        lightingPointerStrokePaint.strokeWidth = commonStrokeWidth
        innerPointerStrokePaint.strokeWidth = commonStrokeWidth
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) return true

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (state == State.OFF) return false

                val touchPoint = PointF(event.x, event.y)
                val distanceToCenter =
                    calculateDistance(touchPoint, PointF(size / 2F, size / 2F))
                touchState = if (distanceToCenter <= colorWheelRadius + huePointerRadius) {
                    TouchState.HUE_SATURATION
                } else if (distanceToCenter <= size / 2F) {
                    TouchState.LIGHTING
                } else {
                    TouchState.NONE
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (touchState) {
                    TouchState.LIGHTING -> {
                        onDraggingLighting(event.x, event.y)
                    }

                    TouchState.HUE_SATURATION -> {
                        onDraggingHueAndSaturation(event.x, event.y)
                    }

                    TouchState.NONE -> {

                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchState = TouchState.NONE
                invokeColorChanged(true)
            }
        }

        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (canvas != null) {
            drawLightingBackground(canvas)
            drawLightingProgress(canvas)
            drawCenterCircle(canvas)
            drawLightingPointer(canvas)
            drawColorWheel(canvas)
            drawHuePointer(canvas)
        }
    }

    private fun drawLightingBackground(canvas: Canvas) {
        backgroundLightingDrawable.draw(canvas)
        backgroundLightingDrawable2.draw(canvas)
    }

    private fun drawLightingProgress(canvas: Canvas) {
        if (state == State.ON) {
            canvas.save()
            canvas.rotate(270F, size / 2F, size / 2F)
            val degrees = (lighting.toFloat() / maxLighting) * 360
            canvas.drawArc(
                0F + commonStrokeWidth - 1,
                0F + commonStrokeWidth - 1,
                size - commonStrokeWidth + 1,
                size - commonStrokeWidth + 1,
                0F,
                degrees,
                true,
                progressLightingPaint
            )
            canvas.restore()
        }
    }

    private fun drawCenterCircle(canvas: Canvas) {
        val radius = colorWheelRadius + guidelineSize
        canvas.drawCircle(size / 2F, size / 2F, radius, centerCirclePaint)
    }

    private fun drawLightingPointer(canvas: Canvas) {
        canvas.save()
        val degrees = if (state == State.ON) (lighting.toFloat() / maxLighting) * 360 else 0F
        canvas.rotate(degrees, size / 2F, size / 2F)
        val width = size * 0.02F
        val height = lightingSize + 2 * commonStrokeWidth
        canvas.drawRect(
            size / 2F - width / 2,
            0F,
            size / 2F + width / 2,
            height,
            lightingPointerFillPaint
        )
        canvas.drawRect(
            size / 2F - width / 2,
            0F,
            size / 2F + width / 2,
            height,
            lightingPointerStrokePaint
        )
        canvas.restore()
    }

    private fun drawColorWheel(canvas: Canvas) {
        if (state == State.ON) {
            canvas.save()
            canvas.rotate(315F, size / 2F, size / 2F)
            hueGradient.setBounds(
                (size / 2 - colorWheelRadius).toInt(),
                (size / 2 - colorWheelRadius).toInt(),
                (size / 2 + colorWheelRadius).toInt(),
                (size / 2 + colorWheelRadius).toInt()
            )
            hueGradient.draw(canvas)
            saturationGradient.setBounds(
                (size / 2 - colorWheelRadius).toInt(),
                (size / 2 - colorWheelRadius).toInt(),
                (size / 2 + colorWheelRadius).toInt(),
                (size / 2 + colorWheelRadius).toInt()
            )
            saturationGradient.gradientRadius = colorWheelRadius
            saturationGradient.draw(canvas)
            canvas.restore()
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

    private fun drawHuePointer(canvas: Canvas) {
        if (state == State.ON) {
            val cx = size / 2F
            val cy = size / 2F - colorWheelRadius * (saturation.toFloat() / maxSaturation)
            val angle = (hue.toFloat() / maxHue) * 360 + 45

            canvas.save()
            canvas.rotate(angle, size / 2F, size / 2F)

            val color = Color.HSVToColor(FloatArray(3).apply {
                this[0] = (hue.toFloat() / maxHue) * 360
                this[1] = saturation.toFloat() / maxSaturation
                this[2] = 1F
            })
            innerPointerFillPaint.color = color

            canvas.drawCircle(cx, cy, huePointerRadius, innerPointerFillPaint)
            canvas.drawCircle(cx, cy, huePointerRadius, innerPointerStrokePaint)
            canvas.restore()
        }
    }

    private fun onDraggingLighting(x: Float, y: Float) {
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
        var newLighting = ((angle / 360) * maxLighting).toInt()

        newLighting = if (lighting > maxLighting * 0.985 && newLighting < maxLighting * 0.015) maxLighting
        else if (lighting < maxLighting * 0.015 && newLighting > maxLighting * 0.985) 0
        else if (abs(newLighting - lighting) > maxLighting * 0.15) return
        else newLighting

        lighting = newLighting
        invalidate()
    }

    private fun onDraggingHueAndSaturation(x: Float, y: Float) {
        fun calculateAngle(
            centerPointX: Float,
            centerPointY: Float,
            pointX: Float,
            pointY: Float
        ): Float {
            val dx = pointX - centerPointX
            val dy = pointY - centerPointY
            val rawAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 45F
            return (if (rawAngle <= 0) 360F + rawAngle else rawAngle).toFloat()
        }

        val angle = calculateAngle(size / 2F, size / 2F, x, y)
        hue = ((angle / 360) * maxHue).toInt()
        var distance = calculateDistance(PointF(size / 2, size / 2), PointF(x, y)).toFloat()
        distance = min(distance, colorWheelRadius)
        saturation = ((distance / colorWheelRadius) * maxSaturation).toInt()
        invalidate()
    }

    private fun calculateDistance(point1: PointF, point2: PointF): Double {
        return sqrt(
            (point1.x - point2.x).toDouble().pow(2.0) + (point1.y - point2.y).toDouble().pow(2.0)
        )
    }

    fun setOnColorChangedListener(action: ((Boolean, Int, Int, Int) -> Unit)) {
        this.onColorChanged = action
    }

    fun setColor(hue: Int, saturation: Int, lighting: Int) {
        this.hue = hue
        this.saturation = saturation
        this.lighting = lighting
        invalidate()
    }

    fun getHSL(): Triple<Int, Int, Int> {
        return Triple(hue, saturation, lighting)
    }

    private fun onStateUpdated(state: State) {
        when (state) {
            State.ON -> {
                lightingPointerFillPaint.apply {
                    color = Color.parseColor("#EC8122")
                }
            }

            State.OFF -> {
                lightingPointerFillPaint.apply {
                    color = Color.parseColor("#8E8E8E")
                }
            }
        }
        invalidate()
    }

    private fun invokeColorChanged(isFromUser: Boolean) {
        onColorChanged?.invoke(isFromUser, hue, saturation, lighting)
    }

    companion object {
        val maxLighting = 65535
        val maxHue = 65535
        val maxSaturation = 65535
    }
}