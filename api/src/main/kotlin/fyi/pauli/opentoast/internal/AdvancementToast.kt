package fyi.pauli.opentoast.internal

import fyi.pauli.opentoast.Toast
import fyi.pauli.opentoast.ToastFrame
import io.papermc.paper.datacomponent.DataComponentTypes
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

open class AdvancementToast(
	 private val icon: ItemStack,
	 private val text: Component,
	 private val frame: ToastFrame,
) : Toast {
	 init {
		  if(icon.type == Material.PLAYER_HEAD) {
				icon.setData(DataComponentTypes.ITEM_MODEL, FLAT_HEAD)
		  }
	 }

	 private val title = ToastLayout.advancement(text) ?: text.also(::fallbackWarn)
	 override fun text(): Component {
		  return text
	 }

	 override fun icon(): ItemStack {
		  return icon
	 }

	 override fun show(player: Player) {
		  ToastSender.send(player, icon, title, frame)
	 }

	 private companion object {
		  val FLAT_HEAD = Key.key(NAMESPACE, "flat_head")
	 }
}
