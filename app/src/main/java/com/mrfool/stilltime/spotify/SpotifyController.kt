package com.mrfool.stilltime.spotify

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.mrfool.stilltime.BuildConfig
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.AuthenticationFailedException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SpotifyStatus { SETUP_REQUIRED, APP_MISSING, SDK_UNAVAILABLE, ACCESS_REQUIRED, DISCONNECTED, CONNECTING, CONNECTED, ERROR }

data class SpotifyUiState(
    val status: SpotifyStatus = SpotifyStatus.DISCONNECTED,
    val title: String = "",
    val artist: String = "",
    val artwork: Bitmap? = null,
    val timeline: PlaybackTimeline = PlaybackTimeline(),
    val canSkipPrevious: Boolean = false,
    val canSkipNext: Boolean = false,
    val error: String? = null,
    val trackId: String = "",
    val canSeek: Boolean = false,
    val canShuffle: Boolean = false,
    val shuffled: Boolean = false,
)

/** Owns only a visible screen's IPC connection. Spotify remains responsible for audio playback. */
class SpotifyController(private val context: Context) : StandbyPlayer {
    private val handler = Handler(Looper.getMainLooper())
    private var remote: SpotifyAppRemote? = null
    private var subscription: Subscription<PlayerState>? = null
    private var generation = 0L
    private var active = false
    private var artworkUri: String? = null
    private var timeout: Runnable? = null
    private val mutableState = MutableStateFlow(SpotifyUiState(status = initialStatus()))
    override val state = mutableState.asStateFlow()

    private fun initialStatus(): SpotifyStatus = when {
        BuildConfig.SPOTIFY_CLIENT_ID.isBlank() -> SpotifyStatus.SETUP_REQUIRED
        !SpotifyAppRemote.isSpotifyInstalled(context) -> if (SpotifyPackages.installed(context).isNotEmpty()) SpotifyStatus.SDK_UNAVAILABLE else SpotifyStatus.APP_MISSING
        else -> SpotifyStatus.DISCONNECTED
    }

    override fun setActive(value: Boolean) {
        if (value == active) return
        active = value
        if (value) connect(authorize = false) else disconnect()
    }

