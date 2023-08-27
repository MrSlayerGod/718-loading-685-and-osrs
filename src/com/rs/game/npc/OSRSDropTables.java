package com.rs.game.npc;

import com.rs.game.player.Player;
import com.rs.game.player.SuperiorSlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * OSRS-style drop table system
 *
 * @author Simplex
 * @since Sep 24, 2020
 */
public class OSRSDropTables {

    public static HashMap<Integer, BiConsumer<Player, NPC>> noc_drop_tables = new HashMap<>();

    public static void init() {
        Arrays.stream(SuperiorSlayer.SUPERIOR_CREATURES)
                .forEach(id -> noc_drop_tables.put(id, SuperiorSlayer::dropItems));
    }

    public static boolean dropItems(Player killer, NPC dropper) {
        BiConsumer<Player, NPC> dropEvent = noc_drop_tables.get(dropper.getId());

        if(dropEvent == null) {
            return false;
        }

        dropEvent.accept(killer, dropper);
        return true;
    }
}
