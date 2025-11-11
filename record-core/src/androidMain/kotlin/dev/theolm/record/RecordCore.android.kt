@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal actual object RecordCore {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var recordingThread: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recorder: MediaRecorder? = null
    private var output: String? = null
    @Volatile
    private var myRecordingState: RecordingState = RecordingState.IDLE

    private var volumeCallback: VolumeCallback? = null


    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        volumeCallback = config.volumeCallback
        output = config.getOutput()
        File(output!!).parentFile?.mkdirs() //Ensure the output file path exists before recording starts
        when(config.outputFormat) {
            OutputFormat.MPEG_4 -> {
                recorder = createMediaRecorder(config)

                recorder?.apply {
                    runCatching {
                        prepare()
                    }.onFailure {
                        throw RecordFailException()
                    }

                    setOnErrorListener { _, _, _ ->
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

                    recordingJob = recordingThread.launch {
                        try {
                            writeAudioDataToFile(bufferSize, config.sampleRate)
                        } catch (e: Exception) {
                            Log.e("RecordCore", "Recording failed", e)
                        }
                    }
                }
            }
        }
    }

    @Throws(NoOutputFileException::class)
    internal actual fun stopRecording(config: RecordConfig): String {
        myRecordingState = RecordingState.IDLE
        volumeCallback = null
        when (config.outputFormat) {
            OutputFormat.MPEG_4 -> {
                recorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e("RecordCore", "Error stopping MediaRecorder", e)
                    } finally {
                        release()
                    }
                }

                return output.also {
                    output = null
                    recorder = null
                } ?: throw NoOutputFileException()
            }
            OutputFormat.WAV -> {
                recordingJob?.cancel()
                audioRecord?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e("RecordCore", "Error stopping AudioRecord", e)
                    } finally {
                        release()
                    }
                }
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

    @OptIn(ExperimentalVolumeCallback::class)
    private fun writeAudioDataToFile(bufferSize: Int, sampleRate: Int) {
        val data = ByteArray(bufferSize)
        var totalAudioLength = 0

        FileOutputStream(output).use { fos ->
            // Write a placeholder for the WAV file header
            fos.write(ByteArray(44))

            // Write PCM data
            while (isRecording()) {
                val read = audioRecord?.read(data, 0, data.size) ?: 0
                if (read > 0) {
                    fos.write(data, 0, read)
                    totalAudioLength += read
                    // Calculate and emit volume
                    volumeCallback?.let { callback ->
                        val volume = calculateVolume(data, read)
                        callback.onVolumeChanged(volume)
                    }
                }
            }

            // Update WAV header after recording is done
            fos.channel.position(0) // Rewind to start of file
            fos.writeWavHeader(sampleRate, totalAudioLength + 36) // Data size + 36 bytes for header
        }
    }

    /**
     * Calculates the volume level from raw PCM audio data.
     *
     * This function processes 16-bit PCM audio samples stored in a byte array and calculates
     * the Root Mean Square (RMS) amplitude, which represents the average loudness of the audio.
     *
     * @param buffer ByteArray containing raw PCM audio data in 16-bit little-endian format
     * @param read Number of bytes actually read into the buffer (must be even number since each sample is 2 bytes)
     * @return Volume level as a Double in the range 0.0-100.0, where:
     *         - 0.0 represents silence
     *         - 100 represents very loud audio (near clipping)
     *
     * ## How it works:
     * 1. Converts pairs of bytes into 16-bit audio samples (little-endian format)
     * 2. Calculates RMS (Root Mean Square): sqrt(mean(sample²))
     * 3. Normalizes the result to 0-100 scale by dividing by max 16-bit value (32767)
     *
     * ## Technical details:
     * - Uses PCM (Pulse Code Modulation) 16-bit format standard
     * - Assumes little-endian byte order (low byte first, high byte second)
     * - RMS is the industry-standard method for measuring audio amplitude
     * - Sample range: -32768 to +32767 (signed 16-bit integer)
     *
     * @see [Root Mean Square](https://en.wikipedia.org/wiki/Root_mean_square)
     * @see [PCM Audio Format](https://en.wikipedia.org/wiki/Pulse-code_modulation)
     */
    private fun calculateVolume(buffer: ByteArray, read: Int): Double {
        var sum = 0.0
        var sampleCount = 0

        var i = 0
        while (i < read - 1) {
            val low = buffer[i].toInt() and 0xFF
            val high = buffer[i + 1].toInt()
            val sample = ((high shl 8) or low).toShort()

            sum += (sample.toDouble() * sample.toDouble())
            sampleCount++
            i += 2
        }

        if (sampleCount == 0) return 0.0

        val rms = kotlin.math.sqrt(sum / sampleCount)

        // Normalize to 0-100 scale (16-bit max is 32767)
        return (rms / 32767.0) * 100.0
    }
}