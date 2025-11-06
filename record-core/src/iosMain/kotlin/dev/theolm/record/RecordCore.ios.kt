@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioQuality
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptions
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive

import platform.Foundation.NSError
import platform.Foundation.NSURL.Companion.fileURLWithPath

internal actual object RecordCore {
    private var recorder: AVAudioRecorder? = null
    private var output: String? = null
    private var myRecordingState: Boolean = false
    private var volumeCallback: VolumeCallback? = null
    private var meteringJob: Job? = null
    private val meteringScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalForeignApi::class)
    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        configureAudioSession()

        volumeCallback = config.volumeCallback
        output = config.getOutput()

        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to config.outputFormat.toAVFormatID(),
            AVSampleRateKey to config.sampleRate,
            AVNumberOfChannelsKey to 1,
            AVLinearPCMBitDepthKey to 16,
            AVLinearPCMIsFloatKey to false,
            AVEncoderAudioQualityKey to AVAudioQuality.MAX_VALUE
        )

        val url = fileURLWithPath(output!!)
        recorder = AVAudioRecorder(url, settings, null)

        recorder?.let { rec ->
            rec.meteringEnabled = true  // Enable metering

            if (!rec.prepareToRecord()) {
                throw RecordFailException()
            }
            if (!rec.record()) {
                throw RecordFailException()
            }
            myRecordingState = true

            // Start metering job
            startMetering(rec)
        } ?: throw RecordFailException()
    }

    internal actual fun stopRecording(config: RecordConfig): String {
        myRecordingState = false
        meteringJob?.cancel()
        meteringJob = null
        volumeCallback = null
        recorder?.stop()

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    internal actual fun isRecording(): Boolean = myRecordingState

    private fun startMetering(recorder: AVAudioRecorder) {
        meteringJob = meteringScope.launch {
            while (isActive && myRecordingState) {
                recorder.updateMeters()

                // Get average power for channel 0 (mono)
                val averagePower = recorder.averagePowerForChannel(0u)

                // Convert dB to 0-100 scale
                // averagePower ranges from -160 dB (silence) to 0 dB (max)
                /*     Dividing percentage by 10 to get it on scale 0 to 10. In android side threshold is 5.0
                     with this dividing this value is valid as well */
                val normalizedVolume = convertDecibelToPercentage(averagePower).div(10.0)

                volumeCallback?.onVolumeChanged(normalizedVolume)

                delay(100) // Update every 100ms (adjust as needed)
            }
        }
    }

    private fun convertDecibelToPercentage(decibel: Float): Double {
        // dB range: -160 (silence) to 0 (max)
        // Normalize to 0-100
        val minDb = -50.0f // Practical minimum (adjust based on testing)
        val maxDb = 0.0f

        val clampedDb = decibel.coerceIn(minDb, maxDb)

        // Linear conversion
        val percentage = ((clampedDb - minDb) / (maxDb - minDb)) * 100.0

        return percentage.coerceIn(0.0, 100.0)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun configureAudioSession() {
        memScoped {
            val audioSession = AVAudioSession.sharedInstance()
            val categoryErrorPtr = alloc<ObjCObjectVar<NSError?>>()
            audioSession.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                withOptions = AVAudioSessionCategoryOptions.MAX_VALUE,
                error = categoryErrorPtr.ptr
            )
            val categoryError = categoryErrorPtr.value
            if (categoryError != null) {
                println("Failed to set AVAudioSession category: ${categoryError.localizedDescription}")
                throw RecordFailException()
            }

            val activateErrorPtr = alloc<ObjCObjectVar<NSError?>>()
            audioSession.setActive(true, error = activateErrorPtr.ptr)
            val activateError = activateErrorPtr.value
            if (activateError != null) {
                println("Failed to activate AVAudioSession: ${activateError.localizedDescription}")
                throw RecordFailException()
            }
        }
    }

    private fun checkPermission() {
        val audioSession = AVAudioSession.sharedInstance()
        when (audioSession.recordPermission()) {
            AVAudioSessionRecordPermissionDenied -> {
                throw PermissionMissingException()
            }

            AVAudioSessionRecordPermissionUndetermined -> {
                // Permission has not been asked yet; requesting permission
                audioSession.requestRecordPermission { granted ->
                    if (!granted) {
                        throw PermissionMissingException()
                    }
                }
            }
        }
    }
}
