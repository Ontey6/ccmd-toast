/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast

import com.destroystokyo.paper.profile.PlayerProfile
import fyi.pauli.opentoast.internal.ToastStyle
import fyi.pauli.opentoast.internal.NAMESPACE
import fyi.pauli.opentoast.internal.ToastLayout
import fyi.pauli.opentoast.internal.ToastSender
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.logging.Logger

private val LOGGER = Logger.getLogger(NAMESPACE)

internal fun fallbackWarn(text: Component) {
    val plain = PlainTextComponentSerializer.plainText().serialize(text)
    LOGGER.warning("Toast \"$plain\" does not fit (too long or too many lines), showing a vanilla advancement toast with header.")
}

sealed class Toast {
    abstract fun show(player: Player)

    fun show(players: Iterable<Player>) {
        for (player in players) {
            show(player)
        }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun advancement(icon: ItemStack, text: Component, frame: ToastFrame = ToastFrame.TASK): Toast {
            return AdvancementToast(icon.clone(), text, frame)
        }

        @JvmStatic
        @JvmOverloads
        fun head(profile: PlayerProfile, text: Component, frame: ToastFrame = ToastFrame.TASK): Toast {
            val head = ItemStack.of(Material.PLAYER_HEAD)
            head.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile))
            return AdvancementToast(head, text, frame)
        }

        @JvmStatic
        fun system(text: Component): Toast {
            return StyledToast(ToastStyle.SYSTEM, null, null, text)
        }

        @JvmStatic
        fun recipe(icon: ItemStack, text: Component): Toast {
            return StyledToast(ToastStyle.RECIPE, icon.clone(), null, text)
        }

        @JvmStatic
        fun tutorial(icon: TutorialIcon, text: Component): Toast {
            return StyledToast(ToastStyle.TUTORIAL, null, icon, text)
        }
    }
}

private class AdvancementToast(
    private val icon: ItemStack,
    private val text: Component,
    private val frame: ToastFrame,
) : Toast() {
    init {
        if (icon.type == Material.PLAYER_HEAD) {
            icon.setData(DataComponentTypes.ITEM_MODEL, FLAT_HEAD)
        }
    }

    private val title = ToastLayout.advancement(text) ?: text.also(::fallbackWarn)

    override fun show(player: Player) {
        ToastSender.send(player, icon, title, frame)
    }

    private companion object {
        val FLAT_HEAD = Key.key(NAMESPACE, "flat_head")
    }
}

private class StyledToast(
    private val toastStyle: ToastStyle,
    private val icon: ItemStack?,
    private val symbol: TutorialIcon?,
    private val text: Component,
) : Toast() {
    private val title = ToastLayout.styled(text, toastStyle, symbol)

    private val fallback by lazy {
        fallbackWarn(text)
        AdvancementToast(icon?.clone() ?: ItemStack.of(Material.PAPER), text, ToastFrame.TASK)
    }

    override fun show(player: Player) {
        if (title == null) {
            fallback.show(player)
            return
        }

        ToastSender.send(player, icon ?: invisibleIcon(), title, ToastFrame.TASK)
    }

    private fun invisibleIcon(): ItemStack {
        val item = ItemStack.of(Material.PAPER)
        item.setData(DataComponentTypes.ITEM_MODEL, NO_ICON)
        return item
    }

    private companion object {
        val NO_ICON = Key.key(NAMESPACE, "empty")
    }
}
