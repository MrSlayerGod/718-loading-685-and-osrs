package com.rs.game.npc.cox.impl;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.MuttadileChamber;

/**
 * @author Simplex
 * @since Nov 10, 2020
 */
public class MuttadileChild extends COXBoss {
    MuttadileChamber chamber;

    public MuttadileChild(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);

        this.chamber = (MuttadileChamber) chamber;
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(!getChamber().isActivated() || attacker.getX() < raid.getMuttadileChamber().getEntranceTendrils()[0].getX()) {
            attacker.sendMessage("I can't reach that!");
            return false;
        }
        return super.preAttackCheck(attacker);
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        MuttadileChamber chamber = ((MuttadileChamber) getChamber());

        if(chamber.eatingTree && (chamber.getTree().isDead() || chamber.getTree().hasFinished())) {
            chamber.eatingTree = false;
        }

        if(chamber.eatingTree)
            hit.setDamage(0);
        else
            super.handleIngoingHit(hit);
    }


    @Override
    public void sendDeath(Entity killer) {
        chamber.activateMother();
        super.sendDeath(killer);
    }
}
