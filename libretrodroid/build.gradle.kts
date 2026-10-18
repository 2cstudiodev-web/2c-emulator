// LibretroDroid 0.14.0 (https://github.com/Swordfish90/LibretroDroid, GPLv3), vendored so 2C Emulator
// can fix frontend bugs that the published artifact has. See libretrodroid/README-2C.md.
plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "com.swordfish.libretrodroid"
    ndkVersion = "27.0.12077973"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
        ndk {
            abiFilters += "arm64-v8a"
        }
        externalNativeBuild {
            cmake {
                arguments += "-DANDROID_STL=c++_static"
            }
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(deps.libs.androidx.lifecycle.runtime)
}
