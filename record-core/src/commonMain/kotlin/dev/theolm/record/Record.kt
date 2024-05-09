package dev.theolm.record

public expect object Record {
    public fun startRecording()
    public fun stopRecording() : String
}
