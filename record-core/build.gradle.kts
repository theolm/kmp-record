import com.vanniktech.maven.publish.SonatypeHost
import config.Config
import plugins.setupKmpTargets

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("android-lib-setup")
    id("detekt-setup")
    id("publish-setup")
}

android {
    namespace = Config.applicationId
}

kotlin {
    explicitApi()
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
        description.set("TODO")
    }
}