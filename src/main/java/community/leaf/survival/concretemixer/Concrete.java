/*
 * Copyright © 2022-2024, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public record Concrete(DyeColor color, Material powder, Material concrete) {
	private static final Map<DyeColor, Concrete> CONCRETE_BY_COLOR = new EnumMap<>(DyeColor.class);
	private static final Map<Material, Concrete> CONCRETE_BY_POWDER = new EnumMap<>(Material.class);
	
	static {
		Arrays.stream(DyeColor.values())
			.map(color -> new Concrete(
				color,
				Material.valueOf(color.name() + "_CONCRETE_POWDER"),
				Material.valueOf(color.name() + "_CONCRETE")
			))
			.forEach(concrete -> {
				CONCRETE_BY_COLOR.put(concrete.color(), concrete);
				CONCRETE_BY_POWDER.put(concrete.powder(), concrete);
			});
	}
	
	public static Concrete ofColor(DyeColor color) {
		return CONCRETE_BY_COLOR.get(color);
	}
	
	public static Optional<Concrete> ofPowder(Material maybePowder) {
		return Optional.ofNullable(CONCRETE_BY_POWDER.get(maybePowder));
	}
}
