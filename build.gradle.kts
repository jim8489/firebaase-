buildscript {
    repositories {
        // Make sure that you have the following two repositories
        gradlePluginPortal()
        google()  // Google's Maven repository

        mavenCentral()  // Maven Central repository

    }
    dependencies {

        // Add the dependency for the Google services Gradle plugin
        classpath ("com.google.gms:google-services:4.4.0")
//        id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin' version '2.0.1' apply false

        classpath ("io.realm:realm-gradle-plugin:10.11.1")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
