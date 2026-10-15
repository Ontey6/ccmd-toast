/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast.internal

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import ontey.ccmd.toast.TutorialToast

internal const val NAMESPACE = "open-toast"

private class Glyph(val char: Char, val style: Style, val sprite: ObjectComponent? = null) {

	 val width: Int
		  get() {
				val bold = if(style.hasDecoration(TextDecoration.BOLD)) 1 else 0

				if(sprite != null) {
					 return SPRITE_WIDTH + bold
				}

				val width = ASCII_WIDTH.getOrNull(char - SPACE)?.digitToInt() ?: FALLBACK_WIDTH
				return width + bold
		  }

	 companion object {
		  const val SPRITE = '￼'

		  private const val SPRITE_WIDTH = 8
		  private const val FALLBACK_WIDTH = 6

		  private const val SPACE = ' '

		  /**
			* Width for all the ascii characters (32-126)
			* https://www.ascii-code.com/
			*/
		  private const val ASCII_WIDTH =
				"42466662444626266666666666225656766666666466666666666666666464663666665662653666666646666664247"
	 }
}

private class Line(val glyphs: List<Glyph>) {
	 val width: Int
		  get() = glyphs.sumOf(Glyph::width)

	 val hasSprite: Boolean
		  get() = glyphs.mapNotNull(Glyph::sprite).any()

	 val isEmpty: Boolean
		  get() = glyphs.isEmpty()

	 fun withWord(word: List<Glyph>): Line {
		  if(isEmpty) {
				return Line(word)
		  }

		  val space = Glyph(' ', glyphs.last().style)
		  return Line(glyphs + space + word)
	 }
}

private class ToastText private constructor(val lines: List<Line>) {
	 fun adjustShiftsForSprites(shifts: List<Int>): List<Int>? {
		  val noShiftedLineWithSprite = lines.withIndex().all { (index, line) ->
				shifts[index] == 0 || !line.hasSprite
		  }

		  if(noShiftedLineWithSprite) {
				return shifts
		  }

		  if(lines.size == 1) {
				return listOf(0)
		  }

		  return null
	 }

	 companion object {
		  fun of(component: Component, defaultStyle: Style, maxWidth: Int): ToastText? {
				val glyphs = mutableListOf<Glyph>()
				collect(component, defaultStyle, glyphs)

				val lines = wrap(glyphs, maxWidth)
				if(lines.any { line -> line.width > maxWidth }) {
					 return null
				}

				return ToastText(lines)
		  }

		  private fun collect(component: Component, inherited: Style, glyphs: MutableList<Glyph>) {
				val style = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)

				when(component) {
					 is TextComponent -> component.content().forEach { char -> glyphs += Glyph(char, style) }
					 is ObjectComponent -> glyphs += Glyph(Glyph.SPRITE, style, component)
					 else -> throw IllegalArgumentException(
						  "Toast text may only contain text and object components. ${component::class.simpleName} is not supported."
					 )
				}

				component.children().forEach { child -> collect(child, style, glyphs) }
		  }

