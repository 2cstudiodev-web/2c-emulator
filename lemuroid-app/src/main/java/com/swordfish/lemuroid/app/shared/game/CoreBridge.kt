package com.swordfish.lemuroid.app.shared.game

import timber.log.Timber

/** Native hooks for libretro cores that need more from the host process than the libretro API gives. */
object CoreBridge {
    /** Play! (PS2) needs Framework::CJavaVM::SetJavaVM called before its emulation thread starts. */
    private const val PLAY_SET_JAVA_VM = "_ZN9Framework7CJavaVM9SetJavaVMEP7_JavaVM"

    private val loaded =
        runCatching { System.loadLibrary("twoc_corebridge") }
            .onFailure { Timber.e(it, "Unable to load core bridge") }
            .isSuccess

    fun preparePlayCore(corePath: String): Boolean {
        if (!loaded) return false
        return provideJavaVm(corePath, PLAY_SET_JAVA_VM)
    }

    private external fun provideJavaVm(
        corePath: String,
        symbol: String,
    ): Boolean
}
