plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.quickboxwear"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quickboxwear"
        minSdk = 30
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-wearable:xx.x.x")
    implementation(libs.play.services.wearable)
    implementation(libs.firebase.crashlytics.buildtools)
}