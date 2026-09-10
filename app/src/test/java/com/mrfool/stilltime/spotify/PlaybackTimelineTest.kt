package com.mrfool.stilltime.spotify

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTimelineTest {
    @Test fun `playing position advances by elapsed realtime at playback speed`() {
        assertEquals(13_000, PlaybackTimeline(10_000, 60_000, 5_000, false, 1.5f).positionAt(7_000))
    }
    @Test fun `paused position stays still`() {
        assertEquals(10_000, PlaybackTimeline(10_000, 60_000, 5_000, true).positionAt(30_000))
    }
    @Test fun `end of track is clamped`() {
        assertEquals(60_000, PlaybackTimeline(59_000, 60_000, 5_000, false).positionAt(20_000))
    }
    @Test fun `older timestamp never moves playback backward`() {
        assertEquals(10_000, PlaybackTimeline(10_000, 60_000, 5_000, false).positionAt(1_000))
    }
    @Test fun `missing duration remains zero and formatting supports long tracks`() {
        assertEquals(0, PlaybackTimeline(10_000, 0, 0, false).positionAt(99_000))
        assertEquals("61:05", playbackTimeLabel(3_665_000))
        assertEquals("0:00", playbackTimeLabel(-1))
    }
}
