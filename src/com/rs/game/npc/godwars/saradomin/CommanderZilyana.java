package com.rs.game.npc.godwars.saradomin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.player.Player;
import com.rs.game.player.content.FriendsChat;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.GodWars;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class CommanderZilyana extends NPC {

	private List<GodWarMinion> minions;
	
	
	public CommanderZilyana(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		minions = new ArrayList<GodWarMinion>();
		setIntelligentRouteFinder(true);
		setForceFollowClose(true);
		setLureDelay(6000);//approximately 6 seconds lure
		setDropRateFactor(4); //triples chances
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
									godControler.incrementKillCount(2);
								}
							}
						}
					}
					drop();
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
		if (!this.isSpawned()) {
			long respawnDelay = getCombatDefinitions().getRespawnDelay() * 600;
			if(getBossInstance() != null) 
				respawnDelay /= getBossInstance().getSettings().getSpawnSpeed();
		final NPC npc = this;
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					if(getBossInstance() != null && getBossInstance().isFinished())
						return;
					setFinished(false);
					World.addNPC(npc);
					npc.setLastRegionId(0);
					World.updateEntityRegion(npc);
					loadMapRegions();
					checkMultiArea();
					respawnMinions();
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Error e) {
					e.printStackTrace();
				}
			}
		}, respawnDelay, TimeUnit.MILLISECONDS);
		}
	}
	
	public void addMinion(GodWarMinion npc) {
		if (minions == null)
			return;
		minions.add(npc);
	}
	
	public void respawnMinions() {
		for (GodWarMinion minion : minions) {
			if (minion != null && minion.hasFinished())
				minion.respawn();
		}
	}

}
