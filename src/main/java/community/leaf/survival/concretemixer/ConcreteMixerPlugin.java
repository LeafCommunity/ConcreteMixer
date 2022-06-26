/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.github.zafarkhaja.semver.Version;
import community.leaf.eventful.bukkit.BukkitEventSource;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class ConcreteMixerPlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource
{
    private final Version version;
    private final Path directory;
    private final Config config;
    private final PermissionHandler permissions;
    private final EffectHandler effects;
    
    public ConcreteMixerPlugin()
    {
        this.version = Version.valueOf(getDescription().getVersion());
        this.directory = getDataFolder().toPath();
        this.config = new Config(this);
        this.permissions = new PermissionHandler(config);
        this.effects = new EffectHandler(config);
    }
    
    @Override
    public void onEnable()
    {
        config.reload();
        events().register(new CauldronPowderDropListener(this));
    }
    
    @Override
    public Plugin plugin() { return this; }
    
    public Version version() { return version; }
    
    public Path directory() { return directory; }
    
    public Config config() { return config; }
    
    public PermissionHandler permissions() { return permissions; }
    
    public EffectHandler effects() { return effects; }
}
