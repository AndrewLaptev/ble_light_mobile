package com.example.ble_light.light_picker.components.light

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import com.example.ble_light.light_picker.Metrics
import kotlin.math.ln

internal class RgbMetrics(centerX: Float = 0f, centerY: Float = 0f, color: FloatArray, density: Float) : Metrics(centerX, centerY, color, density) {
    private val coeffRed = (255f - 204f) / 255f
    private val coeffGreen = (255f - 204f) / 255f
    private val coeffBlue = (255f - 102f) / 255f
    override var red: Float = 0f
    override var green: Float = 0f
    override var blue: Float = 0f
    override var brightness: Float = 0f

    override fun hue(): Float {
        val hsl = FloatArray(3)
        ColorUtils.RGBToHSL(color[0].toInt(), color[1].toInt(), color[2].toInt(), hsl)
        return hsl[0]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getColor(): Int {
        brightness = (18.05f * ln(color[0])) / 100 // law of brightness change

        if (brightness > 0) {
            red =  ((color[1] * coeffRed) + 204) * brightness
            green = (255 - (color[1] * coeffGreen)) * brightness
            blue = (255 - (color[1] * coeffBlue)) * brightness
        } else {
            red =  0f
            green = 0f
            blue = 0f
        }
        return Color.rgb(red.toInt(), green.toInt(), blue.toInt())
    }
}