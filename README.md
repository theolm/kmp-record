# KMP-Record: Audio Recording Library for Kotlin Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/dev.theolm.record/record-core)](https://mvnrepository.com/artifact/dev.theolm)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/theolm/kmp-record)
[![GitHub License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

KMP-Record is a lightweight Kotlin Multiplatform library designed to facilitate audio recording functionality across iOS and Android platforms. By abstracting platform-specific details, KMP-Record enables developers to manage audio recording in a unified manner, enhancing code reuse and maintaining consistency across platforms.



## WARNING
This library is in super-early stages. Currently it only support Android (iOS comming soon) and the API is constantly changing. Using it at your own risk.

## Getting Started

### Prerequisites

- Kotlin Multiplatform project setup
- For Android: Minimum SDK version 22
- For iOS: iOS 13.0 or later

### Installation Process

The library is available via Maven Central:

```kt
implementation("dev.theolm.record:record-core:<latest_version>")
