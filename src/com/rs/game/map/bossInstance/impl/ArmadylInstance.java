package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.player.Player;

public class ArmadylInstance extends BossInstance {


	public ArmadylInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}


	@Override
	public int[] getMapPos() {
		return new int[] {352, 656};
	}

	@Override
	public int[] getMapSize() {
		return new int[] {1, 1};
	}


	
	@Override
	public void loadMapInstance() {
		KreeArra boss = (KreeArra) World.spawnNPC(6222, getTile(2832, 5302, 0), -1, true, false).setBossInstance(this);
		boss.addMinion((GodWarMinion) World.spawnNPC(6223, getTile(2838, 5303, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6225, getTile(2828, 5299, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6227, getTile(2833, 5297, 0), -1, true, false).setBossInstance(this));
		BossInstance instance = boss.getBossInstance();
		if (instance != null && instance.getSettings().isHardMode())
			boss.setDifficultyMultiplier(1.5);
	}




}
