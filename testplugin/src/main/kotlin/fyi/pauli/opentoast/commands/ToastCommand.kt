package fyi.pauli.opentoast.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import fyi.pauli.opentoast.ToastFrame
import fyi.pauli.opentoast.TutorialIcon
import fyi.pauli.opentoast.recipeToast
import fyi.pauli.opentoast.systemToast
import fyi.pauli.opentoast.toast
import fyi.pauli.opentoast.tutorialToast
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

private typealias Context = CommandContext<CommandSourceStack>
private typealias Node = ArgumentBuilder<CommandSourceStack, *>

object ToastCommand {

    fun build(): LiteralCommandNode<CommandSourceStack> {
        val players = Commands.argument("players", ArgumentTypes.players())
            .then(system())
            .then(recipe())
            .then(tutorial())
            .then(advancement())

        return Commands.literal("toast")
            .then(players)
            .build()
    }

    private fun system(): Node {
        val text = text { player, _, text -> player.systemToast(text) }
        return Commands.literal("system").then(text)
    }

    private fun recipe(): Node {
        val text = text { player, context, text -> player.recipeToast(item(context), text) }
        val item = Commands.argument("item", ArgumentTypes.itemStack()).then(text)
        return Commands.literal("recipe").then(item)
    }

    private fun tutorial(): Node {
        val tutorial = Commands.literal("tutorial")

        for (icon in TutorialIcon.entries) {
            val text = text { player, _, text -> player.tutorialToast(icon, text) }
            tutorial.then(Commands.literal(icon.name.lowercase()).then(text))
        }

        return tutorial
    }

    private fun advancement(): Node {
        val item = Commands.argument("item", ArgumentTypes.itemStack())

        for (frame in ToastFrame.entries) {
            val text = text { player, context, text -> player.toast(item(context), text, frame) }
            item.then(Commands.literal(frame.name.lowercase()).then(text))
        }

        return item
    }

    private fun text(show: (Player, Context, Component) -> Unit): Node {
        return Commands.argument("text", StringArgumentType.greedyString()).executes { context ->
            val text = MiniMessage.miniMessage().deserialize(StringArgumentType.getString(context, "text"))
            val players = context.getArgument("players", PlayerSelectorArgumentResolver::class.java)
                .resolve(context.source)

            for (player in players) {
                show(player, context, text)
            }

            Command.SINGLE_SUCCESS
        }
    }

    private fun item(context: Context): ItemStack {
        return context.getArgument("item", ItemStack::class.java)
    }
}
