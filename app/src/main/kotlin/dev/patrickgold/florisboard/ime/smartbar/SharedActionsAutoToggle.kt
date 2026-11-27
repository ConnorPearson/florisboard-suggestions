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

/**
 * Decides when the shared Smartbar actions row should be opened or closed automatically.
 *
 * The decision is only made when one of its inputs actually changed since the last call. Re-evaluating with the same
 * inputs returns null, so a manual toggle by the user is not overwritten by unrelated re-evaluations (for example
 * every time the keyboard state changes, which also happens when the user taps the toggle).
 */
class SharedActionsAutoToggle {
    private data class Inputs(
        val candidates: List<*>,
        val inlineSuggestions: List<*>,
        val hasSelection: Boolean,
        val collapsedByDefault: Boolean,
    )

    private var lastInputs: Inputs? = null

    /**
     * @return The expanded state to apply, or null if the actions row should be left as it is.
     */
    @Synchronized
    fun update(
        candidates: List<*>,
        inlineSuggestions: List<*>,
        hasSelection: Boolean,
        collapsedByDefault: Boolean,
    ): Boolean? {
        val inputs = Inputs(candidates, inlineSuggestions, hasSelection, collapsedByDefault)
        if (inputs == lastInputs) return null
        lastInputs = inputs
        return hasSelection || (!collapsedByDefault && candidates.isEmpty() && inlineSuggestions.isEmpty())
    }

    /**
     * Forgets the last inputs, so the next [update] always produces a decision. Call this when the keyboard opens.
     */
    @Synchronized
    fun reset() {
        lastInputs = null
    }
}
