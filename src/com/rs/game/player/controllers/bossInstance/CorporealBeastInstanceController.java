package com.rs.game.player.controllers.bossInstance;

import com.rs.game.WorldObject;
import com.rs.game.map.bossInstance.BossInstance;

public class CorporealBeastInstanceController extends BossInstanceController {

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 37929 || object.getId() == 38811) {
			getInstance().leaveInstance(player, BossInstance.EXITED);
			removeControler();
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(final WorldObject object) {
		if (object.getId() == 37929 || object.getId() == 38811) {
			player.getPackets().sendGameMessage("You can't peak when you are already inside the encounter!");
			return false;
		}
		return true;
	}
}
