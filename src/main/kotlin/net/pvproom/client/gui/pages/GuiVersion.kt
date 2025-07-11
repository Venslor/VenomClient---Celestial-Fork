package net.pvproom.client.gui.pages

import net.pvproom.client.f
import net.pvproom.client.gui.elements.GuiAddonManager
import net.pvproom.client.gui.elements.GuiVersionSelect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder

class GuiVersion : JPanel() { // DO MODERN BROSKI
    init {
        border = CompoundBorder(
            LineBorder(Color(60, 60, 70), 1, true),
            EmptyBorder(20, 25, 20, 25)
        )

        background = Color(25, 25, 30) // Daha koyu gri/mavi ton
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        initGui()
    }

    private fun initGui() {
        //Jlabel :))))
        val titleLabel = JLabel(f.getString("gui.version.title")).apply {
            foreground = Color(200, 200, 255)
            font = Font("Segoe UI", Font.BOLD, 18)
            alignmentX = LEFT_ALIGNMENT
        }
        add(titleLabel)
        add(Box.createVerticalStrut(20))

        add(GuiVersionSelect().apply {
            alignmentX = LEFT_ALIGNMENT
        })

        add(Box.createVerticalStrut(20))

        add(GuiAddonManager().apply {
            alignmentX = LEFT_ALIGNMENT
        })
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GuiVersion::class.java)
    }
}
