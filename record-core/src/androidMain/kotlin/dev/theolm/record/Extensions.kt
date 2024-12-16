package dev.theolm.record

import android.media.AudioFormat
import android.media.MediaRecorder
import dev.theolm.record.config.AudioEncoder
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal fun OutputFormat.toMediaRecorderOutputFormat(): Int = when (this) {
    OutputFormat.MPEG_4 -> MediaRecorder.OutputFormat.MPEG_4
    OutputFormat.WAV -> AudioFormat.CHANNEL_IN_MONO
}

internal fun AudioEncoder.toMediaRecorderAudioEncoder(): Int = when (this) {
    AudioEncoder.AAC -> MediaRecorder.AudioEncoder.AAC
    AudioEncoder.PCM_16BIT -> AudioFormat.ENCODING_PCM_16BIT
}

internal fun RecordConfig.getOutput(): String {
    val fileName = "${System.currentTimeMillis()}${outputFormat.extension}"
    return when (this.outputLocation) {
        OutputLocation.Cache -> "${applicationContext.cacheDir.absolutePath}/$fileName"
        OutputLocation.Internal -> "${applicationContext.filesDir}/$fileName"
        is OutputLocation.Custom -> "${this.outputLocation.path}/$fileName"
    }
}

internal fun FileOutputStream.writeWavHeader(sampleRate: Int, totalAudioLength: Int) {
    // Update this to use totalAudioLength instead of bufferSize
    write("RIFF".toByteArray())
    write(intToByteArray(36 + totalAudioLength)) // Total file size - 8 bytes
    write("WAVE".toByteArray())
    write("fmt ".toByteArray())
    write(intToByteArray(16)) // Subchunk1 size (16 for PCM)
    write(shortToByteArray(1)) // Audio format (1 for PCM)
    write(shortToByteArray(1)) // Number of channels
    write(intToByteArray(sampleRate))
    write(intToByteArray(sampleRate * 2)) // Byte rate (SampleRate * NumChannels * BitsPerSample/8)
    write(shortToByteArray(2)) // Block align (NumChannels * BitsPerSample/8)
    write(shortToByteArray(16)) // Bits per sample
    write("data".toByteArray())
    write(intToByteArray(totalAudioLength)) // Subchunk2 size
}

private fun intToByteArray(value: Int): ByteArray =
    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

private fun shortToByteArray(value: Short): ByteArray =
    ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
