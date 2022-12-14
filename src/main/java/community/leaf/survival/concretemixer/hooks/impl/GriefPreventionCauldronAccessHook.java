/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks.impl;

import com.github.zafarkhaja.semver.Version;
import community.leaf.survival.concretemixer.ConcreteMixerPlugin;
import community.leaf.survival.concretemixer.hooks.CauldronAccessHook;
import community.leaf.survival.concretemixer.util.Versions;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.function.Supplier;

public class GriefPreventionCauldronAccessHook implements CauldronAccessHook
{
    private static final String GRIEF_PREVENTION = "GriefPrevention";
    private static final Version MINIMUM_VERSION = Version.forIntegers(16, 18);
    
    private boolean failedToSendRestriction = false;
    private @NullOr Version griefPreventionVersion = null;
    
    private final ConcreteMixerPlugin plugin;
    
    public GriefPreventionCauldronAccessHook(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean isEnabled()
    {
        @NullOr Plugin gp = plugin.getServer().getPluginManager().getPlugin(GRIEF_PREVENTION);
        if (gp == null || !gp.isEnabled()) { return false; }
        
        if (griefPreventionVersion == null)
        {
            griefPreventionVersion = Versions.parse(gp.getDescription().getVersion()).orElse(Versions.ZERO);
            
            if (griefPreventionVersion.lessThan(MINIMUM_VERSION))
            {
                plugin.getLogger().warning("Your version of GriefPrevention is out of date.");
                plugin.getLogger().warning("Please update to at least version 16.18 in order to respect claimed cauldrons.");
            }
        }
        
        return griefPreventionVersion.greaterThanOrEqualTo(MINIMUM_VERSION);
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
