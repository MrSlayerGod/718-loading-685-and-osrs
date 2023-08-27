package com.rs.game.npc.theatreOfBlood.maiden;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * 
 * @author cjay
 * Converted to onyx by dragonkk(alex)
 */
public class NylocasMatomenos extends NPC {

    public static final int NYLOCAS_MATOMENOS_ID = 28366;

    public NylocasMatomenos(Maiden maiden, WorldTile tile) {
       	super(NYLOCAS_MATOMENOS_ID, tile, -1, true, true);
     	this.maiden = maiden;
     	setForceMultiArea(true);
     	setCantFollowUnderCombat(true);
     	addFreezeDelay(1800); //1.8sec at begin
     	setNextAnimation(new Animation(28098));
    }

    private Maiden maiden;

    @Override
    public void processNPC() {
        if (maiden == null || this.isDead()) {
            return;
        } else if (maiden.isDead() || maiden.hasFinished()) {
            finish();
            return;
        } else if (!this.isWithinDistanceIgnoreHeight(maiden.getMiddleWorldTile(), 4)) {
        	resetWalkSteps();
        	if (!isFrozen())
        		addWalkSteps(maiden.getX(), maiden.getY(), 1, false);
            return;
        }

        maiden.heal(getHitpoints());
        maiden.incrementMinimumTornadoDamage();
        setHitpoints(0);
        sendDeath(this);
    }
    
    public boolean isWithinDistanceIgnoreHeight(WorldTile other, int distance) {
        final int deltaX = Math.abs(getX() - other.getX()), deltaY = Math.abs(getY() - other.getY());
        return deltaX <= distance && deltaY <= distance;
    }
    
	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}

}
