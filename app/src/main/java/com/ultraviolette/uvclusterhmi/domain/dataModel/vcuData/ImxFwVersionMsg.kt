package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class ImxFwVersionMsg(
    val bmsFw: ByteArray = byteArrayOf(),
    val vcuFw: ByteArray = byteArrayOf(),
    val mcSwVersion: ByteArray = byteArrayOf(),
    val mcHwVersion: ByteArray = byteArrayOf(),
    val padding: ByteArray = byteArrayOf(),
    val mcProductCode: UInt = 0u,
    val mcDcfChecksum: UShort = 0u,
    val displayFw: ByteArray = byteArrayOf(),
    val chgFw: ByteArray = byteArrayOf(),
    val chargerType: UByte = 0u,
    val extChgFwVersion: UByte = 0u
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImxFwVersionMsg

        if (!bmsFw.contentEquals(other.bmsFw)) return false
        if (!vcuFw.contentEquals(other.vcuFw)) return false
        if (!mcSwVersion.contentEquals(other.mcSwVersion)) return false
        if (!mcHwVersion.contentEquals(other.mcHwVersion)) return false
        if (!padding.contentEquals(other.padding)) return false
        if (mcProductCode != other.mcProductCode) return false
        if (mcDcfChecksum != other.mcDcfChecksum) return false
        if (!displayFw.contentEquals(other.displayFw)) return false
        if (!chgFw.contentEquals(other.chgFw)) return false
        if (chargerType != other.chargerType) return false
        if (extChgFwVersion != other.extChgFwVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bmsFw.contentHashCode()
        result = 31 * result + vcuFw.contentHashCode()
        result = 31 * result + mcSwVersion.contentHashCode()
        result = 31 * result + mcHwVersion.contentHashCode()
        result = 31 * result + padding.contentHashCode()
        result = 31 * result + mcProductCode.hashCode()
        result = 31 * result + mcDcfChecksum.hashCode()
        result = 31 * result + displayFw.contentHashCode()
        result = 31 * result + chgFw.contentHashCode()
        result = 31 * result + chargerType.hashCode()
        result = 31 * result + extChgFwVersion.hashCode()
        return result
    }
}