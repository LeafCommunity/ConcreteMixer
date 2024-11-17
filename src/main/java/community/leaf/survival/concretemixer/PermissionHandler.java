/*
 * Copyright © 2022-2024, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PermissionHandler {
	private final ConcreteMixerPlugin plugin;
	
	public PermissionHandler(ConcreteMixerPlugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean isAdmin(Permissible permissible) {
		return permissible.hasPermission("concretemixer.admin");
	}
	
	public boolean allowsConvertingConcretePowder(Permissible permissible) {
		return !plugin.config().getOrDefault(Config.REQUIRE_PERMISSION) || permissible.hasPermission("concretemixer.cauldrons");
	}
	
	public boolean canAccessCauldron(Player player, Block cauldron) {
		return plugin.hooks().isCauldronAccessibleToPlayer(player, cauldron);
	}
}
