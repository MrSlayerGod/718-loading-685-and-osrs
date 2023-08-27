package com.rs.game.map.bossInstance.impl;

import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;

public class KingBlackDragonInstance extends BossInstance {

	public KingBlackDragonInstance(Player owner, InstanceSettings settings) {
		super(owner, settings);
	}

	@Override
	public int[] getMapPos() {
		return new int[] { 281, 584 };
	}

	@Override
	public int[] getMapSize() {
		return new int[] { 1, 1 };
	}

	@Override
	public void enterInstance(Player player, boolean login) {
		if (!login)
			player.getPackets().sendGameMessage("and teleport into the lair of the King Black Dragon!", true);
		super.enterInstance(player, login);
	}

	@Override
	public void loadMapInstance() {
		World.spawnNPC(50, getTile(2270, 4701, 0), -1, true, false).setBossInstance(this).setDifficultyMultiplier(getSettings().isHardMode() ? 2 : 0);
	}
}
