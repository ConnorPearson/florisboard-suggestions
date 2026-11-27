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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.FlorisImeService
import dev.patrickgold.florisboard.app.FlorisPreferenceStore
import dev.patrickgold.jetpref.datastore.model.collectAsState

/** Share of the bar height taken by a button icon, so the icons shrink and grow with the bar. */
internal const val BottomBarIconScale = 0.6f

/** Bar heights are limited to this range, whatever is stored in the preferences. */
internal val BottomBarHeightRange = 0..64

internal fun bottomBarHeightOf(preferenceValue: Int): Dp {
    return preferenceValue.coerceIn(BottomBarHeightRange).dp
}

internal fun bottomBarIconSizeOf(barHeight: Dp): Dp {
    return barHeight * BottomBarIconScale
}

/**
 * The bar below the keyboard in fixed window mode. It replaces the system caption bar (see [ImeSystemUi]), which
 * can be neither resized nor have its buttons hidden.
 *
 * The bar takes up no space if its height is set to 0. The buttons can be hidden separately, which leaves an empty bar.
 */
@Composable
fun ImeBottomBar() {
    val prefs by FlorisPreferenceStore
    val windowController = LocalWindowController.current

    val windowSpec by windowController.activeWindowSpec.collectAsState()
    val heightPref by prefs.keyboard.bottomBarHeight.collectAsState()
    val buttonsVisible by prefs.keyboard.bottomBarButtonsVisible.collectAsState()

    if (windowSpec !is ImeWindowSpec.Fixed) return
    val barHeight = bottomBarHeightOf(heightPref)
    if (barHeight <= 0.dp) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (buttonsVisible) {
            BottomBarButton(
                barHeight = barHeight,
                icon = Icons.Default.KeyboardArrowDown,
                description = "Hide keyboard",
                onClick = { FlorisImeService.hideUi() },
            )
            BottomBarButton(
                barHeight = barHeight,
                icon = Icons.Default.Keyboard,
                description = "Switch keyboard",
                onClick = { FlorisImeService.switchToNextInputMethod() },
                onLongClick = { FlorisImeService.showImePicker() },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomBarButton(
    barHeight: Dp,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            // Wide touch area. The height can not exceed the bar, so a small bar has a flat touch area.
            .widthIn(min = 56.dp)
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(bottomBarIconSizeOf(barHeight)),
            imageVector = icon,
            contentDescription = description,
        )
    }
}
