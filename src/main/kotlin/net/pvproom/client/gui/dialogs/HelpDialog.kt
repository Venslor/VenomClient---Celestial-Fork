package net.pvproom.client.gui.dialogs

import net.pvproom.client.f
import net.pvproom.client.gui.elements.HelpPage
import net.pvproom.client.gui.elements.HelpPageX
import net.pvproom.client.gui.elements.SearchableList
import net.pvproom.client.gui.elements.help.HelpWelcome
import net.pvproom.client.utils.resolvePackage
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import javax.swing.DefaultListModel
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel

class HelpDialog : JDialog() {
    init {
        this.title = f.getString("gui.help")
        this.layout = BorderLayout()
        this.size = Dimension(600, 600)
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        val panelDocument = JPanel()
        val layout = CardLayout()
        panelDocument.layout = layout

        val modelDocuments = DefaultListModel<HelpPageX>()
        val listDocuments = JList(modelDocuments)

        // add pages
        modelDocuments.addHelpPage(HelpWelcome()) // must be on top
        "net.pvproom.client.gui.elements.help".resolvePackage(HelpPage::class.java).forEach {
            val instance = it.getDeclaredConstructor().newInstance()
            if (instance !is HelpWelcome) {
                modelDocuments.addHelpPage(instance)
            }
        }

        for (page in modelDocuments.elements()) {
            panelDocument.add(page.name, page)
        }

        listDocuments.addListSelectionListener {
            val source = it.source as JList<HelpPageX>
            val page = source.selectedValue ?: return@addListSelectionListener
            layout.show(panelDocument, page.name)
        }

        this.add(SearchableList(modelDocuments, listDocuments), BorderLayout.NORTH)
        this.add(panelDocument, BorderLayout.CENTER)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

private fun DefaultListModel<HelpPageX>.addHelpPage(page: HelpPage) {
    this.addElement(HelpPageX(page))
}