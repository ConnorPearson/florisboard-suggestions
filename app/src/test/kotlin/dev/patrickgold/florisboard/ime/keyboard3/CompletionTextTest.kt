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

package dev.patrickgold.florisboard.ime.keyboard3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CompletionTextTest : FunSpec({

    context("extractWordForCompletion") {
        test("empty and whitespace-only") {
            extractWordForCompletion("") shouldBe ""
            extractWordForCompletion("   ") shouldBe ""
            extractWordForCompletion("\n") shouldBe ""
        }
        test("stops at whitespace, newlines and tabs") {
            extractWordForCompletion("a b") shouldBe "b"
            extractWordForCompletion("a\nb") shouldBe "b"
            extractWordForCompletion("a\tb") shouldBe "b"
        }
        test("stops at punctuation and digits") {
            extractWordForCompletion("hi,there") shouldBe "there"
            extractWordForCompletion("(hel") shouldBe "hel"
            extractWordForCompletion("abc123") shouldBe ""
            extractWordForCompletion("123abc") shouldBe "abc"
            extractWordForCompletion("user@exa") shouldBe "exa"
        }
        test("hyphen ends the fragment so suggestions complete the part after it") {
            extractWordForCompletion("well-kn") shouldBe "kn"
            extractWordForCompletion("well-") shouldBe ""
        }
        test("apostrophes stay inside the fragment") {
            extractWordForCompletion("don't") shouldBe "don't"
            extractWordForCompletion("it's don'") shouldBe "don'"
        }
        test("unicode letters") {
            extractWordForCompletion("un café") shouldBe "café"
            extractWordForCompletion("Straße") shouldBe "Straße"
        }
        test("emoji is not part of a word") {
            extractWordForCompletion("hi \uD83D\uDE00hel") shouldBe "hel"
            extractWordForCompletion("hel\uD83D\uDE00") shouldBe ""
        }
    }

    context("applyCompletion") {
        test("replaces the partial word and adds a trailing space") {
            applyCompletion("hell", "hello") shouldBe "hello "
            applyCompletion("get ou", "out") shouldBe "get out "
        }
        test("does not duplicate the typed fragment (regression: hellhello)") {
            applyCompletion("hell", "hello?") shouldBe "hello? "
            applyCompletion("I said hell", "hello") shouldBe "I said hello "
        }
        test("preserves everything before the word, including newlines") {
            applyCompletion("line one\nline tw", "two") shouldBe "line one\nline two "
        }
        test("no active word: appends the suggestion") {
            applyCompletion("get ", "out") shouldBe "get out "
            applyCompletion("", "out") shouldBe "out "
        }
        test("after punctuation only the trailing fragment is replaced") {
            applyCompletion("wait,he", "help") shouldBe "wait,help "
            applyCompletion("well-kn", "known") shouldBe "well-known "
        }
        test("apostrophe fragments") {
            applyCompletion("it's don'", "don't") shouldBe "it's don't "
        }
        test("completion that equals the fragment still adds the space") {
            applyCompletion("hello", "hello") shouldBe "hello "
        }
        test("result ends with exactly the suggestion plus one space") {
            val out = applyCompletion("xx yy zz", "zebra")
            out.endsWith("zebra ") shouldBe true
            out.length shouldBe "xx yy ".length + "zebra ".length
        }
    }
})
