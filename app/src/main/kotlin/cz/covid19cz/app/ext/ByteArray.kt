package cz.covid19cz.app.ext

val ByteArray.asHexLower inline get() = this.joinToString(separator = ""){ String.format("%02x",(it.toInt() and 0xFF))}
val String.hexAsByteArray inline get() = this.chunked(2).map { it.toUpperCase().toInt(16).toByte() }.toByteArray()