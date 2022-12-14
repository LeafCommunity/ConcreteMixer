/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.util;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.util.valuables.Adapter;
import community.leaf.configvalues.bukkit.YamlAccessor;

import java.util.Optional;

public class Versions
{
    private Versions() {}
    
    public static final Version ZERO = Version.forIntegers(0);
    
    public static final YamlAccessor<Version> YAML =
        YamlAccessor.of(Adapter.of(
            o -> {
                try { return Optional.of(Version.valueOf(String.valueOf(o))); }
                catch (RuntimeException ignored) { return Optional.empty(); }
            },
            version -> Optional.of(version.toString())
        ));
    
    public static Optional<Version> parse(String text)
    {
        try { return Optional.of(Version.valueOf(text)); }
        catch (RuntimeException ignored) { return Optional.empty(); }
    }
}
