package net.pvproom.client.gui.elements.help

import net.pvproom.client.f
import net.pvproom.client.gui.elements.HelpPage
import net.pvproom.client.gui.layouts.VerticalFlowLayout
import net.pvproom.client.open
import net.pvproom.client.toJLabel
import java.net.URI
import javax.swing.JButton

class HelpWelcome : HelpPage("Welcome") {
    init {
        this.layout = VerticalFlowLayout()
        this.add("Welcome to the Venom internal document!".toJLabel())

        val online = JButton(f.getString("gui.help.document"))
        online.addActionListener {
            URI.create("https://github.com/earthsworth/celestial/wiki").open()
        }
        this.add(online)
    }
}
