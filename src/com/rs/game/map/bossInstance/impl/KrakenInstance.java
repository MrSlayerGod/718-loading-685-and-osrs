package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;

public class KrakenInstance extends BossInstance {


	public KrakenInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}


	@Override
	public int[] getMapPos() {
		return new int[] {280, 1248};
	}

	@Override
	public int[] getMapSize() {
		return new int[] {1, 1};
	}


	
	@Override
	public void loadMapInstance() {
		World.spawnNPC(20496, getTile(2278, 10034, 0), -1, true, false).setBossInstance(this);
	}




}
