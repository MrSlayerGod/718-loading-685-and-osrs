package com.rs.game.npc.wild;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class Galvek extends NPC {

	public Galvek(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setCapDamage(400);
		setIntelligentRouteFinder(true);
		setCantFollowUnderCombat(true);
		setForceMultiAttacked(true);
	}

}
