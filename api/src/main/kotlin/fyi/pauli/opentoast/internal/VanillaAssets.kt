/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast.internal

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.awt.image.BufferedImage
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.HexFormat
import java.time.Duration
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

internal fun sha1Hex(bytes: ByteArray): String {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes))
}

internal class VanillaAssets private constructor(private val root: Path) {
    fun exists(path: String): Boolean {
        return root.resolve(path).exists()
    }

    fun json(path: String): JsonObject {
        return JsonParser.parseString(root.resolve(path).readText()).asJsonObject
    }

    fun image(path: String): BufferedImage {
        return ImageIO.read(root.resolve(path).toFile())
    }

    companion object {
        const val DEFAULT_FONT = "assets/minecraft/font/include/default.json"
        const val FONT_TEXTURES = "assets/minecraft/textures/font/"
        const val TOAST_SPRITES = "assets/minecraft/textures/gui/sprites/toast/"

        private const val MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
        private const val LAST_NEEDED_FILE = TOAST_SPRITES + "tutorial.png"

        fun load(cache: Path, version: String): VanillaAssets {
            val root = cache.resolve(version)
            if (!root.resolve(LAST_NEEDED_FILE).exists()) {
                download(cache, version, root)
            }
            return VanillaAssets(root)
        }

        private fun download(cache: Path, version: String, target: Path) {
            val http = HttpClient.newBuilder()
                .connectTimeout(10.seconds.toJavaDuration())
                .build()

            val clientJar = clientJar(http, version)

            Files.createDirectories(cache)
            val jar = Files.createTempFile(cache, "client", ".jar")
            val unpacked = Files.createTempDirectory(cache, "unpack")

            try {
                get(http, clientJar.url, BodyHandlers.ofFile(jar))
                val sha1 = sha1Hex(jar.readBytes())
                check(sha1 == clientJar.sha1) { "client jar has SHA-1 $sha1, expected ${clientJar.sha1}" }
                extract(jar, unpacked)

                target.toFile().deleteRecursively()
                Files.move(unpacked, target, StandardCopyOption.ATOMIC_MOVE)
            } finally {
                Files.deleteIfExists(jar)
                unpacked.toFile().deleteRecursively()
            }
        }

        private fun clientJar(http: HttpClient, version: String): ClientJar {
            val versions = fetchJson(http, MANIFEST)["versions"].asJsonArray
            val entry = versions
                .map { element -> element.asJsonObject }
                .firstOrNull { candidate -> candidate["id"].asString == version }
                ?: error("Minecraft $version is not in Mojang's version manifest")

            val downloads = fetchJson(http, entry["url"].asString)["downloads"].asJsonObject
            val client = downloads["client"].asJsonObject
            return ClientJar(client["url"].asString, client["sha1"].asString)
        }

        private fun fetchJson(http: HttpClient, url: String): JsonObject {
            val body = get(http, url, BodyHandlers.ofString())
            return JsonParser.parseString(body).asJsonObject
        }

        private fun <T> get(http: HttpClient, url: String, handler: BodyHandler<T>): T {
            val request = HttpRequest.newBuilder(URI(url))
                .timeout(30.seconds.toJavaDuration())
                .build()

            val response = http.send(request, handler)
            check(response.statusCode() == 200) { "GET $url returned ${response.statusCode()}" }
            return response.body()
        }

        private fun extract(jar: Path, into: Path) {
            ZipFile(jar.toFile()).use { zip ->
                for (entry in zip.entries()) {
                    if (entry.isDirectory || !isNeeded(entry.name)) {
                        continue
                    }

                    val file = into.resolve(entry.name).normalize()
                    check(file.startsWith(into)) { "unexpected path in client jar: ${entry.name}" }

                    Files.createDirectories(file.parent)
                    zip.getInputStream(entry).use { content -> Files.copy(content, file) }
                }
            }
        }

        private fun isNeeded(name: String): Boolean {
            return name == DEFAULT_FONT || name.startsWith(FONT_TEXTURES) || name.startsWith(TOAST_SPRITES)
        }

        private class ClientJar(val url: String, val sha1: String)
    }
}
