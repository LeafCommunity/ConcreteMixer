/*
 * Copyright © 2022-2024, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.rezzedup.util.constants.types.Cast;
import community.leaf.eventful.bukkit.CancellationPolicy;
import community.leaf.eventful.bukkit.ListenerOrder;
import community.leaf.eventful.bukkit.annotations.CancelledEvents;
import community.leaf.eventful.bukkit.annotations.EventListener;
import community.leaf.survival.concretemixer.metrics.TransformationsPerHour;
import community.leaf.survival.concretemixer.util.internal.ConcreteDebug;
import community.leaf.tasks.TaskContext;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
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
import java.util.Objects;
import java.util.UUID;

public class CauldronPowderDropListener implements Listener {
	private final Map<UUID, TaskContext<BukkitTask>> transformationTasksByItemUuid = new HashMap<>();
	
	private final ConcreteMixerPlugin plugin;
	private final TransformationsPerHour counter;
	
	public CauldronPowderDropListener(ConcreteMixerPlugin plugin, TransformationsPerHour counter) {
		this.plugin = plugin;
		this.counter = counter;
	}
	
	@EventListener
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (!plugin.permissions().allowsConvertingConcretePowder(player)) {
			return;
		}
		
		Item item = event.getItemDrop();
		if (Concrete.ofPowder(item.getItemStack().getType()).isEmpty()) {
			return;
		}
		
		ConcreteDebug.debugItem("Player drop", item);
		transformConcretePowder(item);
	}
	
	@EventListener(ListenerOrder.MONITOR)
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onItemMerge(ItemMergeEvent event) {
		Item aggregate = event.getTarget();
		Item piece = event.getEntity();
		
		if (cancelExistingTransformation(aggregate) || cancelExistingTransformation(piece)) {
			// Merge thrower information too.
			if (aggregate.getThrower() == null) {
				aggregate.setThrower(piece.getThrower());
			}
			
			ConcreteDebug.debugItem("Merge", aggregate);
			transformConcretePowder(aggregate);
		}
	}
	
	@EventListener(ListenerOrder.MONITOR)
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		if (!plugin.permissions().allowsConvertingConcretePowder(player)) {
			return;
		}
		
		Block block = event.getBlock();
		BlockData pre = block.getBlockData();
		BlockData post = event.getNewState().getBlockData();
		
		if (pre.getMaterial() != Material.CAULDRON || post.getMaterial() != Material.WATER_CAULDRON) {
			return;
		}
		
		block.getWorld().getNearbyEntities(block.getBoundingBox()).stream()
			.flatMap(entity -> Cast.as(Item.class, entity).stream())
			.filter(item -> Concrete.ofPowder(item.getItemStack().getType()).isPresent())
			.filter(item -> !transformationTasksByItemUuid.containsKey(item.getUniqueId()))
			.limit(64L)
			.forEach(this::transformConcretePowder);
	}
	
	private boolean cancelExistingTransformation(Item item) {
		@NullOr TaskContext<BukkitTask> task =
			transformationTasksByItemUuid.remove(item.getUniqueId());
		
		if (task != null) {
			task.cancel();
			return true;
		}
		
		return false;
	}
	
	private void cancel(Item item, TaskContext<BukkitTask> task) {
		transformationTasksByItemUuid.remove(item.getUniqueId());
		task.cancel();
	}
	
	private @NullOr Entity entity(@NullOr UUID uuid) {
		return (uuid == null) ? null : plugin.getServer().getEntity(uuid);
	}
	
	private void transformConcretePowder(Item item) {
		class IterationCounter {
			int outside = 0;
			int inside = 0;
		}
		
		IterationCounter iterations = new IterationCounter();
		
		transformationTasksByItemUuid.put(
			item.getUniqueId(),
			plugin.sync().delay(2).ticks().every(2).ticks().run(task ->
			{
				Block cauldron = item.getLocation().getBlock();
				Material material = item.getItemStack().getType();
				
				// Outside the cauldron, dropping in ... (or not)
				if (cauldron.getType() != Material.WATER_CAULDRON) {
					iterations.outside++;
					
					// Took too long to drop in, might not even be a cauldron nearby for all we know
					if (iterations.outside > 20) {
						cancel(item, task);
					}
					
					return;
				}
				
				// Inside the cauldron
				iterations.inside++;
				boolean lowerWaterLevel = plugin.config().getOrDefault(Config.LOWER_WATER_LEVEL);
				
				// Check if player is allowed to use this specific cauldron
				// (only if water level gets lowered, since that could be considered griefing)
				if (lowerWaterLevel && entity(item.getThrower()) instanceof Player player) {
					if (!plugin.permissions().canAccessCauldron(player, cauldron)) {
						cancel(item, task);
						return;
					}
				}
				
				// TODO: more sophisticated merging
				// Attempt to merge item stacks
				if (item.getItemStack().getAmount() == 1) {
					for (Entity nearby : item.getNearbyEntities(0.5, 0.5, 0.5)) {
						if (!(nearby instanceof Item cooking)) {
							continue;
						}
						if (!transformationTasksByItemUuid.containsKey(cooking.getUniqueId())) {
							continue;
						}
						if (!Objects.equals(item.getThrower(), cooking.getThrower())) {
							continue;
						}
						
						ItemStack cooked = cooking.getItemStack();
						if (cooked.getAmount() >= 64 || cooked.getType() != material) {
							continue;
						}
						if (plugin.events().call(new ItemMergeEvent(item, cooking)).isCancelled()) {
							continue; // merge cancelled, try another neighbor
						}
						
						cooked.setAmount(cooked.getAmount() + 1);
						item.remove();
						
						cancel(item, task);
						return;
					}
				}
				
				if (iterations.inside == 1) {
					item.setPickupDelay(40);
					plugin.effects().cauldronSplashSound(item.getLocation());
				}
				
				 if (iterations.inside < 15) {
//				if (iterations.inside < 30) {
					plugin.effects().cauldronSplashParticles(cauldron);
					return;
				}
				
				// Done with this task... it's finally time to transform the powder!
				cancel(item, task);
				
				@NullOr Concrete concrete = Concrete.ofPowder(material).orElse(null);
				if (concrete == null) {
					return;
				}
				
				ItemStack stack = item.getItemStack();
				stack.setType(concrete.concrete());
				item.setItemStack(stack);
				
				item.setVelocity(new Vector(0, 0.3, 0));
				item.setPickupDelay(10);
				
				plugin.effects().concreteTransform(cauldron);
				counter.transformed(stack.getAmount());
				
				if (!lowerWaterLevel) {
					return;
				}
				if (!(cauldron.getBlockData() instanceof Levelled levelled)) {
					return;
				}
				
				int level = levelled.getLevel() - 1;
				
				if (level <= 0) {
					cauldron.setType(Material.CAULDRON);
				} else {
					levelled.setLevel(level);
					cauldron.setBlockData(levelled);
				}
			})
		);
	}
	
}
