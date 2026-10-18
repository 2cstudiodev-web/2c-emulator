package com.swordfish.lemuroid.lib.storage.scanner

import com.swordfish.lemuroid.common.files.FileUtils
import com.swordfish.lemuroid.common.kotlin.indexOf
import com.swordfish.lemuroid.common.kotlin.kiloBytes
import com.swordfish.lemuroid.common.kotlin.megaBytes
import com.swordfish.lemuroid.common.kotlin.startsWithAny
import com.swordfish.lemuroid.lib.library.SystemID
import timber.log.Timber
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.math.ceil
import kotlin.math.roundToInt

object SerialScanner {
    private val READ_BUFFER_SIZE = 64.kiloBytes()

    data class DiskInfo(val serial: String?, val systemID: SystemID?)

    @ExperimentalUnsignedTypes
    private val MAGIC_NUMBERS =
        listOf(
            MagicNumber(
                0x8008,
                ubyteArrayOf(
                    0x50U,
                    0x4cU,
                    0x41U,
                    0x59U,
                    0x53U,
                    0x54U,
                    0x41U,
                    0x54U,
                    0x49U,
                    0x4fU,
                    0x4eU,
                ).toByteArray(),
                SystemID.PSX,
            ),
            MagicNumber(
                0x9320,
                ubyteArrayOf(
                    0x50U,
                    0x4cU,
                    0x41U,
                    0x59U,
                    0x53U,
                    0x54U,
                    0x41U,
                    0x54U,
                    0x49U,
                    0x4fU,
                    0x4eU,
                ).toByteArray(),
                SystemID.PSX,
            ),
            MagicNumber(
                0x8008,
                ubyteArrayOf(0x50U, 0x53U, 0x50U, 0x20U, 0x47U, 0x41U, 0x4dU, 0x45U).toByteArray(),
                SystemID.PSP,
            ),
        )

    private val PS_SERIAL_REGEX = Regex("^([A-Z]+)-?([0-9]+)")
    private val PS_SERIAL_REGEX2 = Regex("^([A-Z]+)_?([0-9]{3})\\.([0-9]{2})")

    private const val PS_SERIAL_MAX_SIZE = 12

    private const val ISO_VOLUME_SPACE_SIZE_OFFSET = 0x8050

    // An 80 minute CD holds 360,000 sectors; leave some margin for overburned discs.
    private const val CD_MAX_BLOCKS = 405_000L

    private val PSX_BASE_SERIALS =
        listOf(
            "CPCS",
            "SCES",
            "SIPS",
            "SLKA",
            "SLPS",
            "SLUS",
            "ESPM",
            "SLED",
            "SCPS",
            "SCAJ",
            "PAPX",
            "SLES",
            "HPS",
            "LSP",
            "SLPM",
            "SCUS",
            "SCED",
        )

    private val PSP_BASE_SERIALS =
        listOf(
            "ULES",
            "ULUS",
            "ULJS",
            "ULEM",
            "ULUM",
            "ULJM",
            "ULKS",
            "ULAS",
            "UCES",
            "UCUS",
            "UCJS",
            "UCAS",
            "NPEH",
            "NPUH",
            "NPJH",
            "NPEG",
            "NPEX",
            "NPUG",
            "NPJG",
            "NPJJ",
            "NPHG",
            "NPEZ",
            "NPUZ",
            "NPJZ",
            "NPUF",
            "NPUZ",
            "NPUG",
            "NPUX",
        )

    fun extractInfo(
        fileName: String,
        inputStream: InputStream,
    ): DiskInfo {
        Timber.d("Extracting disk info for $fileName")
        inputStream.buffered(READ_BUFFER_SIZE).use {
            return when (FileUtils.extractExtension(fileName)) {
                "pbp" -> extractInfoForPBP(it)
                "iso", "bin" -> standardExtractInfo(it)
                "3ds" -> extractInfoFor3DS(it)
                else -> DiskInfo(null, null)
            }
        }
    }

    private fun standardExtractInfo(openedStream: InputStream): DiskInfo {
        openedStream.mark(READ_BUFFER_SIZE)
        val header = readByteArray(openedStream, ByteArray(READ_BUFFER_SIZE))

        val detectedSystem =
            MAGIC_NUMBERS
                .firstOrNull {
                    header.copyOfRange(it.offset, it.offset + it.numbers.size).contentEquals(it.numbers)
                }
                ?.systemID

        Timber.d("SystemID detected via magic numbers: $detectedSystem")

        openedStream.reset()

        return when (detectedSystem) {
            SystemID.PSX ->
                runCatching { extractInfoForPSX(openedStream, isDvdSized(header)) }
                    .getOrDefault(DiskInfo(null, SystemID.PSX))

            SystemID.PSP ->
                runCatching { extractInfoForPSP(openedStream) }
                    .getOrDefault(DiskInfo(null, SystemID.PSP))

            else -> DiskInfo(null, null)
        }
    }

    private fun extractInfoFor3DS(openedStream: InputStream): DiskInfo {
        Timber.d("Parsing 3DS game")
        openedStream.mark(0x2000)
        openedStream.skip(0x1150)

        val rawSerial = String(readByteArray(openedStream, ByteArray(10)), Charsets.US_ASCII)

        openedStream.reset()

        Timber.d("Found 3DS serial: $rawSerial")
        return DiskInfo(rawSerial, SystemID.NINTENDO_3DS)
    }

