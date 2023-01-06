/*
 * Copyright © 2022-2023, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Versions
{
    private Versions() {}
    
    public static final Version ZERO = Version.forIntegers(0);
    
    public static final YamlAccessor<Version> YAML =
        YamlAccessor.of(Adapter.of(
            o -> parse(String.valueOf(o)),
            version -> Optional.of(version.toString())
        ));
    
    public static Optional<Version> parse(String text)
    {
        try { return Optional.of(Version.valueOf(text)); }
        catch (RuntimeException ignored) { return parsePartial(text); }
    }
    
    private static final Pattern PARTIAL_VERSION =
        Pattern.compile("(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?)?");
    
    private static Optional<Version> parsePartial(String text)
    {
        Matcher matcher = PARTIAL_VERSION.matcher(text);
        if (!matcher.find()) { return Optional.empty(); }
        
        String version =
            Strings.orDefault(matcher.group("major"), "0") + "." +
            Strings.orDefault(matcher.group("minor"), "0") + "." +
            Strings.orDefault(matcher.group("patch"), "0");
        
        try { return Optional.of(Version.valueOf(version)); }
        catch (RuntimeException ignored) { return Optional.empty(); }
    }
}
