package com.rs.game.npc.slayer;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;

@SuppressWarnings("serial")
public class ThermonuclearSmokeDevil extends NPC {

	public ThermonuclearSmokeDevil(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea,
			boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setDropRateFactor(2);
		setLureDelay(3000);
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 1;
	}
}
