package net.pvproom.client.game.addon

import net.pvproom.client.config
import net.pvproom.client.configDir
import net.pvproom.client.f
import net.pvproom.client.game.BaseAddon
import net.pvproom.client.gui.GuiLauncher
import net.pvproom.client.toFile
import net.pvproom.client.utils.downloadLoader
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class LunarCNMod(val file: File) : BaseAddon() {
    override fun toString(): String {
        return file.name
    }

    override val isEnabled: Boolean
        get() = file.name.endsWith(".jar")

    override fun toggle(): Boolean {
        return toggle(file)
    }

    companion object {
        val modFolder: File = File(configDir, "mods")
        private val log: Logger = LoggerFactory.getLogger(LunarCNMod::class.java)

        init {
            if (modFolder.mkdirs()) {
                log.info("Making lunarCN mods folder")
            }
        }

        /**
         * Find all mods in the lunarcn mods folder
         */
        @Contract(pure = true)
        fun findEnabled(): MutableList<LunarCNMod> {
            val list: MutableList<LunarCNMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar") && file.isFile) {
                        list.add(LunarCNMod(file))
                    }
                }
            }
            return list
        }

        fun findDisabled(): List<LunarCNMod> {
            val list: MutableList<LunarCNMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar.disabled") && file.isFile) {
                        list.add(LunarCNMod(file))
                    }
                }
            }
            return list
        }


        fun findAll(): List<LunarCNMod> {
            val list = findEnabled()
            list.addAll(findDisabled())
            return Collections.unmodifiableList(list)
        }


        val installation: File
            get() = config.addon.lunarcn.installationDir.toFile()


        fun add(file: File?): LunarCNMod? {
            val target = autoCopy(file!!, modFolder)
            return if ((target == null)) null else LunarCNMod(target)
        }


        fun checkUpdate(): Boolean {
            log.info("Updating LunarCN Loader...")
            GuiLauncher.statusBar.text = f.getString("gui.addon.mods.cn.warn")
            return downloadLoader(
                "CubeWhyMC/LunarClient-CN",
                config.addon.lunarcn.installationDir.toFile()
            )
        }
    }
}
