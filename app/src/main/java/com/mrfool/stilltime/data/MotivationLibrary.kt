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
        epochSecond: Long,
    ): MotivationEntry {
        val matching = entries(context).let { all ->
            if (category == MotivationCategory.ALL) all else all.filter { it.category == category }
        }
        check(matching.isNotEmpty()) { "Motivation library has no entries for $category" }
        return matching[indexFor(epochSecond, matching.size)]
    }

    fun indexFor(epochSecond: Long, size: Int): Int {
        require(size > 0)
        return Math.floorMod(Math.floorDiv(epochSecond, ROTATION_SECONDS) + INDEX_SEED, size.toLong()).toInt()
    }

    fun entries(context: Context): List<MotivationEntry> = cachedEntries ?: synchronized(this) {
        cachedEntries ?: listOf(R.raw.motivation, R.raw.series_motivation).flatMap { resource ->
            context.resources.openRawResource(resource).bufferedReader().use { parse(it.readText()) }
        }.also { cachedEntries = it }
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
    const val ROTATION_SECONDS = 30L
    private const val INDEX_SEED = 17L
}
