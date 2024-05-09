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

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlin.coroutines.swing)
        }
    }
}
