/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/CauldronConcrete>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.cauldronconcrete;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import com.rezzedup.util.valuables.Adapter;
import community.leaf.configvalues.bukkit.DefaultYamlValue;
import community.leaf.configvalues.bukkit.YamlAccessor;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.data.Load;
import community.leaf.configvalues.bukkit.data.YamlDataFile;

import java.util.List;
import java.util.Optional;

public class Config extends YamlDataFile
{
    public static final YamlValue<Version> VERSION =
        YamlValue.of(
            "version",
            YamlAccessor.of(Adapter.of(
                o -> {
                    try { return Optional.of(Version.valueOf(String.valueOf(o))); }
                    catch (RuntimeException ignored) { return Optional.empty(); }
                },
                version -> Optional.of(version.toString())
            ))
        )
        .maybe();
    
    public static final DefaultYamlValue<Boolean> ENABLE_SPLASH_PARTICLES =
        YamlValue.ofBoolean("splash-effect.enable-particles").defaults(true);
    
    @AggregatedResult
    private static final List<YamlValue<?>> VALUES =
        Aggregates.fromThisClass().constantsOfType(YamlValue.type()).toList();
    
    public Config(CauldronConcretePlugin plugin)
    {
        super(plugin.directory(), "config.yml", Load.LATER);
        
        reloadsWith(() ->
        {
            if (isInvalid()) { return; }
            
            Version zero = Version.forIntegers(0);
            Version existing = get(VERSION).orElse(zero);
            boolean outdated = existing.lessThan(plugin.version());
            
            if (outdated) { set(VERSION, plugin.version()); }
            
            headerFromResource("config.header.txt");
            defaultValues(VALUES);
            
            if (isUpdated())
            {
                if (outdated)
                {
                    if (existing.greaterThan(zero))
                    {
                        plugin.getLogger().info("Updating config...");
                        backupThenSave(plugin.directory().resolve("backups"), "v" + existing);
                    }
                    else
                    {
                        plugin.getLogger().info("Generating config...");
                        save();
                    }
                }
                else
                {
                    plugin.getLogger().info("Adding missing config values...");
                    backupThenSave(plugin.directory().resolve("backups"), "v" + existing + "-missing-values");
                }
            }
        });
    }
}
