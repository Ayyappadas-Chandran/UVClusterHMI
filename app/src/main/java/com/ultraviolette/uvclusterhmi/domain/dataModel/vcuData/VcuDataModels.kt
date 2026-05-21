package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

import com.ultraviolette.uvclusterhmi.domain.ennumerate.VcuMiscFlags
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.domain.ennumerate.VcuStatusFlags


data class VcuMiscInfo(
    val data: IntArray = IntArray(4)
) {
    // Helper to check bits directly on the object
    fun hasFlag(flagIndex: Int): Boolean {
        val arrayIndex = flagIndex / 32
        val bitIndex = flagIndex % 32
        if (arrayIndex >= data.size) return false
        return ((data[arrayIndex] shr bitIndex) and 1) == 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VcuMiscInfo
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int = data.contentHashCode()
}

// This lets you do: vcuInfoMsg.hasFlag(VcuStatusFlags.SIDE_STAND)
fun VcuInfoMsg.hasFlag(flagIndex: Int): Boolean {
    // Stitch the High and Low UInts into a single Long
    val fullStatus = (this.vcuStatusH.toULong() shl 32) or (this.vcuStatusL.toULong())
    return ((fullStatus shr flagIndex) and 1uL) == 1uL
}

// BMS Flag Helper (Stitched from statusH + statusL)
fun VcuInfoMsg.hasBmsFlag(flagIndex: Int): Boolean {
    // statusH is high word, statusL is low word
    val fullStatus = (this.statusH.toULong() shl 32) or (this.statusL.toULong())
    return ((fullStatus shr flagIndex) and 1uL) == 1uL
}
