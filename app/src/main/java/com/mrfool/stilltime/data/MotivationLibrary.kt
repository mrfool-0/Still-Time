package com.mrfool.stilltime.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.mrfool.stilltime.R
import com.mrfool.stilltime.model.MotivationCategory
import com.mrfool.stilltime.model.MotivationEntry

object MotivationLibrary {
    @Volatile
    private var cachedEntries: List<MotivationEntry>? = null

    fun entryFor(
        context: Context,
        category: MotivationCategory,
        epochMinute: Long,
    ): MotivationEntry {
        val matching = entries(context).let { all ->
            if (category == MotivationCategory.ALL) all else all.filter { it.category == category }
        }
        check(matching.isNotEmpty()) { "Motivation library has no entries for $category" }
        val rotationBucket = Math.floorDiv(epochMinute, ROTATION_MINUTES)
        val index = Math.floorMod(rotationBucket * INDEX_STEP + INDEX_SEED, matching.size.toLong())
        return matching[index.toInt()]
    }

    fun entries(context: Context): List<MotivationEntry> = cachedEntries ?: synchronized(this) {
        cachedEntries ?: context.resources.openRawResource(R.raw.motivation).bufferedReader().use {
            parse(it.readText()).also { parsed -> cachedEntries = parsed }
        }
    }

    @VisibleForTesting
    fun parse(raw: String): List<MotivationEntry> = raw.lineSequence()
        .filter { it.isNotBlank() && !it.startsWith('#') }
        .mapIndexed { index, line ->
            val fields = line.split('\t')
            require(fields.size == FIELD_COUNT) {
                "Invalid motivation row ${index + 1}: expected $FIELD_COUNT tab-separated fields"
            }
            MotivationEntry(
                category = MotivationCategory.valueOf(fields[0]),
                text = fields[1],
                attribution = fields[2],
                sourceTitle = fields[3],
                sourceUrl = fields[4],
            )
        }
        .toList()

    private const val FIELD_COUNT = 5
    private const val ROTATION_MINUTES = 15L
    private const val INDEX_STEP = 73L
    private const val INDEX_SEED = 17L
}
