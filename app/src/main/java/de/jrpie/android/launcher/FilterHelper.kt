package de.jrpie.android.launcher

import android.icu.text.Normalizer2
import android.os.Build
import java.util.Locale
import kotlin.text.Regex.Companion.escape

abstract class FilterHelper {
    private fun unicodeNormalize(s: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val normalizer = Normalizer2.getNFKDInstance()
            return normalizer.normalize(s.lowercase(Locale.ROOT))
        }
        return s.lowercase(Locale.ROOT)
    }

    private fun buildNormalizeFunction(query: String): (String) -> String {
        // normalize text for search
        val allowedSpecialCharacters = unicodeNormalize(query)
            .lowercase(Locale.ROOT)
            .toCharArray()
            .distinct()
            .filter { c -> !c.isLetter() }
            .map { c -> escape(c.toString()) }
            .fold("") { x, y -> x + y }
        val disallowedCharsRegex = "[^\\p{L}$allowedSpecialCharacters]".toRegex()

        return { text: String ->
            unicodeNormalize(text).replace(disallowedCharsRegex, "")
        }
    }

    /**
     * Filters the elements of the list according to the query.
     * The result contains the elements that have a key that start with the query,
     * followed by the elements with a key that contains the query as a substring.
     */
    protected fun <T> List<T>.filterByQuery(query: String, keys: (T) -> List<String>): List<T> {
        val normalize = buildNormalizeFunction(query)
        val resultsPrimary: MutableList<T> = ArrayList()
        val resultsSecondary: MutableList<T> = ArrayList()
        val normalizedQuery: String = normalize(query)
        for (item in this) {
            val itemKeys = keys(item).map(normalize)
            if (itemKeys.any { it.startsWith(normalizedQuery) }) {
                resultsPrimary.add(item)
            } else if (itemKeys.any { it.contains(normalizedQuery) }) {
                resultsSecondary.add(item)
            }
        }
        resultsPrimary.addAll(resultsSecondary)
        return resultsPrimary
    }
}