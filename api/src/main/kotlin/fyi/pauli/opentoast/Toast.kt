package fyi.pauli.opentoast

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

interface Toast {
	 @Contract(pure = true)
	 fun text(): Component

	 @Contract(pure = true)
	 fun icon(): ItemStack?

	 fun show(player: Player)
}
