plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
//    id("com.google.devtools.ksp") version "1.8.21-1.0.11" // Correct application


}

android {
    namespace = "com.thanhdang.findmyphone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.thanhdang.findmyphone"
        minSdk = 27
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
    val roomVersion: String by project

    // testing
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    //android X
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.material)

    //lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    // socket.io
    implementation(libs.socket.io.client)


//    // gson
//    implementation("com.google.code.gson:gson:$gsonVersion")

    // google map services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // glide
    implementation(libs.glide)
//    implementation(libs.compiler)

    //lottie animation
    implementation(libs.lottie)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // retrofit2
    implementation(libs.retrofit)
    implementation (libs.converter.gson)

    // Country Code Picker
    implementation(libs.ccp)

    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
//    ksp("androidx.room:room-compiler:$roomVersion")

    implementation(libs.androidx.room.ktx)

    // shimmer effect
    implementation(libs.shimmer)

    // Fourier Transform
    implementation(libs.jtransforms)

}