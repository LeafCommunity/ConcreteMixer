/*
 * Copyright © 2022-2024, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import com.github.zafarkhaja.semver.Version;
import community.leaf.eventful.bukkit.BukkitEventSource;
import community.leaf.survival.concretemixer.hooks.HookHandler;
import community.leaf.survival.concretemixer.metrics.TransformationsPerHour;
import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.nio.file.Path;

public class ConcreteMixerPlugin extends JavaPlugin implements BukkitEventSource, BukkitTaskSource {
	private @NullOr Version version;
	private @NullOr Path directory;
	private @NullOr Config config;
	private @NullOr EffectHandler effects;
	private @NullOr HookHandler hooks;
	private @NullOr PermissionHandler permissions;
	private @NullOr UpdateChecker updates;
	
	@Override
	public void onEnable() {
		this.version = Version.valueOf(getDescription().getVersion());
		this.directory = getDataFolder().toPath();
		this.config = new Config(this);
		this.effects = new EffectHandler(config);
		this.hooks = new HookHandler(this);
		this.permissions = new PermissionHandler(this);
		this.updates = new UpdateChecker(this);
		
		TransformationsPerHour counter = new TransformationsPerHour(config);
		events().register(new CauldronPowderDropListener(this, counter));
		
		ConcreteMixerCommand command = new ConcreteMixerCommand(this);
		PluginCommand executor = initialized(getCommand("concretemixer"));
		executor.setExecutor(command);
		executor.setTabCompleter(command);
		
		if (config.getOrDefault(Config.METRICS)) {
			Metrics metrics = new Metrics(this, 15590);
			
			metrics.addCustomChart(new SingleLineChart(
				"transformations-per-hour",
				counter::totalTransformationsInTheLastHour
			));
		}
	}
	
	private static <T> T initialized(@NullOr T thing) {
		if (thing != null) {
			return thing;
		}
		throw new IllegalStateException("Not initialized.");
	}
	
	@Override
	public Plugin plugin() {
		return this;
	}
	
	public Version version() {
		return initialized(version);
	}
	
	public Path directory() {
		return initialized(directory);
	}
	
	public Config config() {
		return initialized(config);
	}
	
	public EffectHandler effects() {
		return initialized(effects);
	}
	
	public HookHandler hooks() {
		return initialized(hooks);
	}
	
	public PermissionHandler permissions() {
		return initialized(permissions);
	}
	
	public UpdateChecker updates() {
		return initialized(updates);
	}
}
