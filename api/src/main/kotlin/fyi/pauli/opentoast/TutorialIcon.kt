package fyi.pauli.opentoast

/**
 * https://minecraft.wiki/w/Tutorial_hints
 */
enum class TutorialIcon {
    WASD,
    MOUSE,
    TREE,
    RECIPE_BOOK,
    WOODEN_PLANKS,
    SOCIAL_INTERACTIONS,
    RIGHT_CLICK;

    internal val char: Char
        get() = FIRST_CHAR + ordinal

    private companion object {
        const val FIRST_CHAR = ''
    }
}
