package dev.theolm.record.config

public data class RecordConfig(
    val outputFormat: OutputFormat = OutputFormat.MPEG_4,
    val audioEncoder: AudioEncoder = AudioEncoder.AAC
)

public sealed class OutputFormat(public val extension: String) {
    public data object MPEG_4 : OutputFormat(".mp4")
    public data object THREE_GPP : OutputFormat(".3gp")
}

public sealed class AudioEncoder {
    public data object AAC : AudioEncoder()
}