package com.mrfool.stilltime.spotify

import org.junit.Assert.*
import org.junit.Test

class SeekTargetTest {
    private val track = SpotifyUiState(status = SpotifyStatus.CONNECTED, trackId = "song-a", canSeek = true,
        timeline = PlaybackTimeline(15_000, 180_000))
    @Test fun targetIsClampedToTheCurrentSong() {
        assertEquals(0L, seekTarget(track, -10, "song-a"))
        assertEquals(180_000L, seekTarget(track, Long.MAX_VALUE, "song-a"))
        assertEquals(62_000L, seekTarget(track, 62_000, "song-a"))
    }
    @Test fun staleAndUnsupportedSeeksAreRejected() {
        assertNull(seekTarget(track, 1000, "previous-song"))
        assertNull(seekTarget(track.copy(canSeek = false), 1000, "song-a"))
        assertNull(seekTarget(track.copy(status = SpotifyStatus.DISCONNECTED), 1000, "song-a"))
        assertNull(seekTarget(track.copy(timeline = PlaybackTimeline()), 1000, "song-a"))
    }
}
