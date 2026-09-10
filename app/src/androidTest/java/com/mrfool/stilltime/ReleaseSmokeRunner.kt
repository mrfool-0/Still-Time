package com.mrfool.stilltime

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle

/** Native runner avoids AndroidX-test references to libraries stripped from a release APK. */
class ReleaseSmokeRunner : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val result = Bundle()
        try {
            check(targetContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0)
            val loader = targetContext.classLoader
            val provider = Class.forName("com.spotify.android.appremote.internal.ReleaseSpotifyLocator", true, loader)
                .getConstructor().newInstance()
            check(Class.forName("com.spotify.android.appremote.internal.PackageProvider", true, loader).isInstance(provider))
            startActivitySync(Intent().setClassName(targetContext.packageName, "com.mrfool.stilltime.MainActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            waitForIdleSync()
            result.putString("stream", "\nOK: non-debuggable APK; Spotify reflection constructor and interface retained; main Activity launched.\n")
            finish(Activity.RESULT_OK, result)
        } catch (failure: Throwable) {
            result.putString("stream", "\nFAIL: ${failure.javaClass.name}: ${failure.message}\n")
            finish(Activity.RESULT_CANCELED, result)
        }
    }
}
