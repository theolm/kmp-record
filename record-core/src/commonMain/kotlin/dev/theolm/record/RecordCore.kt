package dev.theolm.record

import dev.theolm.record.config.RecordConfig

internal expect object RecordCore {
    internal fun startRecording(config: RecordConfig)
    internal fun stopRecording() : String
    internal fun isRecording() : Boolean
}

