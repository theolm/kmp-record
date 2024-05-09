@file:Suppress("MatchingDeclarationName")

package dev.theolm.record

import android.media.MediaRecorder
import android.os.Build
import dev.theolm.record.error.NoOutputFileException
import dev.theolm.record.error.RecordFailException
import java.io.IOException

@Suppress("TooGenericExceptionCaught", "SwallowedException")
public actual object Record {
    private var recorder: MediaRecorder? = null
    private var output: String? = null

    @Throws(RecordFailException::class)
    public actual fun startRecording() {
        output = "${getCacheDir()}/${getFileName()}"
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(output)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
            } catch (e: IOException) {
                throw RecordFailException()
            }

            start()
        }
    }

    @Throws(NoOutputFileException::class)
    public actual fun stopRecording(): String {
        recorder?.apply {
            stop()
            release()
        }

        return output.also {
            output = null
            recorder = null
        } ?: throw NoOutputFileException()
    }

    private fun getCacheDir(): String? =
        runCatching { applicationContext.cacheDir.absolutePath }.getOrNull()

    private fun getFileName(): String {
        val timestamp = System.currentTimeMillis().toString()
        return "$timestamp.mp3"
    }
}