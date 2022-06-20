package com.example.ble_light.light_picker.listeners

/**
 * Empty implementation of [OnLightComponentSelectionListener] so that the clients can override only the methods they care
 */
open class SimpleLightSelectionListener : OnLightComponentSelectionListener {

    override fun onLightComponentSelection(color: Int, angle: Float, coeffBright: Float, id: String) {}

    override fun onLightComponentSelectionStart(color: Int) {}

    override fun onLightComponentSelectionEnd(color: Int) {}
}