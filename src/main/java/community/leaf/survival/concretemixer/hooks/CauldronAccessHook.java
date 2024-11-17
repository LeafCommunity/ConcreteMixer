/*
 * Copyright © 2022-2024, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface CauldronAccessHook extends Hook {
	boolean isCauldronAccessibleToPlayer(Player player, Block cauldron);
}
