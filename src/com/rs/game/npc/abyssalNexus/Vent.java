/**
 * 
 */
package com.rs.game.npc.abyssalNexus;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

/**
 * @author dragonkk(Alex)
 * Nov 9, 2017
 */
@SuppressWarnings("serial")
public class Vent extends NPC {

	private static final int ID = 25915, RESPIRATORY_ID = 25914;
	
	public Vent(WorldTile tile) {
		super(ID, tile, -1, true, true);
		setCantFollowUnderCombat(true);
		setCantSetTargetAutoRelatio(true);
	}

	public void setAwaken() {
		this.setNextNPCTransformation(RESPIRATORY_ID);
	}
	
	public void setSleeping() {
		setNextNPCTransformation(ID);
	}
	
	@Override
	public void finish() {
		setNPC(ID);
		super.finish();
	}
	
}
