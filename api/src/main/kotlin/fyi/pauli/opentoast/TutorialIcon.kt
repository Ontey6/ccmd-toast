/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast

/**
 * https://minecraft.wiki/w/Tutorial_hints
 */
enum class TutorialIcon {
    WASD,
    MOUSE,
    TREE,
    RECIPE_BOOK,
    WOODEN_PLANKS,
    SOCIAL_INTERACTIONS,
    RIGHT_CLICK;

    internal val char: Char
        get() = FIRST_CHAR + ordinal

    private companion object {
        const val FIRST_CHAR = ''
    }
}
