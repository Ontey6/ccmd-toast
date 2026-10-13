/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast

import com.sun.net.httpserver.HttpServer
import fyi.pauli.opentoast.commands.DemoCommand
import fyi.pauli.opentoast.commands.ToastCommand
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status
import org.bukkit.plugin.java.JavaPlugin
import java.net.InetSocketAddress
import java.net.URI

class TestPlugin : JavaPlugin(), Listener {
    private lateinit var packServer: HttpServer
    private lateinit var packRequest: ResourcePackRequest

    override fun onEnable() {
        saveDefaultConfig()

        logger.info("Preparing the resource pack; the first start per Minecraft version downloads the vanilla client for this.")
        val cache = dataFolder.toPath().resolve("vanilla")
        val pack = ToastPack.build(cache, server.minecraftVersion)

        packRequest = pack.requestBuilder(URI(config.getString("pack-url")!!)).build()
        packServer = startPackServer(pack)

        server.pluginManager.registerEvents(this, this)
        registerCommands()
    }

    override fun onDisable() {
        packServer.stop(0)
    }

    private fun startPackServer(pack: ToastPack): HttpServer {
        val server = HttpServer.create(InetSocketAddress(config.getInt("pack-port")), 0)

        server.createContext("/pack.zip") { exchange ->
            exchange.sendResponseHeaders(200, pack.bytes.size.toLong())
            exchange.responseBody.use { body -> body.write(pack.bytes) }
        }

        server.start()
        return server
    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(ToastCommand.build())
            event.registrar().register(DemoCommand.build())
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.sendResourcePacks(packRequest)
    }

    @EventHandler
    fun onPackStatus(event: PlayerResourcePackStatusEvent) {
        when (event.status) {
            Status.ACCEPTED, Status.DOWNLOADED, Status.SUCCESSFULLY_LOADED -> {}
            else -> event.player.kick(Component.text("This server requires its resource pack (${event.status})."))
        }
    }
}
