plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.bharathassignment2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bharathassignment2"
        minSdk = 25
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Google Play Services for Wear OS
    implementation("com.google.android.gms:play-services-wearable:18.0.0")

    // Wear OS dependencies
    implementation("androidx.wear:wear:1.2.0")
    implementation("androidx.wear:wear-input:1.1.0")

    // RecyclerView for displaying lists
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // ConstraintLayout for UI design
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")

    // Material Design components
    implementation("com.google.android.material:material:1.4.0")


}