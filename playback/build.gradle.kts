plugins {
	id("com.android.library")
	id("kotlin-android")
	id("kotlin-android-extensions")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(29)

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

	compileOptions {
		// Use Java 1.8 features
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	kotlinOptions {
		jvmTarget = compileOptions.targetCompatibility.toString()
	}

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
	// Kotlin
	implementation(kotlin("stdlib-jdk8"))
	val kotlinCoroutinesVersion = "1.3.5"
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion")

	// Android(x)
	implementation("androidx.core:core-ktx:1.2.0")
	implementation("androidx.fragment:fragment-ktx:1.2.4")
	implementation("androidx.appcompat:appcompat:1.1.0")

	// Testing
	testImplementation("junit:junit:4.12")
	testImplementation("androidx.test.ext:junit:1.1.1")
	testImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
