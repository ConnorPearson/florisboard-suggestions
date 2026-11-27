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

package dev.patrickgold.florisboard.ime.window

import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class ImeBottomBarTest : FunSpec({
    test("height follows the preference value") {
        bottomBarHeightOf(0) shouldBe 0.dp
        bottomBarHeightOf(24) shouldBe 24.dp
        bottomBarHeightOf(48) shouldBe 48.dp
        bottomBarHeightOf(64) shouldBe 64.dp
    }
    test("out of range preference values are clamped") {
        bottomBarHeightOf(-10) shouldBe 0.dp
        bottomBarHeightOf(1000) shouldBe 64.dp
    }
    test("icons shrink with the bar and always fit inside it") {
        for (h in BottomBarHeightRange) {
            val bar = bottomBarHeightOf(h)
            val icon = bottomBarIconSizeOf(bar)
            (icon.value <= bar.value) shouldBe true
            icon.value shouldBe ((h * BottomBarIconScale) plusOrMinus 0.0001f)
        }
    }
    test("a zero height bar has zero sized icons") {
        bottomBarIconSizeOf(bottomBarHeightOf(0)) shouldBe 0.dp
    }
})
