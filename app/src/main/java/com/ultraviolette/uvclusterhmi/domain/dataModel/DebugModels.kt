package com.ultraviolette.uvclusterhmi.domain.dataModel

// For the left column (big values + normal key-value pairs)
data class TelemetryItem(
    val label: String,
    val value: String,
    val isBig: Boolean = false
) {
    companion object {
        fun fromRaw(label: String, rawValue: Any?, isBig: Boolean = false): TelemetryItem =
            TelemetryItem(
                label = label,
                value = rawValue.formatTelemetryValue(),
                isBig = isBig
            )

        private fun Any?.formatTelemetryValue(): String = when (this) {
            null -> "—"
            is String -> trim().ifBlank { "—" }
            is Float, is Double -> "%.2f".format(this as Number)
            is Int, is Long, is UInt, is ULong, is UShort -> toString()
            is ByteArray -> {
                toFloatFromBytes()?.let { "%.1f".format(it) } ?: toHex()
            }
            else -> toString()
        }

        private fun ByteArray.toHex(): String =
            joinToString(" ") { "%02x".format(it.toUByte()) }

        private fun ByteArray.toFloatFromBytes(
            order: java.nio.ByteOrder = java.nio.ByteOrder.LITTLE_ENDIAN
        ): Float? = try {
            java.nio.ByteBuffer.wrap(this).order(order).getFloat(0)
        } catch (_: Exception) {
            null
        }
    }
}

// For middle column (status / modes / some non-critical errors)
data class StatusItem(
    val message: String,
    val severity: Severity = Severity.INFO
)

enum class Severity {
    INFO, WARNING, ERROR
}

// For right column (faults/alerts with colored backgrounds)
data class FaultItem(
    val message: String,
    val severity: Severity = Severity.ERROR
)
