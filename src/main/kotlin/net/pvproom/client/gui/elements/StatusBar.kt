package net.pvproom.client.gui.elements

import net.pvproom.client.event.EventManager
import net.pvproom.client.event.EventTarget
import net.pvproom.client.event.impl.GameStartEvent
import net.pvproom.client.event.impl.GameTerminateEvent
import net.pvproom.client.f
import net.pvproom.client.gamePid
import net.pvproom.client.getInputStream
import net.pvproom.client.gui.dialogs.LogsDialog
import net.pvproom.client.toJLabel
import java.awt.BorderLayout
import javax.swing.*

class StatusBar : JPanel() {
    private val label = JLabel()
    private val pidLabel = "game-pid".toJLabel()
    private val autoClearTimer = Timer(10000) {
        this.clear()
    }


    val dialog = LogsDialog(f.getString("gui.status.logs"))

    init {
        EventManager.register(this)

        this.layout = BorderLayout(0, 0)
        val btnOpenDialog =
            JButton(ImageIcon("/images/logs.png".getInputStream()!!.readAllBytes()))

        btnOpenDialog.addActionListener {
            dialog.isVisible = true
        }
        this.add(label, BorderLayout.WEST)

        val otherComponents = JPanel()
        otherComponents.add(pidLabel)
        otherComponents.add(btnOpenDialog)

        pidLabel.isVisible = false

        this.add(otherComponents, BorderLayout.EAST)
    }

    private fun clear() {
        this.label.text = ""
    }

    var isRunningGame: Boolean = false
        set(value) {
            this@StatusBar.pidLabel.text = if (value) "PID $gamePid" else "NOT RUNNING"
            this@StatusBar.pidLabel.isVisible = value
            field = value
        }

    var text: String? = null
        set(value) {
            if (this.label.text.isNotEmpty()) {
                autoClearTimer.stop()
            }
            this.label.setText(value)
            if (!value.isNullOrEmpty()) {
                dialog.addMessage(value)
                autoClearTimer.start()
            }
        }

    @EventTarget
    fun onGameStart(e: GameStartEvent) {
        gamePid.set(e.pid)
        this.isRunningGame = true
    }

    @EventTarget
    fun onGameTerminate(e: GameTerminateEvent) {
        gamePid.set(0)
        this.isRunningGame = false
    }
}
