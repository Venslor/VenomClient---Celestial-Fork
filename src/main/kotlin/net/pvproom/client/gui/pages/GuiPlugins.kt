package net.pvproom.client.gui.pages

import net.pvproom.client.event.EventTarget
import net.pvproom.client.event.impl.APIReadyEvent
import net.pvproom.client.f
import net.pvproom.client.launcherData
import net.pvproom.client.format
import net.pvproom.client.game.RemoteAddon
import net.pvproom.client.game.addon.JavaAgent
import net.pvproom.client.game.addon.LunarCNMod
import net.pvproom.client.game.addon.WeaveMod
import net.pvproom.client.gui.dialogs.AddonInfoDialog
import net.pvproom.client.gui.layouts.VerticalFlowLayout
import net.pvproom.client.withScroller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.GridLayout
import java.io.File
import javax.swing.*
import javax.swing.border.TitledBorder

class GuiPlugins : JPanel() {
    private val tab: JTabbedPane

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GuiPlugins::class.java)
    }

    init {
        this.border = TitledBorder(
            null,
            f.getString("gui.plugins.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color(180, 180, 255)
        )
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)

        this.tab = JTabbedPane()
    }

    @EventTarget
    fun onAPIReady(e: APIReadyEvent) {
        this.removeAll()
        this.initGui()
    }

    private fun initGui() {
        this.add(tab)
        addTabs()

        // refresh addons
        val btnRefresh = JButton(f.getString("gui.plugins.refresh"))
        btnRefresh.addActionListener {
            this.refresh()
        }
        this.add(tab)
        this.add(btnRefresh)
    }

    private fun refresh() {
        this.tab.removeAll()
        this.addTabs()
    }

    private fun addTabs() {
        val addons: List<RemoteAddon>? = launcherData.plugins
        if (addons == null) {
            this.add(JLabel(f.getString("gui.plugins.unsupported")))
            return
        }
        val panelWeave = JPanel()
        panelWeave.layout = VerticalFlowLayout()
        val panelAgents = JPanel()
        panelAgents.layout = VerticalFlowLayout()
        val panelCN = JPanel()
        panelCN.layout = VerticalFlowLayout()
        tab.addTab("Weave", panelWeave.withScroller())
        tab.addTab("Agents", panelAgents.withScroller())
        tab.addTab("LunarCN", panelCN.withScroller())
        for (addon in addons) {
            when (addon.category) {
                RemoteAddon.Category.WEAVE -> {
                    addPlugin(panelWeave, addon, WeaveMod.modFolder)
                }

                RemoteAddon.Category.AGENT -> {
                    addPlugin(panelAgents, addon, JavaAgent.javaAgentFolder)
                }

                RemoteAddon.Category.CN -> {
                    addPlugin(panelCN, addon, LunarCNMod.modFolder)
                }
            }
        }
    }

    /**
     * Add label and download button
     *
     * @param panel  the panel
     * @param addon  the addon
     * @param folder target folder
     */
    private fun addPlugin(panel: JPanel, addon: RemoteAddon, folder: File) {
        val p = JPanel()
        p.layout = GridLayout()
        p.add(JLabel(addon.name))
        val file = File(folder, addon.name)
        p.add(getInfoButton(addon, file))
        panel.add(p)
    }

    private fun getInfoButton(addon: RemoteAddon, file: File): JButton {
        val btn = JButton(f.format("gui.plugins.info", file.name))
        btn.addActionListener {
            log.info("Open plugin info dialog for " + addon.name)
            AddonInfoDialog(addon, file).isVisible = true // show info dialog
        }
        return btn
    }
}
