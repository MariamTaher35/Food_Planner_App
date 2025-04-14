plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0") // Or the latest version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // Or the latest version
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5") // Or the latest version
        classpath("com.google.gms:google-services:4.4.2") // Or the latest version
    }
}

