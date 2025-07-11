package net.pvproom.client.event.impl

import net.pvproom.client.event.Event
import java.io.File

class FileDownloadEvent(val file: File, val type: Type) : Event() {
    enum class Type {
        START,
        SUCCESS, FAILURE
    }
}
