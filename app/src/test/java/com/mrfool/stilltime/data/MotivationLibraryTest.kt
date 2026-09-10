package com.mrfool.stilltime.data

import com.mrfool.stilltime.model.MotivationCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MotivationLibraryTest {
    @Test fun `rotation changes exactly at thirty second boundaries`() {
        assertEquals(MotivationLibrary.indexFor(0, 203), MotivationLibrary.indexFor(29, 203))
        assertTrue(MotivationLibrary.indexFor(29, 203) != MotivationLibrary.indexFor(30, 203))
        assertEquals(MotivationLibrary.indexFor(30, 203), MotivationLibrary.indexFor(59, 203))
        assertTrue(MotivationLibrary.indexFor(59, 203) != MotivationLibrary.indexFor(60, 203))
    }

    @Test fun `each category cycles through every entry without adjacent repetition`() {
        for (category in MotivationCategory.entries) {
            val count = entries.count { category == MotivationCategory.ALL || it.category == category }
            val indices = (0 until count).map { MotivationLibrary.indexFor(it * 30L, count) }
            assertEquals(count, indices.toSet().size)
            assertEquals(indices.first(), MotivationLibrary.indexFor(count * 30L, count))
            assertTrue(MotivationLibrary.indexFor(-1, count) in 0 until count)
        }
    }

    private val entries by lazy {
        listOf("motivation", "series_motivation").flatMap {
            MotivationLibrary.parse(File("src/main/res/raw/$it.tsv").readText())
        }
    }

    @Test
    fun `library contains more than two hundred sourced entries`() {
        assertTrue("Expected at least 300 entries", entries.size >= 300)
        assertTrue(entries.count { it.category == MotivationCategory.WISDOM } >= 60)
        assertTrue(entries.count { it.category == MotivationCategory.LIFE_FACT } >= 90)
        assertTrue(entries.count { it.category == MotivationCategory.ANIME } >= 30)
    }

    @Test fun `each requested series has twenty original clearly labelled reflections`() {
        val series = MotivationCategory.entries.filter { it.inspiredBy != null }
        assertEquals(5, series.size)
        series.forEach { category ->
            val pack = entries.filter { it.category == category }
            assertEquals(20, pack.size)
            pack.forEach {
                assertEquals("Stilltime · original reflection", it.attribution)
                assertTrue(it.sourceTitle.startsWith(category.inspiredBy!!))
                assertTrue(it.sourceTitle.endsWith("inspired, not canon"))
            }
        }
    }

    @Test
    fun `every entry is complete and links to its source`() {
        entries.forEach { entry ->
            assertTrue(entry.text.isNotBlank())
            assertTrue(entry.attribution.isNotBlank())
            assertTrue(entry.sourceTitle.isNotBlank())
            assertTrue(entry.sourceUrl.startsWith("https://"))
        }
    }

    @Test
    fun `quoted entries stay short and texts are unique`() {
        entries
            .filter { it.category != MotivationCategory.LIFE_FACT }
            .forEach { entry ->
                val wordCount = entry.text.trim().split(Regex("\\s+")).size
                assertTrue("Quote is too long: ${entry.text}", wordCount <= 20)
            }

        assertEquals(entries.size, entries.map { it.text.lowercase() }.toSet().size)
    }
}
