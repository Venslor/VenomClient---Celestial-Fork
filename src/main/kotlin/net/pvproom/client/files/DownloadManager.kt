package net.pvproom.client.files

import cn.hutool.crypto.SecureUtil
import org.apache.commons.io.FileUtils
import net.pvproom.client.config
import net.pvproom.client.configDir
import net.pvproom.client.event.impl.FileDownloadEvent
import net.pvproom.client.gui.GuiLauncher
import net.pvproom.client.runningOnGui
import net.pvproom.client.utils.RequestUtils.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object DownloadManager {
    val cacheDir: File = File(configDir, "cache")
    private val log: Logger = LoggerFactory.getLogger(DownloadManager::class.java)
    private var pool: ExecutorService? = null

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }


    fun waitForAll() {
        if (pool == null) return
        pool!!.shutdown()
        while (!pool!!.awaitTermination(1, TimeUnit.SECONDS)) {
            Thread.onSpinWait()
        }
        // create a new pool (on invoking download)
        pool = null
    }


    /**
     * Cache something
     *
     * @param url      url to the target file (online)
     * @param name     file name
     * @param override allow override?
     * @return status (true=success, false=failure)
     */
    fun cache(url: URL, name: String, override: Boolean): Boolean {
        val file = File(cacheDir, name)
        if (file.exists() && !override) {
            return true
        }
        log.info("Caching $name (from $url)")
        // download
        return download0(url, file)
    }

    /**
     * Download a file
     *
     * @param url url to the target file (online)
     * @param file file instance of the local file
     * @return is success
     */

    fun download0(url: URL, file: File, crcSha: String?, type: Downloadable.Type): Boolean {
        // connect
        if (file.isFile && crcSha != null) {
            // compare hash
            if (compareHash(file, crcSha, type)) {
                return true
            }
        }
        FileDownloadEvent(file, FileDownloadEvent.Type.START).call()
        log.info("Downloading $url to $file")
        get(url).execute().use { response ->
            if (!response.isSuccessful || response.body == null) {
                FileDownloadEvent(file, FileDownloadEvent.Type.FAILURE).call()
                return false
            }
            val bytes = response.body!!.bytes()
            FileUtils.writeByteArrayToFile(file, bytes)
        }
        if (runningOnGui) GuiLauncher.statusBar.text = "Download " + file.name + " success."
        if (crcSha != null) {
            val result = compareHash(file, crcSha, type)
            if (!result) {
                FileDownloadEvent(file, FileDownloadEvent.Type.FAILURE).call()
            }
            return result
        }
        FileDownloadEvent(file, FileDownloadEvent.Type.SUCCESS).call()
        return true
    }

    private fun compareHash(file: File, crcSha: String, type: Downloadable.Type): Boolean {
        return type == Downloadable.Type.SHA1 && SecureUtil.sha1(file) == crcSha || type == Downloadable.Type.SHA256 && SecureUtil.sha256(
            file
        ) == crcSha
    }


    private fun download0(url: URL, file: File): Boolean {
        return download0(url, file, null, Downloadable.Type.SHA1)
    }


    fun download(downloadable: Downloadable) {
        if (pool == null || pool!!.isTerminated) {
            pool = Executors.newWorkStealingPool(config.maxThreads)
        }
        pool!!.execute(downloadable)
    }
}
