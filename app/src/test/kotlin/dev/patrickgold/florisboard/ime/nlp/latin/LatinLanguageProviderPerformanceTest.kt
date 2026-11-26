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

import io.kotest.core.spec.style.FunSpec
import kotlin.system.measureTimeMillis

class LatinLanguageProviderPerformanceTest : FunSpec({

    // Helper to generate synthetic dictionaries of size N
    fun generateDictionary(size: Int): Map<String, Int> {
        val baseWords = listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
            "hello", "world", "floris", "board", "keyboard", "suggestion", "completion",
            "performance", "benchmark", "compute", "time", "memory", "android", "kotlin"
        )
        val dict = mutableMapOf<String, Int>()
        for (i in 0 until size) {
            val word = if (i < baseWords.size) {
                baseWords[i]
            } else {
                "word$i"
            }
            dict[word] = (255 * (1.0 - (i.toDouble() / size))).toInt().coerceIn(1, 255)
        }
        return dict
    }

    // Damerau-Levenshtein distance implementation matching LatinLanguageProvider
    fun damerauLevenshteinDistance(s1: String, s2: String, maxDist: Int = 2): Int {
        if (kotlin.math.abs(s1.length - s2.length) > maxDist) return maxDist + 1
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            var minInRow = maxDist + 1
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                var dist = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
                if (i > 1 && j > 1 && s1[i - 1] == s2[j - 2] && s1[i - 2] == s2[j - 1]) {
                    dist = minOf(dist, dp[i - 2][j - 2] + cost)
                }
                dp[i][j] = dist
                minInRow = minOf(minInRow, dist)
            }
            if (minInRow > maxDist) return maxDist + 1
        }
        return dp[len1][len2]
    }

    context("Performance Benchmarks for NLP Suggestion & Spellcheck") {

        test("benchmark prefix search scaling with dictionary size") {
            val sizes = listOf(1_000, 5_000, 10_000, 25_000, 50_000)
            val prefix = "he"

            println("\n=== Prefix Search Benchmark (Query: '$prefix') ===")
            for (size in sizes) {
                val dict = generateDictionary(size)
                // Warm-up
                repeat(10) {
                    dict.entries.filter { it.key.startsWith(prefix) }
                }

                val iterations = 100
                val totalTimeMs = measureTimeMillis {
                    repeat(iterations) {
                        dict.entries
                            .filter { it.key.startsWith(prefix) }
                            .sortedWith(compareBy({ it.key.length - prefix.length }, { -it.value }))
                            .take(8)
                    }
                }
                val avgTimeMs = totalTimeMs.toDouble() / iterations
                println("Dictionary size: $size words -> Avg time per query: %.3f ms".format(avgTimeMs))
            }
        }

        test("benchmark typo correction (Damerau-Levenshtein) scaling with dictionary size") {
            val sizes = listOf(1_000, 5_000, 10_000, 25_000)
            val typo = "helo"
            val maxDist = 2

            println("\n=== Typo Correction Benchmark (Query: '$typo', maxDist: $maxDist) ===")
            for (size in sizes) {
                val dict = generateDictionary(size)
                // Warm-up
                repeat(2) {
                    dict.entries.asSequence()
                        .map { entry -> entry.key to damerauLevenshteinDistance(typo, entry.key, maxDist) }
                        .filter { it.second <= maxDist }
                        .toList()
                }

                val iterations = 10
                val totalTimeMs = measureTimeMillis {
                    repeat(iterations) {
                        dict.entries.asSequence()
                            .map { entry -> entry.key to damerauLevenshteinDistance(typo, entry.key, maxDist) }
                            .filter { it.second <= maxDist }
                            .sortedWith(compareBy({ it.second }, { -(dict[it.first] ?: 0) }))
                            .take(8)
                            .toList()
                    }
                }
                val avgTimeMs = totalTimeMs.toDouble() / iterations
                println("Dictionary size: $size words -> Avg time per typo query: %.3f ms".format(avgTimeMs))
            }
        }

        test("benchmark rapid typing simulation (sequence of keystrokes)") {
            val dict = generateDictionary(10_000)
            val keystrokes = listOf("h", "he", "hel", "hell", "hello")

            println("\n=== Rapid Typing Simulation Benchmark (10k words) ===")
            val totalTimeMs = measureTimeMillis {
                repeat(100) {
                    for (query in keystrokes) {
                        dict.entries
                            .filter { it.key.startsWith(query) }
                            .sortedWith(compareBy({ it.key.length - query.length }, { -it.value }))
                            .take(8)
                    }
                }
            }
            val avgPerKeystroke = totalTimeMs.toDouble() / (100 * keystrokes.size)
            println("Completed 100 typing sessions (5 keystrokes each) in ${totalTimeMs}ms (Avg per keystroke: %.3f ms)".format(avgPerKeystroke))
        }
    }
})
