package com.example.general_first_aid_kit.presentation.utils

import me.xdrop.fuzzywuzzy.FuzzySearch

object FuzzyMatcher {
    private const val THRESHOLD = 65

    fun matches(query: String, target: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        val t = target.lowercase()
        return t.contains(q) || FuzzySearch.partialRatio(q, t) >= THRESHOLD
    }
}
