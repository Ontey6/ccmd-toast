package fyi.pauli.opentoast

import fyi.pauli.opentoast.internal.PackArchive
import fyi.pauli.opentoast.internal.PackBuilder
import fyi.pauli.opentoast.internal.VanillaAssets
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import org.bukkit.Bukkit
import java.net.URI
import java.nio.file.Path
import java.security.MessageDigest
import java.util.HexFormat
import java.util.UUID

class ToastPack private constructor(val bytes: ByteArray) {
    val hash: String = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(bytes))

    val id: UUID = UUID.nameUUIDFromBytes(bytes)

    @JvmOverloads
    fun buildRequest(url: URI, required: Boolean = true): ResourcePackRequest.Builder {
        return ResourcePackRequest.resourcePackRequest()
            .packs(info(url))
            .required(required)
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
        @JvmOverloads
        fun build(cache: Path, minecraftVersion: String = Bukkit.getMinecraftVersion()): ToastPack {
            val vanilla = VanillaAssets.load(cache, minecraftVersion)
            return ToastPack(PackBuilder(vanilla).build())
        }
    }
}
