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

package dev.patrickgold.florisboard.ime.smartbar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SharedActionsAutoToggleTest : FunSpec({

    fun SharedActionsAutoToggle.step(
        candidates: List<String> = emptyList(),
        inline: List<String> = emptyList(),
        selection: Boolean = false,
        collapsedByDefault: Boolean = false,
    ) = update(candidates, inline, selection, collapsedByDefault)

    context("default behavior (collapse on open off)") {
        test("first evaluation with no suggestions opens the actions") {
            SharedActionsAutoToggle().step() shouldBe true
        }
        test("first evaluation with suggestions closes the actions") {
            SharedActionsAutoToggle().step(candidates = listOf("hello")) shouldBe false
        }
        test("inline suggestions alone also close the actions") {
            SharedActionsAutoToggle().step(inline = listOf("autofill")) shouldBe false
        }
        test("a text selection opens the actions even with suggestions") {
            SharedActionsAutoToggle().step(candidates = listOf("hello"), selection = true) shouldBe true
        }
    }

    context("regression: re-evaluating with unchanged inputs must not override a manual toggle") {
        test("same inputs return null, however often they repeat") {
            val t = SharedActionsAutoToggle()
            t.step() shouldBe true
            repeat(50) { t.step() shouldBe null }
        }
        test("same suggestions in an equal but new list instance return null") {
            val t = SharedActionsAutoToggle()
            t.step(candidates = listOf("help", "hello")) shouldBe false
            t.step(candidates = listOf("help", "hello")) shouldBe null
        }
    }

    context("changes are applied") {
        test("suggestions appearing closes the actions") {
            val t = SharedActionsAutoToggle()
            t.step() shouldBe true
            t.step(candidates = listOf("hello")) shouldBe false
        }
        test("suggestions disappearing opens the actions") {
            val t = SharedActionsAutoToggle()
            t.step(candidates = listOf("hello")) shouldBe false
            t.step() shouldBe true
        }
        test("a different suggestion list re-applies the decision (closes again after a manual open)") {
            val t = SharedActionsAutoToggle()
            t.step(candidates = listOf("hel")) shouldBe false
            t.step(candidates = listOf("help")) shouldBe false
        }
        test("selection starting opens and ending closes again") {
            val t = SharedActionsAutoToggle()
            t.step(candidates = listOf("a")) shouldBe false
            t.step(candidates = listOf("a"), selection = true) shouldBe true
            t.step(candidates = listOf("a"), selection = false) shouldBe false
        }
    }

    context("collapse on open") {
        test("stays closed with no suggestions") {
            SharedActionsAutoToggle().step(collapsedByDefault = true) shouldBe false
        }
        test("a selection still opens the actions") {
            SharedActionsAutoToggle().step(selection = true, collapsedByDefault = true) shouldBe true
        }
        test("turning the setting on re-applies immediately") {
            val t = SharedActionsAutoToggle()
            t.step() shouldBe true
            t.step(collapsedByDefault = true) shouldBe false
        }
        test("unchanged inputs still leave a manual open alone") {
            val t = SharedActionsAutoToggle()
            t.step(collapsedByDefault = true) shouldBe false
            repeat(20) { t.step(collapsedByDefault = true) shouldBe null }
        }
    }

    context("reset (keyboard opened in a new field)") {
        test("reset makes the next evaluation decide even with identical inputs") {
            val t = SharedActionsAutoToggle()
            t.step() shouldBe true
            t.step() shouldBe null
            t.reset()
            t.step() shouldBe true
        }
        test("after reset with collapse on open, the actions close") {
            val t = SharedActionsAutoToggle()
            t.step(collapsedByDefault = true) shouldBe false
            t.reset()
            t.step(collapsedByDefault = true) shouldBe false
        }
    }
})
