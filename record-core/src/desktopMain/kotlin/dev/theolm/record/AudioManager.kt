package dev.theolm.record

import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.TargetDataLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


internal class AudioRecorder(
    fileName: String,
    private val audioFormat: AudioFormat = DEFAULT_FORMAT
) : CoroutineScope by MainScope() {

    private val audioFile: File = File(fileName)
    private var microphone: TargetDataLine? = null
    private var audioInputStream: AudioInputStream? = null
    private var recordingJob: Job? = null

    @Throws(LineUnavailableException::class)
    fun startRecording() {
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)


        if (!AudioSystem.isLineSupported(info)) {
            // @TheoLM what to do is the line is not supported?
            throw LineUnavailableException("Unsuported audio format or line.")
        }

        microphone = AudioSystem.getLine(info) as TargetDataLine?
        microphone!!.open(audioFormat)

        microphone!!.start()

        audioInputStream = AudioInputStream(microphone)

        println("Recording started.")

        recordingJob = launch {
            runCatching {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile)
            }.onFailure {
                System.err.println("Failed to save recorded audio: ${it.message}")
            }.onSuccess {
                println("Recording saved successfully.")
            }
        }
    }

    fun stopRecording() {
        if (microphone != null) {
            microphone!!.stop()
            microphone!!.close()
        }

        if (audioInputStream != null) {
            try {
                audioInputStream!!.close()
                recordingJob?.cancel()
                recordingJob = null
            } catch (e: IOException) {
                System.err.println("Could not close audio input stream: " + e.message)
            }
        }

        println("Audio recorded successfully to: " + audioFile.absolutePath)
    }

    companion object {
        private val DEFAULT_FORMAT = AudioFormat(
            44100f,  // Sample rate (Hz)
            16,  // Sample size in bits
            2,  // Channels (stereo)
            true,  // Signed
            false // Big endian
        )
    }
}