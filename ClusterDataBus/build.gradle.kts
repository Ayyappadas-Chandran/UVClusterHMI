plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.ultraviolette.clusterdatabus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ultraviolette.clusterdatabus"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":cluster-aidl"))
    implementation("androidx.core:core-ktx:1.12.0")
}
