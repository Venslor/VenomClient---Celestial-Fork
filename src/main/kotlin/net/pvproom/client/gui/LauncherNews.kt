package net.pvproom.client.gui

import cn.hutool.crypto.SecureUtil
import net.pvproom.client.*
import net.pvproom.client.files.DownloadManager.cacheDir
import net.pvproom.client.utils.lunar.Alert
import net.pvproom.client.utils.lunar.Blogpost
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Image
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.swing.*
import javax.swing.border.TitledBorder

private val log: Logger = LoggerFactory.getLogger(LauncherNews::class.java)

class LauncherNews(private val blogPost: Blogpost) : JPanel() {
    private val image = File(cacheDir, "news/" + SecureUtil.sha1(blogPost.title))

    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.border = TitledBorder(
            null,
            blogPost.title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color(180, 180, 255)
        )
        this.initGui()
    }

    private fun initGui() {
        val isMoonsworth = blogPost.excerpt == null

        val textLabel: JLabel = if (isMoonsworth) {
            blogPost.title.toJLabel()
        } else {
            (blogPost.excerpt + " - " + blogPost.author).toJLabel()
        }

        //this.add(textLabel)

        val image = ImageIcon(image.path)
        val imageLabel =
            JLabel(ImageIcon(image.image.getScaledInstance(400, 200, Image.SCALE_DEFAULT)), SwingConstants.CENTER)
        this.add(imageLabel)

        val text = if (isMoonsworth) {
            "View"
        } else {
            blogPost.buttonText ?: "View"
        }

        val button = JButton(text)
        button.addActionListener {
            when (blogPost.type) {
                Blogpost.ButtonType.OPEN_LINK -> blogPost.link.toURI().open()
                Blogpost.ButtonType.CHANGE_API -> {
                    if (JOptionPane.showConfirmDialog(
                            this,
                            f.getString("gui.news.api.confirm").format(blogPost.link),
                            "Confirm",
                            JOptionPane.YES_NO_OPTION
                        ) == JOptionPane.OK_OPTION
                    ) {
                        config.api.address = blogPost.link // set api
                        log.info("Change API into ${config.api}")
                        JOptionPane.showMessageDialog(
                            this,
                            f.getString("gui.news.api.reopen"),
                            "Reopen needed",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }
                }

                null -> JOptionPane.showMessageDialog(
                    this,
                    f.getString("gui.news.empty"),
                    "A Joke",
                    JOptionPane.INFORMATION_MESSAGE
                ) // do nothing
            }
        }
        this.add(button)

        textLabel.labelFor = imageLabel
    }
}

class LauncherAlert(alert: Alert) : JPanel() {
    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.border = TitledBorder(
            null,
            f.getString("gui.news.alert.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color(180, 180, 255)
        )
        alert.text?.let {
            this.add(it.toJLabel())
        }
        alert.link?.let { link ->
            this.add(JButton("View").apply {
                addActionListener {
                    link.toURI().open()
                }
            })
        }
    }
}

class LauncherBirthday(birthday: Int) : JPanel() {
    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.border = TitledBorder(
            null,
            f.getString("gui.news.birthday.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color(180, 180, 255)
        )

        val today = LocalDate.now()
        val nextBirthday = LocalDate.of(today.year + 1, 7, 29)
        val betweenNext = ChronoUnit.DAYS.between(today, nextBirthday).toInt()

        if (birthday == 0) {
            this.add(f.getString("gui.news.birthday.today").toJLabel())
        } else if (birthday == 1) {
            this.add(f.getString("gui.news.birthday.tomorrow").toJLabel())
        } else if (birthday == -1) {
            this.add(f.getString("gui.news.birthday.yesterday").format(betweenNext).toJLabel())
        } else if (birthday > 0) {
            this.add(f.getString("gui.news.birthday.coming").format(birthday).toJLabel())
        } else {
            this.add(f.getString("gui.news.birthday.after").format(birthday, betweenNext).toJLabel())
        }
    }
}
