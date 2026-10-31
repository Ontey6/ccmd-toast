package fyi.pauli.opentoast.internal

import fyi.pauli.opentoast.RecipeToast
import fyi.pauli.opentoast.ToastFrame
import fyi.pauli.opentoast.TutorialToast
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class AdvancementToastImpl(
	 private val icon: ItemStack,
	 private val text: Component,
	 frame: ToastFrame
) : AdvancementToast(icon, text, frame), fyi.pauli.opentoast.AdvancementToast {
	 override fun text(): Component {
		  return text
	 }

	 override fun icon(): ItemStack {
		  return icon
	 }
}

internal class RecipeToastImpl(
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

internal class TutorialToastImpl(
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