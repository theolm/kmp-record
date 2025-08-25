package dev.theolm.record

import java.io.File
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


internal class JvmAudioRecorder(
    fileName: String,
    private val audioFormat: AudioFormat
) : CoroutineScope by MainScope() {

    private val audioFile: File = File(fileName)
    private var microphone: TargetDataLine? = null
    private var audioInputStream: AudioInputStream? = null
    private var recordingJob: Job? = null

    val isRecording: Boolean
        get() {
            val jobActive = recordingJob != null && recordingJob?.isActive ?: false
            val microphoneActive = microphone != null && microphone?.isActive ?: false
            return jobActive && microphoneActive
        }

    @Throws(LineUnavailableException::class)
    fun startRecording() {
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)


        if (!AudioSystem.isLineSupported(info)) {
            // @TheoLM what to do is the line is not supported?
            throw LineUnavailableException("Unsuported audio format or line.")
        }

        microphone = AudioSystem.getLine(info) as TargetDataLine?

        microphone?.let {
            it.open(audioFormat)

            it.start()

            audioInputStream = AudioInputStream(microphone)

            println("Recording started.")

            recordingJob = launch {
                runCatching {
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile)
                }.onFailure { writeError ->
                    System.err.println("Failed to save recorded audio: ${writeError.message}")
                }.onSuccess {
                    println("Recording saved successfully.")
                }
            }
        } ?: System.err.println("Microphone is not available.")
    }

    fun stopRecording(): String {
        microphone?.let {
            it.stop()
            it.close()
        }

        audioInputStream?.let {
            runCatching {
                audioInputStream!!.close()
                recordingJob?.cancel()
                recordingJob = null
            }.onFailure {
                System.err.println("Could not close audio input stream: " + it.message)
            }
        }


        return audioFile.absolutePath
    }
}