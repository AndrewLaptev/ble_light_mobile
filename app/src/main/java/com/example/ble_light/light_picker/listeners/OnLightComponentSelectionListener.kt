package com.example.ble_light.light_picker.listeners

/**
 * Listener to listen to color change events
 */
interface OnLightComponentSelectionListener {

    /**
     * Invoked every time the color changes
     *
     * @param color the selected color
     */
    fun onLightComponentSelection(color: Int, angle: Float, brightness: Float, id: String)

    /**
     * Invoked when the color selection started
     *
     * @param color the color before the selection started
     */
    fun onLightComponentSelectionStart(color: Int)

    /**
     * Invoked when the color selection is over
     *
     * @param color the selected color
     */
    fun onLightComponentSelectionEnd(color: Int)
}