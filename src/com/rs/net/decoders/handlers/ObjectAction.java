package com.rs.net.decoders.handlers;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;

/**
 * @author Simplex
 * @since Jul 19, 2020
 */
public interface ObjectAction {

    void handle(Player player, WorldObject obj);
}
