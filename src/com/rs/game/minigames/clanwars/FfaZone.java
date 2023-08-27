package com.rs.game.minigames.clanwars;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Arrays;

/**
 * Handles the FFA Clan Wars zone.
 * 
 * @author Emperor
 * 
 */
public final class FfaZone extends Controller {

	/**
	 * If the player was in the ffa pvp area.
	 */
	private transient boolean wasInArea;

	public static void enter(Player player) {
		if (true == true) {
			player.sendMessage("Spawn PK is currently disabled.");
			return;
		}
		if (player.getControlerManager().getControler() != null) {
			player.getPackets().sendGameMessage("You can't enter spawn pk from here!");
			return;
		}
		if (player.getPet() != null) {
			player.getPackets().sendGameMessage("You can't take your pet to spawn pk!");
			return;
		}
		if (player.getPet() != null || player.getFamiliar() != null) {
			player.getPackets().sendGameMessage("You can't take your familiar to spawn pk!");
			return;
		}
		//Magic.sendCommandTeleportSpell(player, new WorldTile(2990, 9682, 0));
		player.setNextWorldTile(new WorldTile(2815, 5511, 0));
		player.getControlerManager().startControler("clan_wars_ffa", false);
		player.anim(13494);
		player.gfx(2435);
	}
	@Override
	public void start() {
		sendInterfaces();
		player.getPackets().sendGameMessage("<col=551177> Welcome to the Spawn PK zone! Type (::spawn, ::itemn, ::item) to spawn any item you wish!");
		player.getPackets().sendGameMessage("<col=551177> You can also use presets to instantly load your items or (::copy name) to copy other players.");
		player.stopAll();
		player.getInventory().refresh();
		player.getEquipment().init();

		double[] xpArray = new double[player.getSkills().getXp().length];
		Arrays.fill(xpArray, Skills.getXPForLevel(99));
		player.getSkills().setTemporaryXP(xpArray);
		player.getPrayer().reset();
		player.setHitpoints(player.getMaxHitpoints());
	}

	public boolean isRisk() {
		if (getArguments() == null || getArguments().length < 1)
			return false;
		return (Boolean) getArguments()[0];
	}

	@Override
	public void sendInterfaces() {
		player.getInterfaceManager().setOverlay(789, true);
	}

	@Override
	public boolean sendDeath() {
		player.lock(8);
		player.stopAll();
		if (player.getFamiliar() != null)
			player.getFamiliar().sendDeath(player);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						killer.reduceDamage(player);
						killer.increaseKillCount(player);
						killer.getPackets().sendGameMessage(Wilderness.KILL_MESSAGES[Utils.random(Wilderness.KILL_MESSAGES.length)].replace("@name@", player.getDisplayName()));
						if (isRisk())
							player.sendItemsOnDeath(killer, true);
						killer.setAttackedByDelay(Utils.currentTimeMillis() + 8000); // imunity
						if (killer.getAttackedBy() == player)
							killer.setAttackedBy(null);
					}
					if (!isRisk()) {
						player.setNextWorldTile(new WorldTile(2814, 5511, 0));
					} else {
						player.setNextWorldTile(new WorldTile(2993, 9679, 0));
						remove(true);
					}
					player.reset();
					player.setNextAnimation(new Animation(-1));
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
	public void magicTeleported(int type) {
		remove(true);
	}

	@Override
	public void forceClose() {
		remove(false);
	}

	private void remove(boolean needRemove) {
		if (player.getPet() != null) //picks up pet, if no space, dissapears
			player.getPet().pickup(true);
		if (player.getFamiliar() != null)
			player.getFamiliar().dissmissFamiliar(false);

		if (needRemove)
			removeControler();
		if (wasInArea)
			player.setCanPvp(false);
		player.getInterfaceManager().removeOverlay(true);
		if (!isRisk())
			removeFreeItems();
	//	player.reset();
		player.heal(player.getMaxHitpoints());
		player.getPoison().reset();

		player.stopAll();
		player.getInventory().refresh();
		player.getEquipment().init();
		player.getSkills().setTemporaryXP(null);
		player.getPrayer().reset();
		player.setHitpoints(player.getMaxHitpoints());
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		switch (object.getId()) {
		case 38700:
			remove(true);
			player.useStairs(-1, new WorldTile(2993, 9679, 0), 0, 1);
			//player.getControlerManager().startControler("clan_wars_request");
			return false;
		}
		return true;
	}

	@Override
	public void moved() {
		boolean inArea = inPvpArea(player);
		if (inArea && !wasInArea) {
			player.setCanPvp(true);
			wasInArea = true;
			Wilderness.checkBoosts(player);
		} else if (!inArea && wasInArea) {
			player.setCanPvp(false);
			wasInArea = false;
		}
	}

	@Override
	public boolean canAttack(Entity target) {
		if (canHit(target))
			return true;
		return false;
	}

	@Override
	public boolean canHit(Entity target) {
		if (target instanceof NPC)
			return true;
		Player p2 = (Player) target;
	/*	if (player.isBeginningAccount() || p2.isBeginningAccount()) {
			player.getPackets().sendGameMessage("Starter acccounts cannot attack or be attacked for the first hour of playing time.");
			return false;
		}*/
		if (!(p2.getControlerManager().getControler() instanceof FfaZone)) {
			player.sendMessage("You can not attack in this zone!");
			return false;
		}
		return true;
	}


	@Override
	public boolean canPlayerOption4(Player target) {
		if (!(target.getControlerManager().getControler() instanceof FfaZone)) {
			player.sendMessage("You can not trade in this zone!");
			return false;
		}
		return true;
	}


	private boolean inPvpArea(Player player) {
		return player.getY() >= 5512;
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public boolean login() {
		start();
		moved();
		return false;
	}

	/**
	 * Checks if a player's overload effect is changed (due to being in the risk
	 * ffa zone, in pvp)
	 * 
	 * @param player
	 *            The player.
	 * @return {@code True} if so.
	 */
	public static boolean isOverloadChanged(Player player) {
		if (!(player.getControlerManager().getControler() instanceof FfaZone)) {
			return false;
		}
		return player.isCanPvp() && ((FfaZone) player.getControlerManager().getControler()).isRisk();
	}
	
	public void removeFreeItems() {
		player.getInventory().removeItems(new Item(25430, 28), new Item(25431, 28));
		player.getSkills().restoreSkills();
		player.setOverloadDelay(0);
	}
}