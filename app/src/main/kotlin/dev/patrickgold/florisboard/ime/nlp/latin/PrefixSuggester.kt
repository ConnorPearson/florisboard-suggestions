/*
 * Copyright (C) 2026 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ime.nlp.latin

import dev.patrickgold.florisboard.ime.nlp.SuggestionProvider
import dev.patrickgold.florisboard.ime.nlp.WordSuggestionCandidate

/**
 * Pure (Android-free) prefix completion over a word frequency dictionary.
 *
 * @param frequencies Map of word to frequency (0..255). Keys are normalized to lowercase; if two keys collide after
 *  lowercasing, the higher frequency wins.
 */
class PrefixSuggester(frequencies: Map<String, Int>) {
    private val freqs: Map<String, Int>
    private val sortedWords: List<String>

    init {
        val normalized = HashMap<String, Int>(frequencies.size)
        for ((word, freq) in frequencies) {
            if (word.isEmpty()) continue
            val key = word.lowercase()
            val old = normalized[key]
            if (old == null || freq > old) normalized[key] = freq
        }
        freqs = normalized
        sortedWords = normalized.keys.sorted()
    }

    val size: Int get() = sortedWords.size

    /**
     * Suggests completions for [typedWord], the (possibly empty) word fragment directly before the cursor.
     *
     * Results never include the typed word itself, are ordered by descending frequency (shorter word first on ties,
     * then alphabetical), and mirror the capitalization of the typed fragment.
     */
    fun suggest(
        typedWord: String,
        maxCount: Int,
        sourceProvider: SuggestionProvider? = null,
    ): List<WordSuggestionCandidate> {
        if (maxCount <= 0 || typedWord.isEmpty()) return emptyList()
        val prefix = typedWord.lowercase()
        val capitalizeFirst = typedWord.first().isUpperCase()
        val allCaps = typedWord.count { it.isLetter() } > 1 && typedWord.none { it.isLowerCase() }

        // Binary search for the first word >= prefix, then walk while words still start with it.
        var lo = 0
        var hi = sortedWords.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (sortedWords[mid] < prefix) lo = mid + 1 else hi = mid
        }
        val matches = ArrayList<String>()
        var i = lo
        while (i < sortedWords.size && sortedWords[i].startsWith(prefix)) {
            if (sortedWords[i] != prefix) matches.add(sortedWords[i])
            i++
        }

        return matches
            .sortedWith(
                compareByDescending<String> { freqs.getValue(it) }
                    .thenBy { it.length }
                    .thenBy { it }
            )
            .take(maxCount)
            .map { word ->
                val text = when {
                    allCaps -> word.uppercase()
                    capitalizeFirst -> word.replaceFirstChar { it.uppercase() }
                    else -> word
                }
                WordSuggestionCandidate(
                    text = text,
                    confidence = freqs.getValue(word) / 255.0,
                    isEligibleForAutoCommit = false,
                    sourceProvider = sourceProvider,
                )
            }
    }
}
