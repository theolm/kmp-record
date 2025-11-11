package dev.theolm.record

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is experimental and currently only supports WAV format. Other formats are not yet supported and behavior may change in future releases."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalVolumeCallback