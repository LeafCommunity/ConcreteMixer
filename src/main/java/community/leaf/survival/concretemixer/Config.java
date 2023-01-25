/*
 * Copyright © 2022-2023, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import community.leaf.configvalues.bukkit.DefaultYamlValue;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.data.Load;
import community.leaf.configvalues.bukkit.data.YamlDataFile;
import community.leaf.configvalues.bukkit.migrations.Migration;
import community.leaf.survival.concretemixer.util.Versions;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.List;

public class Config extends YamlDataFile
{
    public static final YamlValue<Version> VERSION =
        YamlValue.of("meta.config-version", Versions.YAML)
            .comments(
                "Please do not modify this value (it's used to update the config)."
            )
            .maybe();
    
    public static final DefaultYamlValue<Boolean> METRICS =
        YamlValue.ofBoolean("plugin.metrics")
            .migrates(
                Migration.move("metrics.enabled")
            )
            .comments(
                "Can the plugin submit anonymous usage metrics to bStats?",
                "https://bstats.org/plugin/bukkit/ConcreteMixer/15590",
                "(Global bStats settings can be found in: /plugins/bStats/config.yml)"
            )
            .defaults(true);
    
    public static final DefaultYamlValue<Boolean> UPDATES =
        YamlValue.ofBoolean("plugin.check-for-updates")
            .comments(
                "Can the plugin check for updates?",
                "If an update is found, a notification will be sent to console."
            )
            .defaults(true);
    
    public static final DefaultYamlValue<Boolean> REQUIRE_PERMISSION =
        YamlValue.ofBoolean("cauldrons.require-permission-node")
            .comments(
                "If enabled, players must have access to the 'concretemixer.cauldrons'",
                "permission node in order to create concrete with cauldrons."
            )
            .defaults(false);
    
    public static final DefaultYamlValue<Boolean> LOWER_WATER_LEVEL =
        YamlValue.ofBoolean("cauldrons.lower-water-level")
            .comments(
                "Should the cauldron's water level be lowered after successfully creating concrete?"
            )
            .defaults(true);
    
    public static final DefaultYamlValue<Boolean> ENABLE_EFFECTS =
        YamlValue.ofBoolean("effects.enabled")
            .comments(
                "Toggle all effects."
            )
            .defaults(true);
    
    public static final DefaultYamlValue<Boolean> SPLASH_PARTICLES_EFFECT =
        YamlValue.ofBoolean("effects.splash.particles.enabled").defaults(true);
    
    public static final DefaultYamlValue<Boolean> SPLASH_SOUND_EFFECT =
        YamlValue.ofBoolean("effects.splash.sound.enabled").defaults(true);
    
    public static final DefaultYamlValue<Sound> SPLASH_SOUND_EFFECT_NAME =
        YamlValue.ofSound("effects.splash.sound.name").defaults(Sound.ENTITY_GENERIC_SPLASH);
    
    public static final DefaultYamlValue<Float> SPLASH_SOUND_EFFECT_VOLUME =
        YamlValue.ofFloat("effects.splash.sound.volume").defaults(0.75F);
    
    public static final DefaultYamlValue<Float> SPLASH_SOUND_EFFECT_PITCH =
        YamlValue.ofFloat("effects.splash.sound.pitch").defaults(1F);
    
    public static final DefaultYamlValue<Boolean> TRANSFORM_PARTICLES_EFFECT =
        YamlValue.ofBoolean("effects.transform.particles.enabled").defaults(true);
    
    public static final DefaultYamlValue<Boolean> TRANSFORM_SOUND_EFFECT =
        YamlValue.ofBoolean("effects.transform.sound.enabled").defaults(true);
    
    public static final DefaultYamlValue<Sound> TRANSFORM_SOUND_EFFECT_NAME =
        YamlValue.ofSound("effects.transform.sound.name").defaults(Sound.BLOCK_FIRE_EXTINGUISH);
    
    public static final DefaultYamlValue<Float> TRANSFORM_SOUND_EFFECT_VOLUME =
        YamlValue.ofFloat("effects.transform.sound.volume").defaults(0.65F);
    
    public static final DefaultYamlValue<Float> TRANSFORM_SOUND_EFFECT_PITCH =
        YamlValue.ofFloat("effects.transform.sound.pitch").defaults(1.25F);
    
    @AggregatedResult
    private static final List<YamlValue<?>> VALUES =
        Aggregates.fromThisClass().constantsOfType(YamlValue.type()).toList();
    
    public Config(ConcreteMixerPlugin plugin)
    {
        super(plugin.directory(), "config.yml", Load.NOW);
        
        reloadsWith(() ->
        {
            if (isInvalid()) { return; }
            
            plugin.getLogger().info("Loading config...");
            
            Version existing = get(VERSION).orElse(Versions.ZERO);
            boolean outdated = existing.lessThan(plugin.version());
            
            if (outdated) { set(VERSION, plugin.version()); }
            
            headerFromResource("config.header.txt");
            defaultValues(VALUES);
            
            if (isUpdated())
            {
                removeEmptyConfigurationSections(data());
                
                if (outdated)
                {
                    if (existing.greaterThan(Versions.ZERO))
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
    
    private void removeEmptyConfigurationSections(ConfigurationSection section)
    {
        for (String key : section.getKeys(false))
        {
            @NullOr ConfigurationSection child = section.getConfigurationSection(key);
            if (child == null) { continue; }
            
            removeEmptyConfigurationSections(child);
            if (child.getKeys(false).isEmpty()) { section.set(key, null); }
        }
    }
}
