package com.rs.net.decoders.handlers;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

/**
 * @author Simplex
 * @since Jul 19, 2020
 */
public interface ItemAction {
    void handle(Player player, Item item);
}
