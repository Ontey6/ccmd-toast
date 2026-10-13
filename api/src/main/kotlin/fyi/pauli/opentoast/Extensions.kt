/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast

import com.destroystokyo.paper.profile.PlayerProfile
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@JvmSynthetic
fun Player.toast(icon: ItemStack, text: Component, frame: ToastFrame = ToastFrame.TASK) {
    Toast.advancement(icon, text, frame).show(this)
}

@JvmSynthetic
fun Player.toast(head: PlayerProfile, text: Component, frame: ToastFrame = ToastFrame.TASK) {
    Toast.head(head, text, frame).show(this)
}

@JvmSynthetic
fun Player.systemToast(text: Component) {
    Toast.system(text).show(this)
}

@JvmSynthetic
fun Player.recipeToast(icon: ItemStack, text: Component) {
    Toast.recipe(icon, text).show(this)
}

@JvmSynthetic
fun Player.tutorialToast(icon: TutorialIcon, text: Component) {
    Toast.tutorial(icon, text).show(this)
}
