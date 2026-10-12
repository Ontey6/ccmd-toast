package fyi.pauli.opentoast

import fyi.pauli.opentoast.internal.PackArchive
import fyi.pauli.opentoast.internal.PackBuilder
import fyi.pauli.opentoast.internal.VanillaAssets
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import java.net.URI
import java.nio.file.Path
import java.security.MessageDigest
import java.util.HexFormat
import java.util.UUID

class ToastPack private constructor(val bytes: ByteArray) {
    val hash: String = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes))

    val id: UUID = UUID.nameUUIDFromBytes(bytes)

    @JvmOverloads
    fun request(url: URI, required: Boolean = true): ResourcePackRequest {
        return ResourcePackRequest.resourcePackRequest()
            .packs(info(url))
            .required(required)
            .build()
    }

    fun info(url: URI): ResourcePackInfo {
        return ResourcePackInfo.resourcePackInfo(id, url, hash)
    }

    fun mergeInto(otherPack: ByteArray): ToastPack {
        val merged = PackArchive.read(otherPack)
        merged.addAssets(PackArchive.read(bytes))
        return ToastPack(merged.toBytes())
    }

    companion object {
        @JvmStatic
        fun build(cache: Path, minecraftVersion: String): ToastPack {
            val vanilla = VanillaAssets.load(cache, minecraftVersion)
            return ToastPack(PackBuilder(vanilla).build())
        }
    }
}
