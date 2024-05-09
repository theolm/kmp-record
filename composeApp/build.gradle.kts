import config.Config

plugins {
    id("android-application-setup")
    id("desktop-application-setup")
    id("compose-module-setup")
    id("detekt-setup")
}

android {
    namespace = Config.applicationId + ".sample"
}

kotlin {
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(projects.recordCore)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.koin)
            implementation(compose.material3)
            implementation(libs.materialKolor)
        }
    }
}

android {
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}
