import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.goeslocal.timerbutton.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
}

val hasSigningCredentials = providers.gradleProperty("signingInMemoryKey").isPresent ||
    providers.gradleProperty("signing.secretKeyRingFile").isPresent

mavenPublishing {
    coordinates(
        groupId = "com.goeslocal",
        artifactId = "timerbutton-core",
        version = "0.2.0",
    )

    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = SourcesJar.Sources(),
            javadocJar = JavadocJar.Empty(),
        ),
    )

    publishToMavenCentral()
    if (hasSigningCredentials) {
        signAllPublications()
    }

    pom {
        name.set("TimerButton Core")
        description.set("Shared timer state, progress directions, progress modes, and countdown engine used by TimerButton Compose and XML artifacts.")
        inceptionYear.set("2026")
        url.set("https://github.com/vishnusreddy/timerbutton")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("vishnusreddy")
                name.set("Vishnu S Reddy")
                url.set("https://github.com/vishnusreddy")
            }
        }

        scm {
            url.set("https://github.com/vishnusreddy/timerbutton")
            connection.set("scm:git:git://github.com/vishnusreddy/timerbutton.git")
            developerConnection.set("scm:git:ssh://git@github.com/vishnusreddy/timerbutton.git")
        }
    }
}
