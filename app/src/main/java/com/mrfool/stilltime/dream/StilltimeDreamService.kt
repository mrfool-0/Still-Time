package com.mrfool.stilltime.dream

import android.service.dreams.DreamService
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mrfool.stilltime.data.SettingsStore
import com.mrfool.stilltime.ui.screens.StandbyScreen
import com.mrfool.stilltime.ui.theme.StilltimeTheme

class StilltimeDreamService : DreamService() {
    private var contentView: ComposeView? = null
    private var settingsStore: SettingsStore? = null
    private var viewOwner: DreamViewOwner? = null
    private var dreaming by mutableStateOf(false)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false

        val store = SettingsStore(this)
        val owner = DreamViewOwner()
        viewOwner = owner
        settingsStore = store
        contentView = ComposeView(this).also { view ->
            view.setViewTreeLifecycleOwner(owner)
            view.setViewTreeSavedStateRegistryOwner(owner)
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
                        tickerActive = dreaming,
                        showControls = false,
                    )
                }
            }
            setContentView(view)
        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        viewOwner?.start()
        dreaming = true
    }

    override fun onDreamingStopped() {
        dreaming = false
        viewOwner?.stop()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        dreaming = false
        contentView?.disposeComposition()
        contentView = null
        settingsStore?.close()
        settingsStore = null
        viewOwner?.destroy()
        viewOwner = null
        super.onDetachedFromWindow()
    }
}
