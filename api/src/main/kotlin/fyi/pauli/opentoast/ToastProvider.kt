package fyi.pauli.opentoast

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

interface ToastProvider {
	 /// @param text The text displayed on the toast
	 /// @param icon The icon of the toast
	 /// @throws UnsupportedOperationException If this provider doesn't support creating this type of toast
	 @Throws(UnsupportedOperationException::class)
	 fun advancementToast(text: Component, icon: ItemStack): AdvancementToast

	 /// @param text The text displayed on the toast
	 /// @param profile The profile to get the head from
	 /// @throws UnsupportedOperationException If this provider doesn't support creating this type of toast
	 @Throws(UnsupportedOperationException::class)
	 fun headToast(text: Component, profile: PlayerProfile): HeadToast {
		  return headToast(text, ResolvableProfile.resolvableProfile(profile))
	 }

	 /// @param text The text displayed on the toast
	 /// @param profile The profile to get the head from
	 /// @throws UnsupportedOperationException If this provider doesn't support creating this type of toast
	 @Throws(UnsupportedOperationException::class)
	 fun headToast(text: Component, profile: ResolvableProfile): HeadToast

	 /// @param text The text displayed on the toast
	 /// @param icon The icon of the toast
	 /// @throws UnsupportedOperationException If this provider doesn't support creating this type of toast
	 @Throws(UnsupportedOperationException::class)
	 fun recipeToast(text: Component, icon: ItemStack): RecipeToast

	 /// @param text The text displayed on the toast
	 /// @throws UnsupportedOperationException If this provider doesn't support creating this type of toast
	 @Throws(UnsupportedOperationException::class)
	 fun systemToast(text: Component): SystemToast

	 /// @param text The text displayed on the toast
	 /// @param icon The icon of the toast
	 /// @throws UnsupportedOperationException If this provider doesn't support creating this type of toast
	 @Throws(UnsupportedOperationException::class)
	 fun tutorialToast(text: Component, icon: TutorialToast.Icon): TutorialToast

	 companion object {
		  fun defaultToastProvider(): ToastProvider {
				return DefaultToastProvider.INSTANCE
		  }

		  @JvmStatic
		  fun advancedToastProvider(): ToastProvider {
				return AdvancedToastProvider.INSTANCE
		  }
	 }
}
