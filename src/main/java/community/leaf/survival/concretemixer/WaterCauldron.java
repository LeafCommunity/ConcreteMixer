/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import community.leaf.eventful.bukkit.Events;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.block.CauldronLevelChangeEvent;

import java.util.Optional;

public class WaterCauldron
{
    public static Optional<WaterCauldron> of(Block block)
    {
        return (block.getType() == Material.WATER_CAULDRON) ? Optional.of(new WaterCauldron(block)) : Optional.empty();
    }
    
    private final Block block;
    
    private WaterCauldron(Block block)
    {
        this.block = block;
    }
    
    public Block block() { return block; }
    
    public void lowerWaterLevel()
    {
        if (block.getType() != Material.WATER_CAULDRON) { return; }
        if (!(block.getBlockData() instanceof Levelled levelled)) { return; }
        
        int level = levelled.getLevel() - 1;
    
        if (level <= 0)
        {
            block.setType(Material.CAULDRON);
        }
        else
        {
            levelled.setLevel(level);
            block.setBlockData(levelled);
        }
    }
    
    public boolean isUsableBy(Player player)
    {
        return Events.dispatcher().call(new LevelChangeTestEvent(player)).allowed;
    }
    
    class LevelChangeTestEvent extends CauldronLevelChangeEvent
    {
        private boolean completed = false;
        private boolean allowed = false;
        
        LevelChangeTestEvent(Player player)
        {
            super(WaterCauldron.this.block, player, ChangeReason.BOTTLE_FILL, WaterCauldron.this.block.getState());
        }
        
        void complete()
        {
            if (!completed)
            {
                this.completed = true;
                this.allowed = !isCancelled();
            }
            setCancelled(true);
        }
    }
}
