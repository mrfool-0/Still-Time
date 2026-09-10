package com.mrfool.stilltime.spotify

import com.spotify.android.appremote.api.error.*
import org.junit.Assert.*
import org.junit.Test

class SpotifyConnectionErrorTest {
    @Test fun authenticationFailuresHaveStableRedactedMessages() {
        assertEquals("Spotify authorization failed.", spotifyConnectionError(AuthenticationFailedException("private payload", null)))
        assertEquals("Spotify authorization required.", spotifyConnectionError(UserNotAuthorizedException("private payload", null)))
        assertEquals("Sign in to Spotify.", spotifyConnectionError(NotLoggedInException("private payload", null)))
        assertEquals("Spotify connection failed.", spotifyConnectionError(IllegalStateException("private payload")))
    }
}
