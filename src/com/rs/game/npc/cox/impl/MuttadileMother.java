package com.rs.game.npc.cox.impl;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.MuttadileChamber;
import com.rs.utils.Direction;
import com.rs.utils.NPCDrops;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simplex
 * @since Nov 10, 2020
 */
public class MuttadileMother extends COXBoss {
    public MuttadileMother(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        setCantFollowUnderCombat(true);
        setDrops();
    }

    @Override
    public void setNextFaceEntity(Entity target) {
        if(!isLocked() && getId() != MuttadileChamber.MOTHER_SWIMMING_ID) {
            super.setNextFaceEntity(target);
        }
    }
    @Override
    public void faceEntity(Entity target) {
        if(!isLocked() && getId() != MuttadileChamber.MOTHER_SWIMMING_ID && target != null) {
            super.faceEntity(target);
        }
    }

    public void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        for(Drop drop : ALWAYS_DROPS) {
            dList[Drops.ALWAYS].add(drop);
        }
        drops.addDrops(dList);
        NPCDrops.addDrops(MuttadileChamber.CHILD_ID, drops);
        NPCDrops.addDrops(MuttadileChamber.MOTHER_ID, drops);
    }

    private static Drop[] ALWAYS_DROPS =
            {
                    new Drop(50897, 1, 1), 	// Houndmaster's diary
                    new Drop(50996, 1, 1),	// Overload (+)(4)
                    new Drop(50972, 1, 1),	// Prayer enhance (+)(4)
                    new Drop(50984, 1, 1),	// Xeric's aid (+)(4)
                    new Drop(50960, 1, 1),	// Revitalisation (+)(4)
            };
    @Override
    public void handleIngoingHit(Hit hit) {
        MuttadileChamber chamber = ((MuttadileChamber) getChamber());

        if(chamber.eatingTree && (chamber.getTree().isDead() || chamber.getTree().hasFinished())) {
            chamber.eatingTree = false;
        }

        if(getId() == MuttadileChamber.MOTHER_SWIMMING_ID || chamber.eatingTree)
            hit.setDamage(0);
        else
            super.handleIngoingHit(hit);
    }

    @Override
    public void processNPC() {
        if(getId() == MuttadileChamber.MOTHER_SWIMMING_ID) {
            setNextFaceEntity(null);
            setDirection(Direction.NORTH, true);
        }
        super.processNPC();
    }

    @Override
    public void sendDeath(Entity killer) {
        getChamber().bossDeath();
        super.sendDeath(killer);
    }

    @Override
    public boolean preAttackCheck(Player target) {

        if(!getChamber().isActivated() || target.getX() < raid.getMuttadileChamber().getEntranceTendrils()[0].getX()) {
            target.sendMessage("I can't reach that!");
            return false;
        }
        if(raid.getTile(target).getX() <= 64) {
            target.sendMessage("I can't reach that!" + raid.getTile(target).getX());
            return false;
        }
        if(getId() == 27561) {
            target.sendMessage("The Muttadile is underwater, your attacks can't reach it!");
            return false;
        }

        return true;
    }
}
