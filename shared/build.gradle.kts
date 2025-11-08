import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidLibrary {
        namespace = "com.skyd.mvi"
        compileSdk = 36
        minSdk = 24
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.jetbrains.lifecycle.viewmodel)
            implementation(libs.jetbrains.lifecycle.runtime.compose)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        all {
            with(languageSettings) {
                optIn("kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi")
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates("io.github.skyd666", "mvi", "1.0-beta05")

    pom {
        name = "Compone"
        description = "A Compose Multiplatform MVI Kit."
        inceptionYear = "2025"
        url = "https://github.com/SkyD666/MVIKit"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "SkyD666"
                name = "SkyD666"
                url = "https://github.com/SkyD666"
            }
        }
        scm {
            url = "https://github.com/SkyD666/MVIKit"
            connection = "scm:git:git://github.com/SkyD666/MVIKit.git"
            developerConnection = "scm:git:ssh://git@github.com/SkyD666/MVIKit.git"
        }
    }
}
