@file:Suppress("MatchingDeclarationName")
package dev.theolm.record

import dev.theolm.record.config.RecordConfig

internal actual object RecordCore {
    internal actual fun startRecording(config: RecordConfig) {
        val format = buildAudioFormat()
    }

    internal actual fun isRecording(): Boolean {
        TODO("Not yet implemented")
    }

    internal actual fun stopRecording(config: RecordConfig): String {
        TODO("Not yet implemented")
    }
}