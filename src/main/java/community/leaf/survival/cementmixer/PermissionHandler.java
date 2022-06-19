/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/CementMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.cementmixer;

import org.bukkit.permissions.Permissible;

public class PermissionHandler
{
    private final Config config;
    
    public PermissionHandler(Config config)
    {
        this.config = config;
    }
    
    public boolean allowsConvertingConcretePowder(Permissible permissible)
    {
        return !config.getOrDefault(Config.REQUIRE_PERMISSION) || permissible.hasPermission("cauldronconcrete.use");
    }
}