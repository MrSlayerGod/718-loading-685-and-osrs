package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.minigames.ZarosGodwars;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class ZGDControler extends Controller {

	@Override
	public void start() {
		ZarosGodwars.addPlayer(player);
		sendInterfaces();
	}

	@Override
	public boolean logout() {
		ZarosGodwars.removePlayer(player);
		return false; // so doesnt remove script
	}

	@Override
	public boolean login() {
		ZarosGodwars.addPlayer(player);
		sendInterfaces();
		return false; // so doesnt remove script
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(601, true);
	}

	@Override
	public boolean sendDeath() {
		/*remove();
		removeControler();*/
		if (true) {
			player.lock(8);
			player.stopAll();

			WorldTasksManager.schedule(new WorldTask() {
				int loop;

				@Override
				public void run() {
					if (loop == 0) {
						player.setNextAnimation(new Animation(836));
					} else if (loop == 1) {
						player.getPackets().sendGameMessage("Oh dear, you have died.");
					} else if (loop == 3) {
						player.getControlerManager().forceStop();
						player.getControlerManager().startControler("DeathEvent", new WorldTile(2908, 3707, 0)/*CHAMBER_TELEPORTS[(sector * 2) + 1]*/, player.hasSkull());
					} else if (loop == 4) {
						player.getPackets().sendMusicEffect(90);
						stop();
					}
					loop++;
				}
			}, 0, 1);
			return false;
		}
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		remove();
		removeControler();
	}

	@Override
	public void forceClose() {
		remove();
	}

	public void remove() {
		ZarosGodwars.removePlayer(player);
		player.getPackets().sendCSVarInteger(1435, 255);
		player.getInterfaceManager().removeOverlay(true);
	}
}
