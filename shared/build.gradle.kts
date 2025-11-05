import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    jvm()

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach {
//        it.binaries.framework {
//            baseName = "shared"
//            isStatic = true
//        }
//    }

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

android {
    namespace = "com.skyd.mvi"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates("io.github.skyd666", "mvi", "1.0-beta04")

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
