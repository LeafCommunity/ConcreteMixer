/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/CauldronConcrete>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.cauldronconcrete;

import com.rezzedup.util.constants.types.Cast;
import community.leaf.eventful.bukkit.CancellationPolicy;
import community.leaf.eventful.bukkit.ListenerOrder;
import community.leaf.eventful.bukkit.annotations.CancelledEvents;
import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.tasks.TaskContext;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CauldronPowderDropListener implements Listener
{
    private final Map<UUID, TaskContext<BukkitTask>> transformationTasksByItemUuid = new HashMap<>();
    
    private final CauldronConcretePlugin plugin;
    
    public CauldronPowderDropListener(CauldronConcretePlugin plugin)
    {
        this.plugin = plugin;
    }
    
    @EventListener
    @CancelledEvents(CancellationPolicy.REJECT)
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        if (!plugin.permissions().allowsConvertingConcretePowder(player)) { return; }
        
        Item item = event.getItemDrop();
        if (Concrete.ofPowder(item.getItemStack().getType()).isEmpty()) { return; }
        
        transformConcretePowder(item);
    }
    
    @EventListener(ListenerOrder.MONITOR)
    @CancelledEvents(CancellationPolicy.REJECT)
    public void onItemMerge(ItemMergeEvent event)
    {
        if (cancelExistingTransformation(event.getTarget()) || cancelExistingTransformation(event.getEntity()))
        {
            transformConcretePowder(event.getTarget());
        }
    }
    
    @EventListener(ListenerOrder.MONITOR)
    @CancelledEvents(CancellationPolicy.REJECT)
    public void onCauldronLevelChange(CauldronLevelChangeEvent event)
    {
        if (!(event.getEntity() instanceof Player player)) { return; }
        if (!plugin.permissions().allowsConvertingConcretePowder(player)) { return; }
        
        Block block = event.getBlock();
        BlockData pre = block.getBlockData();
        BlockData post = event.getNewState().getBlockData();
        
        if (pre.getMaterial() != Material.CAULDRON || post.getMaterial() != Material.WATER_CAULDRON) { return; }
        
        block.getWorld().getNearbyEntities(block.getBoundingBox()).stream()
            .flatMap(entity -> Cast.as(Item.class, entity).stream())
            .filter(item -> Concrete.ofPowder(item.getItemStack().getType()).isPresent())
            .filter(item -> !transformationTasksByItemUuid.containsKey(item.getUniqueId()))
            .limit(64L)
            .forEach(this::transformConcretePowder);
    }
    
    private boolean cancelExistingTransformation(Item item)
    {
        @NullOr TaskContext<BukkitTask> task =
            transformationTasksByItemUuid.remove(item.getUniqueId());
        
        if (task != null)
        {
            task.cancel();
            return true;
        }
        
        return false;
    }
    
    private void cancel(Item item, TaskContext<BukkitTask> task)
    {
        transformationTasksByItemUuid.remove(item.getUniqueId());
        task.cancel();
    }
    
    private void transformConcretePowder(Item item)
    {
        class IterationCounter
        {
            int outside = 0;
            int inside = 0;
        }
        
        IterationCounter iterations = new IterationCounter();
        
        transformationTasksByItemUuid.put(
            item.getUniqueId(),
            plugin.sync().delay(2).ticks().every(2).ticks().run(task ->
            {
                Block cauldron = item.getLocation().getBlock();
                
                if (cauldron.getBlockData().getMaterial() != Material.WATER_CAULDRON)
                {
                    iterations.outside++;
                    if (iterations.outside > 20) { cancel(item, task); }
                    return;
                }
                
                iterations.inside++;
                
                if (iterations.inside == 1)
                {
                    item.setPickupDelay(40);
                    plugin.effects().splashSoundEffect(item.getLocation());
                }
                
                if (iterations.inside < 15)
                {
                    plugin.effects().cauldronSplashParticles(cauldron);
                    return;
                }
                
                cancel(item, task);
                
                @NullOr Concrete concrete = Concrete.ofPowder(item.getItemStack().getType()).orElse(null);
                if (concrete == null) { return; }
                
                ItemStack stack = item.getItemStack();
                stack.setType(concrete.concrete());
                item.setItemStack(stack);
                
                item.setVelocity(new Vector(0, 0.3, 0));
                item.setPickupDelay(10);
                
                plugin.effects().concreteTransformedParticles(cauldron);
                plugin.effects().transformSoundEffect(cauldron.getLocation());
                
                if (!plugin.config().getOrDefault(Config.LOWER_WATER_LEVEL)) { return; }
    
                Levelled levelled = (Levelled) cauldron.getBlockData();
                int level = levelled.getLevel() - 1;
                
                if (level <= 0)
                {
                    cauldron.setType(Material.CAULDRON);
                }
                else
                {
                    levelled.setLevel(level);
                    cauldron.setBlockData(levelled);
                }
            })
        );
    }
}