		  private fun wrap(glyphs: List<Glyph>, maxWidth: Int): List<Line> = buildList {
				for(paragraph in split(glyphs, '\n')) {
					 var line = Line(emptyList())

					 for(word in split(paragraph, ' ')) {
						  val extended = line.withWord(word)
						  if(line.isEmpty || extended.width <= maxWidth) {
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
				add(mutableListOf())
				for(glyph in glyphs) {
					 if(glyph.char == separator) {
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
	 private const val MAX_LINE_WIDTH = 125
	 private const val TUTORIAL_ICON_X = 6

	 private const val COVER = ''
	 private const val COVER_ADVANCE = 125

	 private const val FIRST_NEGATIVE_SPACE = ''
	 private const val NEGATIVE_SPACES = 8

	 const val BACKGROUND_RIGHT = ''
	 const val BACKGROUND_LEFT = ''
	 private const val BACKGROUND_RIGHT_ADVANCE = TOAST_WIDTH - TITLE_X + 1
	 private const val BACKGROUND_LEFT_ADVANCE = TITLE_X + 1

	 private val ADVANCEMENT_LINE_SHIFTS = mapOf(
		  1 to listOf(6),
		  2 to listOf(11, 0),
		  3 to listOf(15, 6, -3),
	 )

	 private val TOAST_LAYOUT_FONT = font("toast")
	 private val TUTORIAL_ICONS_FONT = font("tutorial_icons")

	 val fontShifts: List<Int>
		  get() {
				val advancement = ADVANCEMENT_LINE_SHIFTS.values.flatten()
				val looks = (1..ToastStyle.MAX_LINES).flatMap { lines -> ToastStyle.shifts(lines) }
				return (advancement + looks).distinct().filter { shift -> shift != 0 }
		  }

	 fun advancement(title: Component): Component? {
		  val text = ToastText.of(title, Style.empty(), MAX_LINE_WIDTH) ?: return null
		  val layout = ADVANCEMENT_LINE_SHIFTS[text.lines.size] ?: return null
		  val shifts = text.adjustShiftsForSprites(layout) ?: return null

		  return Component.text { builder ->
				builder.append(layoutText(COVER + rewind(COVER_ADVANCE)))
				appendLines(builder, text, shifts)
		  }
	 }

	 fun styled(text: Component, toastStyle: ToastStyle, tutorialIcon: TutorialToast.Icon?): Component? {
		  val defaultStyle = Style.style().color(toastStyle.textColor).build()
		  val toastText = ToastText.of(text, defaultStyle, toastStyle.lineWidth) ?: return null
		  val lines = toastText.lines
		  if(lines.size > ToastStyle.MAX_LINES) {
				return null
		  }

		  val shifts = toastText.adjustShiftsForSprites(ToastStyle.shifts(lines.size)) ?: return null
		  val background = font(toastStyle.id + ToastStyle.height(lines.size))

		  return Component.text { builder ->
				builder.append(Component.text(BACKGROUND_RIGHT, background))
				builder.append(layoutText(rewind(BACKGROUND_RIGHT_ADVANCE + TITLE_X)))
				builder.append(Component.text(BACKGROUND_LEFT, background))
				builder.append(layoutText(rewind(BACKGROUND_LEFT_ADVANCE - toastStyle.textX)))
				appendLines(builder, toastText, shifts)

				if(tutorialIcon != null) {
					 val textEnd = toastStyle.textX + lines.last().width
					 builder.append(layoutText(rewind(textEnd - TUTORIAL_ICON_X)))
					 builder.append(Component.text(tutorialIcon.char, TUTORIAL_ICONS_FONT))
				}
		  }
	 }

	 private fun appendLines(result: TextComponent.Builder, text: ToastText, shifts: List<Int>) {
		  text.lines.forEachIndexed { index, line ->
				if(index > 0) {
					 val previous = text.lines[index - 1]
					 result.append(layoutText(rewind(previous.width)))
				}

				val shift = shifts[index]
				val font = if(shift == 0) null else Key.key(NAMESPACE, "shift$shift")
				result.append(render(line.glyphs, font))
		  }
	 }

	 private fun rewind(pixels: Int): String = buildString {
		  for(bit in NEGATIVE_SPACES - 1 downTo 0) {
				val needed = pixels shr bit and 1 == 1
				if(needed) {
					 append(FIRST_NEGATIVE_SPACE + bit)
				}
		  }
	 }

	 private fun layoutText(text: String): Component {
		  return Component.text(text, TOAST_LAYOUT_FONT)
	 }

	 private fun font(name: String): Style {
		  return Style.style()
				.font(Key.key(NAMESPACE, name))
				.color(NamedTextColor.WHITE)
				.build()
	 }
}

private fun render(line: List<Glyph>, font: Key?): Component {
	 return Component.text { builder ->
		  line.forEach { glyph ->
				builder.append(
					 glyph.sprite?.children(emptyList())?.style(glyph.style)
						  ?: Component.text(glyph.char, glyph.style.font(font))
				)
		  }
	 }
}
