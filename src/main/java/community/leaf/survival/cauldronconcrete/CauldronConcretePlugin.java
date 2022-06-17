/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/CauldronConcrete>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.cauldronconcrete;

import com.github.zafarkhaja.semver.Version;
import community.leaf.eventful.bukkit.BukkitEventSource;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class CauldronConcretePlugin extends JavaPlugin implements BukkitEventSource
{
    private final Version version;
    private final Path directory;
    private final Config config;
    
    public CauldronConcretePlugin()
    {
        this.version = Version.valueOf(getDescription().getVersion());
        this.directory = getDataFolder().toPath();
        this.config = new Config(this);
    }
    
    @Override
    public void onEnable()
    {
        config.reload();
        
    }
    
    @Override
    public Plugin plugin() { return this; }
    
    public Version version() { return version; }
    
    public Path directory() { return directory; }
    
    public Config config() { return config; }
}
