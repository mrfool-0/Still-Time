package com.mrfool.stilltime.dream

import android.service.dreams.DreamService
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mrfool.stilltime.data.SettingsStore
import com.mrfool.stilltime.ui.screens.StandbyScreen
import com.mrfool.stilltime.ui.theme.StilltimeTheme

class StilltimeDreamService : DreamService() {
    private var contentView: ComposeView? = null
    private var settingsStore: SettingsStore? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false

        val store = SettingsStore(this)
        settingsStore = store
        contentView = ComposeView(this).also { view ->
            view.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindow,
            )
            view.setContent {
                StilltimeTheme {
                    val preferences by store.state.collectAsState()
                    DisposableEffect(preferences.brightness) {
                        val attributes = window.attributes
                        attributes.screenBrightness = preferences.brightness.windowBrightness
                            ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                        window.attributes = attributes
                        onDispose {
                            val restored = window.attributes
                            restored.screenBrightness =
                                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                            window.attributes = restored
                        }
                    }
                    StandbyScreen(
                        preferences = preferences,
                        settingsStore = store,
                        tickerActive = true,
                        showControls = false,
                    )
                }
            }
            setContentView(view)
        }
    }

    override fun onDetachedFromWindow() {
        contentView?.disposeComposition()
        contentView = null
        settingsStore?.close()
        settingsStore = null
        super.onDetachedFromWindow()
    }
}
