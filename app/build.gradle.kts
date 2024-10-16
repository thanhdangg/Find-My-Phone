plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")


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
    val coreKtxVersion: String by project
    val appcompatVersion: String by project
    val materialVersion: String by project
    val constraintLayoutVersion: String by project
    val mapsVersion: String by project
    val locationVersion: String by project
    val junitVersion: String by project
    val androidxTestExtJunitVersion: String by project
    val espressoCoreVersion: String by project
    val socketIoClientVersion: String by project
    val gsonVersion: String by project
    val lifecycleVersion: String by project
    val glideVersion: String by project
    val lottieVersion: String by project
    val navigationVersion: String by project
    val retrofit2Version: String by project
    val countryCodePickerVersion: String by project
    val roomVersion: String by project
    val playServiceAdsVersion: String by project
    val shimmerVersion: String by project

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
    implementation(libs.compiler)

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
//    ksp("androidx.room:room-compiler:2.5.0")

    implementation(libs.androidx.room.ktx)

    // shimmer effect
    implementation(libs.shimmer)
}