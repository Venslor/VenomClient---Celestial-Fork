package net.pvproom.client.gui.elements.help

import net.pvproom.client.f
import net.pvproom.client.gui.elements.HelpPage
import net.pvproom.client.readOnly
import net.pvproom.client.toJTextArea

class HelpLogin : HelpPage(f.getString("gui.help.login.title")) {

    init {
        this.add(f.getString("gui.help.login").toJTextArea().readOnly())
    }
}