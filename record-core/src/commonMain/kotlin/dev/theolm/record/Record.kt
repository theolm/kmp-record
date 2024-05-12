package dev.theolm.record

import dev.theolm.record.config.RecordConfig

public object Record {
    private var recordConfig = RecordConfig()

    public fun startRecording() {
        RecordCore.startRecording(recordConfig)
    }

    public fun stopRecording(): String {
        return RecordCore.stopRecording()
    }

    public fun isRecording(): Boolean {
        return RecordCore.isRecording()
    }

    public fun setConfig(config: RecordConfig) {
        recordConfig = config
    }
}
