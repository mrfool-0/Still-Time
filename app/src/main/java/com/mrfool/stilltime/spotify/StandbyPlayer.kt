package com.mrfool.stilltime.spotify

import kotlinx.coroutines.flow.StateFlow

interface StandbyPlayer : AutoCloseable {
    val state: StateFlow<SpotifyUiState>
    fun setActive(value: Boolean)
    fun connect(authorize: Boolean = true)
    fun previous()
    fun next()
    fun togglePlayback()
    fun seekTo(positionMs: Long, trackId: String)
    fun toggleShuffle()
}
