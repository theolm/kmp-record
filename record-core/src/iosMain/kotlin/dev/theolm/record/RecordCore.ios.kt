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
    private var isRecording: Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        configureAudioSession()

        output = config.getOutput()

        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to config.outputFormat.toAVFormatID(),
            AVSampleRateKey to config.sampleRate,
            AVNumberOfChannelsKey to 1, // you may want to put a condition here?
            AVLinearPCMBitDepthKey to 16,
            AVLinearPCMIsFloatKey to false,
            AVEncoderAudioQualityKey to AVAudioQuality.MAX_VALUE
        )

        val url = fileURLWithPath(output!!)
        recorder = AVAudioRecorder(
            url,
            settings,
            null
        )

        recorder?.let {
            if (!it.prepareToRecord()) {
                throw RecordFailException()
            }
            if (!it.record()) {
                throw RecordFailException()
            }
            isRecording = true
        } ?: throw RecordFailException()
    }

    internal actual fun stopRecording(config: RecordConfig): String {
        isRecording = false
        recorder?.stop()

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    internal actual fun isRecording(): Boolean = isRecording


    /**
     * Config and Activate AVAudioSession
     */
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