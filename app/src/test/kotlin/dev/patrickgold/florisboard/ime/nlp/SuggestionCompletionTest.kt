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

package dev.patrickgold.florisboard.ime.nlp

import dev.patrickgold.florisboard.ime.keyboard3.extractWordForCompletion
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SuggestionCompletionTest : FunSpec({

    fun simulateCompletion(textBefore: String, suggestion: String): String {
        val wordToReplace = extractWordForCompletion(textBefore)
        val remainingTextBefore = textBefore.dropLast(wordToReplace.length)
        return "$remainingTextBefore$suggestion "
    }

    context("Suggestion Word Extraction Logic") {
        test("extracts simple word at end of string") {
            extractWordForCompletion("ou") shouldBe "ou"
            extractWordForCompletion("helli") shouldBe "helli"
        }

        test("extracts word following space") {
            extractWordForCompletion("get ou") shouldBe "ou"
            extractWordForCompletion("please get ou") shouldBe "ou"
        }

        test("extracts word in multiline string") {
            val multiline = "Ah sorry babe, was really hoping you \n might get ou"
            extractWordForCompletion(multiline) shouldBe "ou"
        }

        test("extracts word with apostrophes or hyphens correctly") {
            extractWordForCompletion("don't wa") shouldBe "wa"
            extractWordForCompletion("well-known fe") shouldBe "fe"
            extractWordForCompletion("don't") shouldBe "don't"
        }
    }

    context("Boundary & Edge Cases for Word Extraction") {
        test("empty input returns empty string") {
            extractWordForCompletion("") shouldBe ""
        }

        test("trailing space returns empty string (no active word)") {
            extractWordForCompletion("get ou ") shouldBe ""
            extractWordForCompletion("hello ") shouldBe ""
        }

        test("trailing punctuation returns empty string") {
            extractWordForCompletion("hello,") shouldBe ""
            extractWordForCompletion("world!") shouldBe ""
            extractWordForCompletion("test...") shouldBe ""
        }

        test("single character word") {
            extractWordForCompletion("o") shouldBe "o"
            extractWordForCompletion("get o") shouldBe "o"
        }
    }

    context("Full Completion Result Simulation") {
        test("replaces partial word accurately in sentence") {
            simulateCompletion("get ou", "out") shouldBe "get out "
            simulateCompletion("helli", "hello") shouldBe "hello "
        }

        test("replaces partial word accurately in multiline sentence") {
            val multilineBefore = "Ah sorry babe, was really hoping you \n might get ou"
            val expectedAfter = "Ah sorry babe, was really hoping you \n might get out "
            simulateCompletion(multilineBefore, "out") shouldBe expectedAfter
        }

        test("appends suggestion when cursor is after space") {
            simulateCompletion("get ", "out") shouldBe "get out "
        }

        test("replaces partial word with hyphens") {
            simulateCompletion("well-known fe", "feature") shouldBe "well-known feature "
        }
    }
})
