package com.mrfool.stilltime.spotify

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Enables Android's user-consented media-session access. No notification bodies are read. */
class SpotifyMediaAccessService : NotificationListenerService() {
    override fun onListenerConnected() { super.onListenerConnected(); ready.value = true }
    override fun onListenerDisconnected() { ready.value = false; super.onListenerDisconnected() }
    override fun onDestroy() { ready.value = false; super.onDestroy() }
    companion object { internal val ready = MutableStateFlow(false) }
}

object SpotifyPackages {
    val supported = setOf("com.spotify.music", "com.spotify.lite", "com.spotify.music.canary", "com.spotify.music.partners")
    @Suppress("DEPRECATION")
    fun installed(context: Context) = supported.filter { name ->
        try { context.packageManager.getPackageInfo(name, 0); true } catch (_: PackageManager.NameNotFoundException) { false }
    }
}

/** Optional SDK-independent connection, subscribed only while the Spotify face is visible. */
class DevicePlayerController(private val context: Context) : StandbyPlayer {
    private val handler = Handler(Looper.getMainLooper())
    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val component = ComponentName(context, SpotifyMediaAccessService::class.java)
    private val mutableState = MutableStateFlow(SpotifyUiState(status = SpotifyStatus.ACCESS_REQUIRED))
    override val state = mutableState.asStateFlow()
    private var active = false
    private var listening = false
    private var selected: MediaController? = null
    private var compat: MediaControllerCompat? = null
    private val compatCallback = object : MediaControllerCompat.Callback() {
        override fun onSessionReady() = update()
        override fun onShuffleModeChanged(shuffleMode: Int) = update()
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) = update()
    }
    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = update()
        override fun onPlaybackStateChanged(state: PlaybackState?) = update()
        override fun onSessionDestroyed() { if (active) connect(false) }
    }
    private val sessions = MediaSessionManager.OnActiveSessionsChangedListener { if (active) choose(it.orEmpty()) }

    override fun setActive(value: Boolean) {
        if (active == value) return
        active = value
        if (value) connect(false) else detach()
    }

    override fun connect(authorize: Boolean) {
        if (!active) return
        if (!NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) {
            detach()
            mutableState.value = SpotifyUiState(status = SpotifyStatus.ACCESS_REQUIRED)
            if (authorize) {
                try { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                catch (_: Exception) { mutableState.value = mutableState.value.copy(error = "Open Android Settings → Notification access → Stilltime media access.") }
            }
            return
        }
        try {
            if (!listening) {
                manager.addOnActiveSessionsChangedListener(sessions, component, handler)
                listening = true
            }
            choose(manager.getActiveSessions(component))
        } catch (_: SecurityException) {
            detach()
            mutableState.value = SpotifyUiState(status = SpotifyStatus.ACCESS_REQUIRED,
                error = "Allow Stilltime media access in Android Settings, then return here.")
        }
    }

    private fun choose(controllers: List<MediaController>) {
        val candidate = controllers.filter { it.packageName in SpotifyPackages.supported }
            .sortedByDescending { it.playbackState?.state == PlaybackState.STATE_PLAYING }.firstOrNull()
        if (selected?.sessionToken != candidate?.sessionToken) {
            compat?.unregisterCallback(compatCallback)
            compat = null
            selected?.unregisterCallback(callback)
            selected = candidate
            candidate?.registerCallback(callback, handler)
            if (candidate != null) {
                compat = MediaControllerCompat(context, MediaSessionCompat.Token.fromToken(candidate.sessionToken)!!)
                    .also { it.registerCallback(compatCallback, handler) }
            }
        }
        update()
    }

    private fun update() {
        if (!active) return
        val controller = selected
        mutableState.value = if (controller == null) SpotifyUiState(status = SpotifyStatus.CONNECTED,
            error = null)
        else devicePlayerSnapshot(controller.metadata, controller.playbackState).let { snapshot ->
            val bridge = compat
            val mode = if (bridge?.isSessionReady == true) bridge.shuffleMode else PlaybackStateCompat.SHUFFLE_MODE_INVALID
            snapshot.copy(canShuffle = shuffleAvailable(bridge?.isSessionReady == true, bridge?.playbackState?.actions ?: 0, mode),
                shuffled = mode == PlaybackStateCompat.SHUFFLE_MODE_ALL || mode == PlaybackStateCompat.SHUFFLE_MODE_GROUP)
        }
    }

    override fun previous() { if (state.value.canSkipPrevious) command { it.skipToPrevious() } }
    override fun next() { if (state.value.canSkipNext) command { it.skipToNext() } }
    override fun togglePlayback() = command { if (state.value.timeline.paused) it.play() else it.pause() }
    override fun seekTo(positionMs: Long, trackId: String) {
        val target = seekTarget(state.value, positionMs, trackId) ?: return
        command { it.seekTo(target) }
    }
    override fun toggleShuffle() {
        if (!active || !state.value.canShuffle) return
        try {
            compat?.transportControls?.setShuffleMode(if (state.value.shuffled) PlaybackStateCompat.SHUFFLE_MODE_NONE else PlaybackStateCompat.SHUFFLE_MODE_ALL)
        } catch (_: SecurityException) {
            mutableState.value = state.value.copy(error = "Shuffle unavailable. Open Spotify.")
        }
    }
    private fun command(action: (MediaController.TransportControls) -> Unit) {
        if (!active) return
        try { selected?.transportControls?.let(action) }
        catch (_: SecurityException) { mutableState.value = SpotifyUiState(status = SpotifyStatus.ACCESS_REQUIRED) }
    }
    private fun detach() {
        if (listening) manager.removeOnActiveSessionsChangedListener(sessions)
        listening = false
        selected?.unregisterCallback(callback)
        selected = null
        compat?.unregisterCallback(compatCallback)
        compat = null
        mutableState.value = SpotifyUiState(status = SpotifyStatus.DISCONNECTED)
    }
    override fun close() { active = false; detach() }
}

internal fun shuffleAvailable(ready: Boolean, actions: Long, mode: Int): Boolean =
    ready && actions and PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE != 0L &&
        mode in setOf(PlaybackStateCompat.SHUFFLE_MODE_NONE, PlaybackStateCompat.SHUFFLE_MODE_ALL, PlaybackStateCompat.SHUFFLE_MODE_GROUP)

fun devicePlayerSnapshot(metadata: MediaMetadata?, playback: PlaybackState?): SpotifyUiState {
    val actions = playback?.actions ?: 0
    return SpotifyUiState(status = SpotifyStatus.CONNECTED,
        title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
        artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
        artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
        timeline = PlaybackTimeline(playback?.position ?: 0, metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0,
            playback?.lastPositionUpdateTime ?: 0, playback?.state != PlaybackState.STATE_PLAYING, playback?.playbackSpeed ?: 0f),
        canSkipPrevious = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
        canSkipNext = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
        canSeek = actions and PlaybackState.ACTION_SEEK_TO != 0L && (metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0) > 0,
        trackId = metadata?.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
            ?: "${metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)}|${metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)}|${metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)}")
}
