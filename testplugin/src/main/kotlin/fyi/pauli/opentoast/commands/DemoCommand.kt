/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast.commands

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import fyi.pauli.opentoast.*
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object DemoCommand {
	 private const val GERMAN_FLAG = "5e7899b4806858697e283f084d9173fe487886453774626b24bd8cfecc77b3f"
	 private const val BRITISH_FLAG = "a9edcdd7b06173d7d221c7274c86cba35730170788bb6a1db09cc6810435b92c"
	 private const val FRENCH_FLAG = "51269a067ee37e63635ca1e723b676f139dc2dbddff96bbfef99d8b35c996bc"
	 private const val SPANISH_FLAG = "32bd4521983309e0ad76c1ee29874287957ec3d96f8d889324da8c887e485ea8"

	 private val TOASTS: Map<String, (Player) -> Unit> = mapOf(
		  "friend" to ::friend,
		  "party" to ::party,
		  "clan" to ::clan,
		  "language" to ::language,
		  "system" to ::system,
		  "recipe" to ::recipe,
		  "tutorial" to ::tutorial,
	 )

	 fun build(): LiteralCommandNode<CommandSourceStack> {
		  val toast = Commands.literal("toast")
				.requires { source -> source.sender is Player }
				.executes { context ->
					 val player = context.source.sender as Player
					 TOASTS.values.forEach { show -> show(player) }
					 TOASTS.size
				}

		  for((name, show) in TOASTS) {
				toast.then(Commands.literal(name).executes { context ->
					 show(context.source.sender as Player)
					 Command.SINGLE_SUCCESS
				})
		  }

		  toast.then(Commands.literal("types").executes { context ->
				types(context.source.sender as Player)
				Command.SINGLE_SUCCESS
		  })

		  return Commands.literal("demo")
				.then(toast)
				.build()
	 }

	 private fun types(player: Player) {
		  player.toast(
				ItemStack.of(Material.DIAMOND),
				mini("<yellow>Advancement toast<newline><white>any item as icon"),
		  )
		  player.toast(
				ItemStack.of(Material.NETHER_STAR),
				mini("<light_purple>Challenge frame<newline><white>plays a sound"),
				ToastFrame.CHALLENGE,
		  )
		  player.toast(
				player.playerProfile,
				mini("<aqua>Head toast<newline><white>flat player face"),
		  )
		  player.systemToast(
				mini("<yellow>System toast<newline><white>grows with the text"),
		  )
		  player.recipeToast(
				ItemStack.of(Material.CRAFTING_TABLE),
				mini("<#500050>Recipe toast<newline>item on a light background"),
		  )
		  player.tutorialToast(
				TutorialIcon.MOUSE,
				mini("<#500050>Tutorial toast<newline>one of seven symbols"),
		  )
	 }

	 private fun friend(player: Player) {
		  player.toast(
				player.playerProfile,
				mini("<aqua>${player.name}</aqua> sent you a <green>friend request"),
		  )
	 }

	 private fun party(player: Player) {
		  player.toast(
				ItemStack.of(Material.FIREWORK_ROCKET),
				mini("<light_purple><bold>Party invite</bold><newline><white>from <aqua>Notch</aqua> <gray>(3/4)"),
		  )
	 }

	 private fun clan(player: Player) {
		  player.toast(
				ItemStack.of(Material.GOLDEN_HELMET),
				mini("<purple><bold>GHG</bold><newline><white>You have been <green>promoted<white>!"),
		  )
	 }

	 private fun language(player: Player) {
		  player.toast(
				flag(GERMAN_FLAG),
				mini("<gray>Sprache auf <gradient:#555555:#DD0000:#FFCE00>Deutsch</gradient> gestellt")
		  )
		  player.toast(flag(BRITISH_FLAG), mini("<gray>Language set to <gradient:#C8102E:#FFFFFF:#3B5BDB>English"))
		  player.toast(flag(FRENCH_FLAG), mini("<gray>Langue réglée sur <gradient:#3B5BDB:#FFFFFF:#ED2939>Français"))
		  player.toast(flag(SPANISH_FLAG), mini("<gray>Idioma cambiado a <gradient:#C60B1E:#FFC400:#C60B1E>Español"))
	 }

	 private fun system(player: Player) {
		  player.systemToast(
				mini("<yellow>Server restart in 5 minutes!<newline><white>Leave your dungeon now or your <red>loot will be lost<white>."),
		  )
	 }

	 private fun recipe(player: Player) {
		  player.recipeToast(
				ItemStack.of(Material.CAKE),
				mini("<#500050>Party perk unlocked!<newline>Cake for <dark_green>everyone"),
		  )
	 }

	 private fun tutorial(player: Player) {
		  player.tutorialToast(
				TutorialIcon.SOCIAL_INTERACTIONS,
				mini("<#500050>Find your friends<newline>Press <dark_aqua><bold>P</bold></dark_aqua> for the list"),
		  )
	 }

	 private fun flag(texture: String): PlayerProfile {
		  val skin = """{"textures":{"SKIN":{"url":"https://textures.minecraft.net/texture/$texture"}}}"""
		  val encoded = Base64.getEncoder().encodeToString(skin.toByteArray())

		  val profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.toByteArray()))
		  profile.setProperty(ProfileProperty("textures", encoded))
		  return profile
	 }

	 private fun mini(text: String): Component {
		  return MiniMessage.miniMessage().deserialize(text)
	 }
}
