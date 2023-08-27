package com.rs.game.npc.cox.impl;

import com.rs.game.*;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTasksManager;

import java.util.Arrays;

/**
 * @author Simplex
 * @since Dec 07, 2020
 */
public class VespulaPortal extends COXBoss {
    Vespula vespula;

    public VespulaPortal(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber, Vespula vespula) {
        super(raid, id, tile, chamber);
        this.vespula = vespula;
        setCustomCombatScript(CombatScript.DO_NOTHING);
    }

    @Override
    public void onAttack(Player player) {
        vespula.attackedPortal(player);
    }

    @Override
    public void sendDeath(final Entity source) {
        getChamber().bossDeath();
        super.sendDeath(source);
    }
}
