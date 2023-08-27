package com.rs.game.player.controllers.pestcontrol;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.minigames.pest.Lander;
import com.rs.game.minigames.pest.PestControl;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class PestControlGame extends Controller {

	private PestControl control;
	private double points;

	@Override
	public void start() {
		control = (PestControl) getArguments()[0];
		setArguments(null);
		setPoints(0.0D);
		sendInterfaces();
		player.setForceMultiArea(true);
		playMusic();
	}

	private void playMusic() {
		player.getMusicsManager().playMusic(401); //fixed
	}

	@Override
	public void sendInterfaces() {
		updatePestPoints();
		player.getInterfaceManager().setOverlay(408, false);
	}

	private void updatePestPoints() {
		boolean isGreen = getPoints() > 750;
		player.getPackets().sendIComponentText(408, 11, (isGreen ? "<col=75AE49>" : "") + (int) getPoints() + "</col>");
	}

	@Override
	public void forceClose() {
		if (control != null) {
			if (control.getPortalCount() != 0) {
				if (control.getPlayers().contains(player))
					control.getPlayers().remove(player);
			}
			player.useStairs(-1, Lander.getLanders()[control.getPestData().ordinal()].getLanderRequierment().getExitTile(), 1, 2);
		} else
			player.useStairs(-1, new WorldTile(2657, 2639, 0), 1, 2);
		player.setForceMultiArea(false);
		player.getInterfaceManager().removeOverlay(false);
		player.reset();
	}

	@Override
	public void magicTeleported(int teleType) {
		player.getControlerManager().forceStop();
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave the pest control area like this.");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave the pest control area like this.");
		return false;
	}

	@Override
	public boolean canMove(int dir) {
		WorldTile toTile = new WorldTile(player.getX() + Utils.DIRECTION_DELTA_X[dir], player.getY() + Utils.DIRECTION_DELTA_Y[dir], player.getPlane());
		return !control.isBrawlerAt(toTile);
	}

	@Override
	public boolean login() { //shouldnt happen
		player.useStairs(-1, new WorldTile(2657, 2639, 0), 0, 2);
		return true;
	}

	@Override
	public boolean logout() {
		if (control != null) {
			control.getPlayers().remove(player);
			player.reset();
			player.setLocation(new WorldTile(2657, 2639, 0));
		}
		return true;
	}

	@Override
	public boolean canSummonFamiliar() {
		player.getPackets().sendGameMessage("You feel it's best to keep your Familiar away during this game.");
		return false;
	}

	@Override
	public boolean sendDeath() {
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
					player.reset();
					player.setNextWorldTile(control.getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))));
					player.setNextAnimation(new Animation(-1));
					playMusic();
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void processIncommingHit(Hit hit, Entity target) {
		if (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE) {
			int hit_damage = hit.getDamage();
			if (hit_damage > 0) {
				setPoints(getPoints() + hit.getDamage());
				updatePestPoints();
			}
		}
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		this.points = points;
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 14296) {
			player.useStairs(-1, player.transform(
					
					
					object.getRotation() == 0 ? 0 : object.getRotation() == 3 ? (object.getX() < player.getX() ?  -2 : 2) : object.getX() > player.getX() ?  2 : -2,
							object.getRotation() == 0 ? (object.getY() > player.getY() ?  2 : -2) : 0, 0), 0, 1);
			return false;
		}
		return true;
	}
}
