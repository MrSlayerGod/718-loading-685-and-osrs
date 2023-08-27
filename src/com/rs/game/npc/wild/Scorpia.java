/**
 * 
 */
package com.rs.game.npc.wild;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
@SuppressWarnings("serial")
public class Scorpia extends NPC {

	private static final int ID = 26615, REBORN_ID = 26612, DOG_ID = 26613;
	
	private boolean summoned;
	private ScorpiaGuardian[] guardians = new ScorpiaGuardian[2];
	private int timer;
	
	public Scorpia(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setDropRateFactor(3);
		setIntelligentRouteFinder(true);
	}
	
	
	@Override
	public void processNPC() {
		if (isDead())
			return;
		summonGuardians();
		super.processNPC();
	}
	
	public void summonGuardians() {
		if (summoned || getHitpoints() >= getMaxHitpoints()/2) 
			return;
		summoned = true;
		int[][] dirs = Utils.getCoordOffsetsNear(2);
		int count = 0;
		for (int dir = 0; dir < dirs[0].length; dir++) {
			final WorldTile tile = new WorldTile(new WorldTile(getX() + dirs[0][dir], getY() + dirs[1][dir], getPlane()));
			if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 2)) {
				if (guardians[count] != null) 
					guardians[count].finish();
				guardians[count++] = new ScorpiaGuardian(this, tile, -1, true, true);
			}
			if (count == 2)
				break;
		}
	}
	
	public void resetGuardians() {
		summoned = false;
		for (int i = 0; i < guardians.length; i++) {
			if (guardians[i] != null) {
				guardians[i].finish();
				guardians[i] = null;
			}
		}
	}
	
	public void finish() {
		resetGuardians();
		super.finish();
	}
}
