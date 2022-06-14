package com.example.ble_light.light_picker.components.rgb

import android.graphics.Color
import com.example.ble_light.light_picker.Metrics
import com.example.ble_light.light_picker.Paints
import com.example.ble_light.light_picker.components.ArcComponent

internal class RedComponent(metrics: Metrics, paints: Paints, arcLength: Float, arcStartAngle: Float) : ArcComponent(metrics, paints, arcLength, arcStartAngle) {

    override val componentIndex: Int = 0
    override val range: Float = 255f
    override val noOfColors = 2
    override val colors = IntArray(noOfColors)
    override val colorPosition = FloatArray(noOfColors)

    override fun getColorArray(color: FloatArray): IntArray {
        colors[0] = Color.BLACK
        colors[1] = Color.rgb(255, 0, 0)
        return colors
    }
}