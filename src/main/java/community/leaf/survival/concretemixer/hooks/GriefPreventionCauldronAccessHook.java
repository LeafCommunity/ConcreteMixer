/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks;

import community.leaf.survival.concretemixer.ConcreteMixerPlugin;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.function.Supplier;

public class GriefPreventionCauldronAccessHook implements CauldronAccessHook
{
    private boolean failedToSendRestriction = false;
    private final ConcreteMixerPlugin plugin;
    
    public GriefPreventionCauldronAccessHook(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean isEnabled()
    {
        return plugin.getServer().getPluginManager().isPluginEnabled("GriefPrevention");
    }
    
    @Override
    public boolean isCauldronAccessibleToPlayer(Player player, Block cauldron)
    {
        // - - - Player can access - - -
        @NullOr Claim claim = GriefPrevention.instance.dataStore.getClaimAt(cauldron.getLocation(), false, null);
        if (claim == null) { return true; } // no claim
        
        @NullOr Supplier<String> restriction = claim.checkPermission(player, ClaimPermission.Inventory, null);
        if (restriction == null) { return true; } // allowed
        
        // - - - Player cannot access - - -
        if (failedToSendRestriction) { return false; } // not allowed & couldn't send failure message, so skip it.
        
        // Send restriction message as if it came from GriefPrevention itself.
        // RED == TextMode.Err, which is package private for whatever reason.
        try { GriefPrevention.sendMessage(player, ChatColor.RED, restriction.get()); }
        catch (Exception ignored) { failedToSendRestriction = true; } // future proofing! :)
        return false; // not allowed
    }
}
