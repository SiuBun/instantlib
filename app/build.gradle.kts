plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.baselib.use"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.baselib.use"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.multidex)

    implementation(libs.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlin.stdlib)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.viewbinding)
    ksp(libs.lifecycle.compiler)
    implementation(libs.lifecycle.common.java8)

    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.rxjava2)
    implementation(libs.room.ktx)
    implementation(libs.room.testing)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxadapter)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okio)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)


    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(project(":instantlib"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(libs.room.testing)
}