package fyi.pauli.opentoast.commands

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import fyi.pauli.opentoast.TutorialIcon
import fyi.pauli.opentoast.recipeToast
import fyi.pauli.opentoast.systemToast
import fyi.pauli.opentoast.toast
import fyi.pauli.opentoast.tutorialToast
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Base64
import java.util.UUID

object DemoCommand {
    private const val GERMAN_FLAG = "5e7899b4806858697e283f084d9173fe487886453774626b24bd8cfecc77b3f"

    fun build(): LiteralCommandNode<CommandSourceStack> {
        val toast = Commands.literal("toast")
            .requires { source -> source.sender is Player }
            .executes { context ->
                showToasts(context.source.sender as Player)
                Command.SINGLE_SUCCESS
            }

        return Commands.literal("demo")
            .then(toast)
            .build()
    }

    private fun showToasts(player: Player) {
        player.toast(
            player.playerProfile,
            mini("<aqua>${player.name}</aqua> sent you a <green>friend request"),
        )
        player.toast(
            ItemStack.of(Material.FIREWORK_ROCKET),
            mini("<light_purple><bold>Party invite</bold><newline><white>from <aqua>Notch</aqua> <gray>(3/4)"),
        )
        player.toast(
            ItemStack.of(Material.GOLDEN_HELMET),
            mini("<gold><bold>Clan Wolfpack</bold><newline><white>You are now <green>Officer"),
        )
        player.toast(
            germanFlag(),
            mini("<gray>Sprache auf <red>Deutsch <yellow>gestellt"),
        )
        player.systemToast(
            mini("<yellow>Server restart in 5 minutes!<newline><white>Leave your dungeon now or your <red>loot will be lost<white>."),
        )
        player.recipeToast(
            ItemStack.of(Material.CAKE),
            mini("<#500050>Party perk unlocked!<newline>Cake for <dark_green>everyone"),
        )
        player.tutorialToast(
            TutorialIcon.SOCIAL_INTERACTIONS,
            mini("<#500050>Find your friends<newline>Press <dark_aqua><bold>P</bold></dark_aqua> for the list"),
        )
    }

    private fun germanFlag(): PlayerProfile {
        val skin = """{"textures":{"SKIN":{"url":"https://textures.minecraft.net/texture/$GERMAN_FLAG"}}}"""
        val encoded = Base64.getEncoder().encodeToString(skin.toByteArray())

        val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(GERMAN_FLAG.toByteArray()))
        profile.setProperty(ProfileProperty("textures", encoded))
        return profile
    }

    private fun mini(text: String): Component {
        return MiniMessage.miniMessage().deserialize(text)
    }
}
