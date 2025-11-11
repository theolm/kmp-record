package dev.theolm.record


public interface VolumeCallback {
    @ExperimentalVolumeCallback
    public fun onVolumeChanged(volume: Double)
}
