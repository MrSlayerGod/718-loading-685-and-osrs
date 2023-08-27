package com.rs.game.minigames.stealingcreation;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.FloorItem;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class StealingCreationLobbyController extends Controller {

	@Override
	public void start() {
		sendInterfaces();
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(804, false);
		StealingCreationLobby.updateTeamInterface(player, false);
	}

	@Override
	public boolean canTakeItem(FloorItem item) {
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 39508 || object.getId() == 39509) {
			player.getControlerManager().forceStop();
			climOverStile(player, object, false);
			return false;
		}
		return true;
	}

	public static void climOverStile(final Player player, final WorldObject object, final boolean enterance) {
		player.lock(3);
		if (enterance) {
			if (!StealingCreationLobby.enterTeamLobby(player, object.getId() == 39509))
				return;
		} else
			StealingCreationLobby.removePlayer(player);
		player.setNextAnimation(new Animation(1560));
		final WorldTile toTile = new WorldTile(enterance ? object.getX() : object.getX() + 2, object.getY(), object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, enterance ? ForceMovement.WEST : ForceMovement.EAST));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(toTile);
			}
		}, 1);
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		StealingCreationLobby.removePlayer(player);
	}

	@Override
	public void forceClose() {
		player.getInterfaceManager().removeOverlay(false);
		StealingCreationLobby.removePlayer(player);
	}

	@Override
	public boolean logout() {
		StealingCreationLobby.removePlayer(player);
		player.setLocation(Helper.EXIT);
		return true;
	}

	@Override
	public boolean login() {
		StealingCreationLobby.removePlayer(player);
		player.setLocation(Helper.EXIT);
		return true;
	}

}
