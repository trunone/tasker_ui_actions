plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun getGitCommitHash(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "HEAD").start()
        val commitHash = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (commitHash.isNotEmpty()) {
            commitHash.take(7)
        } else {
            "1.0"
        }
    } catch (e: Exception) {
        "1.0"
    }
}

android {
    namespace = "com.example.taskerplugin"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.taskerplugin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = getGitCommitHash()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.11.1")
}
