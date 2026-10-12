package fyi.pauli.opentoast.internal

import fyi.pauli.opentoast.TutorialIcon
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

internal const val NAMESPACE = "open-toast"

private class Glyph(val char: Char, val style: Style, val inline: ObjectComponent? = null) {
    val advance: Int
        get() {
            val bold = if (style.hasDecoration(TextDecoration.BOLD)) 1 else 0
            if (inline != null) {
                return OBJECT_ADVANCE + bold
            }

            val width = ADVANCES.getOrNull(char - FIRST_PRINTABLE)?.digitToInt() ?: DEFAULT_ADVANCE
            return width + bold
        }

    companion object {
        const val OBJECT = '￼'

        private const val OBJECT_ADVANCE = 8
        private const val DEFAULT_ADVANCE = 6
        private const val FIRST_PRINTABLE = ' '
        private const val ADVANCES =
            "42466662444626266666666666225656766666666466666666666666666464663666665662653666666646666664247"
    }
}

private class Line(val glyphs: List<Glyph>) {
    val width: Int
        get() = glyphs.sumOf { glyph -> glyph.advance }

    val hasInline: Boolean
        get() = glyphs.any { glyph -> glyph.inline != null }

    val isEmpty: Boolean
        get() = glyphs.isEmpty()

    fun withWord(word: List<Glyph>): Line {
        if (isEmpty) {
            return Line(word)
        }

        val space = Glyph(' ', glyphs.last().style)
        return Line(glyphs + space + word)
    }
}

private class ToastText private constructor(val lines: List<Line>) {
    fun pinned(shifts: List<Int>): List<Int>? {
        val inlineLinesUnshifted = lines.withIndex().all { (index, line) ->
            shifts[index] == 0 || !line.hasInline
        }

        if (inlineLinesUnshifted) {
            return shifts
        }

        if (lines.size == 1) {
            return listOf(0)
        }

        return null
    }

    companion object {
        fun of(component: Component, defaultStyle: Style, maxWidth: Int): ToastText? {
            val glyphs = mutableListOf<Glyph>()
            if (!collect(component, defaultStyle, glyphs)) {
                return null
            }

            val lines = wrap(glyphs, maxWidth)
            if (lines.any { line -> line.width > maxWidth }) {
                return null
            }

            return ToastText(lines)
        }

        private fun collect(component: Component, inherited: Style, glyphs: MutableList<Glyph>): Boolean {
            val style = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)

            when (component) {
                is TextComponent -> component.content().forEach { char -> glyphs += Glyph(char, style) }
                is ObjectComponent -> glyphs += Glyph(Glyph.OBJECT, style, component)
                else -> return false
            }

            return component.children().all { child -> collect(child, style, glyphs) }
        }

        private fun wrap(glyphs: List<Glyph>, maxWidth: Int): List<Line> = buildList {
            for (paragraph in split(glyphs, '\n')) {
                var line = Line(emptyList())

                for (word in split(paragraph, ' ')) {
                    val extended = line.withWord(word)
                    if (line.isEmpty || extended.width <= maxWidth) {
                        line = extended
                    } else {
                        add(line)
                        line = Line(word)
                    }
                }

                add(line)
            }
        }


        private fun split(glyphs: List<Glyph>, separator: Char): List<MutableList<Glyph>> = buildList {
            for (glyph in glyphs) {
                if (glyph.char == separator) {
                    add(mutableListOf())
                } else {
                    last() += glyph
                }
            }
        }
    }
}

internal object ToastLayout {
    const val TITLE_X = 30
    const val TOAST_WIDTH = 160
    const val BACKGROUND_RIGHT = ''
    const val BACKGROUND_LEFT = ''

    private const val MAX_LINE_WIDTH = 125
    private const val COVER = ''
    private const val COVER_ADVANCE = 125
    private const val FIRST_NEGATIVE_SPACE = ''
    private const val NEGATIVE_SPACES = 8
    private const val BACKGROUND_RIGHT_ADVANCE = TOAST_WIDTH - TITLE_X + 1
    private const val BACKGROUND_LEFT_ADVANCE = TITLE_X + 1
    private const val TUTORIAL_ICON_X = 6

    private val ADVANCEMENT_SHIFTS = mapOf(
        1 to listOf(6),
        2 to listOf(11, 0),
        3 to listOf(15, 6, -3),
    )

    private val CONTROL = font("toast")
    private val TUTORIAL_ICONS = font("tutorial_icons")

    val fontShifts: List<Int>
        get() {
            val advancement = ADVANCEMENT_SHIFTS.values.flatten()
            val looks = (1..Look.MAX_LINES).flatMap { lines -> Look.shifts(lines) }
            return (advancement + looks).distinct().filter { shift -> shift != 0 }
        }

    fun advancement(title: Component): Component? {
        val text = ToastText.of(title, Style.empty(), MAX_LINE_WIDTH) ?: return null
        val layout = ADVANCEMENT_SHIFTS[text.lines.size] ?: return null
        val shifts = text.pinned(layout) ?: return null

        return Component.text { builder ->
            builder.append(control(COVER + rewind(COVER_ADVANCE)))
            appendLines(builder, text, shifts)
        }
    }

    fun look(text: Component, look: Look, icon: TutorialIcon?): Component? {
        val defaultStyle = Style.style().color(look.textColor).build()
        val toastText = ToastText.of(text, defaultStyle, look.lineWidth) ?: return null
        val lines = toastText.lines
        if (lines.size > Look.MAX_LINES) {
            return null
        }

        val shifts = toastText.pinned(Look.shifts(lines.size)) ?: return null
        val background = font(look.id + Look.height(lines.size))

        return Component.text { builder ->
            builder.append(Component.text(BACKGROUND_RIGHT, background))
            builder.append(control(rewind(BACKGROUND_RIGHT_ADVANCE + TITLE_X)))
            builder.append(Component.text(BACKGROUND_LEFT, background))
            builder.append(control(rewind(BACKGROUND_LEFT_ADVANCE - look.textX)))
            appendLines(builder, toastText, shifts)

            if (icon != null) {
                val textEnd = look.textX + lines.last().width
                builder.append(control(rewind(textEnd - TUTORIAL_ICON_X)))
                builder.append(Component.text(icon.char, TUTORIAL_ICONS))
            }
        }
    }

    private fun appendLines(result: TextComponent.Builder, text: ToastText, shifts: List<Int>) {
        text.lines.forEachIndexed { index, line ->
            if (index > 0) {
                val previous = text.lines[index - 1]
                result.append(control(rewind(previous.width)))
            }

            val shift = shifts[index]
            val font = if (shift == 0) null else Key.key(NAMESPACE, "shift$shift")
            result.append(render(line.glyphs, font))
        }
    }

    private fun rewind(pixels: Int): String = buildString {
        for (bit in NEGATIVE_SPACES - 1 downTo 0) {
            val needed = pixels shr bit and 1 == 1
            if (needed) {
                append(FIRST_NEGATIVE_SPACE + bit)
            }
        }
    }

    private fun control(text: String): Component {
        return Component.text(text, CONTROL)
    }

    private fun font(name: String): Style {
        return Style.style()
            .font(Key.key(NAMESPACE, name))
            .color(NamedTextColor.WHITE)
            .build()
    }
}

private fun render(line: List<Glyph>, font: Key?): Component {
    return Component.text().apply { builder ->
        line.forEach { glyph ->
            builder.append(
                glyph.inline?.children(emptyList())?.style(glyph.style)
                    ?: Component.text(glyph.char, glyph.style.font(font))
            )
        }
    }.build()
}
