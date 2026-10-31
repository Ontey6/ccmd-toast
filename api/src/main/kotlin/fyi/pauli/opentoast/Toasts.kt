package fyi.pauli.opentoast

interface HeadToast : Toast
interface RecipeToast : Toast
interface SystemToast : Toast
interface AdvancementToast : Toast
interface TutorialToast : Toast {
	 fun tutorialIcon(): Icon

	 enum class Icon {
		  WASD,
		  MOUSE,
		  TREE,
		  RECIPE_BOOK,
		  WOODEN_PLANKS,
		  SOCIAL_INTERACTIONS,
		  RIGHT_CLICK;

		  val char: Char
				get() = FIRST_CHAR + ordinal

		  companion object {
				private const val FIRST_CHAR = ''
		  }
	 }
}
