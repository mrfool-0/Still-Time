package com.mrfool.stilltime.data

import com.mrfool.stilltime.model.MotivationCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MotivationLibraryTest {
    private val entries by lazy {
        MotivationLibrary.parse(
            File("src/main/res/raw/motivation.tsv").readText(),
        )
    }

    @Test
    fun `library contains more than two hundred sourced entries`() {
        assertTrue("Expected at least 200 entries", entries.size >= 200)
        assertTrue(entries.count { it.category == MotivationCategory.WISDOM } >= 60)
        assertTrue(entries.count { it.category == MotivationCategory.LIFE_FACT } >= 90)
        assertTrue(entries.count { it.category == MotivationCategory.ANIME } >= 30)
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
