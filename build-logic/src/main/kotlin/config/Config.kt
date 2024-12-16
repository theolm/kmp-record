package config

import org.gradle.api.JavaVersion

object Config {
    const val applicationId = "dev.theolm.record"
    const val minSdk = 22
    const val targetSdk = 35
    const val compileSdk = 35
    const val versionCode = 1
    const val versionName = "1.0.0"
    const val packageVersion = versionName
    val javaVersion = JavaVersion.VERSION_17

    // Libraries versions
    const val artifactId = "record"
    const val groupId = "dev.theolm.record"
    const val libVersion = "0.4.0"
}