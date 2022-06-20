package com.example.ble_light.light_picker

/*
 * This package base on Pikolo library (https://github.com/Madrapps/Pikolo)
 */

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.example.ble_light.R
import com.example.ble_light.light_picker.components.ColorComponent
import com.example.ble_light.light_picker.components.light.ColorTempComponent
import com.example.ble_light.light_picker.components.light.BrightComponent
import com.example.ble_light.light_picker.components.light.RgbMetrics
import com.example.ble_light.light_picker.listeners.OnLightComponentSelectionListener

open class LightPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : Picker(context, attrs, defStyleAttr) {

    private val metrics = RgbMetrics(color = floatArrayOf(200f, 200f, 200f), density = resources.displayMetrics.density)

    private val redComponent: ColorComponent
    private val greenComponent: ColorComponent

    private val redRadiusOffset: Float
    private val greenRadiusOffset: Float

    override val color: Int
        @RequiresApi(Build.VERSION_CODES.O)
        get() = metrics.getColor()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LightPicker, defStyleAttr, 0)

        with(config) {
            val redArcLength = typedArray.getFloat(R.styleable.LightPicker_red_arc_length, if (arcLength == 0f) 120f else arcLength)
            val redStartAngle = typedArray.getFloat(R.styleable.LightPicker_red_start_angle, 120f)
            redComponent = BrightComponent(metrics, paints, redArcLength, redStartAngle).also {
                it.fillWidth = typedArray.getDimension(R.styleable.LightPicker_red_arc_width, arcWidth)
                it.strokeWidth = typedArray.getDimension(R.styleable.LightPicker_red_stroke_width, strokeWidth)
                it.indicatorStrokeWidth = typedArray.getDimension(R.styleable.LightPicker_red_indicator_stroke_width, indicatorStrokeWidth)
                it.indicatorStrokeColor = typedArray.getColor(R.styleable.LightPicker_red_indicator_stroke_color, indicatorStrokeColor)
                it.strokeColor = typedArray.getColor(R.styleable.LightPicker_red_stroke_color, strokeColor)
                it.indicatorRadius = typedArray.getDimension(R.styleable.LightPicker_red_indicator_radius, indicatorRadius)
                it.id = "bright_temp"
            }

            val greenArcLength = typedArray.getFloat(R.styleable.LightPicker_green_arc_length, if (arcLength == 0f) 120f else arcLength)
            val greenStartAngle = typedArray.getFloat(R.styleable.LightPicker_green_start_angle, 300f)
            greenComponent = ColorTempComponent(metrics, paints, greenArcLength, greenStartAngle).also {
                it.fillWidth = typedArray.getDimension(R.styleable.LightPicker_green_arc_width, arcWidth)
                it.strokeWidth = typedArray.getDimension(R.styleable.LightPicker_green_stroke_width, strokeWidth)
                it.indicatorStrokeWidth = typedArray.getDimension(R.styleable.LightPicker_green_indicator_stroke_width, indicatorStrokeWidth)
                it.indicatorStrokeColor = typedArray.getColor(R.styleable.LightPicker_green_indicator_stroke_color, indicatorStrokeColor)
                it.strokeColor = typedArray.getColor(R.styleable.LightPicker_green_stroke_color, strokeColor)
                it.indicatorRadius = typedArray.getDimension(R.styleable.LightPicker_green_indicator_radius, indicatorRadius)
                it.id = "color_temp"
            }

            redRadiusOffset = typedArray.getDimension(R.styleable.LightPicker_red_radius_offset, if (radiusOffset == 0f) dp(25f) else radiusOffset)
            greenRadiusOffset = typedArray.getDimension(R.styleable.LightPicker_green_radius_offset, if (radiusOffset == 0f) dp(25f) else radiusOffset)
        }
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        redComponent.drawComponent(canvas)
        greenComponent.drawComponent(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldW: Int, oldH: Int) {
        val minimumSize = if (width > height) height else width
        val padding = (paddingLeft + paddingRight + paddingTop + paddingBottom) / 4f
        val outerRadius = minimumSize.toFloat() / 2f - padding

        redComponent.setRadius(outerRadius, redRadiusOffset)
        greenComponent.setRadius(outerRadius, greenRadiusOffset)

        metrics.centerX = width / 2f
        metrics.centerY = height / 2f

        greenComponent.updateComponent(greenComponent.angle)
        redComponent.updateComponent(redComponent.angle)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var isTouched = true
        if (!redComponent.onTouchEvent(event)) {
            isTouched = greenComponent.onTouchEvent(event)
        }
        invalidate()
        return isTouched
    }

    override fun setColorSelectionListener(listener: OnLightComponentSelectionListener) {
        redComponent.setColorSelectionListener(listener)
        greenComponent.setColorSelectionListener(listener)
    }

    override fun setColor(color: Int) {
        val red = Color.blue(color).toFloat()
        metrics.color[0] = red
        redComponent.updateAngle(red)

        val green = Color.blue(color).toFloat()
        metrics.color[1] = green
        greenComponent.updateAngle(green)

        invalidate()
    }
}