plugins {
    id("com.android.application")
}

android {
    namespace = "com.opentapper.tap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.opentapper.tap"
        minSdk = 24
        targetSdk = 34
        versionCode = 100
        versionName = "1.00"
    }

    // If a keystore is provided through environment variables (e.g. in CI), the
    // release build is signed with it. Otherwise the release build falls back to
    // the standard debug signing config so `assembleRelease` always produces an
    // installable APK without any extra setup.
    val keystoreFile: String? = System.getenv("KEYSTORE_FILE")
    signingConfigs {
        if (keystoreFile != null) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystoreFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }
}
