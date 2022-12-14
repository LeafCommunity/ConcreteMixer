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
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
        @NullOr List<CauldronAccessHook> exceptional = null;
        
        for (CauldronAccessHook hook : cauldronAccessHooks)
        {
            try
            {
                if (hook.isEnabled() && !hook.isCauldronAccessibleToPlayer(player, cauldron)) { return false; }
            }
            catch (RuntimeException e)
            {
                if (exceptional == null) { exceptional = new ArrayList<>(); }
                exceptional.add(hook);
                
                plugin.getLogger().log(Level.WARNING, hook.getClass().getSimpleName(), e);
            }
        }
        
        if (exceptional != null) { exceptional.forEach(cauldronAccessHooks::remove); }
        return true;
    }
}
