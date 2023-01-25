/*
 * Copyright © 2022-2023, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.hooks;

class RegisteredHook<H extends Hook> implements Hook
{
    private boolean enabled = true;
    
    private final H hook;
    
    RegisteredHook(H hook)
    {
        this.hook = hook;
    }
    
    public H get() { return hook; }
    
    public String name() { return hook.getClass().getSimpleName(); }
    
    public void enable() { enabled = true; }
    
    public void disable() { enabled = false; }
    
    @Override
    public void reload() { hook.reload(); }
    
    @Override
    public boolean isEnabled() { return enabled && hook.isEnabled(); }
}
