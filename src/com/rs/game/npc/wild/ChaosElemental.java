package com.rs.game.npc.wild;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class ChaosElemental extends NPC {

	public ChaosElemental(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setIntelligentRouteFinder(true);
		setDropRateFactor(3);
	}

}
