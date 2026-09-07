package com.mrfool.stilltime.model

import androidx.compose.runtime.Immutable

@Immutable
data class MotivationEntry(
    val category: MotivationCategory,
    val text: String,
    val attribution: String,
    val sourceTitle: String,
    val sourceUrl: String,
)
