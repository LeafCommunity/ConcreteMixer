/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import com.rezzedup.util.valuables.Adapter;
import community.leaf.configvalues.bukkit.DefaultYamlValue;
import community.leaf.configvalues.bukkit.YamlAccessor;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.data.Load;
import community.leaf.configvalues.bukkit.data.YamlDataFile;
import org.bukkit.Sound;

import java.util.List;
import java.util.Optional;

public class Config extends YamlDataFile
{
    public static final YamlValue<Version> VERSION =
        YamlValue.of(
            "meta.config-version",
            YamlAccessor.of(Adapter.of(
                o -> {
                    try { return Optional.of(Version.valueOf(String.valueOf(o))); }
                    catch (RuntimeException ignored) { return Optional.empty(); }
                },
                version -> Optional.of(version.toString())
            ))
        )
        .maybe();
    
    public static final DefaultYamlValue<Boolean> REQUIRE_PERMISSION =
        YamlValue.ofBoolean("cauldron.require-permission-node").defaults(false);
    
    public static final DefaultYamlValue<Boolean> LOWER_WATER_LEVEL =
        YamlValue.ofBoolean("cauldron.lower-water-level").defaults(true);
    
    public static final DefaultYamlValue<Boolean> ENABLE_EFFECTS =
        YamlValue.ofBoolean("effects.enabled").defaults(true);
    
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
