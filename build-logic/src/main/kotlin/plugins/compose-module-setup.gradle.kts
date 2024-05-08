import org.gradle.accessors.dm.LibrariesForLibs
import plugins.setupKmpTargets

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    setupKmpTargets(
        onBinariesFramework = {
            it.baseName = "ComposeApp"
            it.isStatic = true
        }
    )

    val libs = the<LibrariesForLibs>()
    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.koin.core)
            implementation(libs.koin.test)
            implementation(libs.koin.compose)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlin.coroutines.swing)
        }

        iosMain.dependencies {
            //Workaround to fix koin on ios
            implementation(libs.stately.common)
        }
    }
}
