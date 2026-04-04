plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "qzwx.app.module_ui"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //设置系统状态栏
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    //Icons图标
    api("androidx.compose.material:material-icons-core:1.7.8")
    api("androidx.compose.material:material-icons-extended:1.7.8")
    //时间日期选择器
    api("com.maxkeppeler.sheets-compose-dialogs:core:1.3.0")
    api("com.maxkeppeler.sheets-compose-dialogs:calendar:1.3.0")
    api("com.maxkeppeler.sheets-compose-dialogs:clock:1.3.0")
    //自定义左右滑动
    api("me.saket.swipe:swipe:1.3.0")
    //日历组件
    api("com.kizitonwose.calendar:view:2.10.1")
    api("com.kizitonwose.calendar:compose:2.10.1")

    //实体类依赖
    api(project(":Module_Data"))

    //nav3
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    api(libs.androidx.material3.adaptive.navigation3)
    api(libs.kotlinx.serialization.core)
    //hilt
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")
}