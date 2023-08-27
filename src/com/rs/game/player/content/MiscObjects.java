package com.rs.game.player.content;

import com.rs.game.WorldTile;
import com.rs.game.player.content.prayer.Burying;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;

/**
 * @author Simplex
 * @since Oct 24, 2020
 */
public class MiscObjects {
    public static void init() {
        // barbarian village entrance to Stronghold of security
        ObjectHandler.register(120790, 1, ((player, obj) ->
                player.useStairs(827, new WorldTile(1860, 5244, 0), 1, 1, "You climb down the rocks..")));
    }
}
