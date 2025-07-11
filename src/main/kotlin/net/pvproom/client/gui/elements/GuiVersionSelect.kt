package net.pvproom.client.gui.elements

import com.sun.tools.attach.AttachNotSupportedException
import org.apache.commons.io.FileUtils
import net.pvproom.client.*
import net.pvproom.client.event.EventManager
import net.pvproom.client.event.EventTarget
import net.pvproom.client.event.impl.APIReadyEvent
import net.pvproom.client.event.impl.GameStartEvent
import net.pvproom.client.event.impl.GameTerminateEvent
import net.pvproom.client.files.DownloadManager.waitForAll
import net.pvproom.client.game.GameProperties
import net.pvproom.client.game.LaunchCommandJson
import net.pvproom.client.game.addon.LunarCNMod
import net.pvproom.client.game.addon.WeaveMod
import net.pvproom.client.game.thirdparty.LunarQT
import net.pvproom.client.gui.GuiLauncher.Companion.statusBar
import net.pvproom.client.utils.CrashReportType
import net.pvproom.client.utils.findJava
import net.pvproom.client.utils.lunar.GameArtifactInfo
import net.pvproom.client.utils.lunar.LauncherData.Companion.getMainClass
import net.pvproom.client.utils.lunar.LauncherData.Companion.getSupportModules
import net.pvproom.client.utils.lunar.LauncherData.Companion.getSupportVersions
import net.pvproom.client.utils.saveFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.GridLayout
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.filechooser.FileNameExtensionFilter

private val log: Logger = LoggerFactory.getLogger(GuiVersionSelect::class.java)

class GuiVersionSelect : JPanel() {
    private val versionSelect = JComboBox<String>()
    private val moduleSelect = JComboBox<String>()
    private val branchInput = JTextField()
    private var isFinishOk = false
    private val btnOnline: JButton = JButton(f.getString("gui.version.online"))
    private val btnOffline: JButton = JButton(f.getString("gui.version.offline"))
    private var isLaunching = false

