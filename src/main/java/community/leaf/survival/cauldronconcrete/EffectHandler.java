/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/CauldronConcrete>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.cauldronconcrete;

import community.leaf.configvalues.bukkit.DefaultYamlValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.concurrent.ThreadLocalRandom;

public class EffectHandler
{
    private final Config config;
    
    public EffectHandler(Config config)
    {
        this.config = config;
    }
    
    private boolean isEffectEnabled(DefaultYamlValue<Boolean> effect)
    {
        return config.getOrDefault(Config.ENABLE_EFFECTS) && config.getOrDefault(effect);
    }
    
    private float fluctuate(float middle)
    {
        return ThreadLocalRandom.current().nextFloat(middle - 0.125F, middle + 0.125F);
    }
    
    private void sound(Location location, DefaultYamlValue<Sound> sound, DefaultYamlValue<Float> volume, DefaultYamlValue<Float> pitch)
    {
        @NullOr World world = location.getWorld();
        if (world == null) { return; }
        
        world.playSound(
            location,
            config.getOrDefault(sound),
            config.getOrDefault(volume),
            fluctuate(config.getOrDefault(pitch))
        );
    }
    
    public void splashSoundEffect(Location location)
    {
        if (isEffectEnabled(Config.SPLASH_SOUND_EFFECT))
        {
            sound(
                location,
                Config.SPLASH_SOUND_EFFECT_NAME,
                Config.SPLASH_SOUND_EFFECT_VOLUME,
                Config.SPLASH_SOUND_EFFECT_PITCH
            );
        }
    }
    
    public void cauldronSplashParticles(Block cauldron)
    {
        if (!isEffectEnabled(Config.SPLASH_PARTICLES_EFFECT)) { return; }
        if (cauldron.getBlockData().getMaterial() != Material.WATER_CAULDRON) { return; }
        
        double waterHeight = .9 - (.1875 * (3 - ((Levelled) cauldron.getBlockData()).getLevel()));
        
        cauldron.getWorld().spawnParticle(
            Particle.WATER_SPLASH,
            cauldron.getLocation().getBlockX() + .5,
            cauldron.getLocation().getBlockY() + waterHeight,
            cauldron.getLocation().getBlockZ() + .5,
            8,
            .15,
            .05,
            .15
        );
    }
    
    public void concreteTransformedParticles(Block cauldron)
    {
        if (!isEffectEnabled(Config.TRANSFORM_PARTICLES_EFFECT)) { return; }
    
        cauldron.getWorld().spawnParticle(
            Particle.EXPLOSION_NORMAL,
            cauldron.getLocation().getBlockX() + .5,
            cauldron.getLocation().getBlockY() + 1,
            cauldron.getLocation().getBlockZ() + .5,
            3,
            .1,
            0,
            .1,
            .03
        );
    }
    
    public void transformSoundEffect(Location location)
    {
        if (!isEffectEnabled(Config.TRANSFORM_SOUND_EFFECT)) { return; }
        
        sound(
            location,
            Config.TRANSFORM_SOUND_EFFECT_NAME,
            Config.TRANSFORM_SOUND_EFFECT_VOLUME,
            Config.TRANSFORM_SOUND_EFFECT_PITCH
        );
    }
}