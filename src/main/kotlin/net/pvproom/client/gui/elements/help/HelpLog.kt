package net.pvproom.client.gui.elements.help

import net.pvproom.client.f
import net.pvproom.client.launcherLogFile
import net.pvproom.client.gui.elements.HelpPage
import net.pvproom.client.gui.layouts.VerticalFlowLayout
import net.pvproom.client.readOnly
import net.pvproom.client.toJTextArea
import net.pvproom.client.utils.createButtonOpenFolder

class HelpLog : HelpPage("Log") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(f.getString("gui.help.log").toJTextArea().readOnly())
        this.add(createButtonOpenFolder(f.getString("gui.settings.folder.log"), launcherLogFile.parentFile))
    }
}