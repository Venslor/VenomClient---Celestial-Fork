package net.pvproom.client.gui

import com.sun.tools.attach.AttachNotSupportedException
import net.pvproom.client.*
import net.pvproom.client.event.EventManager
import net.pvproom.client.event.EventTarget
import net.pvproom.client.event.impl.AuthEvent
import net.pvproom.client.event.impl.GameStartEvent
import net.pvproom.client.event.impl.GameTerminateEvent
import net.pvproom.client.gui.elements.StatusBar
import net.pvproom.client.utils.findJava
import net.pvproom.client.utils.lunar.LauncherData.Companion.getMainClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.URI
import javax.swing.*
import kotlin.math.max
import kotlin.math.min

class GuiLauncher : JFrame() {
    private lateinit var layoutX: CardLayout
    private lateinit var mainPanel: JPanel
    private lateinit var currentPageLabel: JLabel
    private var pageNames = listOf<String>()
    private var currentPageIndex = 0

    init {
        EventManager.register(this)
        this.setBounds(100, 100, 1200, 700)
        this.title = f.getString("gui.launcher.title")
        this.resetIcon()
        this.initGui()
    }

    private fun initGui() {
        this.contentPane.layout = BorderLayout()
        this.add(statusBar, BorderLayout.NORTH)

        layoutX = CardLayout()
        mainPanel = JPanel(layoutX).apply { background = Color(30, 30, 30) }
        this.add(mainPanel, BorderLayout.CENTER)

        val bottomPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Color(25, 25, 25)
        }

        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 30, 10)).apply {
            background = Color(25, 25, 25)
        }

        val btnPrevious = createModernButton(f.getString("gui.previous")) {
            if (currentPageIndex > 0) {
                updateCurrentPage(-1)
                layoutX.show(mainPanel, pageNames[currentPageIndex])
            }
        }

        val btnNext = createModernButton(f.getString("gui.next")) {
            if (currentPageIndex < pageNames.size - 1) {
                updateCurrentPage(1)
                layoutX.show(mainPanel, pageNames[currentPageIndex])
            }
        }


        val btnDiscord = createModernButton(f.getString("gui.discord")) {
            try {
                Desktop.getDesktop().browse(URI("https://discord.gg/ny4a9B9bbn"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        buttonPanel.add(btnPrevious)
        buttonPanel.add(btnNext)
        buttonPanel.add(btnDiscord)

        currentPageLabel = JLabel().apply {
            foreground = Color(200, 200, 200)
            font = Font("Segoe UI", Font.PLAIN, 14)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        bottomPanel.add(buttonPanel)
        bottomPanel.add(Box.createVerticalStrut(5))
        bottomPanel.add(currentPageLabel)

        this.add(bottomPanel, BorderLayout.SOUTH) // Alt kısmına ekledik

        pageNames = config.pages.map { it.pageName }
        config.pages.forEach { page ->
            mainPanel.add(page.pageName, page.clazz.getConstructor().newInstance())
        }

        currentPageIndex = 0
        updateCurrentPage(0)

        Thread {
            try {
                this.findExistGame()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }.start()
    }

    private fun createModernButton(text: String, onClick: () -> Unit): JComponent {
        return object : JComponent() {
            var hovered = false
            var color = Color(40, 40, 40)
            val hoverColor = Color(60, 60, 60)
            val baseColor = Color(40, 40, 40)

            init {
                preferredSize = Dimension(160, 45)
                isOpaque = false
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

                addMouseListener(object : MouseAdapter() {
                    override fun mouseEntered(e: MouseEvent) { hovered = true }
                    override fun mouseExited(e: MouseEvent) { hovered = false }
                    override fun mouseClicked(e: MouseEvent) { onClick() }
                })

                Timer(16) {
                    val r = approach(color.red, if (hovered) hoverColor.red else baseColor.red)
                    val g = approach(color.green, if (hovered) hoverColor.green else baseColor.green)
                    val b = approach(color.blue, if (hovered) hoverColor.blue else baseColor.blue)
                    color = Color(r, g, b)
                    repaint()
                }.start()
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Drop shadow
                g2.color = Color(0, 0, 0, 100)
                g2.fillRoundRect(3, 3, width - 6, height - 6, 18, 18)

                // Main button
                g2.color = color
                g2.fillRoundRect(0, 0, width, height, 18, 18)

                // Text
                g2.color = Color(230, 230, 230)
                g2.font = Font("Segoe UI", Font.PLAIN, 15)
                val fm = g2.fontMetrics
                val tw = fm.stringWidth(text)
                val th = fm.ascent
                g2.drawString(text, (width - tw) / 2, (height + th) / 2 - 2)
            }
        }
    }


    private fun approach(current: Int, target: Int, speed: Int = 10): Int {
        return when {
            current < target -> min(current + speed, target)
            current > target -> max(current - speed, target)
            else -> current
        }
    }

    private fun updateCurrentPage(delta: Int) {
        currentPageIndex = (currentPageIndex + delta).coerceIn(0, pageNames.size - 1)
        val name = pageNames.getOrNull(currentPageIndex)?.let { formatPageName(it) } ?: "Unknown"
        currentPageLabel.text = "Page: $name"
    }

    private fun formatPageName(name: String): String {
        return name.replace(Regex("([a-z])([A-Z])"), "$1 $2") // PascalCase → Pascal Case
            .replaceFirstChar { it.uppercaseChar() }
    }

    private fun findExistGame() {
        try {
            val java = findJava(getMainClass(null))
            if (java != null) {
                val pid = java.id()
                log.info("Exist game process found! Pid: $pid")
                gamePid.set(pid.toLong())
                GameStartEvent(gamePid.get()).call()
                JOptionPane.showMessageDialog(
                    this,
                    f.format("gui.launcher.game.exist.message", pid),
                    f.getString("gui.launcher.game.exist.title"),
                    JOptionPane.INFORMATION_MESSAGE
                )
                java.detach()
            }
        } catch (e: AttachNotSupportedException) {
            log.error("Failed to find the game process, is launched with the official launcher? (attach not support)")
            log.error(e.stackTraceToString())
        }
    }

    private fun setIconImage(name: String) {
        this.iconImage = ImageIcon("/images/icons/$name.png".getInputStream()!!.readAllBytes()).image
    }

    private fun resetIcon() {
        when (config.theme) {
            "light" -> this.setIconImage("icon-dark")
            "dark" -> this.setIconImage("icon-light")
            else -> this.setIconImage("icon-light")
        }
    }

    @EventTarget
    fun onGameStart(e: GameStartEvent) {
        this.setIconImage("running")
    }

    @EventTarget
    fun onGameTerminate(e: GameTerminateEvent) {
        this.resetIcon()
    }

    @EventTarget
    fun onAuth(e: AuthEvent) {
        log.info("Request for login")
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(e.authURL.toString()), null)
        val link = JOptionPane.showInputDialog(
            this,
            f.getString("gui.launcher.auth.message"),
            f.getString("gui.launcher.auth.title"),
            JOptionPane.QUESTION_MESSAGE
        )
        e.put(link)
    }

    companion object {
        val statusBar = StatusBar()
        private val log: Logger = LoggerFactory.getLogger(GuiLauncher::class.java)
    }
}
