package com.example.myapplication.util

enum class SortBy {
    ID, TITLE, CONTENT
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

data class SortOption(
    val sortBy: SortBy = SortBy.ID,
    val sortOrder: SortOrder = SortOrder.ASCENDING
)