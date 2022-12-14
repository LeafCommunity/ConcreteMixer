/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks;

import community.leaf.survival.concretemixer.ConcreteMixerPlugin;
import community.leaf.survival.concretemixer.hooks.impl.GriefPreventionCauldronAccessHook;
import community.leaf.survival.concretemixer.hooks.impl.UniversalCauldronAccessHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class HookHandler implements CauldronAccessHook
{
    private final List<RegisteredHook<?>> allRegisteredHooks = new ArrayList<>();
    private final List<RegisteredHook<CauldronAccessHook>> cauldronAccessHooks = new ArrayList<>();
    
    private final ConcreteMixerPlugin plugin;
    
    public HookHandler(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
    
        register(new UniversalCauldronAccessHook(plugin));
        register(new GriefPreventionCauldronAccessHook(plugin));
        
        isEnabled(); // initialize hooks & send any warnings to console immediately
    }
    
    @SuppressWarnings("unchecked")
    private <H extends Hook> void register(H impl)
    {
        RegisteredHook<H> hook = new RegisteredHook<>(impl);
        allRegisteredHooks.add(hook);
        
        if (impl instanceof CauldronAccessHook)
        {
            cauldronAccessHooks.add((RegisteredHook<CauldronAccessHook>) hook);
        }
    }
    
    @Override
    public boolean isCauldronAccessibleToPlayer(Player player, Block cauldron)
    {
        for (RegisteredHook<CauldronAccessHook> hook : cauldronAccessHooks)
        {
            try
            {
                if (hook.isEnabled())
                {
                    if (!hook.get().isCauldronAccessibleToPlayer(player, cauldron))
                    {
                        return false;
                    }
                }
            }
            catch (RuntimeException e)
            {
                plugin.getLogger().log(Level.WARNING, hook.name(), e);
                hook.disable();
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isEnabled()
    {
        boolean anyHookIsEnabled = false;
        
        for (RegisteredHook<?> hook : allRegisteredHooks)
        {
            try
            {
                if (hook.isEnabled()) { anyHookIsEnabled = true; }
            }
            catch (RuntimeException e)
            {
                plugin.getLogger().log(Level.WARNING, hook.name(), e);
                hook.disable();
            }
        }
        
        return anyHookIsEnabled;
    }
    
    private static class RegisteredHook<H extends Hook> implements Hook
    {
        private boolean enabled = true;
        
        private final H hook;
        
        private RegisteredHook(H hook)
        {
            this.hook = hook;
        }
        
        public H get() { return hook; }
        
        public String name() { return hook.getClass().getSimpleName(); }
        
        @Override
        public boolean isEnabled() { return enabled && hook.isEnabled(); }
        
        public void disable() { enabled = false; }
    }
}
