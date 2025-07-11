package net.pvproom.client.game

import org.jetbrains.annotations.Contract
import java.net.URL

data class RemoteAddon(var name: String, var downloadURL: URL, var sha1: String, var category: Category, val meta: AddonMeta?) {
    enum class Category {
        AGENT, CN, WEAVE;

        companion object {
            /**
             * Parse plugin type from a string
             */
            @Contract(pure = true)
            fun parse(category: String): Category? {
                return when (category) {
                    "cn" -> CN
                    "weave", "Mod" -> WEAVE
                    "Agent" -> AGENT
                    else -> null
                }
            }
        }
    }

    override fun toString(): String {
        return "RemoteAddon(name='$name', downloadURL=$downloadURL, category=$category, meta=$meta)"
    }
}
