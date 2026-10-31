package fyi.pauli.opentoast

import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

internal class DefaultToastProvider private constructor() : ToastProvider {
	 @Contract(value = "_, _ -> new", pure = true)
	 override fun advancementToast(text: Component, icon: ItemStack): AdvancementToast {
		  return DefaultAdvancementToastIml(text, icon)
	 }

	 @Contract("_, _ -> fail")
	 override fun headToast(text: Component, profile: ResolvableProfile): HeadToast {
		  throw UnsupportedOperationException("The default toast provider can't send recipe toasts")
	 }

	 @Contract("_, _ -> fail")
	 override fun recipeToast(text: Component, icon: ItemStack): RecipeToast {
		  throw UnsupportedOperationException("The default toast provider can't send recipe toasts")
	 }

	 @Contract("_ -> fail")
	 override fun systemToast(text: Component): SystemToast {
		  throw UnsupportedOperationException("The default toast provider can't send system toasts")
	 }

	 @Contract("_, _ -> fail")
	 override fun tutorialToast(text: Component, icon: TutorialToast.Icon): TutorialToast {
		  throw UnsupportedOperationException("The default toast provider can't send tutorial toasts")
	 }

	 companion object {
		  @JvmField
		  val INSTANCE: DefaultToastProvider = DefaultToastProvider()
	 }
}
