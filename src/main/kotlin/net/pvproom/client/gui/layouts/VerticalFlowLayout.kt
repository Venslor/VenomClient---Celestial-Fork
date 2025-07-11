package net.pvproom.client.gui.layouts

import java.awt.*
import java.io.Serial
import java.io.Serializable
import java.util.*
import kotlin.math.max

@Suppress("unused")
class VerticalFlowLayout(
    private var hAlign: Int = LEFT,
    private var vAlign: Int = TOP,
    private var hPadding: Int = 5,
    private var vPadding: Int = 5,
    private var hGap: Int = 5,
    var vGap: Int = 5,
    private val fill: Boolean = true,
    private val wrap: Boolean = false
) : LayoutManager, Serializable {
    constructor(padding: Int, gap: Int) : this(LEFT, TOP, padding, padding, gap, gap, true, false)

    override fun addLayoutComponent(name: String, comp: Component) {}
    override fun removeLayoutComponent(comp: Component) {}

    override fun preferredLayoutSize(container: Container): Dimension {
        synchronized(container.treeLock) {
            var width = 0
            var height = 0

            val components = getVisibleComponents(container)
            for (component in components) {
                val dimension = component.preferredSize
                width = max(width.toDouble(), dimension.width.toDouble()).toInt()
                height += dimension.height
            }

            if (components.isNotEmpty()) {
                height += vGap * (components.size - 1)
            }

            val insets = container.insets
            width += insets.left + insets.right
            height += insets.top + insets.bottom

            if (components.isNotEmpty()) {
                width += hPadding * 2
                height += vPadding * 2
            }
            return Dimension(width, height)
        }
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            var width = 0
            var height = 0

            val components = getVisibleComponents(parent)
            for (component in components) {
                val dimension = component.minimumSize
                width = max(width.toDouble(), dimension.width.toDouble()).toInt()
                height += dimension.height
            }

            if (components.isNotEmpty()) {
                height += vGap * (components.size - 1)
            }

            val insets = parent.insets
            width += insets.left + insets.right
            height += insets.top + insets.bottom

            if (components.isNotEmpty()) {
                width += hPadding * 2
                height += vPadding * 2
            }
            return Dimension(width, height)
        }
    }

    override fun layoutContainer(container: Container) {
        synchronized(container.treeLock) {
            val size = container.size
            val insets = container.insets

            val availableWidth = size.width - insets.left - insets.right - hPadding * 2
            val availableHeight = size.height - insets.top - insets.bottom - vPadding * 2

            val components = getVisibleComponents(container)

            var xBase = insets.left + hPadding

            val list: MutableList<Component> = LinkedList()

            for (component in components) {
                list.add(component)

                if (wrap && list.size > 1 && availableHeight + vPadding < getPreferredHeight(list)) {
                    list.remove(component)
                    batch(insets, availableWidth, availableHeight, xBase, list, components)
                    xBase += hGap + getPreferredWidth(list)
                    list.clear()
                    list.add(component)
                }
            }
            if (list.isNotEmpty()) {
                batch(insets, availableWidth, availableHeight, xBase, list, components)
            }
        }
    }

    private fun batch(
        insets: Insets,
        availableWidth: Int,
        availableHeight: Int,
        xBase: Int,
        list: List<Component>,
        components: List<Component>
    ) {
        val preferredWidth = getPreferredWidth(list)
        val preferredHeight = getPreferredHeight(list)

        var y = when (vAlign) {
            TOP -> insets.top + vPadding
            CENTER -> (availableHeight - preferredHeight) / 2 + insets.top + vPadding
            BOTTOM -> availableHeight - preferredHeight + insets.top + vPadding
            else -> insets.top + vPadding
        }

        for (i in list.indices) {
            val item = list[i]

            val x = if (fill) {
                xBase
            } else {
                when (hAlign) {
                    LEFT -> xBase
                    CENTER -> xBase + (preferredWidth - item.preferredSize.width) / 2
                    RIGHT -> xBase + preferredWidth - item.preferredSize.width
                    else -> xBase
                }
            }

            var width: Int
            if (fill) {
                width = if (wrap) preferredWidth else availableWidth
                if (list.size == components.size) width = availableWidth
            } else {
                width = item.preferredSize.width
            }

            if (i != 0) y += vGap

            item.setBounds(x, y, width, item.preferredSize.height)
            y += item.height
        }
    }

    private fun getVisibleComponents(container: Container): List<Component> =
        container.components.filter { it.isVisible }

    private fun getPreferredWidth(components: List<Component>): Int =
        components.maxOfOrNull { it.preferredSize.width } ?: 0

    private fun getPreferredHeight(components: List<Component>): Int =
        components.sumOf { it.preferredSize.height } + vGap * (components.size - 1)

    override fun toString(): String =
        "VerticalFlowLayout{hAlign=$hAlign, vAlign=$vAlign, hPadding=$hPadding, vPadding=$vPadding, hGap=$hGap, vGap=$vGap, fill=$fill, wrap=$wrap}"

    companion object {
        @Serial
        private val serialVersionUID = 1L

        const val CENTER: Int = 0
        const val TOP: Int = 1
        const val BOTTOM: Int = 2
        const val LEFT: Int = 3
        const val RIGHT: Int = 4
    }
}
