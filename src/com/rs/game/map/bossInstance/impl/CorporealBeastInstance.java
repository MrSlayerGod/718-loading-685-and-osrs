package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;

public class CorporealBeastInstance extends BossInstance {


	public CorporealBeastInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}


	@Override
	public int[] getMapPos() {
		return new int[] {368, 544};
	}

	@Override
	public int[] getMapSize() {
		return new int[] {1, 1};
	}


	
	@Override
	public void loadMapInstance() {
		World.spawnNPC(8133, getTile(2993, 4378, 2), -1, true, false).setBossInstance(this);
	}




}
