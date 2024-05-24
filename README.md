# KMP-Record: Audio Recording Library for Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/dev.theolm.record/record-core)](https://mvnrepository.com/artifact/dev.theolm)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/theolm/kmp-record)
[![GitHub License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

KMP-Record is a lightweight Kotlin Multiplatform library designed to facilitate audio recording functionality across iOS and Android platforms. By abstracting platform-specific details, KMP-Record enables developers to manage audio recording in a unified manner, enhancing code reuse and maintaining consistency across platforms.



## WARNING
This library is in super-early stages so the API can change a lot. Using it at your own risk.

## Getting Started

### Prerequisites

- Kotlin Multiplatform project setup
- For Android: Minimum SDK version 22
- For iOS: iOS 13.0 or later
- It does not work on simulator (it crashes)
- The application should manage the necessary permissions

### Installation Process

The library is available via Maven Central:

```kt
commonMain.dependencies {
    implementation("dev.theolm.record:record-core:<latest_version>")
}
```

## Usage

### Recording

To start recording make sure the user provided the right dependencies and just call startRecording. This method will throw exception in case anything goes wrong.

```kt
Record.startRecording()
```

This should start recording the audio with the default configuration.

To stop the recording call the method `stopRecording` that will return the path of the saved audio.

```kt
Record.stopRecording().also { savedAudioPath ->
    println("Recording stopped. File saved at $savedAudioPath")
}
```

You can also call the method `Record.isRecording()` to check the status of the `Record`.

### Setting Record configuration
To change the default configuration just call the method `Record.setConfig` and pass the configuration object.

```kt
Record.setConfig(
    RecordConfig(
        outputLocation = OutputLocation.Cache,
        outputFormat = OutputFormat.MPEG_4
    )
)
```

For now the configuration options are pretty limited. If you need a different configuration (e.g. different format), please open an issue or a PR.
