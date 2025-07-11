package net.pvproom.client.event.impl

import net.pvproom.client.event.Event

class ChangeConfigEvent<T>(val configObject: Any, val key: String, val newValue: T?, val oldValue: T? = null) : Event() 