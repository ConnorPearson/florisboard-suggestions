/*
 * Copyright (C) 2022-2025 The FlorisBoard Contributors
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

package dev.patrickgold.florisboard.ime.media

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.florisboard.ime.keyboard.FlorisImeSizing
import dev.patrickgold.florisboard.ime.keyboard3.ImeActions
import dev.patrickgold.florisboard.ime.keyboard3.ui.Display3
import dev.patrickgold.florisboard.ime.keyboard3.ui.ImeKeyButton
import dev.patrickgold.florisboard.ime.media.emoji.EmojiData
import dev.patrickgold.florisboard.ime.media.emoji.EmojiPaletteView
import dev.patrickgold.florisboard.ime.media.emoji.EmojiSearchLayout
import dev.patrickgold.florisboard.ime.theme.FlorisImeUi
import dev.patrickgold.florisboard.ime.window.LocalWindowController
import dev.patrickgold.jetpref.datastore.model.collectAsState
import org.florisboard.lib.snygg.ui.SnyggButton
import org.florisboard.lib.snygg.ui.SnyggColumn
import org.florisboard.lib.snygg.ui.SnyggIcon
import org.florisboard.lib.snygg.ui.SnyggRow
import org.florisboard.lib.snygg.ui.SnyggText
import org.florisboard.lib.snygg.ui.rememberSnyggThemeQuery
import org.k3lp.lib.text.K3Descriptor
import org.k3lp.lib.text.K3StringOrDescriptor

private val BottomRowButtonMinWidth = 64.dp

// Icon size relative to the themed font size of the button (which already includes the font size slider). The ABC
// icon's glyph only fills about 75% x 25% of its icon box, so its box has to be much larger to look comparable.
private const val AbcIconScale = 1.6f
private const val BackspaceIconScale = 1.3f

@Composable
private fun BottomRowIconButton(
    output: K3StringOrDescriptor,
    iconScale: Float,
    modifier: Modifier = Modifier,
) {
    val style = rememberSnyggThemeQuery(FlorisImeUi.MediaBottomRowButton.elementName)
    val iconSize = with(LocalDensity.current) { style.fontSize(default = 22.sp).toDp() } * iconScale
    ImeKeyButton(
        elementName = FlorisImeUi.MediaBottomRowButton.elementName,
        output = output,
        modifier = modifier,
    ) { display ->
        when (display) {
            // An explicit size here takes precedence over the icon's own size derived from the font size.
            is K3Descriptor -> Display3(display = display, modifier = Modifier.size(iconSize))
            else -> Display3(display = display, modifier = Modifier.fillMaxHeight())
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MediaInputLayout(
    modifier: Modifier = Modifier,
) {
    val prefs by FlorisPreferenceStore
    val context = LocalContext.current
    val windowController = LocalWindowController.current
    val windowSpec by windowController.activeWindowSpec.collectAsState()
    // The same multiplier the font size slider applies to the theme. Only grow the bottom row (never shrink it) so
    // the buttons stay comfortably tappable at small font sizes.
    val fontScale = windowSpec.fontScale
    val bottomRowScale = fontScale.coerceAtLeast(1f)

    var emojiLayoutDataMap by remember { mutableStateOf(EmojiData.Fallback) }
    LaunchedEffect(Unit) {
        emojiLayoutDataMap = EmojiData.get(context, "ime/media/emoji/root.txt")
    }

    var isEmojiSearch by remember { mutableStateOf(false) }
    if (isEmojiSearch) {
        EmojiSearchLayout()
    } else {
        SnyggColumn(
            elementName = FlorisImeUi.Media.elementName,
            modifier = modifier
                .fillMaxWidth()
                .height(FlorisImeSizing.imeUiHeight()),
        ) {
            EmojiPaletteView(
                modifier = Modifier.weight(1f),
                fullEmojiMappings = emojiLayoutDataMap,
            )
            SnyggRow(
                elementName = FlorisImeUi.MediaBottomRow.elementName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FlorisImeSizing.keyboardRowBaseHeight * 0.8f * bottomRowScale),
            ) {
                BottomRowIconButton(
                    output = ImeActions.ShowTextPanel,
                    iconScale = AbcIconScale,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = BottomRowButtonMinWidth * bottomRowScale),
                )
                Spacer(modifier = Modifier.weight(1f))
                // TODO no feature gate once emoji search PR is implemented!
                val experimentalEmojiSearch by prefs.emoji.experimentalEmojiSearchEnabled.collectAsState()
                if (experimentalEmojiSearch) {
                    SnyggButton(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .align(Alignment.CenterVertically),
                        onClick = { isEmojiSearch = true },
                    ) {
                        SnyggIcon(
                            imageVector = Icons.Default.Search,
                        )
                        SnyggText(
                            text = "Search",
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                BottomRowIconButton(
                    output = ImeActions.Backspace,
                    iconScale = BackspaceIconScale,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = BottomRowButtonMinWidth * bottomRowScale),
                )
            }
        }
    }
}
