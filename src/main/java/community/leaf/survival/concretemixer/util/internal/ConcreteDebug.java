/*
 * Copyright © 2022-2024, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.util.internal;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;

import java.awt.*;
import java.util.regex.Pattern;

public class ConcreteDebug {
	private ConcreteDebug() {}
	
	private static ChatColor chatColorOf(DyeColor color) {
		return ChatColor.of(new Color(color.getColor().asRGB()));
	}
	
	private static ChatColor chatColorOf(Material material) {
		return chatColorOf(dyeColorOf(material));
	}
	
	private static final Pattern CONCRETE_COLOR_PATTERN =
		Pattern.compile("^(?<color>\\w+)_CONCRETE");
	
	private static DyeColor dyeColorOf(Material material) {
		var matcher = CONCRETE_COLOR_PATTERN.matcher(material.name());
		if (!matcher.find()) {
			return DyeColor.WHITE;
		}
		
		return DyeColor.valueOf(matcher.group("color"));
	}
	
	public static void debugItem(String message, Item item) {
		var material = item.getItemStack().getType();
		var color = chatColorOf(material);
		
		Bukkit.spigot().broadcast(
			new ComponentBuilder()
				.append(message)
					.style(ComponentStyle.builder().color(color).build())
					.event(new HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						new Text(
							new ComponentBuilder()
								.append("Item: " + material + "x" + item.getItemStack().getAmount() + "\n")
								.append("Item UUID: " + item.getUniqueId() + "\n")
								.append("Thrower UUID: " + item.getThrower())
								.build()
						)
					))
				.build()
		);
	}
}
