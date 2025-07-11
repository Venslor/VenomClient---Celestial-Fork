package net.pvproom.client.game.addon

import net.pvproom.client.config
import net.pvproom.client.game.BaseAddon
import net.pvproom.client.toFile
import java.io.File
import java.util.*

class FabricMod(val file: File) : BaseAddon() {
    override fun toString(): String {
        return file.name
    }

    override val isEnabled: Boolean
        get() =// TODO fabric: isEnabled
            true

    override fun toggle(): Boolean {
        return toggle(file)
    }

    companion object {
        val modFolder: File = config.installationDir.toFile().resolve("mods")


        fun findAll(): List<FabricMod> {
            val list: MutableList<FabricMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar") && file.isFile) {
                        list.add(FabricMod(file))
                    }
                }
            }
            return list
        }


        fun add(file: File?): FabricMod? {
            val target = autoCopy(file!!, modFolder)
            return if ((target == null)) null else FabricMod(target)
        }
    }
}
