package net.pvproom.client.gui.dialogs

import net.pvproom.client.LauncherPage
import net.pvproom.client.config
import net.pvproom.client.f
import net.pvproom.client.gui.elements.GuiDraggableList
import net.pvproom.client.withScroller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JDialog
import javax.swing.JPanel

class SortPagesDialog : JDialog() {
    private val log: Logger = LoggerFactory.getLogger(SortPagesDialog::class.java)
    private val panel = JPanel()

    init {
        this.title = f.getString("gui.settings.pages.title")
        this.setSize(600, 600)
        this.layout = BorderLayout()
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        // todo save pages, translate
        val draggableList = GuiDraggableList<LauncherPage>()
        val model = draggableList.model as DefaultListModel<LauncherPage>
        model.addAll(config.pages)
        panel.add(draggableList)
        this.add(this.panel.withScroller())
    }
}