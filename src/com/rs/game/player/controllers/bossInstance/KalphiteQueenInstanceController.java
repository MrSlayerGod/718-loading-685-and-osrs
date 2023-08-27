package com.rs.game.player.controllers.bossInstance;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;


public class KalphiteQueenInstanceController extends BossInstanceController {

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (object.getId() == 3832) { //kq
			player.lock();
			player.setNextAnimation(new Animation(828));
			WorldTasksManager.schedule(new WorldTask() { //to remove at same time it teleports
				@Override
				public void run() {
					getInstance().leaveInstance(player, BossInstance.EXITED);
					removeControler();
				}
			});
			return false;
		}
		return true;
	}
	
}


