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

package dev.patrickgold.florisboard.ime.media.emoji

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class EmojiScaleTest : FunSpec({
    test("default font size gives scale 1") {
        emojiScaleOf(22.sp) shouldBe 1f
    }
    test("scale is proportional to the font size multiplier (slider range 50%..150%)") {
        for (pct in 50..150 step 5) {
            val multiplier = pct / 100f
            emojiScaleOf((22f * multiplier).sp) shouldBe (multiplier plusOrMinus 0.0001f)
        }
    }
    test("unusable sizes fall back to scale 1") {
        emojiScaleOf(TextUnit.Unspecified) shouldBeExactly 1f
        emojiScaleOf(0.sp) shouldBeExactly 1f
        emojiScaleOf((-5).sp) shouldBeExactly 1f
        emojiScaleOf(2.em) shouldBeExactly 1f
    }
})
