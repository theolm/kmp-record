import com.vanniktech.maven.publish.SonatypeHost
import config.Config
import plugins.setupKmpTargets
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    id("detekt-setup")
    alias(libs.plugins.kotlinMultiplatform)
    id("publish-setup")
}

kotlin {
    explicitApi()
    
    androidLibrary {
        namespace = Config.applicationId
        compileSdk = Config.compileSdk
        
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Config.javaVersion.majorVersion))
        }
        
        androidResources {
            enable = true
        }
    }
    
    setupKmpTargets()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.startup)
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    val version = System.getenv("VERSION") ?: Config.libVersion
    coordinates(
        groupId = Config.groupId,
        artifactId = Config.artifactId + "-core",
        version = version
    )

    pom {
        name.set("KMP-Record")
        description.set("Simple library to record audio on Android and iOS")
    }
}
