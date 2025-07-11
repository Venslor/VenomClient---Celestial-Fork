/*package net.pvproom.client.gui.elements.help

import net.pvproom.client.launcherFrame
import net.pvproom.client.f
import net.pvproom.client.gui.elements.HelpPage
import net.pvproom.client.gui.layouts.VerticalFlowLayout
import net.pvproom.client.readOnly
import net.pvproom.client.toJTextArea
import javax.swing.JButton

class HelpApi : HelpPage("API") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(f.getString("gui.help.api").toJTextArea().readOnly())
        this.add(JButton(f.getString("gui.settings.title")).let {
            it.addActionListener {
                launcherFrame.layoutX.show(launcherFrame.mainPanel, "settings")
            }
            it
        })
    }
}
*/