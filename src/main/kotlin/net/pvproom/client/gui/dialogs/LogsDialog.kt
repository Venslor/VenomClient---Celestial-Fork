package net.pvproom.client.gui.dialogs

import net.pvproom.client.readOnly
import net.pvproom.client.withScroller
import javax.swing.JDialog
import javax.swing.JTextArea

class LogsDialog(gaveTitle: String) : JDialog() {
    private val area = JTextArea().readOnly()
    private val sb = StringBuilder()

    init {
        this.title = gaveTitle
        this.setSize(600, 600)
        this.isLocationByPlatform = true
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.contentPane = area.withScroller()
    }

    fun addMessage(msg: String) {
        sb.append(msg).append("\n")
        area.text = sb.toString()
    }
}