package de.jrpie.android.launcher.util

/**
 * Returns true if `search` is a subsequence of `text`.
 * A subsequence means all characters in `search` appear in `text`
 * in the same order, but not necessarily contiguously.
 */
fun isSubsequent(text: String, search: String): Boolean {
    var i = 0
    for (char in text) {
        if (char != search[i]) continue
        i++
        if (i == search.length) {
            return true
        }
    }
    return false
}

/**
 * Returns the amount of characters from `search` that occur inside `text`.
 * If `text` contains the same character multiple times, it is only counted
 * as often as it occurs in `search`.
 */
fun countOccurrences(text: String, search: String): Int {
    val frequencies = mutableMapOf<Char, Int>()
    for (char in text) {
        frequencies[char] = frequencies.getOrElse(char) { 0 } + 1
    }
    var result = 0
    for (char in search) {
        val charFrequency = frequencies[char] ?: 0
        if (charFrequency > 0) {
            result++
            frequencies[char] = charFrequency - 1
        }
    }
    return result
}
