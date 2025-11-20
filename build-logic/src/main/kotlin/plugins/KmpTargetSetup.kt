package plugins

import config.Config
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun KotlinMultiplatformExtension.setupKmpTargets(
    onBinariesFramework: (Framework) -> Unit = {}
) {
    androidTarget {
        compilerOptions {
            jvmTarget.set(
                JvmTarget.fromTarget(Config.javaVersion.majorVersion)
            )
        }
    }

//    jvm("desktop")
//
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            onBinariesFramework(this)
        }
    }
}