    override fun connect(authorize: Boolean) {
        if (!active || remote?.isConnected == true || mutableState.value.status == SpotifyStatus.CONNECTING) return
        val status = initialStatus()
        if (status != SpotifyStatus.DISCONNECTED) {
            mutableState.value = SpotifyUiState(status = status)
            return
        }
        val token = ++generation
        mutableState.value = SpotifyUiState(status = SpotifyStatus.CONNECTING)
        val params = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
            .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
            .showAuthView(authorize)
            .build()
        timeout = Runnable {
            if (token == generation) fail("Connection timed out. Open Spotify, then try again.")
        }.also { handler.postDelayed(it, 30_000) }
        try {
            SpotifyAppRemote.connect(context, params, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    handler.post {
                        if (!active || token != generation) {
                            SpotifyAppRemote.disconnect(appRemote)
                            return@post
                        }
                        clearTimeout()
                        remote = appRemote
                        mutableState.value = SpotifyUiState(status = SpotifyStatus.CONNECTED)
                        val stream = appRemote.playerApi.subscribeToPlayerState()
                        subscription = stream
                        stream.setEventCallback { player -> handler.post {
                            if (active && token == generation) updatePlayer(player, appRemote, token)
                        } }
                        stream.setLifecycleCallback(object : Subscription.LifecycleCallback {
                            override fun onStart() = Unit
                            override fun onStop() { handler.post {
                                if (active && token == generation) fail("Spotify disconnected. Tap Connect to retry.")
                            } }
                        })
                        stream.setErrorCallback { handler.post {
                            if (token == generation) fail("Spotify disconnected. Tap Connect to retry.")
                        } }
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    handler.post {
                        if (token == generation) {
                            fail(spotifyConnectionError(throwable))
                        }
                    }
                }
            })
        } catch (_: Exception) {
            fail("Could not open Spotify. Open the Spotify app and try again.")
        }
    }

    private fun updatePlayer(player: PlayerState, appRemote: SpotifyAppRemote, token: Long) {
        val track = player.track
        val imageUri = track?.imageUri?.raw
        val changedArtwork = imageUri != artworkUri
        artworkUri = imageUri
        mutableState.value = mutableState.value.copy(
            status = SpotifyStatus.CONNECTED,
            title = track?.name.orEmpty(),
            artist = track?.artists?.joinToString(", ") { it.name }.orEmpty().ifBlank { track?.artist?.name.orEmpty() },
            artwork = if (changedArtwork) null else mutableState.value.artwork,
            timeline = PlaybackTimeline(player.playbackPosition, track?.duration ?: 0, SystemClock.elapsedRealtime(), player.isPaused, player.playbackSpeed),
            canSkipPrevious = player.playbackRestrictions?.canSkipPrev == true,
            canSkipNext = player.playbackRestrictions?.canSkipNext == true,
            trackId = track?.uri.orEmpty(),
            canSeek = player.playbackRestrictions?.canSeek == true && (track?.duration ?: 0) > 0,
            canShuffle = player.playbackRestrictions?.canToggleShuffle == true,
            shuffled = player.playbackOptions?.isShuffling == true,
            error = null,
        )
        if (changedArtwork && imageUri != null) {
            appRemote.imagesApi.getImage(track.imageUri, Image.Dimension.MEDIUM)
                .setResultCallback { bitmap -> handler.post {
                    if (active && token == generation && artworkUri == imageUri) {
                        mutableState.value = mutableState.value.copy(artwork = bitmap)
                    }
                } }
                .setErrorCallback { /* Keep the neutral artwork placeholder on an image-only failure. */ }
        }
    }

    override fun previous() { if (mutableState.value.canSkipPrevious) command { it.playerApi.skipPrevious() } }
    override fun next() { if (mutableState.value.canSkipNext) command { it.playerApi.skipNext() } }
    override fun togglePlayback() = command { if (mutableState.value.timeline.paused) it.playerApi.resume() else it.playerApi.pause() }
    override fun seekTo(positionMs: Long, trackId: String) {
        val target = seekTarget(state.value, positionMs, trackId) ?: return
        command { it.playerApi.seekTo(target) }
    }
    override fun toggleShuffle() {
        if (state.value.canShuffle) command { it.playerApi.setShuffle(!state.value.shuffled) }
    }

    private fun command(action: (SpotifyAppRemote) -> CallResult<*>) {
        val appRemote = remote?.takeIf { active && it.isConnected } ?: return
        val token = generation
        try {
            action(appRemote).setErrorCallback { handler.post {
                if (token == generation) mutableState.value = mutableState.value.copy(error = "Spotify couldn't perform that action. Try in Spotify.")
            } }
        } catch (_: Exception) {
            if (token == generation) fail("Spotify disconnected. Tap Connect to retry.")
        }
    }

    private fun clearTimeout() { timeout?.let(handler::removeCallbacks); timeout = null }

    private fun fail(message: String) {
        disconnect()
        mutableState.value = SpotifyUiState(status = SpotifyStatus.ERROR, error = message)
    }

    private fun disconnect() {
        ++generation
        clearTimeout()
        subscription?.cancel()
        subscription = null
        remote?.let(SpotifyAppRemote::disconnect)
        remote = null
        artworkUri = null
        mutableState.value = SpotifyUiState(status = initialStatus())
    }

    override fun close() { active = false; disconnect() }
}

/** Type checks survive R8 obfuscation; never display raw SDK payloads or obfuscated names. */
internal fun spotifyConnectionError(error: Throwable): String = when (error) {
    is AuthenticationFailedException -> "Spotify authorization failed."
    is UserNotAuthorizedException -> "Spotify authorization required."
    is NotLoggedInException -> "Sign in to Spotify."
    else -> "Spotify connection failed."
}
