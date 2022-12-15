/*
 * Copyright © 2022, RezzedUp and Contributors <https://github.com/LeafCommunity/ConcreteMixer>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package community.leaf.survival.concretemixer.metrics;

import community.leaf.survival.concretemixer.Config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransformationsPerHour
{
    private final Map<Instant, Integer> countersByMinute = new LinkedHashMap<>();
    
    private final Config config;
    
    public TransformationsPerHour(Config config)
    {
        this.config = config;
    }
    
    public void transformed(int totalPowderToConcrete)
    {
        if (totalPowderToConcrete < 0) { throw new IllegalArgumentException(); }
        if (!config.getOrDefault(Config.METRICS)) { return; }
        
        Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        int counter = countersByMinute.computeIfAbsent(minute, k -> 0);
        countersByMinute.put(minute, counter + totalPowderToConcrete);
    }
    
    public int totalTransformationsInTheLastHour()
    {
        int total = 0;
        Instant now = Instant.now();
        Iterator<Map.Entry<Instant, Integer>> it = countersByMinute.entrySet().iterator();
        
        while (it.hasNext())
        {
            Map.Entry<Instant, Integer> entry = it.next();
            
            if (ChronoUnit.MINUTES.between(entry.getKey(), now) >= 60) { it.remove(); }
            else { total += entry.getValue(); }
        }
        
        return total;
    }
}
