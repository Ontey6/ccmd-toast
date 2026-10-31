/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package fyi.pauli.opentoast;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket.PositionedAdvancement;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

final class ToastUtil {
	
	private static final String NAMESPACE = "ccmd_toast";
	
	private ToastUtil() {
	
	}
	
	private static final String CRITERION = "x";
	
	/// Sends a tooltip to a player
	/// @param icon The icon displayed on the left of the tooltip
	/// @param text The text
	
	public static void send(Player player, ItemStack icon, Component text) {
		var identifier = Identifier.fromNamespaceAndPath(NAMESPACE, UUID.randomUUID().toString());
		var advancement = advancement(icon, text);
		
		var connection = ((CraftPlayer) player).getHandle().connection;
		connection.send(grantPacket(identifier, advancement));
		connection.send(removalPacket(identifier));
	}
	
	private static Advancement advancement(ItemStack icon, Component title) {
		boolean showToast = true;
		boolean announceChat = false;
		boolean hidden = true;
		var display = new DisplayInfo(
		  ItemStackTemplate.fromNonEmptyStack(CraftItemStack.asNMSCopy(icon)),
		  PaperAdventure.asVanilla(title),
		  net.minecraft.network.chat.Component.empty(),
		  Optional.<net.minecraft.core.ClientAsset.ResourceTexture>empty(),
		  AdvancementType.TASK,
		  showToast,
		  announceChat,
		  hidden
		);
		
		boolean sendsTelemetry = false;
		return new Advancement(
		  Optional.<Identifier>empty(),
		  Optional.<DisplayInfo>of(display),
		  AdvancementRewards.EMPTY,
		  Map.<String, Criterion<?>>of(CRITERION, new Criterion<>(CriteriaTriggers.IMPOSSIBLE, new ImpossibleTrigger.TriggerInstance())),
		  AdvancementRequirements.allOf(List.<String>of(CRITERION)),
		  sendsTelemetry
		);
	}
	
	private static ClientboundUpdateAdvancementsPacket grantPacket(Identifier id, Advancement advancement) {
		var progress = new AdvancementProgress();
		progress.update(advancement.requirements());
		var criterion = progress.getCriterion(CRITERION);
		assert criterion != null;
		criterion.grant();
		
		var positioned = new PositionedAdvancement(new AdvancementHolder(id, advancement), 0f, 0f);
		boolean reset = false;
		boolean showAdvancements = true;
		return new ClientboundUpdateAdvancementsPacket(
		  reset,
		  List.<PositionedAdvancement>of(positioned),
		  Set.<Identifier>of(),
		  Map.<Identifier, AdvancementProgress>of(id, progress),
		  showAdvancements
		);
	}
	
	private static ClientboundUpdateAdvancementsPacket removalPacket(Identifier id) {
		boolean reset = false;
		boolean showAdvancements = false;
		return new ClientboundUpdateAdvancementsPacket(
		  reset,
		  List.<PositionedAdvancement>of(),
		  Set.<Identifier>of(id),
		  Map.<Identifier, AdvancementProgress>of(),
		  showAdvancements
		);
	}
}
