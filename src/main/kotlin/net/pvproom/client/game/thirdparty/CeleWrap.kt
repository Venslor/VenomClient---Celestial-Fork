package net.pvproom.client.game.thirdparty

import net.pvproom.client.configDir
import net.pvproom.client.utils.downloadLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object CeleWrap {
    private val log: Logger = LoggerFactory.getLogger(CeleWrap::class.java)
    val installation = configDir.resolve("celewrap.jar") // celewrap library
    const val MAIN_CLASS = "net.pvproom.client.CeleWrapKt"

    fun checkUpdate() : Boolean {
        log.info("Checking update for CeleWrap")
        return downloadLoader("CubeWhyMC/celewrap", installation)
    }
}