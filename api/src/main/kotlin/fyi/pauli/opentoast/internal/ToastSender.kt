/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast.internal

import fyi.pauli.opentoast.ToastFrame
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementProgress
import net.minecraft.advancements.AdvancementRequirements
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.DisplayInfo
import net.minecraft.advancements.triggers.CriteriaTriggers
import net.minecraft.advancements.triggers.Criterion
import net.minecraft.advancements.triggers.ImpossibleTrigger
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket.PositionedAdvancement
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStackTemplate
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.UUID

internal object ToastSender {
    private const val CRITERION = "x"

    fun send(player: Player, icon: ItemStack, title: Component, frame: ToastFrame) {
        val id = Identifier.fromNamespaceAndPath(NAMESPACE, UUID.randomUUID().toString())
        val advancement = advancement(icon, title, frame)

        val connection = (player as CraftPlayer).handle.connection
        connection.send(grantPacket(id, advancement))
        connection.send(removePacket(id))
    }

    private fun advancement(icon: ItemStack, title: Component, frame: ToastFrame): Advancement {
        val showToast = true
        val announceChat = false
        val hidden = true
        val display = DisplayInfo(
            ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)),
            PaperAdventure.asVanilla(title),
            net.minecraft.network.chat.Component.empty(),
            Optional.empty(),
            AdvancementType.valueOf(frame.name),
            showToast,
            announceChat,
            hidden,
        )

        val sendsTelemetry = false
        return Advancement(
            Optional.empty(),
            Optional.of(display),
            AdvancementRewards.EMPTY,
            mapOf(CRITERION to Criterion(CriteriaTriggers.IMPOSSIBLE, ImpossibleTrigger.TriggerInstance())),
            AdvancementRequirements.allOf(listOf(CRITERION)),
            sendsTelemetry,
        )
    }

    private fun grantPacket(id: Identifier, advancement: Advancement): ClientboundUpdateAdvancementsPacket {
        val progress = AdvancementProgress()
        progress.update(advancement.requirements())
        progress.getCriterion(CRITERION)!!.grant()

        val positioned = PositionedAdvancement(AdvancementHolder(id, advancement), 0f, 0f)
        val reset = false
        val showAdvancements = true
        return ClientboundUpdateAdvancementsPacket(
            reset,
            listOf(positioned),
            emptySet(),
            mapOf(id to progress),
            showAdvancements,
        )
    }

    private fun removePacket(id: Identifier): ClientboundUpdateAdvancementsPacket {
        val reset = false
        val showAdvancements = false
        return ClientboundUpdateAdvancementsPacket(
            reset,
            emptyList(),
            setOf(id),
            emptyMap(),
            showAdvancements,
        )
    }
}
