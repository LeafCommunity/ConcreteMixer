/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.commands.CommandUtils;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import community.leaf.survival.concretemixer.ConcreteMixerPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

public class WorldGuardCauldronAccessHook implements CauldronAccessHook
{
    private @NullOr MessageSimulator messages = null;
    private final ConcreteMixerPlugin plugin;
    
    public WorldGuardCauldronAccessHook(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean isEnabled()
    {
        return plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard");
    }
    
    @Override
    public boolean isCauldronAccessibleToPlayer(Player player, Block cauldron)
    {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
        World world = BukkitAdapter.adapt(cauldron.getWorld());
        Location location = BukkitAdapter.adapt(cauldron.getLocation());
        
        // - - - Player can access - - -
        if (platform.getSessionManager().hasBypass(localPlayer, world)) { return true; } // bypass
    
        RegionQuery query = platform.getRegionContainer().createQuery();
        if (query.testBuild(location, localPlayer, Flags.CHEST_ACCESS)) { return true; } // allowed
        
        // - - - Player cannot access - - -
        if (messages == null) { messages = new MessageSimulator(); }
        
        messages.sendDenyMessage(platform, localPlayer, location, query);
        return false; // not allowed
    }
    
    // Simulate WorldGuard's deny message (as if it came from WorldGuard itself):
    // https://github.com/EngineHub/WorldGuard/blob/51fa25dd03ec49a7e0b6b0ddab2006d7d6ac74a9/worldguard-bukkit/src/main/java/com/sk89q/worldguard/bukkit/listener/RegionProtectionListener.java#L112-L124
    // ...
    // The class loader didn't like the use of WG's Flag class when not installed sooo
    // here's this little class to help isolate things ¯\_(ツ)_/¯
    private static class MessageSimulator
    {
        boolean failedToSendDenyMessage = false;
        
        void sendDenyMessage(WorldGuardPlatform platform, LocalPlayer localPlayer, Location location, RegionQuery query)
        {
            if (failedToSendDenyMessage) { return; }
            
            try
            {
                @NullOr String message = query.queryValue(location, localPlayer, Flags.DENY_MESSAGE);
                if (message == null || message.isEmpty()) { return; } // no deny message here
                
                message = platform.getMatcher().replaceMacros(localPlayer, message);
                message = CommandUtils.replaceColorMacros(message);
                localPlayer.printRaw(message.replace("%what%", "transform concrete"));
            }
            catch (Exception ignored)
            {
                // future-proof, since the DENY_MESSAGE flag is deprecated...
                failedToSendDenyMessage = true;
            }
        }
    }
}
