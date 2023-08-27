package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class CombatDummy extends NPC {

	public CombatDummy(int id, WorldTile tile, boolean spawn) {
		super(id, tile, -1, true, spawn);
		setCantFollowUnderCombat(true);
		setCantSetTargetAutoRelatio(true);
		setCapDamage(2000);
	}

	
	@Override
	public void setTarget(Entity entity) {
		
	}
	
	@Override
	public void faceEntity(Entity entity) {
		
	}
}
