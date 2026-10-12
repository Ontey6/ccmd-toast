package fyi.pauli.opentoast.internal

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal class PackBuilder(private val vanilla: VanillaAssets) {
    private val archive = PackArchive.read(staticPack())

    private val bitmapProviders: List<JsonObject> by lazy {
        vanilla.json(VanillaAssets.DEFAULT_FONT)["providers"].asJsonArray
            .map { provider -> provider.asJsonObject }
            .filter { provider -> provider["type"].asString == "bitmap" }
    }

    fun build(): ByteArray {
        for (shift in ToastLayout.fontShifts) {
            addShiftedFont(shift)
        }

        for (look in Look.entries) {
            addBackgrounds(look)
        }

        return archive.toBytes()
    }

    private fun staticPack(): ByteArray {
        val resource = PackBuilder::class.java.getResourceAsStream(STATIC_PACK)
            ?: error("$STATIC_PACK is missing from the jar")
        return resource.use { stream -> stream.readBytes() }
    }

    private fun addShiftedFont(shift: Int) {
        val providers = JsonArray()
        providers.add(spaceProvider())
        for (bitmap in bitmapProviders) {
            providers.add(shiftedProvider(bitmap, shift))
        }

        archive["$FONTS/shift$shift.json"] = font(providers)
    }

    private fun spaceProvider(): JsonObject {
        val provider = JsonObject()
        provider.addProperty("type", "reference")
        provider.addProperty("id", "minecraft:include/space")
        return provider
    }

    private fun shiftedProvider(bitmap: JsonObject, shift: Int): JsonObject {
        val name = bitmap["file"].asString.substringAfterLast('/')
        val source = vanilla.image(VanillaAssets.FONT_TEXTURES + name)
        val rows = bitmap["chars"].asJsonArray.size()
        val height = bitmap["height"]?.asInt ?: DEFAULT_GLYPH_HEIGHT
        val cell = source.height / rows
        check(cell == height) { "$name: scaled glyphs are not handled" }

        val padding = maxOf(shift, 0)
        val provider = bitmap.deepCopy()
        if (padding > 0) {
            val texture = "shift${shift}_$name"
            archive["$TEXTURES/$texture"] = png(padRows(source, cell, padding))
            provider.addProperty("file", "$NAMESPACE:font/$texture")
        }
        provider.addProperty("height", height + padding)
        provider.addProperty("ascent", bitmap["ascent"].asInt + shift)
        return provider
    }

    private fun padRows(source: BufferedImage, cell: Int, padding: Int): BufferedImage {
        val rows = source.height / cell
        val padded = BufferedImage(source.width, rows * (cell + padding), BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until source.height) {
            val row = y / cell
            val paddedY = row * (cell + padding) + y % cell
            for (x in 0 until source.width) {
                padded.setRGB(x, paddedY, pixel(source, x, y))
            }
        }

        return padded
    }

    private fun addBackgrounds(look: Look) {
        val sprite = VanillaAssets.TOAST_SPRITES + look.id + ".png"
        val source = vanilla.image(sprite)
        check(source.width == ToastLayout.TOAST_WIDTH) { "${look.id} toast texture is ${source.width} wide" }
        val border = border(sprite)

        for (height in Look.heights) {
            val toast = stretch(source, height, border)
            val right = toast.getSubimage(ToastLayout.TITLE_X, 0, toast.width - ToastLayout.TITLE_X, height)
            val left = toast.getSubimage(0, 0, ToastLayout.TITLE_X, height)

            val providers = JsonArray()
            providers.add(backgroundProvider("${look.id}${height}_right.png", right, ToastLayout.BACKGROUND_RIGHT))
            providers.add(backgroundProvider("${look.id}${height}_left.png", left, ToastLayout.BACKGROUND_LEFT))

            archive["$FONTS/${look.id}$height.json"] = font(providers)
        }
    }

    private fun border(sprite: String): Border {
        val meta = "$sprite.mcmeta"
        if (!vanilla.exists(meta)) {
            return Border(DEFAULT_BORDER, DEFAULT_BORDER)
        }

        val border = vanilla.json(meta)["gui"].asJsonObject["scaling"].asJsonObject["border"]
        if (!border.isJsonObject) {
            return Border(border.asInt, border.asInt)
        }
        return Border(border.asJsonObject["top"].asInt, border.asJsonObject["bottom"].asInt)
    }

    private fun stretch(source: BufferedImage, height: Int, border: Border): BufferedImage {
        val stretched = BufferedImage(source.width, height, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until height) {
            val distanceToBottom = height - y
            val sourceY = if (distanceToBottom <= border.bottom) {
                source.height - distanceToBottom
            } else {
                minOf(y, border.top)
            }

            for (x in 0 until source.width) {
                stretched.setRGB(x, y, pixel(source, x, sourceY))
            }
        }

        return stretched
    }

    private fun backgroundProvider(texture: String, image: BufferedImage, char: Char): JsonObject {
        archive["$TEXTURES/$texture"] = png(image)

        val chars = JsonArray()
        chars.add(char.toString())

        val provider = JsonObject()
        provider.addProperty("type", "bitmap")
        provider.addProperty("file", "$NAMESPACE:font/$texture")
        provider.addProperty("height", image.height)
        provider.addProperty("ascent", BACKGROUND_ASCENT)
        provider.add("chars", chars)
        return provider
    }

    private fun font(providers: JsonArray): ByteArray {
        val font = JsonObject()
        font.add("providers", providers)
        return font.toString().toByteArray()
    }

    private fun png(image: BufferedImage): ByteArray {
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }

    private fun pixel(image: BufferedImage, x: Int, y: Int): Int {
        if (image.type != BufferedImage.TYPE_BYTE_GRAY) {
            return image.getRGB(x, y)
        }

        val gray = image.raster.getSample(x, y, 0)
        return OPAQUE or (gray shl 16) or (gray shl 8) or gray
    }

    private class Border(val top: Int, val bottom: Int)

    private companion object {
        const val STATIC_PACK = "/open-toast-pack.zip"
        const val FONTS = "assets/$NAMESPACE/font"
        const val TEXTURES = "assets/$NAMESPACE/textures/font"

        const val DEFAULT_GLYPH_HEIGHT = 8
        const val DEFAULT_BORDER = 4
        const val BACKGROUND_ASCENT = 25
        const val OPAQUE = 0xFF000000.toInt()
    }
}
