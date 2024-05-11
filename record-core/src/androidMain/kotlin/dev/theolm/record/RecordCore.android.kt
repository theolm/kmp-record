@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import dev.theolm.record.config.AudioEncoder
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException
import java.io.IOException

@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal actual object RecordCore {
    private var recorder: MediaRecorder? = null
    private var output: String? = null
    private var isRecording: Boolean = false

    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        output = "${getCacheDir()}/${getFileName(config.outputFormat.extension)}"
        recorder = createMediaRecorder(config)

        recorder?.apply {
            runCatching {
                prepare()
            }.onFailure {
                throw RecordFailException()
            }

            setOnErrorListener { mr, what, extra ->
                stopRecording()
            }

            start()
            isRecording = true
        }
    }

    @Throws(NoOutputFileException::class)
    internal actual fun stopRecording(): String {
        isRecording = false
        recorder?.apply {
            stop()
            release()
        }

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    internal actual fun isRecording(): Boolean = isRecording

    private fun createMediaRecorder(config: RecordConfig) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(config.outputFormat.toMediaRecorderOutputFormat())
            setOutputFile(output)
            setAudioEncoder(config.audioEncoder.toMediaRecorderAudioEncoder())
        }

    private fun getCacheDir(): String? =
        runCatching { applicationContext.cacheDir.absolutePath }.getOrNull()

    private fun getFileName(extension: String): String {
        val timestamp = System.currentTimeMillis().toString()
        return "$timestamp$extension"
    }

    private fun checkPermission() {
        if (
            ContextCompat.checkSelfPermission(
                applicationContext,
                RECORD_AUDIO
            ) != PERMISSION_GRANTED
        ) {
            throw PermissionMissingException()
        }
    }
}

private fun OutputFormat.toMediaRecorderOutputFormat(): Int = when (this) {
    OutputFormat.MPEG_4 -> MediaRecorder.OutputFormat.MPEG_4
    OutputFormat.THREE_GPP -> MediaRecorder.OutputFormat.THREE_GPP
}

private fun AudioEncoder.toMediaRecorderAudioEncoder(): Int = when (this) {
    AudioEncoder.AAC -> MediaRecorder.AudioEncoder.AAC
}