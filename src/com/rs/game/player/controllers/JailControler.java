package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class JailControler extends Controller {

	private int nextCheck;
	private long time;

	@Override
	public void start() {
		time = (long) this.getArguments()[0];
	}

	@Override
	public boolean logout() {
		setArguments(new Object[]
		{ time });
		return false;
	}

	@Override
	public boolean login() {
		start();
		return false;
	}

	@Override
	public void process() {
		nextCheck++;
		if (nextCheck % 500 == 0) {
			if (time < Utils.currentTimeMillis()) {
				leaveJail(player, true);
				return;
			}
		}
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getDefinitions().name.equals("Ladder")) {
			player.getPackets().sendGameMessage("You cannot do any activities while being jailed.");
			return false;
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				player.stopAll();
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					player.reset();
				}
				loop++;
			}
		}, 0, 1);
		return true;
	}

	public static void leaveJail(Player player, boolean full) {
		if (full) {
			player.getPackets().sendGameMessage("You have served your time and may return back to "+Settings.SERVER_NAME+".");
		} else {
			player.getPackets().sendGameMessage("Your punishment has been removed early.");
		}
		player.setNextWorldTile(Settings.START_PLAYER_LOCATION);
		player.getControlerManager().removeControlerWithoutCheck();
	}
	
	public static void imprison(Player player, long time) {
		player.stopAll();
		player.lock(4);
		player.getControlerManager().startControler("JailControler", time);
	}
}
