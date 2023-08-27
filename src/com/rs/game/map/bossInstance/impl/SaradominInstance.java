package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zammorak.KrilTstsaroth;
import com.rs.game.player.Player;

public class SaradominInstance extends BossInstance {


	public SaradominInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}


	@Override
	public int[] getMapPos() {
		return new int[] {360, 654};
	}

	@Override
	public int[] getMapSize() {
		return new int[] {1, 1};
	}


	
	@Override
	public void loadMapInstance() {
		CommanderZilyana boss = (CommanderZilyana) World.spawnNPC(6247, getTile(2924, 5250, 0), -1, true, false).setBossInstance(this).setDifficultyMultiplier(getSettings().isHardMode() ? 2 : 0);
		boss.addMinion((GodWarMinion) World.spawnNPC(6248, getTile(2928, 5252, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6250, getTile(2928, 5252, 0), -1, true, false).setBossInstance(this));
		boss.addMinion((GodWarMinion) World.spawnNPC(6252, getTile(2926, 5250, 0), -1, true, false).setBossInstance(this));
		
	}




}
