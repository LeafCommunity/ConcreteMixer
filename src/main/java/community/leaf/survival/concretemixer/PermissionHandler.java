/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import community.leaf.survival.concretemixer.hooks.CauldronAccessHook;
import community.leaf.survival.concretemixer.hooks.GriefPreventionCauldronAccessHook;
import community.leaf.survival.concretemixer.hooks.UniversalCauldronAccessHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionHandler
{
    private final Set<CauldronAccessHook> cauldronAccessHooks = new LinkedHashSet<>();
    
    private final ConcreteMixerPlugin plugin;
    
    public PermissionHandler(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
        
        cauldronAccessHooks.add(new UniversalCauldronAccessHook(plugin));
        cauldronAccessHooks.add(new GriefPreventionCauldronAccessHook(plugin));
    }
    
    public boolean allowsConvertingConcretePowder(Permissible permissible)
    {
        return !plugin.config().getOrDefault(Config.REQUIRE_PERMISSION) || permissible.hasPermission("concretemixer.cauldrons");
    }
    
    public boolean canAccessCauldron(Player player, Block cauldron)
    {
        return cauldronAccessHooks.stream()
            .filter(CauldronAccessHook::isEnabled)
            .allMatch(hook -> hook.isCauldronAccessibleToPlayer(player, cauldron));
    }
}
