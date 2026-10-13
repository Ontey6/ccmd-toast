/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast

/**
 * Challenge will play the ui.toast.challenge_complete sound. https://minecraft.wiki/w/Toasts#Sounds
 * Task and goal are silent.
 */
enum class ToastFrame {
    TASK,
    GOAL,
    CHALLENGE,
}
