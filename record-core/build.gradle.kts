import config.Config
import plugins.setupKmpTargets

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("android-lib-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId
}

kotlin {
    setupKmpTargets()

    sourceSets {
//        androidMain.dependencies {
//            implementation(libs.androidx.activity.ktx)
//        }
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotation)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
