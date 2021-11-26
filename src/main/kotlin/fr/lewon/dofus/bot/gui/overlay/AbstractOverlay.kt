package fr.lewon.dofus.bot.gui.overlay

import java.awt.Color
import javax.swing.JFrame
import javax.swing.JPanel

abstract class AbstractOverlay(contentPane: JPanel) : JFrame() {

    init {
        isAlwaysOnTop = true
        isUndecorated = true
        this.contentPane = contentPane
        this.contentPane.background = Color.GRAY
        opacity = 0.60f
    }

    abstract fun updateOverlay(pid: Long)

}