package net.pvproom.client.gui.pages

import net.pvproom.client.config
import net.pvproom.client.f
import net.pvproom.client.readOnly
import net.pvproom.client.toJTextArea
import net.pvproom.client.utils.GitUtils.branch
import net.pvproom.client.utils.GitUtils.buildUser
import net.pvproom.client.utils.GitUtils.buildUserEmail
import net.pvproom.client.utils.GitUtils.buildVersion
import net.pvproom.client.utils.GitUtils.commitMessage
import net.pvproom.client.utils.GitUtils.commitTime
import net.pvproom.client.utils.GitUtils.getCommitId
import net.pvproom.client.utils.GitUtils.remote
import java.awt.Color
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.border.TitledBorder


class GuiAbout : JPanel() {
    init {
        this.name = "about"
        this.border = TitledBorder(
            null,
            f.getString("gui.about.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color(180, 180, 255)
        )
        val env = String.format(
            """
                
                Venom v%s (Running on Java %s)
                Data sharing state: %s
                -----
                Git build info:
                    Build user: %s
                    Email: %s
                    Remote (%s): %s
                    Commit time: %s
                    Commit: %s
                    Commit Message: %s
                
                """.trimIndent(),
            buildVersion,
            System.getProperty("java.version"),
            if (config.dataSharing) "turn on" else "turn off",
            buildUser,
            buildUserEmail,
            branch,
            remote,
            commitTime,
            getCommitId(true),
            commitMessage
        )

        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val textArea = (f.getString("gui.about") + "\n" + env).toJTextArea().readOnly()
        this.add(textArea)
    }
}
