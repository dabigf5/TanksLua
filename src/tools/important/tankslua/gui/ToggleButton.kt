package tools.important.tankslua.gui

import tanks.gui.Button
import tanks.gui.screen.ScreenOptions

class ToggleButton(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    val label: String,
    val onToggled: (Boolean) -> Unit,
    initiallyEnabled: Boolean = false,
) : Button(x, y, width, height, label) {
    private fun updateText() {
        val onOff = if (toggled) ScreenOptions.onText else ScreenOptions.offText
        setText("$label: $onOff")
    }

    var toggled: Boolean = initiallyEnabled
        set(newEnabled) {
            field = newEnabled
            updateText()
            onToggled(field)
        }

    init {
        updateText()
        function = Runnable { toggled = !toggled }
        enabled = true
    }
}