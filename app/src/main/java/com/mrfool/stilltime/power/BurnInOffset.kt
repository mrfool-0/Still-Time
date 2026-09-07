package com.mrfool.stilltime.power

data class PixelOffset(val xFraction: Float, val yFraction: Float)

object BurnInOffset {
    /**
     * Produces a deterministic low-discrepancy offset for a caller-supplied time bucket.
     * Fractions are in [-1, 1] and are scaled to pixels by the UI.
     */
    fun forEpochMinute(epochMinute: Long): PixelOffset {
        val xStep = Math.floorMod(epochMinute * 7L, 17L).toFloat() / 8f - 1f
        val yStep = Math.floorMod(epochMinute * 11L, 13L).toFloat() / 6f - 1f
        return PixelOffset(xFraction = xStep, yFraction = yStep)
    }
}
