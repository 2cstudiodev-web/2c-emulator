import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlinx-serialization")
    id("androidx.baselineprofile")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseSigning =
    Properties().apply {
        val file = rootProject.file("keystore.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

// Systems withdrawn from this build (for example after a takedown request), as system ids:
//   ./gradlew :lemuroid-app:assembleFreeBundleRelease -PexcludedSystems=3ds,ps2
// The app hides them (see SystemAvailability), and a core library is left out of the APK once
// every system it serves is excluded. Remote withdrawal without a new build: website config.json.
val excludedSystems: Set<String> =
    (findProperty("excludedSystems") as String?)
        ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
        ?: emptySet()

val systemsByCoreLibrary =
    mapOf(
        "libblastem_libretro_android.so" to setOf("md"),
        "libbsnes_libretro_android.so" to setOf("snes"),
        "libcitra_libretro_android.so" to setOf("3ds"),
        "libdesmume_libretro_android.so" to setOf("nds"),
        "libdosbox_pure_libretro_android.so" to setOf("dos"),
        "libfceumm_libretro_android.so" to setOf("nes"),
        "libgambatte_libretro_android.so" to setOf("gb", "gbc"),
        "libgearsystem_libretro_android.so" to setOf("gg", "sms"),
        "libhandy_libretro_android.so" to setOf("lynx"),
        "libmame2016_libretro_android.so" to setOf("arcade"),
        "libmednafen_ngp_libretro_android.so" to setOf("ngc", "ngp"),
        "libmednafen_pce_fast_libretro_android.so" to setOf("pce"),
        "libmednafen_wswan_libretro_android.so" to setOf("ws", "wsc"),
        "libmelonds_libretro_android.so" to setOf("nds"),
        "libmgba_libretro_android.so" to setOf("gba"),
        "libmupen64plus_next_gles3_libretro_android.so" to setOf("n64"),
        "libpcsx_rearmed_libretro_android.so" to setOf("psx"),
        "libplay_libretro_android.so" to setOf("ps2"),
        "libppsspp_libretro_android.so" to setOf("psp"),
        "libprosystem_libretro_android.so" to setOf("atari7800"),
        "libstella_libretro_android.so" to setOf("atari2600"),
    )

val excludedCoreLibraries =
    systemsByCoreLibrary
        .filterValues { systems -> excludedSystems.containsAll(systems) }
        .keys
        .map { "**/$it" }

android {
    defaultConfig {
        versionCode = 1
        versionName = "0.1.0"
        applicationId = "com.twocstudio.emulator"
        buildConfigField("String", "EXCLUDED_SYSTEMS", "\"${excludedSystems.sorted().joinToString(",")}\"")
        ndk {
            // PS2 emulation needs a 64-bit ARM device; we only ship that ABI.
            abiFilters += "arm64-v8a"
        }
    }

    ndkVersion = "27.0.12077973"
    externalNativeBuild {
        // Small JNI bridge for cores that need the JavaVM (see src/main/cpp/core_bridge.cpp).
        ndkBuild {
            path = file("src/main/cpp/Android.mk")
        }
    }

    sourceSets {
        // Extra cores downloaded by extra-cores/fetch-cores.sh
        getByName("main").jniLibs.srcDir("$rootDir/extra-cores/jniLibs")
    }
    flavorDimensions += listOf("opensource", "cores")

    if (usePlayDynamicFeatures()) {
        println("Building Google Play version. Bundling dynamic features.")
        dynamicFeatures.addAll(
            setOf(
                ":lemuroid_core_desmume",
                ":lemuroid_core_dosbox_pure",
                ":lemuroid_core_fbneo",
                ":lemuroid_core_fceumm",
                ":lemuroid_core_gambatte",
                ":lemuroid_core_genesis_plus_gx",
                ":lemuroid_core_handy",
                ":lemuroid_core_mame2003_plus",
                ":lemuroid_core_mednafen_ngp",
                ":lemuroid_core_mednafen_pce_fast",
                ":lemuroid_core_mednafen_wswan",
                ":lemuroid_core_melonds",
                ":lemuroid_core_mgba",
                ":lemuroid_core_mupen64plus_next_gles3",
                ":lemuroid_core_pcsx_rearmed",
                ":lemuroid_core_ppsspp",
                ":lemuroid_core_prosystem",
                ":lemuroid_core_snes9x",
                ":lemuroid_core_stella",
                ":lemuroid_core_citra",
            ),
        )
    }

    // Since some dependencies are closed source we make a completely free as in free speech variant.

    productFlavors {

        create("free") {
            dimension = "opensource"
        }

        create("play") {
            dimension = "opensource"
        }

        // Include cores in the final apk
        create("bundle") {
            dimension = "cores"
        }

        // Download cores on demand (from GooglePlay or GitHub)
        create("dynamic") {
            dimension = "cores"
        }
    }

    packagingOptions {
        jniLibs {
            // Stripping created some issues with some libretro cores such as ppsspp
            keepDebugSymbols += setOf("*/*/*_libretro_android.so")
            useLegacyPackaging = true
            // Upstream cores we replaced because of non-commercial licenses.
            excludes +=
                setOf(
                    "**/libsnes9x_libretro_android.so",
                    "**/libgenesis_plus_gx_libretro_android.so",
                    "**/libfbneo_libretro_android.so",
                    "**/libmame2003_plus_libretro_android.so",
                )
            excludes += excludedCoreLibraries
        }
        resources {
            excludes += setOf("META-INF/DEPENDENCIES", "META-INF/library_release.kotlin_module")
        }
    }

    signingConfigs {
        maybeCreate("debug").apply {
            storeFile = file("$rootDir/debug.keystore")
        }

        // Release signing comes from keystore.properties in the repository root (never committed;
        // backed up in Google Drive, and recreated from Actions secrets in CI). Without it the
        // release build is unsigned, so forks still build.
        if (releaseSigning.containsKey("storeFile")) {
            maybeCreate("release").apply {
                storeFile = rootProject.file(releaseSigning.getProperty("storeFile"))
                storePassword = releaseSigning.getProperty("storePassword")
                keyAlias = releaseSigning.getProperty("keyAlias")
                keyPassword = releaseSigning.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            resValue("string", "lemuroid_name", "2C Emulator")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "lemuroid_name", "2C Emulator Dev")
        }
    }

    lint {
        disable += setOf("MissingTranslation", "ExtraTranslation", "EnsureInitializerMetadata")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = deps.versions.kotlinExtension
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.swordfish.lemuroid"
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":lemuroid-metadata-libretro-db"))
    implementation(project(":lemuroid-touchinput"))

    "baselineProfile"(project(":baselineprofile"))
    implementation(deps.libs.androidx.profileInstaller)

    "bundleImplementation"(project(":bundled-cores"))

    "freeImplementation"(project(":lemuroid-app-ext-free"))
    "playImplementation"(project(":lemuroid-app-ext-play"))

    implementation(deps.libs.androidx.navigation.navigationFragment)
    implementation(deps.libs.androidx.navigation.navigationUi)
    implementation(deps.libs.androidx.navigation.compose)
    implementation(deps.libs.material)
    implementation(deps.libs.coil.coil)
    implementation(deps.libs.coil.coilCompose)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.activity.activity)
    implementation(deps.libs.androidx.activity.activityKtx)
    implementation(deps.libs.androidx.activity.compose)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.androidx.lifecycle.reactiveStreams)

    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.leanback.leanback)
    implementation(deps.libs.androidx.leanback.leanbackPreference)
    implementation(deps.libs.androidx.leanback.leanbackPaging)

    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.ktx)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okio)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.flowPreferences)
    implementation(deps.libs.guava)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.androidx.leanback.tvProvider)
    implementation(deps.libs.harmony)
    implementation(deps.libs.startup)
    implementation(deps.libs.kotlin.serialization)
    implementation(deps.libs.kotlin.serializationJson)

    implementation(platform(deps.libs.androidx.compose.composeBom))
    implementation(deps.libs.androidx.compose.material3)
    implementation(deps.libs.androidx.compose.constraintLayout)
    debugImplementation(deps.libs.androidx.compose.tooling)
    implementation(deps.libs.androidx.compose.toolingPreview)
    implementation(deps.libs.androidx.compose.extendedIcons)
    implementation(deps.libs.androidx.compose.accompanist.systemUiController)
    implementation(deps.libs.androidx.compose.accompanist.navigationMaterial)
    implementation(deps.libs.androidx.compose.accompanist.drawablePainter)
    implementation(deps.libs.androidx.paging.compose)
    implementation(deps.libs.androidx.lifecycle.viewModelCompose)
    implementation(deps.libs.composeHtmlText)

    implementation(deps.libs.composeSettings.uiTiles)
    implementation(deps.libs.composeSettings.uiTilesExtended)
    implementation(deps.libs.composeSettings.diskStorage)
    implementation(deps.libs.composeSettings.memoryStorage)

    implementation(project(":libretrodroid"))

    // Uncomment this when using a local aar file.
    // implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    kapt(deps.libs.dagger.android.processor)
    kapt(deps.libs.dagger.compiler)
}

fun usePlayDynamicFeatures(): Boolean {
    val task = gradle.startParameter.taskRequests.toString()
    return task.contains("Play") && task.contains("Dynamic")
}
