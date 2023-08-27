package com.rs.game.npc.cox.impl;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.VespulaChamber;

/**
 * @author Simplex
 * @since Dec 16, 2020
 */
public class VespulaGrub extends COXBoss {
    int tick = 0;
    int grubId;

    public VespulaGrub(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber, int grubId) {
        super(raid, id, tile, chamber);
        this.grubId = grubId;
    }

    @Override
    public int getMaxHitpoints() {
        return VespulaChamber.GRUB_MAX_HEALTH;
    }

    @Override
    public void sendDeath(Entity source) {
        anim(27467);
        setNextNPCTransformation(27537);
    }

    @Override
    public void processNPC() {
        if(!getRaid().getVespulaChamber().isActivated() || getId() == 27537) {
            // nobody in the chamber
            return;
        }

        if(tick++%4==0) {
            ((VespulaChamber) getChamber()).grubMetamorphisis(this);
        }
    }

    public int getGrubId() {
        return grubId;
    }
}
