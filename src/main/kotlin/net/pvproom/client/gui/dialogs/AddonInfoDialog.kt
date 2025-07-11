package net.pvproom.client.gui.dialogs

import net.pvproom.client.*
import net.pvproom.client.files.DownloadManager
import net.pvproom.client.files.Downloadable
import net.pvproom.client.game.RemoteAddon
import net.pvproom.client.gui.layouts.VerticalFlowLayout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.File
import java.net.URI
import javax.swing.*
import javax.swing.border.TitledBorder

/**
 * @param addon remote addon
 * @param file path to save the addon
 * */
class AddonInfoDialog(val addon: RemoteAddon, val file: File) : JDialog() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AddonInfoDialog::class.java)
    }

    private val panel = JPanel()

    init {
        this.title = f.getString("gui.plugins.info.title")
        this.setSize(600, 600)
        this.panel.layout = VerticalFlowLayout()
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        this.panel.add(JLabel(f.format("gui.plugins.info.name", addon.name)))
        this.panel.add(JLabel(f.format("gui.plugins.info.category", addon.category)))
        val exist = JLabel(f.getString("gui.plugins.exist"))

        val btnDownload = JButton(f.getString("gui.plugins.download"))
        btnDownload.addActionListener {
            DownloadManager.download(Downloadable(addon.downloadURL, file, addon.sha1))
            Thread {
                try {
                    DownloadManager.waitForAll()
                    exist.isVisible = file.exists()
                } catch (err: InterruptedException) {
                    throw RuntimeException(err)
                }
            }.start()
        }

        this.panel.add(exist)
        exist.isVisible = file.exists()

        this.panel.add(btnDownload)
        this.panel.add(JSeparator())

        val metaInfo = JPanel()
        metaInfo.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        metaInfo.border = TitledBorder(
            null,
            f.getString("gui.plugins.info.meta"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color(180, 180, 255)
        )
        if (addon.meta == null) {
            metaInfo.add(JLabel(f.getString("gui.plugins.info.meta.notfound")))
        } else {
            val meta = addon.meta
            metaInfo.add(JLabel(f.format("gui.plugins.info.meta.name", meta.name)))
            metaInfo.add(JLabel(f.format("gui.plugins.info.meta.version", meta.version)))
            metaInfo.add(JLabel(f.format("gui.plugins.info.meta.description", meta.description)))
            metaInfo.add(JLabel(f.format("gui.plugins.info.meta.authors", meta.authors.getAuthorsString())))
            if (meta.website != null) metaInfo.add(
                createOpenWebsiteButton(
                    f.getString("gui.plugins.info.meta.website"),
                    meta.website.toURI()
                )
            )
            if (meta.repository != null) metaInfo.add(
                createOpenWebsiteButton(
                    f.getString("gui.plugins.info.meta.repo"),
                    meta.repository.toURI()
                )
            )
            if (!meta.dependencies.isNullOrEmpty()) {
                val dependencies = JTextArea()
                dependencies.border = TitledBorder(
                    null,
                    f.getString("gui.plugins.info.meta.dependencies"),
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    null,
                    Color(180, 180, 255)
                )
                dependencies.isEditable = false
                val sb = StringBuilder()
                meta.dependencies.forEachIsEnd { it, isEnd ->
                    sb.append(it)
                    if (!isEnd) sb.append("\n")
                }
                metaInfo.add(dependencies.withScroller())
            }
        }
        this.panel.add(metaInfo)
        this.add(this.panel.withScroller())
    }

    private fun createOpenWebsiteButton(text: String, uri: URI): JButton {
        val button = JButton(text)
        button.addActionListener {
            uri.open()
        }
        return button
    }
}

private fun Array<String>.getAuthorsString(): String {
    val sb = StringBuilder()
    this.forEachIndexed { index, author ->
        if (index != 0) {
            if (index != this.size - 1) sb.append(", ") else sb.append(" and ")
        }
        sb.append(author)
        if (this.size == 1) return@forEachIndexed
    }
    return sb.toString()
}


