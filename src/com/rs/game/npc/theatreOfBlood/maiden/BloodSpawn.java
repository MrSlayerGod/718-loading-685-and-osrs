package com.rs.game.npc.theatreOfBlood.maiden;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.maiden.actions.BloodSplat;
import com.rs.utils.Utils;

/**
 * 
 * @author cjay
 * Converted to onyx by dragonkk(alex)
 */
public class BloodSpawn extends NPC {

    public static final int BLOOD_SPAWN_ID = 28367;

    private Maiden maiden;

    public BloodSpawn(Maiden maid, WorldTile tile) {
    	super(BLOOD_SPAWN_ID, tile, -1, true, true);
    	this.maiden = maid;
    	setForceMultiArea(true);
    	setCantFollowUnderCombat(true);
    }

    @Override
    public void processNPC() {
        if (maiden == null || this.isDead()) 
            return;

        int MoveX = 0;
        int MoveY = 0;
        int Rnd = Utils.random(9);
        if (Rnd == 1) {
            MoveX = 1;
            MoveY = 1;
        } else if (Rnd == 2) {
            MoveX = -1;
        } else if (Rnd == 3) {
            MoveY = -1;
        } else if (Rnd == 4) {
            MoveX = 1;
        } else if (Rnd == 5) {
            MoveY = 1;
        } else if (Rnd == 6) {
            MoveX = -1;
            MoveY = -1;
        } else if (Rnd == 7) {
            MoveX = -1;
            MoveY = 1;
        } else if (Rnd == 8) {
            MoveX = 1;
            MoveY = -1;
        }
        this.resetWalkSteps();
        if (!isFrozen()) 
        	addWalkSteps(MoveX + getX(), MoveY + getY(), 1);
        if (!maiden.isDead() && !maiden.hasFinished()) 
            return;
        finish();
        maiden.clearBloodSpots();
    }

    @Override
    public void moveLocation(int x, int y, int z) {
    	super.moveLocation(x, y, z);
        if (isDead() || maiden == null || maiden.hasFinished()) 
            return;
        maiden.addSpot(new WorldTile(this), 8);
        World.sendGraphics(this, BloodSplat.SPLAT_HIT_GRAPHIC, new WorldTile(this));
    }
    
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
}
