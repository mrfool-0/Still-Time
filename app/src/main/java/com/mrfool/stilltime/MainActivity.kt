package com.mrfool.stilltime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrfool.stilltime.data.SettingsStore
import com.mrfool.stilltime.ui.screens.StandbyScreen
import com.mrfool.stilltime.ui.theme.StilltimeTheme

class MainActivity : ComponentActivity() {
    private lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        settingsStore = SettingsStore(this)

        setContent {
            StilltimeTheme {
                val preferences by settingsStore.state.collectAsStateWithLifecycle()
                var isResumed by remember { mutableStateOf(true) }

                LifecycleResumeEffect(Unit) {
                    isResumed = true
                    hideSystemBars()
                    onPauseOrDispose { isResumed = false }
                }

                DisposableEffect(preferences.keepScreenOn, preferences.brightness) {
                    window.decorView.keepScreenOn = preferences.keepScreenOn
                    val attributes = window.attributes
                    attributes.screenBrightness = preferences.brightness.windowBrightness
                        ?: android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    window.attributes = attributes
                    onDispose {
                        window.decorView.keepScreenOn = false
                        val restored = window.attributes
                        restored.screenBrightness =
                            android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                        window.attributes = restored
                    }
                }

                StandbyScreen(
                    preferences = preferences,
                    settingsStore = settingsStore,
                    tickerActive = isResumed,
                    showControls = true,
                )
            }
        }
    }

    override fun onDestroy() {
        settingsStore.close()
        super.onDestroy()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
