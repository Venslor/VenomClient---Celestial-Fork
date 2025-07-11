package net.pvproom.client.gui.elements

import net.pvproom.client.source
import net.pvproom.client.withScroller
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class SearchableList<T>(private val model: DefaultListModel<T>, baseList: JList<T>) : JPanel() {

    private var list = ArrayList<T>()

    private var isInternalChange = false

    init {
        this.layout = BorderLayout(0, 0)
        val searchBar = JTextField("Search")
        searchBar.foreground = Color.GRAY
        searchBar.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                val source = e.source<JTextField>()
                if (source.text == "Search") source.text = ""
                source.foreground = null
            }

            override fun focusLost(e: FocusEvent) {
                val source = e.source<JTextField>()
                if (source.text.isEmpty()) {
                    source.text = "Search"
                    source.foreground = Color.GRAY
                }
            }
        })

        searchBar.addActionListener {
            this.search(it.source<JTextField>().text)
        }
        this.add(searchBar, BorderLayout.NORTH)

        fun refresh() {
            // reload items
            if (isInternalChange) return
            list = ArrayList()
            model.elements().asIterator().forEach {
                list.add(it)
            }
        }

        baseList.addPropertyChangeListener {
            refresh()
        }

        model.addListDataListener(object : ListDataListener {

            override fun intervalAdded(e: ListDataEvent?) {
                refresh()
            }

            override fun intervalRemoved(e: ListDataEvent?) {
                refresh()
            }
            override fun contentsChanged(e: ListDataEvent?) {
                refresh()
            }
        })

        refresh()


        this.add(baseList.withScroller())
    }

    private fun search(text: String) {
        isInternalChange = true
        if (text.isBlank()) {
            model.removeAllElements()
            model.addAll(list)
            isInternalChange = false
            return
        }
        model.removeAllElements()
        list.forEach {
            if (it.toString().contains(text, ignoreCase = true)) {
                model.addElement(it)
            }
        }
        isInternalChange = false
    }
}
