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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class PrefixSuggesterTest : FunSpec({

    fun PrefixSuggester.texts(typed: String, max: Int = 8) = suggest(typed, max).map { it.text.toString() }

    context("basic matching") {
        val s = PrefixSuggester(mapOf("hello" to 200, "help" to 250, "held" to 100, "hell" to 90, "world" to 255))

        test("returns only words starting with the prefix") {
            s.texts("hel").toSet() shouldBe setOf("hello", "help", "held", "hell")
        }
        test("orders by descending frequency") {
            s.texts("hel") shouldBe listOf("help", "hello", "held", "hell")
        }
        test("excludes the typed word itself") {
            s.texts("hell") shouldBe listOf("hello")
            s.texts("hello").shouldBeEmpty()
        }
        test("single character prefix") {
            s.texts("w") shouldBe listOf("world")
        }
        test("no match returns empty") {
            s.texts("xyz").shouldBeEmpty()
        }
        test("prefix longer than any word returns empty") {
            s.texts("helloooooooo").shouldBeEmpty()
        }
        test("empty typed word returns empty (no next-word prediction)") {
            s.texts("").shouldBeEmpty()
        }
    }

    context("maxCount handling") {
        val s = PrefixSuggester((1..20).associate { "ab$it" to it })

        test("limits the number of results") {
            s.texts("ab", 3).size shouldBe 3
        }
        test("keeps the best results when limiting") {
            s.texts("ab", 3) shouldBe listOf("ab20", "ab19", "ab18")
        }
        test("zero and negative counts return empty") {
            s.texts("ab", 0).shouldBeEmpty()
            s.texts("ab", -5).shouldBeEmpty()
        }
        test("count larger than matches returns all matches") {
            s.texts("ab", 1000).size shouldBe 20
        }
    }

    context("tie-breaking is deterministic") {
        val s = PrefixSuggester(mapOf("bat" to 10, "bats" to 10, "bath" to 10, "bar" to 10, "bad" to 10))
        test("equal frequency: shorter first, then alphabetical") {
            s.texts("ba") shouldBe listOf("bad", "bar", "bat", "bath", "bats")
        }
        test("repeated calls give identical results") {
            s.texts("ba") shouldBe s.texts("ba")
        }
    }

    context("capitalization mirrors the typed fragment") {
        val s = PrefixSuggester(mapOf("hello" to 200, "help" to 250))
        test("lowercase stays lowercase") { s.texts("hel") shouldBe listOf("help", "hello") }
        test("leading capital capitalizes first letter") { s.texts("Hel") shouldBe listOf("Help", "Hello") }
        test("all caps uppercases the suggestion") { s.texts("HEL") shouldBe listOf("HELP", "HELLO") }
        test("a single capital letter is not treated as all caps") {
            s.texts("H") shouldBe listOf("Help", "Hello")
        }
        test("mixed case with a later capital only capitalizes the first letter if it is upper") {
            s.texts("hEl") shouldBe listOf("help", "hello")
            s.texts("HeL") shouldBe listOf("Help", "Hello")
        }
        test("matching is case-insensitive") {
            s.texts("HeL").size shouldBe 2
        }
    }

    context("dictionary normalization") {
        test("uppercase dictionary keys are matched") {
            PrefixSuggester(mapOf("Hello" to 5)).texts("he") shouldBe listOf("hello")
        }
        test("keys colliding after lowercasing keep the higher frequency") {
            val s = PrefixSuggester(mapOf("Help" to 10, "help" to 200, "hello" to 100))
            s.texts("hel") shouldBe listOf("help", "hello")
            s.size shouldBe 2
        }
        test("empty keys are ignored") {
            val s = PrefixSuggester(mapOf("" to 255, "a" to 1))
            s.size shouldBe 1
            s.texts("a").shouldBeEmpty()
        }
        test("empty dictionary yields no suggestions") {
            PrefixSuggester(emptyMap()).texts("a").shouldBeEmpty()
        }
    }

    context("binary search boundaries") {
        val s = PrefixSuggester(mapOf("aa" to 1, "ab" to 2, "mm" to 3, "mn" to 4, "zy" to 5, "zz" to 6))
        test("prefix before every word") { s.texts("0").shouldBeEmpty() }
        test("prefix after every word") { s.texts("zzz").shouldBeEmpty() }
        test("prefix matching the first words") { s.texts("a") shouldBe listOf("ab", "aa") }
        test("prefix matching the last words") { s.texts("z") shouldBe listOf("zz", "zy") }
        test("prefix between words") { s.texts("c").shouldBeEmpty() }
        test("exact last word has no completions") { s.texts("zz").shouldBeEmpty() }
        test("single word dictionary") {
            val one = PrefixSuggester(mapOf("only" to 1))
            one.texts("o") shouldBe listOf("only")
            one.texts("only").shouldBeEmpty()
            one.texts("p").shouldBeEmpty()
            one.texts("n").shouldBeEmpty()
        }
        test("words that are prefixes of each other") {
            val n = PrefixSuggester(mapOf("he" to 1, "hell" to 2, "hello" to 3, "hellos" to 4))
            n.texts("he") shouldBe listOf("hellos", "hello", "hell")
            n.texts("hell") shouldBe listOf("hellos", "hello")
            n.texts("hello") shouldBe listOf("hellos")
        }
    }

    context("apostrophes and unicode") {
        val s = PrefixSuggester(mapOf("don't" to 200, "done" to 250, "café" to 100, "cafés" to 50, "straße" to 10))
        test("apostrophe is part of the prefix") {
            s.texts("don'") shouldBe listOf("don't")
            s.texts("don") shouldBe listOf("done", "don't")
        }
        test("accented characters") {
            s.texts("caf") shouldBe listOf("café", "cafés")
            s.texts("café") shouldBe listOf("cafés")
        }
        test("accented capitalization") {
            s.texts("Caf") shouldBe listOf("Café", "Cafés")
        }
    }

    context("candidate properties") {
        val s = PrefixSuggester(mapOf("hello" to 255, "help" to 0, "held" to 128))
        test("confidence is within 0..1 and proportional to frequency") {
            val r = s.suggest("hel", 8)
            r.forEach { it.confidence.shouldBeBetween(0.0, 1.0, 0.0) }
            r.first { it.text == "hello" }.confidence shouldBe 1.0
            r.first { it.text == "help" }.confidence shouldBe 0.0
            r.first { it.text == "held" }.confidence shouldBe 128 / 255.0
        }
        test("never eligible for auto commit") {
            s.suggest("hel", 8).none { it.isEligibleForAutoCommit } shouldBe true
        }
        test("zero frequency words are still suggested, ranked last") {
            s.texts("hel").last() shouldBe "help"
        }
    }

    context("randomized comparison against a brute-force reference") {
        fun reference(dict: Map<String, Int>, typed: String, max: Int): List<String> {
            if (max <= 0 || typed.isEmpty()) return emptyList()
            val norm = HashMap<String, Int>()
            for ((w, f) in dict) {
                if (w.isEmpty()) continue
                val k = w.lowercase()
                if ((norm[k] ?: -1) < f) norm[k] = f
            }
            val prefix = typed.lowercase()
            val cap = typed.first().isUpperCase()
            val caps = typed.count { it.isLetter() } > 1 && typed.none { it.isLowerCase() }
            return norm.keys.filter { it.startsWith(prefix) && it != prefix }
                .sortedWith(compareByDescending<String> { norm.getValue(it) }.thenBy { it.length }.thenBy { it })
                .take(max)
                .map { if (caps) it.uppercase() else if (cap) it.replaceFirstChar { c -> c.uppercase() } else it }
        }

        test("matches the reference on 300 random dictionaries x 40 queries") {
            val rnd = Random(20260101)
            val alphabet = "abcAB'é"
            fun word() = buildString { repeat(rnd.nextInt(1, 6)) { append(alphabet[rnd.nextInt(alphabet.length)]) } }
            repeat(300) {
                val dict = HashMap<String, Int>()
                repeat(rnd.nextInt(0, 80)) { dict[word()] = rnd.nextInt(0, 256) }
                val s = PrefixSuggester(dict)
                repeat(40) {
                    val typed = if (rnd.nextInt(10) == 0) "" else word()
                    val max = rnd.nextInt(-1, 12)
                    val expected = reference(dict, typed, max)
                    val actual = s.texts(typed, max)
                    if (actual != expected) {
                        throw AssertionError("dict=$dict typed='$typed' max=$max expected=$expected actual=$actual")
                    }
                }
            }
        }

        test("every result starts with the typed prefix, is unique, and respects max") {
            val rnd = Random(7)
            val words = (1..2000).associate { i -> "w${rnd.nextInt(0, 400)}x${i % 7}" to rnd.nextInt(0, 256) }
            val s = PrefixSuggester(words)
            repeat(500) {
                val typed = "w" + rnd.nextInt(0, 400).toString().take(rnd.nextInt(0, 3))
                val max = rnd.nextInt(1, 10)
                val r = s.texts(typed, max)
                r.size shouldBeLessThanOrEqual max
                r.toSet().size shouldBe r.size
                r.forEach { it.startsWith(typed) shouldBe true }
                r shouldNotContain typed
            }
        }
    }

    context("bundled dictionary (ime/dict/data.json)") {
        val file = File("src/main/assets/ime/dict/data.json")
        val dict: Map<String, Int> by lazy {
            Json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), file.readText())
        }
        val s by lazy { PrefixSuggester(dict) }

        test("file exists and loads a realistic number of words") {
            file.exists() shouldBe true
            (dict.size > 10_000) shouldBe true
            s.size shouldBe dict.keys.map { it.lowercase() }.filter { it.isNotEmpty() }.toSet().size
        }
        test("'hel' suggests common real words") {
            val r = s.texts("hel", 8)
            r shouldContain "help"
            r shouldContain "hello"
            r.forEach { it.startsWith("hel") shouldBe true }
        }
        test("'th' ranks the most frequent words first") {
            val r = s.texts("th", 3)
            r.first() shouldBe "the"
            r.size shouldBe 3
            r.forEach { it.startsWith("th") shouldBe true }
            val freqs = r.map { dict.getValue(it) }
            freqs shouldBe freqs.sortedDescending()
        }
        test("capitalization carries through on real data") {
            s.texts("Hel", 8) shouldContain "Hello"
            s.texts("HEL", 8) shouldContain "HELLO"
        }
        test("nonsense prefix gives nothing") {
            s.texts("qzxqzx").shouldBeEmpty()
        }
        test("every single letter yields a full result list in frequency order") {
            for (c in 'a'..'z') {
                val r = s.suggest(c.toString(), 8)
                r.size shouldBe 8
                r.map { it.confidence } shouldBe r.map { it.confidence }.sortedDescending()
            }
        }
        test("results for 2-letter prefixes are consistent with a brute force scan") {
            for (a in 'a'..'z') for (b in 'a'..'z') {
                val p = "$a$b"
                val expected = dict.keys.map { it.lowercase() }.toSet()
                    .filter { it.startsWith(p) && it != p }
                    .sortedWith(compareByDescending<String> { w -> dict.filterKeys { it.equals(w, true) }.values.max() }
                        .thenBy { it.length }.thenBy { it })
                    .take(5)
                s.texts(p, 5) shouldBe expected
            }
        }
        test("lookup is fast enough for typing (1000 queries)") {
            s.texts("he") // warm up
            val ms = measureTimeMillis { repeat(1000) { s.texts("he", 8); s.texts("a", 8); s.texts("unc", 8) } }
            // generous bound: this is per 3000 queries, guards against accidental O(n) full scans per keystroke
            (ms < 3000) shouldBe true
        }
    }
})
