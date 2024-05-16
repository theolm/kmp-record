@file:Suppress("MatchingDeclarationName")
package dev.theolm.record

import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.PermissionMissingException
import dev.theolm.record.error.RecordFailException
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioQuality
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.AudioFormatID
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.CoreAudioTypes.kAudioFormatMPEG4CELP
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURL.Companion.fileURLWithPath
import platform.Foundation.NSUserDomainMask
import platform.Foundation.temporaryDirectory
import kotlin.system.getTimeMillis
import kotlin.time.TimeSource

private const val SampleRate = 44100
internal actual object RecordCore {
    private var recorder: AVAudioRecorder? = null
    private var output: String? = null
    private var isRecording: Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    @Throws(RecordFailException::class)
    internal actual fun startRecording(config: RecordConfig) {
        println(config.toString())
        checkPermission()
        output = config.getOutput()

        val settings = mapOf<Any?, Any>(
            AVFormatIDKey to config.outputFormat.toAVFormatID(),
            AVSampleRateKey to SampleRate,
            AVNumberOfChannelsKey to 1,
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

    internal actual fun stopRecording(): String {
        isRecording = false
        recorder?.stop()

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    internal actual fun isRecording(): Boolean = isRecording


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

    private fun RecordConfig.getOutput(): String {
        val timestamp = getTimeMillis().toString()
        val fileName = "${timestamp}${outputFormat.extension}"

        return when (this.outputLocation) {
            OutputLocation.Cache -> "${NSFileManager.defaultManager.temporaryDirectory.path}/$fileName"
            OutputLocation.Internal -> {
                val urls = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
                val documentsURL = urls.first() as? NSURL ?: throw NoOutputFileException()
                "${documentsURL.path!!}/$fileName"
            }
            is OutputLocation.Custom -> "${this.outputLocation.path}/$fileName"
        }
    }

    private fun OutputFormat.toAVFormatID(): AudioFormatID = when (this) {
        OutputFormat.MPEG_4 -> kAudioFormatMPEG4AAC
    }
}