    private fun extractInfoForPSX(
        openedStream: InputStream,
        isDvdSized: Boolean,
    ): DiskInfo {
        val fallbackSystem = if (isDvdSized) SystemID.PS2 else SystemID.PSX
        val headerSize = 64.kiloBytes()
        if (openedStream.available() < headerSize) {
            return DiskInfo(null, fallbackSystem)
        }

        return textSearch(PSX_BASE_SERIALS, openedStream, PS_SERIAL_MAX_SIZE, headerSize)
            .mapNotNull { serial -> parsePSXSerial(serial) }
            .mapNotNull { serial ->
                val system = if (isDvdSized || isPS2Serial(serial)) SystemID.PS2 else SystemID.PSX
                DiskInfo(serial, system)
            }
            .firstOrNull() ?: DiskInfo(null, fallbackSystem)
    }

    /** PS1 games ship on CDs, so an ISO 9660 volume larger than a CD can only be a PS2 DVD. */
    private fun isDvdSized(header: ByteArray): Boolean {
        val offset = ISO_VOLUME_SPACE_SIZE_OFFSET
        if (header.size < offset + 4) return false
        val blocks =
            (header[offset].toLong() and 0xFF) or
                ((header[offset + 1].toLong() and 0xFF) shl 8) or
                ((header[offset + 2].toLong() and 0xFF) shl 16) or
                ((header[offset + 3].toLong() and 0xFF) shl 24)
        return blocks > CD_MAX_BLOCKS
    }

    /** PS2 titles reuse PS1 serial prefixes, but with distinct number ranges. */
    private fun isPS2Serial(serial: String): Boolean {
        val prefix = serial.substringBefore("-")
        val number = serial.substringAfter("-", "")
        return when (prefix) {
            "SLUS", "SLPS", "SLKA", "SCAJ" -> number.startsWith("2")
            "SCUS" -> number.startsWith("97")
            "SLES", "SCES", "SCED" -> number.startsWith("5")
            "SLPM" -> number.startsWith("6") || number.startsWith("5")
            "SCPS" -> number.startsWith("5") || number.startsWith("11") || number.startsWith("15")
            else -> false
        }
    }

    private fun extractInfoForPSP(openedStream: InputStream): DiskInfo {
        val headerSize = 64.kiloBytes()
        if (openedStream.available() < headerSize) {
            return DiskInfo(null, null)
        }

        return textSearch(PSP_BASE_SERIALS, openedStream, PS_SERIAL_MAX_SIZE, headerSize)
            .mapNotNull { serial -> parsePSXSerial(serial) }
            .mapNotNull { serial -> DiskInfo(serial, SystemID.PSP) }
            .firstOrNull() ?: DiskInfo(null, SystemID.PSP)
    }

    private fun extractInfoForPBP(openedStream: InputStream): DiskInfo {
        val headerSize = 2.megaBytes()
        if (openedStream.available() < headerSize) {
            return DiskInfo(null, null)
        }

        val queries = (PSP_BASE_SERIALS + PSX_BASE_SERIALS)

        return textSearch(queries, openedStream, PS_SERIAL_MAX_SIZE, headerSize)
            .mapNotNull { serial -> parsePSXSerial(serial) }
            .mapNotNull { serial ->
                when {
                    serial.startsWithAny(PSX_BASE_SERIALS) -> DiskInfo(serial, SystemID.PSX)
                    serial.startsWithAny(PSP_BASE_SERIALS) -> DiskInfo(serial, SystemID.PSP)
                    else -> DiskInfo(serial, null)
                }
            }
            .firstOrNull() ?: DiskInfo(null, null)
    }

    private fun parsePSXSerial(serial: String): String? {
        return sequenceOf(
            PS_SERIAL_REGEX.find(serial)?.groupValues?.let { "${it[1]}-${it[2]}" },
            PS_SERIAL_REGEX2.find(serial)?.groupValues?.let { "${it[1]}-${it[2]}${it[3]}" },
        ).filter { it != null }
            .firstOrNull()
    }

    private fun textSearch(
        queries: List<String>,
        openedStream: InputStream,
        resultSize: Int,
        streamSize: Int,
        windowSize: Int = 8.kiloBytes(),
        skipSize: Int = windowSize - resultSize,
        charset: Charset = Charsets.US_ASCII,
    ): Sequence<String> {
        val byteQueries = queries.map { it.toByteArray(charset) }
        return movingWidnowSequence(openedStream, windowSize, (skipSize).toLong())
            .take(ceil(streamSize.toDouble() / skipSize.toDouble()).roundToInt())
            .flatMap { serial ->
                byteQueries.asSequence()
                    .map { serial.indexOf(it) }
                    .filter { it >= 0 }
                    .map { serial to it }
            }
            .map { (bytes, index) ->
                val serialBytes = bytes.copyOfRange(index, index + resultSize)
                String(serialBytes, charset)
            }
            .filterNotNull()
    }

    private fun movingWidnowSequence(
        inputStream: InputStream,
        windowSize: Int,
        windowSkip: Long,
    ) = sequence {
        val buffer = ByteArray(windowSize)
        do {
            inputStream.mark(windowSize)
            yield(readByteArray(inputStream, buffer))
            inputStream.reset()
        } while (inputStream.skip(windowSkip) != 0L)
    }

    private fun readByteArray(
        inputStream: InputStream,
        byteArray: ByteArray,
    ): ByteArray {
        val readBytes = inputStream.read(byteArray)
        return if (readBytes < byteArray.size) {
            byteArray.copyOf(readBytes)
        } else {
            byteArray
        }
    }
}
