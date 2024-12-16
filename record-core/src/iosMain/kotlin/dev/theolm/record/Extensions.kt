package dev.theolm.record

import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import platform.CoreAudioTypes.AudioFormatID
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.temporaryDirectory
import kotlin.time.TimeSource

public fun RecordConfig.getOutput(): String {
    val timestamp = TimeSource.Monotonic.markNow().toString()
    val fileName = "${timestamp}${outputFormat.extension}"

    return when (this.outputLocation) {
        OutputLocation.Cache -> "${NSFileManager.defaultManager.temporaryDirectory.path}/$fileName"
        OutputLocation.Internal -> {
            val urls = NSFileManager.defaultManager.URLsForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask
            )
            val documentsURL = urls.first() as? NSURL ?: throw NoOutputFileException()
            "${documentsURL.path!!}/$fileName"
        }

        is OutputLocation.Custom -> "${this.outputLocation.path}/$fileName"
    }
}

public fun OutputFormat.toAVFormatID(): AudioFormatID = when (this) {
    OutputFormat.MPEG_4 -> kAudioFormatMPEG4AAC
    OutputFormat.WAV -> kAudioFormatLinearPCM
}