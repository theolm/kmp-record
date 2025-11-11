package dev.theolm.record.config

import dev.theolm.record.VolumeCallback

/**
 * Configuration for audio recording.
 *
 * @param volumeCallback Optional callback for real-time volume monitoring.
 *        **Note: Volume callback only works with WAV + PCM_16BIT format.**
 *        For MPEG_4/AAC, the callback will not be invoked as compressed formats
 *        don't provide direct access to audio samples.
 */
public data class RecordConfig(
    val outputLocation: OutputLocation = OutputLocation.Cache,
    val outputFormat: OutputFormat = OutputFormat.MPEG_4,
    val audioEncoder: AudioEncoder = AudioEncoder.AAC,
    val sampleRate: Int = 44100,
    val volumeCallback: VolumeCallback? = null
)

public sealed class OutputFormat(public val extension: String) {
    public data object MPEG_4 : OutputFormat(".mp4")
    public data object WAV: OutputFormat(".wav")
}

public sealed class AudioEncoder {
    public data object AAC : AudioEncoder()
    public data object PCM_16BIT: AudioEncoder()
}

public sealed class OutputLocation {
    public data object Cache : OutputLocation()
    public data object Internal : OutputLocation()
    public data class Custom(val path: String) : OutputLocation()
}
