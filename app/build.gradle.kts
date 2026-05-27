plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    //alias(libs.plugins.kotlin.android)  // not needed: AGP 9 handles Kotlin compilation
}

android {
    namespace = "com.ultraviolette.uvclusterhmi"
    compileSdk {
        version = release(34)
    }

    defaultConfig {
        applicationId = "com.ultraviolette.uvclusterhmi"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        viewBinding = false
    }
}


dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // AndroidX
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // ── Jetpack Compose ──────────────────────────────────────────────────────
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // ─────────────────────────────────────────────────────────────────────────

    // Car UI (non-system stub usable in Studio)
    implementation("androidx.car.app:app:1.4.0")

    // Optional safety (even though AAR/JAR already included)
    implementation("com.airbnb.android:lottie:3.4.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okio:okio:1.17.5")

    // Cluster AIDL shared library
    implementation(project(":cluster-aidl"))

    // Lifecycle & Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //For locally getting car and updateengine
    implementation(files("lib/android.car.jar"))
    implementation(files("lib/javalib.jar"))

    implementation(files("lib/sdp-android-1.1.1.aar"))
    implementation(files("lib/ssp-android-1.1.1.aar"))
    implementation(files("lib/media3-common-1.1.1.aar"))
    implementation(files("lib/media3-exoplayer-1.1.1.aar"))
    implementation(files("lib/media3-ui-1.1.1.aar"))
    implementation(files("lib/dotsindicator-4.3.aar"))

    val workVersion = "2.9.1"

    // WorkManager Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    //excluding profileinstaller, for emulator
    configurations.all {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}