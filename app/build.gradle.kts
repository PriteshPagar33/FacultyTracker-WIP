plugins {
    id("com.android.application")
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.facultytracker"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.facultytracker"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.firebase:firebase-database:20.2.2")



    testImplementation("junit:junit:4.13.2")
    implementation ("com.google.android.gms:play-services-location:21.0.1")


    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.firebase:firebase-firestore:24.0.1")
    implementation ("androidx.annotation:annotation:1.3.0")
    implementation ("com.sun.mail:android-mail:1.6.2")
    implementation ("androidx.core:core:1.6.0")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")


    implementation ("com.google.android.material:material:1.4.0")

    implementation ("com.google.gms:google-services:4.3.10")

    implementation ("org.osmdroid:osmdroid-android:6.1.7")
    implementation ("org.osmdroid:osmdroid-wms:6.1.7")









    implementation ("at.favre.lib:bcrypt:0.9.0")
    implementation ("org.springframework.security:spring-security-crypto:5.5.0")


    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics")
}