package com.rs.net.decoders.handlers;

import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

/**
 * @author Simplex
 * @since Sep 15, 2020
 */
public interface NPCAction {
    void handle(Player player, NPC npc);
}
