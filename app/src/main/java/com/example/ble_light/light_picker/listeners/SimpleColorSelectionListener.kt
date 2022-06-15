package com.example.ble_light.light_picker.listeners

/**
 * Empty implementation of [OnColorSelectionListener] so that the clients can override only the methods they care
 */
open class SimpleColorSelectionListener : OnColorSelectionListener {

    override fun onColorSelected(color: Int, angle: Float, coeffBright: Float, id: String) {}

    override fun onColorSelectionStart(color: Int) {}

    override fun onColorSelectionEnd(color: Int) {}
}