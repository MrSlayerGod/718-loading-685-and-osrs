package com.rs.game.player.controllers.bossInstance;

import com.rs.game.WorldObject;
import com.rs.game.map.bossInstance.BossInstance;

public class KrakenInstanceController extends BossInstanceController {

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 100538) {
			getInstance().leaveInstance(player, BossInstance.EXITED);
			removeControler();
			return false;
		}
		return true;
	}
	
}
