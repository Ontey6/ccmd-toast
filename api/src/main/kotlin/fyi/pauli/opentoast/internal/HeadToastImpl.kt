package fyi.pauli.opentoast.internal

import fyi.pauli.opentoast.HeadToast
import fyi.pauli.opentoast.ToastFrame
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class HeadToastImpl(
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
