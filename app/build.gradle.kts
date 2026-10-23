plugins {
    id("com.android.application")
}
android {
    namespace = "com.bandartv.launcher"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.bandartv.launcher"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
