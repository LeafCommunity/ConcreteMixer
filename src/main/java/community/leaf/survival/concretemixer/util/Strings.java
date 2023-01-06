/*
 * Copyright © 2022-2023, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.util;

import net.md_5.bungee.api.ChatColor;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings
{
    private Strings() {}
    
    private static final Pattern HASH_HEX_COLOR_PATTERN = Pattern.compile("(?i)&x?#(?<hex>[a-f0-9]{6})");
    
    public static String colorful(@NullOr String text)
    {
        if (isEmptyOrNull(text)) { return ""; }
        
        Matcher matcher = HASH_HEX_COLOR_PATTERN.matcher(text);
        @NullOr Set<String> replaced = null;
        
        while (matcher.find())
        {
            if (replaced == null) { replaced = new HashSet<>(); }
            
            String match = matcher.group();
            if (replaced.contains(match)) { continue; }
            
            StringBuilder bungeeHexFormat = new StringBuilder("&x");
            
            for (char c : matcher.group("hex").toCharArray())
            {
                bungeeHexFormat.append('&').append(c);
            }
            
            text = text.replace(match, bungeeHexFormat.toString());
            replaced.add(match);
        }
        
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static boolean isEmptyOrNull(@NullOr String text) { return text == null || text.isEmpty(); }
    
    public static String orDefault(@NullOr String text, String def) { return (text == null) ? def : text; }
    
    public static String orEmpty(@NullOr String text) { return orDefault(text, ""); }
}
