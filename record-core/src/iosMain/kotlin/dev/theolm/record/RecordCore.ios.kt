@file:Suppress("MatchingDeclarationName")
package dev.theolm.record

import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.nativeHeap
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioQuality
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.AudioFormatID
import platform.CoreAudioTypes.kAudioFormatAC3
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL.Companion.fileURLWithPath
import platform.Foundation.temporaryDirectory
import kotlin.system.getTimeMillis

internal actual object RecordCore {
    private var recorder: AVAudioRecorder? = null
    private var output: String? = null
    private var isRecording: Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        checkPermission()
        output = getOutputPath(config.outputFormat.extension)

//        val settings = mapOf<Any?, Any>(
//            AVFormatIDKey to config.outputFormat.toAVFormatID(),
//            AVSampleRateKey to 44100.0,
//            AVNumberOfChannelsKey to 1
//        )

        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to kAudioFormatMPEG4AAC,
            AVSampleRateKey to 44100.0,
            AVNumberOfChannelsKey to 1,
            AVEncoderAudioQualityKey to AVAudioQuality.MAX_VALUE
        )

        val url = fileURLWithPath(output!!)
        recorder = AVAudioRecorder(
            url,
            settings,
            null
            )

//        if (recorder == null && errorRef.pointee != null) {
//            println("Error initializing AVAudioRecorder: \(errorRef.pointee?.localizedDescription)")
//        }

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

    internal actual fun stopRecording(): String {
        isRecording = false
        recorder?.stop()

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    internal actual fun isRecording(): Boolean = isRecording

    private fun getCacheDir(): String? = NSFileManager.defaultManager.temporaryDirectory.path

    private fun getFileName(extension: String): String {
        val timestamp = getTimeMillis().toString()
        return "$timestamp$extension"
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

    private fun getOutputPath(extension: String): String {
        val cacheDir = getCacheDir() ?: throw NoOutputFileException()
        return "$cacheDir/${getFileName(extension)}"
    }

    private fun OutputFormat.toAVFormatID(): AudioFormatID = when (this) {
        OutputFormat.MPEG_4 -> kAudioFormatMPEG4AAC
        OutputFormat.THREE_GPP -> kAudioFormatMPEG4AAC
    }
}