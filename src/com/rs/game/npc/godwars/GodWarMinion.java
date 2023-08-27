package com.rs.game.npc.godwars;

import java.util.Arrays;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.FriendsChat;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.GodWars;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class GodWarMinion extends NPC {

	private int type, ticks = 10;

	public GodWarMinion(int id, int type, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.type = type;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (getCombat().underCombat()) {
			if (ticks-- == 0) {
				ticks = 10;
				getCombat().removeTarget();
			}
		}
	}

	/*
	 * gotta override else setRespawnTask override doesnt work
	 */
	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					drop();
					if (source instanceof Player) {
						Player player = (Player) source;
						List<Player> players = FriendsChat.getLootSharingPeople(player);
						if (players == null)
							players = Arrays.asList(player);
						if (players != null) {
							for (Player p : players) {
								if (p == null) continue;
								Controller controler = p.getControlerManager().getControler();
								if (controler != null && controler instanceof GodWars) {
									GodWars godControler = (GodWars) controler;
									godControler.incrementKillCount(type);
								}
							}
						}
					}
					reset();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void setRespawnTask() {
		if(getBossInstance() != null && getBossInstance().isFinished())
			return;
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
	}

	public void respawn() {
		if(getBossInstance() != null && getBossInstance().isFinished())
			return;
		setFinished(false);
		World.addNPC(this);
		setLastRegionId(0);
		World.updateEntityRegion(this);
		loadMapRegions();
		checkMultiArea();
	}

}
