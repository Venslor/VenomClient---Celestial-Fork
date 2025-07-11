package net.pvproom.client.event.impl

import net.pvproom.client.event.Event
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class AuthEvent(val authURL: URL) : Event() {
    private var result = ""
    private val responded = AtomicBoolean(false)

    fun waitForAuth(): String {
        while (!responded.get()) {
            Thread.onSpinWait()
        }
        return result
    }

    fun put(url: String?) {
        if (url != null) {
            this.result = url
        }
        responded.set(true)
    }
}
