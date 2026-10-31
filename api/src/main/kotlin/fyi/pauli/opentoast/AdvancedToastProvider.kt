package fyi.pauli.opentoast

import fyi.pauli.opentoast.internal.*
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

internal class AdvancedToastProvider private constructor() : ToastProvider {
	 override fun advancementToast(text: Component, icon: ItemStack): AdvancementToast {
		  return AdvancementToastImpl(icon, text, ToastFrame.TASK)
	 }

	 override fun headToast(text: Component, profile: ResolvableProfile): HeadToast {
		  val icon = ItemType.PLAYER_HEAD.createItemStack()
		  icon.setData<ResolvableProfile?>(DataComponentTypes.PROFILE, profile)

		  return HeadToastImpl(icon, text, ToastFrame.TASK)
	 }

	 override fun recipeToast(text: Component, icon: ItemStack): RecipeToast {
		  return RecipeToastImpl(icon, text)
	 }

	 override fun systemToast(text: Component): SystemToast {
		  return SystemToastImpl(text)
	 }

	 override fun tutorialToast(text: Component, icon: TutorialToast.Icon): TutorialToast {
		  return TutorialToastImpl(icon, text)
	 }

	 companion object {
		  @JvmField
		  val INSTANCE: AdvancedToastProvider = AdvancedToastProvider()
	 }
}
