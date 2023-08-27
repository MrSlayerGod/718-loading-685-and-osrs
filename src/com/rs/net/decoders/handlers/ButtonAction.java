package com.rs.net.decoders.handlers;

import com.rs.game.player.Player;

/**
 * @author Simplex
 * @since Sep 15, 2020
 */
public interface ButtonAction {
    void handle(Player player, int slot1, int slot2, int action);
}
