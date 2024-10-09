package tools.important.tankslua.gui

import tanks.Drawing
import tanks.Panel
import kotlin.math.min

private val notifications = mutableListOf<Notification>()

enum class NotificationType {
    INFO,
    ERR
}

class Notification(
    val text: String,
    val type: NotificationType,
    val maxTime: Double = 250.0,
) {
    companion object {
        const val FONT_SIZE = 20.0

        const val TEXT_OPACITY = 255.0
        const val BACKGROUND_OPACITY = 100.0

        const val GRACE_PERIOD_LENGTH = 50.0
    }

    init {
        notifications.add(this)
    }
    var remainingTime = maxTime

    fun draw(index: Int) {
        val drawing = Drawing.drawing!!

        val lifetimeFraction = (remainingTime / maxTime)

        val width = drawing.interfaceSizeX * 0.33
        val height = drawing.interfaceSizeY * 0.15

        val x = drawing.interfaceSizeX * 0.85
        val y = (drawing.interfaceSizeY * 0.8) - (height+5.0) * index

        val textOpacity = min(
            lifetimeFraction * TEXT_OPACITY + GRACE_PERIOD_LENGTH,
            TEXT_OPACITY
        )
        val backgroundOpacity = min(
            lifetimeFraction * BACKGROUND_OPACITY + GRACE_PERIOD_LENGTH,
            BACKGROUND_OPACITY
        )

        when (type) {
            NotificationType.INFO -> drawing.setColor(0.0, 0.0, 0.0, backgroundOpacity)
            NotificationType.ERR -> drawing.setColor(125.0, 0.0, 0.0, backgroundOpacity)
        }
        drawing.fillInterfaceRect(x, y, width, height)


        drawing.setColor(255.0, 255.0, 255.0, textOpacity)
        drawing.setInterfaceFontSize(FONT_SIZE)

        val lines = drawing.wrapText(text, width-5, FONT_SIZE)
        val textHeight = drawing.getStringHeight(text)

        for ((lineNum, line) in lines.withIndex()) {
            drawing.drawInterfaceText(x, y + (lineNum * textHeight) - ((height/2)-textHeight), line)
        }
    }
}

fun updateNotifications() {
    val frequency = Panel.frameFrequency

    var i = 0
    while (notifications.size > i) {
        val notif = notifications[i]
        notif.remainingTime -= frequency
        if (notif.remainingTime <= 0) {
            notifications.removeAt(i)
            continue
        }
        i++
    }
}

fun drawNotifications() {
    for ((i, notification) in notifications.withIndex()) {
        notification.draw(i)
    }
}