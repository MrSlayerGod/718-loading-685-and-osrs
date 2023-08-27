package com.rs.game.player.content;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class will contain the amount of kills for each npc by name
 */
public class NpcKillCountTracker implements Serializable {
    @Serial
    private static final long serialVersionUID = 7159348239790965883L;

    private Map<String, Integer> npcKillCounts;

    /**
     * Increments and returns the new value
     * @param name - the name of the npc
     * @param amount - the amount to increment by
     * @return
     */
    public int increment(String name, int amount) {
        name = name.toLowerCase();

        var foundValue = get(name);
        if (foundValue.isEmpty()) {
            set(name, amount);
            return amount;
        }

        var value = foundValue.get();
        value += amount;
        set(name, value);
        return value;
    }


    /**
     * Gets the kill count of a specific npc
     * @param name - the name of the npc, any case accepted
     * @return
     */
    public Optional<Integer> get(String name) {
        name = name.toLowerCase();
        if (npcKillCounts == null) npcKillCounts = new HashMap<>();
        return Optional.ofNullable(npcKillCounts.get(name));
    }



    public void set(String name, int amount) {
        name = name.toLowerCase();

        if (npcKillCounts == null) npcKillCounts = new HashMap<>();;
        this.npcKillCounts.put(name, amount);
    }

}
