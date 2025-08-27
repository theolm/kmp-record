@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import dev.theolm.record.config.AudioEncoder
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

internal actual object RecordCore {
    private var recorder: JvmAudioRecorder? = null
    internal actual fun startRecording(config: RecordConfig) {
        recorder = JvmAudioRecorder(
            fileName = buildFileLocation(config),
            audioFormat = buildAudioFormat(config)
        )
        recorder?.startRecording()
    }

    internal actual fun isRecording(): Boolean {
        return recorder?.isRecording ?: false
    }

    internal actual fun stopRecording(config: RecordConfig): String {
        val filePath = recorder?.stopRecording() ?: ""
        recorder = null
        return filePath
    }

    private fun buildFileLocation(config: RecordConfig): String {
        val basePath = when (config.outputLocation) {
            is OutputLocation.Cache -> System.getProperty("java.io.tmpdir")
            is OutputLocation.Internal -> System.getProperty("user.home")
            is OutputLocation.Custom -> config.outputLocation.path
        }

        return "$basePath/recording${config.outputFormat.extension}"
    }

    private fun buildAudioFormat(config: RecordConfig): AudioFormat {
        /* @TheoLM Java encoding API does not provide AAC as a build-in implementation.
         Do we need to add an external library for that or just use PCM_SIGNED for both? */
        val audioEncoder = when (config.audioEncoder) {
            AudioEncoder.AAC -> AudioFormat.Encoding.PCM_SIGNED
            AudioEncoder.PCM_16BIT -> AudioFormat.Encoding.PCM_SIGNED
        }

        return AudioFormat(
            audioEncoder,
            config.sampleRate.toFloat(),
            DEFAULT_SAMPLE_SIZE,
            DEFAULT_CHANNEL_COUNT,
            calculateFrameSize(
                encoder = audioEncoder,
                sampleSize = DEFAULT_SAMPLE_SIZE,
                channelCount = DEFAULT_CHANNEL_COUNT
            ),
            config.sampleRate.toFloat(),
            false,
            mapOf<String, Objects>()
        )
    }

    private fun calculateFrameSize(
        encoder: AudioFormat.Encoding,
        sampleSize: Int,
        channelCount: Int
    ): Int {
        return if (encoder == AudioFormat.Encoding.PCM_SIGNED || encoder == AudioFormat.Encoding.PCM_UNSIGNED) {
            return (sampleSize / 8) * channelCount
        } else AudioSystem.NOT_SPECIFIED
    }

    private const val DEFAULT_SAMPLE_SIZE = 16
    private const val DEFAULT_CHANNEL_COUNT = 2

}