package fyi.pauli.opentoast

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal data class DefaultAdvancementToastIml(val text: Component, val icon: ItemStack?) : AdvancementToast {
	 override fun text(): Component {
		  return text
	 }

	 override fun icon(): ItemStack? {
		  return icon
	 }

	 override fun show(player: Player) {
		  ToastUtil.send(player, this.icon, this.text)
	 }
}
