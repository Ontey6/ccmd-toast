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
import ontey.ccmd.toast.HeadToast
import ontey.ccmd.toast.RecipeToast
import ontey.ccmd.toast.SystemToast
import ontey.ccmd.toast.ToastProvider
import ontey.ccmd.toast.TutorialToast
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
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
        val PROVIDER: ToastProvider = ToastProviderImpl()

        @JvmStatic
        @JvmOverloads
        fun advancement(icon: ItemStack, text: Component, frame: ToastFrame = ToastFrame.TASK): Toast {
            return AdvancementToastImpl(icon.clone(), text, frame)
        }

        @JvmStatic
        @JvmOverloads
        fun head(profile: PlayerProfile, text: Component, frame: ToastFrame = ToastFrame.TASK): Toast {
            val head = ItemStack.of(Material.PLAYER_HEAD)
            head.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile))
            return HeadToastImpl(head, text, frame)
        }

        @JvmStatic
        fun system(text: Component): Toast {
            return SystemToastImpl(text)
        }

        @JvmStatic
        fun recipe(icon: ItemStack, text: Component): Toast {
            return RecipeToastImpl(icon.clone(), text)
        }

        @JvmStatic
        fun tutorial(symbol: TutorialToast.Icon, text: Component): Toast {
            return TutorialToastImpl(symbol, text)
        }
    }
}

private open class AdvancementToast(
    private val icon: ItemStack,
    text: Component,
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

private abstract class StyledToast(
    toastStyle: ToastStyle,
    private val icon: ItemStack?,
    tutorialIcon: TutorialToast.Icon?,
    private val text: Component
) : Toast() {
    private val title = ToastLayout.styled(text, toastStyle, tutorialIcon)

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

// CustomCommand compat

private class ToastProviderImpl : ToastProvider {
    override fun advancementToast(
        text: Component,
        icon: ItemStack
    ): ontey.ccmd.toast.AdvancementToast {
        return AdvancementToastImpl(icon.clone(), text, ToastFrame.TASK)
    }

    override fun headToast(
        text: Component,
        profile: ResolvableProfile
    ): HeadToast {
        val icon = ItemType.PAPER.createItemStack()
        icon.setData(DataComponentTypes.PROFILE, profile)

        return HeadToastImpl(icon, text, ToastFrame.TASK)
    }

    override fun recipeToast(text: Component, icon: ItemStack): RecipeToast {
        return RecipeToastImpl(icon, text)
    }

    override fun systemToast(text: Component): SystemToast {
        return SystemToastImpl(text)
    }

    override fun tutorialToast(text: Component, icon: TutorialToast.Icon): TutorialToast {
        return TutorialToastImpl(icon, text)
    }

}

private class AdvancementToastImpl(
    private val icon: ItemStack,
    private val text: Component,
    frame: ToastFrame
) : AdvancementToast(icon, text, frame), ontey.ccmd.toast.AdvancementToast {
    override fun text(): Component {
        return text
    }

    override fun icon(): ItemStack {
        return icon
    }
}

private class HeadToastImpl(
    private val head: ItemStack,
    private val text: Component,
    frame: ToastFrame
) : AdvancementToast(head, text, frame), HeadToast {
    override fun text(): Component {
        return text
    }

    override fun icon(): ItemStack {
        return head
    }
}

private class RecipeToastImpl(
    icon: ItemStack?,
    private val text: Component
) : StyledToast(ToastStyle.RECIPE, icon, null, text), RecipeToast {
    override fun text(): Component {
        return text
    }

    override fun icon(): ItemStack? {
        return null
    }
}

private class SystemToastImpl(
    private val text: Component
) : StyledToast(ToastStyle.SYSTEM, null, null, text), SystemToast {
    override fun text(): Component {
        return text
    }

    override fun icon(): ItemStack? {
        return null
    }
}

private class TutorialToastImpl(
    private val tutorialIcon: TutorialToast.Icon,
    private val text: Component
) : StyledToast(ToastStyle.TUTORIAL, null, tutorialIcon, text), TutorialToast {
    override fun text(): Component {
        return text
    }

    override fun icon(): ItemStack? {
        return null
    }

    override fun tutorialIcon(): TutorialToast.Icon {
        return tutorialIcon
    }
}
