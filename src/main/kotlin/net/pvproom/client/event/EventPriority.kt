package net.pvproom.client.event

object EventPriority {
    const val FIRST: Byte = 0
    const val SECOND: Byte = 1
    const val THIRD: Byte = 2
    const val FOURTH: Byte = 3
    const val FIFTH: Byte = 4

    val valueArray: ByteArray = byteArrayOf(FIRST, SECOND, THIRD, FOURTH, FIFTH)
}
