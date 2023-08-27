package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.player.Player;

public class BandosInstance extends BossInstance {


	public BandosInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}


	@Override
	public int[] getMapPos() {
		return new int[] {352, 664};
	}

	@Override
	public int[] getMapSize() {
		return new int[] {1, 1};
	}


	
	@Override
	public void loadMapInstance() {
		GeneralGraardor boss = (GeneralGraardor) World.spawnNPC(6260, getTile(2870, 5369, 0), -1, true, false).setBossInstance(this);
		boss.addMinion((GodWarMinion) World.spawnNPC(6261, getTile(2864, 5360, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6263, getTile(2872, 5353, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6265, getTile(2867, 5361, 0), -1, true, false).setBossInstance(this));
		BossInstance instance = boss.getBossInstance();
		if (instance != null && instance.getSettings().isHardMode())
			boss.setDifficultyMultiplier(1.5);
	}




}
