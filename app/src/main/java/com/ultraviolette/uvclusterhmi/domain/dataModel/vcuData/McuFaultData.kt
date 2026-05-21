package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class McuFaultData(
    val data: IntArray = IntArray(2)
) {
    companion object {
        private const val BITS_PER_INT = 32
        private const val MAX_BITS = 2 * BITS_PER_INT
    }

    // Helper to check bits directly on the object
    fun hasFlag(flagIndex: Int): Boolean {
        if (flagIndex !in 0 until MAX_BITS) return false

        val arrayIndex = flagIndex / BITS_PER_INT
        val bitIndex = flagIndex % BITS_PER_INT

        return ((data[arrayIndex] shr bitIndex) and 1) == 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is McuFaultData) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int = data.contentHashCode()
}