    init {
        EventManager.register(this)

        val titleFont = Font("Segoe UI", Font.BOLD, 14)
        val titleColor = Color(140, 170, 255)

        val titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color(70, 70, 70), 1, true),
            f.getString("gui.version-select.title")
        ).apply {
            this.titleFont = titleFont
            this.titleColor = titleColor
            this.titleJustification = TitledBorder.LEFT
            this.titlePosition = TitledBorder.ABOVE_TOP
        }

        this.border = BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(12, 16, 12, 16) // İç boşluk
        )

        this.background = Color(40, 40, 40)

        this.layout = GridLayout(5, 2, 10, 10)
    }


    @EventTarget
    fun onAPIReady(e: APIReadyEvent) {
        this.removeAll()
        this.isFinishOk = false
        versionSelect.removeAllItems()
        initGui()
    }

    private fun initGui() {
        this.add(JLabel(f.getString("gui.version-select.label.version")))
        this.add(versionSelect)
        //this.add(JLabel(f.getString("gui.version-select.label.module")))
        //this.add(moduleSelect)
        //this.add(JLabel(f.getString("gui.version-select.label.branch")))
        //this.add(branchInput)

        val map = getSupportVersions(metadata)
        @Suppress("UNCHECKED_CAST") val supportVersions: List<String> = map["versions"] as ArrayList<String>

        val sortedVersions = supportVersions.sortedWith { v1, v2 ->
            val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
            val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
            val maxLength = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLength) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                if (p1 != p2) return@sortedWith p1 - p2
            }
            return@sortedWith 0
        }

        for (version in sortedVersions) {
            versionSelect.addItem(version)
        }

        versionSelect.addActionListener {
            try {
                refreshModuleSelect(this.isFinishOk)
                if (this.isFinishOk) {
                    saveVersion()
                }
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }

        moduleSelect.addActionListener {
            if (this.isFinishOk) {
                saveModule()
            }
        }
        refreshModuleSelect(false)
        // get is first launch
        if (config.game.target == null) {
            val game = GameVersionInfo(
                versionSelect.selectedItem as String,
                moduleSelect.selectedItem as String,
                "master"
            )
            config.game.target = game
            versionSelect.selectedItem = map["default"]
        }
        initInput(versionSelect, moduleSelect, branchInput)
        isFinishOk = true

        // add launch buttons
        btnOnline.addActionListener {
            try {
                this.online()
            } catch (e: Exception) {
                log.error(e.stackTraceToString())
            }
        }
        this.add(btnOnline)

        this.add(btnOffline)
        btnOffline.addActionListener {
            try {
                this.offline()
            } catch (e: IOException) {
                log.error(e.stackTraceToString())
            } catch (e: InterruptedException) {
                log.error(e.stackTraceToString())
            } catch (ignored: AttachNotSupportedException) {
                log.warn("Failed to attach to the game process")
            }
        }

        /*val btnWipeCache = JButton(f.getString("gui.version.cache.wipe"))

        btnWipeCache.addActionListener {
            if (JOptionPane.showConfirmDialog(
                    this,
                    f.getString("gui.version.cache.warn"),
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
            ) {
                statusBar.text = f.getString("gui.version.cache.start")
                try {
                    if (wipeCache(null)) {
                        statusBar.text = f.getString("gui.version.cache.success")
                    } else {
                        statusBar.text = f.getString("gui.version.cache.failure")
                    }
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }
            }
        }
        this.add(btnWipeCache)

        val btnFetchJson = JButton(f.getString("gui.version.fetch"))

        btnFetchJson.addActionListener {
            // open file save dialog
            val file = saveFile(FileNameExtensionFilter("Json (*.json)", "json"))
            file?.apply {
                log.info("Fetching version json...")
                val json = launcherData.getVersion(
                    versionSelect.selectedItem as String,
                    branchInput.text,
                    moduleSelect.selectedItem as String,
                )
                var file1 = this
                if (!this.name.endsWith(".json")) {
                    file1 = file + ".json" // add extension
                }
                log.info("Fetch OK! Dumping to ${file1.path}")
                FileUtils.write(file1, JSON.encodeToString(GameArtifactInfo.serializer(), json), StandardCharsets.UTF_8)
            }
        }

        this.add(btnFetchJson)*/
    }


    private fun beforeLaunch() {
        if (gamePid.get() != 0L) {
            if (findJava(/*if (config.celeWrap.state) CeleWrap.MAIN_CLASS else */getMainClass(null)) != null) {
                JOptionPane.showMessageDialog(
                    this,
                    f.getString("gui.version.launched.message"),
                    f.getString("gui.version.launched.title"),
                    JOptionPane.WARNING_MESSAGE
                )
            } else {
                gamePid.set(0)
                statusBar.isRunningGame = false
            }
        }
        // check update for loaders
        val weave = config.addon.weave
        val cn = config.addon.lunarcn
        var checkUpdate = false

        try {
            if (weave.state && weave.checkUpdate) {
                log.info("Checking update for Weave loader")
                checkUpdate = WeaveMod.checkUpdate()
            }
            if (cn.state && cn.checkUpdate) {
                log.info("Checking update for LunarCN loader")
                checkUpdate = LunarCNMod.checkUpdate()
            }
        } catch (e: Exception) {
            log.error("Failed to check loader updates")
            log.error(e.stackTraceToString())
            if (!config.proxy.mirror.containsKey("github.com:443") && JOptionPane.showConfirmDialog(
                    this,
                    f.getString("gui.proxy.suggest.gh"),
                    "Apply GitHub Mirror",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
            ) {
                log.info("Applying GitHub mirror")
                // TODO github.ink is died
                config.proxy.mirror["github.com:443"] = "github.ink:443"
            }
        }

        try {
            if (config.addon.lcqt.state && config.addon.lcqt.checkUpdate) {
                log.info("Checking update for LunarQT")
                checkUpdate = LunarQT.checkUpdate()
            }
        } catch (e: Exception) {
            log.error("Failed to check lcqt updates")
            log.error(e.stackTraceToString())
        }

        if (checkUpdate) {
            statusBar.text = f.getString("gui.addon.update")
            waitForAll()
        }
    }

    @EventTarget
    fun onGameStart(event: GameStartEvent) {
        statusBar.text = f.format("status.launch.started", event.pid)
    }

    @EventTarget
    fun onGameTerminate(event: GameTerminateEvent) {
        statusBar.text = f.getString("status.launch.terminated")
        if (event.code != 0) {
            // upload crash report
            statusBar.text = f.getString("status.launch.crashed")
            log.info("Client looks crashed (code ${event.code})")
            try {
                if (config.dataSharing) {
                    val trace = FileUtils.readFileToString(launcherLogFile, StandardCharsets.UTF_8)
                    val script = FileUtils.readFileToString(launchJson, StandardCharsets.UTF_8)
                    val result = launcherData.uploadCrashReport(trace, CrashReportType.GAME, script)
                    if (result != null) {
                        val url = result.url
                        val id = result.id
                        JOptionPane.showMessageDialog(
                            this,
                            String.format(
                                f.getString("gui.message.clientCrash1"),
                                id,
                                url,
                                launcherLogFile.path,
                                f.getString("gui.version.crash.tip")
                            ),
                            "Game crashed!",
                            JOptionPane.ERROR_MESSAGE
                        )
                    } else {
                        throw RuntimeException("Failed to upload crash report")
                    }
                } else {
                    throw UnsupportedOperationException("Unsupported")
                }
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    String.format(
                        f.getString("gui.message.clientCrash2"),
                        launcherLogFile.path,
                        f.getString("gui.version.crash.tip")
                    ),
                    "Game crashed!",
                    JOptionPane.ERROR_MESSAGE
                )
                if (e !is UnsupportedOperationException) {
                    throw RuntimeException(e)
                }
            }
        }
    }


    private fun online() {
        beforeLaunch()
        val version = versionSelect.selectedItem as String
        val module = moduleSelect.selectedItem as String
        val branch = branchInput.text
        val launchCommand = getArgs(
            version, branch, module, config.installationDir.toFile(),
            gameProperties = GameProperties(
                config.game.resize.width,
                config.game.resize.height,
                File(config.game.gameDir)
            )
        )
        // save launch command
        log.info("Saving launch command to $launchJson")
        launchJson.writeText(
            JSON.encodeToString(
                LaunchCommandJson.serializer(),
                LaunchCommandJson.create(launchCommand)
            )
        )
        log.info("Generating launch scripts...")
        launchScript.writeText(generateScripts())

        Thread {
            isLaunching = true
            statusBar.text = f.getString("status.launch.begin")
            try {
                checkUpdate(
                    (versionSelect.selectedItem as String),
                    moduleSelect.selectedItem as String,
                    branchInput.text
                )
            } catch (e: Exception) {
                log.error("Failed to check update")
                val trace = e.stackTraceToString()
                log.error(trace)
                JOptionPane.showMessageDialog(
                    null,
                    f.format("gui.check-update.error.message", trace),
                    f.getString("gui.check-update.error.title"),
                    JOptionPane.ERROR_MESSAGE
                )
            }
            waitForAll()
            log.info("Everything is OK, starting game...")
            isLaunching = false
            launch(launchCommand).waitFor()
        }.start()
    }

    private fun offline() {
        beforeLaunch()
        Thread {
            statusBar.text = f.getString("status.launch.call-process")
            launchPrevious().waitFor()
        }.start()
    }

    private fun initInput(versionSelect: JComboBox<String>, moduleSelect: JComboBox<String>, branchInput: JTextField) {
        val game = config.game.target!!
        versionSelect.selectedItem = game.version
        moduleSelect.selectedItem = game.module
        branchInput.text = game.branch
    }

    private fun saveVersion() {
        val version = versionSelect.selectedItem as String
        log.info("Select version -> $version")
        config.game.target?.version = version
    }

    private fun saveModule() {
        if (moduleSelect.selectedItem == null) {
            return
        }
        val module = moduleSelect.selectedItem as String
        log.info("Select module -> $module")
        config.game.target?.module = module
    }


    private fun refreshModuleSelect(reset: Boolean) {
        moduleSelect.removeAllItems()
        if (versionSelect.selectedItem == null) {
            return
        }
        val map = getSupportModules(metadata, (versionSelect.selectedItem as String))
        @Suppress("UNCHECKED_CAST") val modules: List<String> = map["modules"] as ArrayList<String>
        val defaultValue = map["default"] as String?
        for (module in modules) {
            moduleSelect.addItem(module)
        }
        if (reset) {
            moduleSelect.selectedItem = defaultValue
        }
    }
}

fun File.unzipNatives(baseDir: File) {
    log.info("Unzipping natives ${this.path}")
    val dir = File(baseDir, "natives")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    this.toZip().unzip(dir)
    log.info("Natives unzipped successful")
}

fun File.unzipUi(baseDir: File) {
    log.info("Unzipping ui.zip ${this.path}")
    val dir = File(baseDir, "ui")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    this.toZip().unzip(dir)
    log.info("Ui unzipped successful")
}

private operator fun File.plus(s: String): File {
    return File(this.path + s)
}
