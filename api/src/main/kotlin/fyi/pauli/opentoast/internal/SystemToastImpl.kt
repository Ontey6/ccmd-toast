package fyi.pauli.opentoast.internal

import net.kyori.adventure.text.Component
import fyi.pauli.opentoast.SystemToast
import org.bukkit.inventory.ItemStack

internal class SystemToastImpl(
	 private val text: Component
) : StyledToast(ToastStyle.SYSTEM, null, null, text), SystemToast {
	 override fun text(): Component {
		  return text
	 }

	 override fun icon(): ItemStack? {
		  return null
	 }
}
