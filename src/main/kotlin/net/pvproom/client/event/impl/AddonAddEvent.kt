package net.pvproom.client.event.impl

import net.pvproom.client.event.Event
import net.pvproom.client.game.AddonType
import net.pvproom.client.game.BaseAddon

class AddonAddEvent(val type: AddonType, val addon: BaseAddon) : Event() {
}
