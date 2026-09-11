package com.mrfool.stilltime

import android.media.MediaMetadata
import android.media.session.PlaybackState
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.mrfool.stilltime.dream.DreamViewOwner
import com.mrfool.stilltime.spotify.devicePlayerSnapshot
import org.junit.Assert.*
import org.junit.Test

class IntegrationRegressionTest {
    @Test fun shuffleRequiresAdvertisedSupportAndKnownState() {
        val action = android.support.v4.media.session.PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
        assertTrue(com.mrfool.stilltime.spotify.shuffleAvailable(true, action, 0))
        assertTrue(com.mrfool.stilltime.spotify.shuffleAvailable(true, action, 1))
        assertFalse(com.mrfool.stilltime.spotify.shuffleAvailable(false, action, 1))
        assertFalse(com.mrfool.stilltime.spotify.shuffleAvailable(true, 0, 1))
        assertFalse(com.mrfool.stilltime.spotify.shuffleAvailable(true, action, -1))
    }
    @Test fun spotifyReflectionProviderSurvivesFullModeR8() {
        val provider = Class.forName("com.spotify.android.appremote.internal.ReleaseSpotifyLocator")
            .getConstructor().newInstance()
        assertTrue(Class.forName("com.spotify.android.appremote.internal.PackageProvider").isInstance(provider))
    }

    @Test fun dreamOwnerSupportsCreationStopAndDestruction() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val owner = DreamViewOwner()
            assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)
            assertTrue(owner.savedStateRegistry.isRestored)
            owner.start()
            assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)
            owner.stop()
            assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)
            owner.destroy()
            assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
        }
    }

    @Test fun devicePlayerUsesSessionMetadataAndTransportRestrictions() {
        val metadata = MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE, "Fixture track")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "Fixture artist")
            .putLong(MediaMetadata.METADATA_KEY_DURATION, 213_000).build()
        val playback = PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING, 72_000, 1f, 5_000)
            .setActions(PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_PAUSE).build()
        val state = devicePlayerSnapshot(metadata, playback)
        assertEquals("Fixture track", state.title)
        assertEquals("Fixture artist", state.artist)
        assertEquals(73_000, state.timeline.positionAt(6_000))
        assertTrue(state.canSkipNext)
        assertFalse(state.canSkipPrevious)
        assertFalse(state.timeline.paused)
        assertFalse(state.canSeek)
        val seekable = PlaybackState.Builder(playback).setActions(PlaybackState.ACTION_SEEK_TO).build()
        assertTrue(devicePlayerSnapshot(metadata, seekable).canSeek)
        assertFalse(devicePlayerSnapshot(null, seekable).canSeek)
        assertTrue(devicePlayerSnapshot(null, null).timeline.paused)
    }
}
