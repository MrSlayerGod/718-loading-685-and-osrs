package com.rs.game.npc.others;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author Simplex
 * @since Sep 11, 2020
 */
public class MimicMinion extends NPC {


    public MimicMinion(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setLureDelay(0);
        setCapDamage(500);
        setCombatLevel(83);
        setRun(true);
        setForceMultiAttacked(true);
        setForceAgressive(true);
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        super.handleIngoingHit(hit);
    }

}

