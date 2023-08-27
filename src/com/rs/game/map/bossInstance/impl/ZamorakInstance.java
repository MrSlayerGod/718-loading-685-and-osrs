package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.zammorak.KrilTstsaroth;
import com.rs.game.player.Player;

public class ZamorakInstance extends BossInstance {


	public ZamorakInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}


	@Override
	public int[] getMapPos() {
		return new int[] {360, 664};
	}

	@Override
	public int[] getMapSize() {
		return new int[] {1, 1};
	}


	
	@Override
	public void loadMapInstance() {
		KrilTstsaroth boss = (KrilTstsaroth) World.spawnNPC(6203, getTile(2926, 5324, 0), -1, true, false).setBossInstance(this);
		boss.addMinion((GodWarMinion) World.spawnNPC(6204, getTile(2919, 5327, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6206, getTile(2930, 5326, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6208, getTile(2927, 5320, 0), -1, true, false).setBossInstance(this));
		BossInstance instance = boss.getBossInstance();
		if (instance != null && instance.getSettings().isHardMode())
			boss.setDifficultyMultiplier(1.5);
	}




}
