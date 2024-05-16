package dev.theolm.record.config

public data class RecordConfig(
    val outputLocation: OutputLocation = OutputLocation.Cache,
    val outputFormat: OutputFormat = OutputFormat.MPEG_4,
    val audioEncoder: AudioEncoder = AudioEncoder.AAC
)

public sealed class OutputFormat(public val extension: String) {
    public data object MPEG_4 : OutputFormat(".mp4")
}

public sealed class AudioEncoder {
    public data object AAC : AudioEncoder()
}

public sealed class OutputLocation {
    public data object Cache : OutputLocation()
    public data object Internal : OutputLocation()
    public data class Custom(val path: String) : OutputLocation()
}
