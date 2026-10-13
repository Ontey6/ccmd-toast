/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast.internal

import java.io.ByteArrayOutputStream
import java.util.SortedMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

internal class PackArchive private constructor(private val files: SortedMap<String, ByteArray>) {
    operator fun set(path: String, content: ByteArray) {
        files[path] = content
    }

    fun addAssets(other: PackArchive) {
        for ((path, content) in other.files) {
            if (path.startsWith("assets/")) {
                files[path] = content
            }
        }
    }

    fun toBytes(): ByteArray {
        val out = ByteArrayOutputStream()

        ZipOutputStream(out).use { zip ->
            for ((path, content) in files) {
                val entry = ZipEntry(path)
                entry.time = 0

                zip.putNextEntry(entry)
                zip.write(content)
                zip.closeEntry()
            }
        }

        return out.toByteArray()
    }

    companion object {
        fun read(bytes: ByteArray): PackArchive {
            val files = sortedMapOf<String, ByteArray>()

            ZipInputStream(bytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        files[entry.name] = zip.readBytes()
                    }
                    entry = zip.nextEntry
                }
            }

            return PackArchive(files)
        }
    }
}
