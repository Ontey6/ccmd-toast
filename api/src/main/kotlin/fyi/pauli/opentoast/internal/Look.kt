package fyi.pauli.opentoast.internal

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

internal enum class Look(val textX: Int, val lineWidth: Int, val textColor: TextColor?) {
    SYSTEM(18, 134, null),
    RECIPE(30, 122, NamedTextColor.BLACK),
    TUTORIAL(30, 122, NamedTextColor.BLACK);

    val id: String
        get() = name.lowercase()

    companion object {
        const val MAX_LINES = 8

        private const val LINE_SPACING = 11
        private const val MIN_HEIGHT = 32
        private const val VERTICAL_PADDING = 10
        private const val SINGLE_LINE_SHIFT = 6
        private const val FIRST_LINE_SHIFT = 11

        val heights: List<Int>
            get() = (1..MAX_LINES).map { lines -> height(lines) }.distinct()

        fun height(lines: Int): Int {
            return maxOf(MIN_HEIGHT, LINE_SPACING * lines + VERTICAL_PADDING)
        }

        fun shifts(lines: Int): List<Int> {
            if (lines == 1) {
                return listOf(SINGLE_LINE_SHIFT)
            }

            return List(lines) { line -> FIRST_LINE_SHIFT - LINE_SPACING * line }
        }
    }
}
