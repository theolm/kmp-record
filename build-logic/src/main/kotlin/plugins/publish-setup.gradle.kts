plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    pom {
        inceptionYear.set("2024")
        url.set("https://github.com/theolm/kmp-record")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
            }
        }
        developers {
            developer {
                id.set("theolm")
                name.set("Theodoro Loureiro Mota")
                url.set("https://github.com/theolm/")
            }
        }
        scm {
            url.set("https://github.com/theolm/kmp-record/")
            connection.set("scm:git:git://github.com/theolm/kmp-record.git")
            developerConnection.set("scm:git:ssh://git@github.com/theolm/kmp-record.git")
        }
    }
}

