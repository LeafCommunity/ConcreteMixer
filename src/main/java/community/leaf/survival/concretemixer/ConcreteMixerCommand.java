/*
 * Copyright © 2022-2023, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer;

import community.leaf.survival.concretemixer.util.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConcreteMixerCommand implements TabExecutor
{
    private final ConcreteMixerPlugin plugin;
    
    public ConcreteMixerCommand(ConcreteMixerPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    private String prefix() { return "[" + plugin.getName() + "]"; }
    
    private void message(CommandSender sender, String message)
    {
        sender.sendMessage(Strings.colorful(message));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0) { return version(sender); }
        
        return switch (args[0].toLowerCase(Locale.ROOT))
        {
            case "version", "ver" -> version(sender);
            case "reload" -> reload(sender);
            default -> false;
        };
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1)
        {
            StringUtil.copyPartialMatches(args[0], List.of("version", "ver", "reload"), completions);
        }
        
        completions.sort(String.CASE_INSENSITIVE_ORDER);
        return completions;
    }
    
    private boolean version(CommandSender sender)
    {
        message(sender, prefix() + "&6 ConcreteMixer v" + plugin.version());
        message(sender, prefix() + "&6&o Throw concrete powder into a cauldron... get concrete!");
        return true;
    }
    
    private boolean reload(CommandSender sender)
    {
        if (!plugin.permissions().isAdmin(sender)) { return false; }
        
        plugin.config().reload();
        plugin.updates().reload();
        
        message(sender, prefix() + "&6 Reloaded.");
        return true;
    }
}
