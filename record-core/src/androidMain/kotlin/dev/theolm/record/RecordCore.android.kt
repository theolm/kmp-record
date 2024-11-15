@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import dev.theolm.record.config.AudioEncoder
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException

@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal actual object RecordCore {
    private var recorder: MediaRecorder? = null
    private var output: String? = null
    private var recordingState : RecordingState = RecordingState.IDLE

    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        output = config.getOutput()
        recorder = createMediaRecorder(config)

        recorder?.apply {
            runCatching {
                prepare()
            }.onFailure {
                throw RecordFailException()
            }

            setOnErrorListener { _, _, _ ->
                stopRecording()
            }

            start()
            recordingState = RecordingState.RECORDING
        }
    }

    @Throws(NoOutputFileException::class)
    internal actual fun stopRecording(): String {
        recordingState = RecordingState.IDLE
        recorder?.apply {
            stop()
            release()
        }

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    internal actual fun isRecording(): Boolean = recordingState == RecordingState.RECORDING

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
}

private fun AudioEncoder.toMediaRecorderAudioEncoder(): Int = when (this) {
    AudioEncoder.AAC -> MediaRecorder.AudioEncoder.AAC
}

private fun RecordConfig.getOutput(): String {
    val fileName = "${System.currentTimeMillis()}${outputFormat.extension}"
    return when (this.outputLocation) {
        OutputLocation.Cache -> "${applicationContext.cacheDir.absolutePath}/$fileName"
        OutputLocation.Internal -> "${applicationContext.filesDir}/$fileName"
        is OutputLocation.Custom -> "${this.outputLocation.path}/$fileName"
    }
}