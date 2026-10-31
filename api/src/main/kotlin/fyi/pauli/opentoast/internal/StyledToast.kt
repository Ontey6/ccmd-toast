package fyi.pauli.opentoast.internal

import fyi.pauli.opentoast.Toast
import fyi.pauli.opentoast.ToastFrame
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import fyi.pauli.opentoast.TutorialToast
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal open class StyledToast (
	 toastStyle: ToastStyle,
	 private val icon: ItemStack?,
	 tutorialIcon: TutorialToast.Icon?,
	 private val text: Component
) : Toast {
	 private val title = ToastLayout.styled(text, toastStyle, tutorialIcon)

	 private val fallback by lazy {
		  fallbackWarn(text)
		  AdvancementToast(icon?.clone() ?: ItemStack.of(Material.PAPER), text, ToastFrame.TASK)
	 }

	 override fun text(): Component {
		  return text
	 }

	 override fun icon(): ItemStack? {
		  return icon
	 }

	 override fun show(player: Player) {
		  if(title == null) {
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
