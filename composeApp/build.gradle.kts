import config.Config
import plugins.setupKmpTargets
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    id("detekt-setup")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    androidLibrary {
        namespace = Config.applicationId + ".sample.library"
        compileSdk = Config.compileSdk
        
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Config.javaVersion.majorVersion))
        }
        
        androidResources {
            enable = true
        }
    }
    
    setupKmpTargets(
        onBinariesFramework = {
            it.baseName = "ComposeApp"
            it.isStatic = true
        }
    )

    sourceSets {
        commonMain.dependencies {
            implementation(projects.recordCore)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)
            implementation(libs.materialKolor)
            implementation(libs.mokoPermissions)
            implementation(libs.moko.permissions.microphone)
        }
    }
}
