@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException
import java.io.FileOutputStream

@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal actual object RecordCore {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var recorder: MediaRecorder? = null
    private var output: String? = null
    private var myRecordingState : RecordingState = RecordingState.IDLE

    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        output = config.getOutput()
        when(config.outputFormat) {
            OutputFormat.MPEG_4 -> {
                recorder = createMediaRecorder(config)

                recorder?.apply {
                    runCatching {
                        prepare()
                    }.onFailure {
                        throw RecordFailException()
                    }

                    setOnErrorListener { mr, what, extra ->
                        stopRecording(config)
                    }

                    start()
                    myRecordingState = RecordingState.RECORDING
                }
            }
            OutputFormat.WAV -> {
                val bufferSize = AudioRecord.getMinBufferSize(
                    config.sampleRate,
                    config.outputFormat.toMediaRecorderOutputFormat(),
                    config.audioEncoder.toMediaRecorderAudioEncoder()
                )

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    config.sampleRate,
                    config.outputFormat.toMediaRecorderOutputFormat(),
                    config.audioEncoder.toMediaRecorderAudioEncoder(),
                    bufferSize
                )

                audioRecord?.apply {
                    startRecording()
                    myRecordingState = RecordingState.RECORDING

                    recordingThread = Thread {
                        writeAudioDataToFile(bufferSize, config.sampleRate)
                    }.apply { start() }
                }
            }
        }
    }

    @Throws(NoOutputFileException::class)
    internal actual fun stopRecording(config: RecordConfig): String {
        myRecordingState = RecordingState.IDLE
        when (config.outputFormat) {
            OutputFormat.MPEG_4 -> {
                recorder?.apply {
                    stop()
                    release()
                }

                return output.also {
                    output = null
                    recorder = null
                } ?: throw NoOutputFileException()
            }
            OutputFormat.WAV -> {
                audioRecord?.apply {
                    stop()
                    release()
                }
                recordingThread?.join() // Wait for the recording thread to finish

                return output ?: throw NoOutputFileException()
            }
        }
    }

    internal actual fun isRecording(): Boolean = myRecordingState == RecordingState.RECORDING

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

    private fun writeAudioDataToFile(bufferSize: Int, sampleRate: Int) {
        val data = ByteArray(bufferSize)
        var totalAudioLength = 0

        FileOutputStream(output).use { fos ->
            // Write a placeholder for the WAV file header
            fos.write(ByteArray(44))

            // Write PCM data
            while (isRecording()) {
                val read = audioRecord!!.read(data, 0, data.size)
                if (read > 0) {
                    fos.write(data, 0, read)
                    totalAudioLength += read
                }
            }

            // Update WAV header after recording is done
            fos.channel.position(0) // Rewind to start of file
            fos.writeWavHeader(sampleRate, totalAudioLength + 36) // Data size + 36 bytes for header
        }
    }
}