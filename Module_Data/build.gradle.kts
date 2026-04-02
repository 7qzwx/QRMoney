plugins {
    alias(libs.plugins.android.library)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

}

android {
    namespace = "qzwx.app.module_data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Room
    val room_version = "2.8.4"
    api("androidx.room:room-runtime:$room_version")
    api("androidx.room:room-ktx:$room_version")
    api("androidx.room:room-rxjava2:$room_version")
    api("androidx.room:room-rxjava3:$room_version")
    api("androidx.room:room-guava:$room_version")
    api("androidx.room:room-paging:${room_version}")
    testImplementation("androidx.room:room-testing:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    annotationProcessor("androidx.room:room-compiler:2.8.4")
    //hilt
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")
}