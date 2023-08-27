/**
 * 
 */
package com.rs.game.npc.combat.impl.dragons.vorkath;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.slayer.Vorkath;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.DragonfireShield;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Jan 11, 2018
 */
public class VorkathCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {28061};
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		Vorkath vorkath = (Vorkath) npc;
		if (vorkath.hasZombifiedSpawn())
			return 0;
		if (vorkath.getAcidPools() != null) {
			WorldTile tile = new WorldTile(target);
			npc.setNextAnimation(new Animation(27952));
			World.sendProjectile(npc, tile, 6482, 38, 26, 50, 36, 16,192 + 90);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
						return;
					for (Entity t : npc.getPossibleTargets()) {
						if (t.hasWalkSteps() || Utils.getDistance(tile.getX(), tile.getY(), t.getX(), t.getY()) > 0)
							continue;
						delayHit(npc, 0, t, getRegularHit(npc, Utils.random(500)+1));
					}
					World.sendGraphics(npc, new Graphics(6436), tile);
				}
			}, 1);
			return 0;
		}
		if (vorkath.getMovesCount() >= 6) {
			if (vorkath.getFirstSpecial()) { //poison pool
				npc.setNextAnimation(new Animation(27960));
				List<Integer> tiles = new ArrayList<Integer>();
				int size = npc.getSize();
				for (int i = 0; i < 50;) {
					WorldTile tile = new WorldTile(npc);
					tile = tile.transform(Utils.random(18 + size) - 9, Utils.random(18 + size) - 9, 0);
					int id = tile.getTileHash();
					if (tiles.contains(id) || !World.isFloorFree(0, tile.getX(), tile.getY()))
						continue;
					tiles.add(id);
					i++;
					World.sendProjectile(npc, tile, 6483, 50, 26, 25, 36, 35, 0);
				}
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
							return;
						for (int tile : tiles) 
							World.spawnObjectTemporary(new WorldObject(132000, 10, 0, new WorldTile(tile)), 16000, true, false);
						vorkath.setAcidPools(tiles);
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								vorkath.setAcidPools(null);
							}
						}, 26);
					}
				}, 3);
				return defs.getAttackDelay();
			} else { //ice dragonfire
				npc.setNextAnimation(new Animation(27952));
				int msDelay = World.sendProjectile(npc, target, 395, 38, 26, 36, 36, 16, npc.getSize() * 32);
				target.setNextGraphics(new Graphics(369, msDelay / 10,  0));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
							return;
						target.addFreezeDelay(30000);
					}
					
				}, CombatScript.getDelay(msDelay));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
							return;
						WorldTile tile = null;
						int tries = 0;
						int size = npc.getSize();
						while (tile == null || tries++ < 50) {
							tile = new WorldTile(npc).transform(Utils.random(18) + size - 9, Utils.random(18) + size - 9, 0);
							if (World.isFloorFree(0, tile.getX(), tile.getY()) && Utils.getDistance(tile, target) >= 10)
								break;
						}
						World.sendProjectile(npc, tile, 6484, 50, 26, 40, 36, 45, 192 + 45);
						WorldTile finalTile = tile;
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
									return;
								NPC npc = World.spawnNPC(28063, finalTile, -1, true, true);
								npc.setForceMultiAttacked(true);
								npc.setIntelligentRouteFinder(true);
								npc.setTarget(target);
								vorkath.setZombifiedSpawn(npc);
							}
						}, 1);
					}
					
				}, 5);
				return 15;
			}
		}
		if (target.isFrozen())
			target.setFreezeDelay(0);
		vorkath.increaseMovesCount();
		switch (Utils.random(Utils.isOnRange(npc, target, 0) ? 7 : 6)) {
		case 6: //melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 320, NPCCombatDefinitions.MELEE, target)));
			break;
		case 5: //magic
			npc.setNextAnimation(new Animation(27952));
			int msDelay = World.sendProjectile(npc, target, 6479, 38, 26, 36, 36, 16, npc.getSize() * 32);
			int damage = getRandomMaxHit(npc, 320, NPCCombatDefinitions.MAGE, target);
			delayHitMS(npc, msDelay, target, getMagicHit(npc, damage));
			target.setNextGraphics(new Graphics(damage == 0 ? 85 : 6480, msDelay / 10,  100));
			break;
		case 4: ///range
			npc.setNextAnimation(new Animation(27952));
			msDelay = World.sendProjectile(npc, target, 6477, 38, 26, 36, 36, 16, npc.getSize() * 32);
			delayHitMS(npc, msDelay, target, getRangeHit(npc, getRandomMaxHit(npc, 320, NPCCombatDefinitions.RANGE, target)));
			target.setNextGraphics(new Graphics(6478, msDelay / 10,  100));
			break;
		case 3: //normal dragonfire
			npc.setNextAnimation(new Animation(27952));
			msDelay = World.sendProjectile(npc, target, 393, 38, 26, 36, 36, 16, npc.getSize() * 32);
			damage = 400 + Utils.random(550);
			Player player = target instanceof Player ? (Player) target : null;
			if (player != null) {
				boolean hasShield = Combat.hasAntiDragProtection(target);
				boolean hasPrayer = player.getPrayer().isMageProtecting();
				boolean hasPot = player.hasFireImmunity();
				if (hasPot) {
					damage = player.isSuperAntiFire() ? Utils.random(150) : Utils.random(400);
					player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
				}
				if (hasPrayer || hasShield) {
					if (damage > 400) {
						damage = Utils.random(400);
						player.getPackets().sendGameMessage("Your " + (hasShield ? "shield" : "prayer") + " absorbs most of the dragon's breath!", true);
					} else
						damage = Utils.random(200);
				} else if (!hasPot)
					player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
				DragonfireShield.chargeDFS(player, false);
			}
			int delay = delayHitMS(npc, msDelay, target, getRegularHit(npc, Utils.random(damage)));
			target.setNextGraphics(new Graphics(5157, msDelay / 10,  100));
			break;
		case 2: //venom dragonfire
			npc.setNextAnimation(new Animation(27952));
			msDelay = World.sendProjectile(npc, target, 6470, 38, 26, 36, 36, 16, npc.getSize() * 32);
			damage = 200 + Utils.random(550);
			player = target instanceof Player ? (Player) target : null;
			if (player != null) {
				boolean hasShield = Combat.hasAntiDragProtection(target);
				boolean hasPrayer = player.getPrayer().isMageProtecting();
				boolean hasPot = player.hasFireImmunity();
				if (hasPot) {
					damage = player.isSuperAntiFire() ? Utils.random(150) : Utils.random(200);
					player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
				}
				if (hasPrayer || hasShield) {
					if (damage > 200) {
						damage = Utils.random(200);
						player.getPackets().sendGameMessage("Your " + (hasShield ? "shield" : "prayer") + " absorbs most of the dragon's breath!", true);
					} else
						damage = Utils.random(100);
				} else if (!hasPot)
					player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
				DragonfireShield.chargeDFS(player, false);
			}
			delay = delayHitMS(npc, msDelay, target, getRegularHit(npc, Utils.random(damage)));
			target.setNextGraphics(new Graphics(6472, msDelay / 10,  100));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
						return;
					target.getPoison().makeEnvenomed(60);
				}
				
			}, delay);
			break;
		case 1: //pink dragonfire
			npc.setNextAnimation(new Animation(27952));
			msDelay = World.sendProjectile(npc, target, 6471, 38, 26, 36, 36, 16, npc.getSize() * 32);
			damage = 300 + Utils.random(550);
			player = target instanceof Player ? (Player) target : null;
			if (player != null) {
				boolean hasShield = Combat.hasAntiDragProtection(target);
				boolean hasPrayer = player.getPrayer().isMageProtecting();
				boolean hasPot = player.hasFireImmunity();
				if (hasPot) {
					damage = player.isSuperAntiFire() ? Utils.random(150) : Utils.random(300);
					player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
				}
				if (hasPrayer || hasShield) {
					if (damage > 300) {
						damage = Utils.random(300);
						player.getPackets().sendGameMessage("Your " + (hasShield ? "shield" : "prayer") + " absorbs most of the dragon's breath!", true);
					} else
						damage = Utils.random(150);
				} else if (!hasPot)
					player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
				DragonfireShield.chargeDFS(player, false);
			}
			delay = delayHitMS(npc, msDelay, target, getRegularHit(npc, Utils.random(damage)));
			target.setNextGraphics(new Graphics(6473, msDelay / 10,  100));
			if (player !=  null) {
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning() || !player.getPrayer().hasPrayersOn())
							return;
						player.getPackets().sendGameMessage("<col=FF0040>Your prayers have been disabled!");
						player.getPrayer().closeAllPrayers();
					}
				}, delay);
			}
			break;
		case 0: //high damage dragonfire
			npc.setNextAnimation(new Animation(27960));
			WorldTile tile = new WorldTile(target);
			msDelay = World.sendProjectile(npc, tile, 6481, 50, 26, 19, 36, 45, 192 + 45);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (vorkath.isDead() || vorkath.hasFinished() || !vorkath.isRunning())
						return;
					for (Entity t : npc.getPossibleTargets()) {
						if (t.hasWalkSteps() || Utils.getDistance(tile.getX(), tile.getY(), t.getX(), t.getY()) > 1)
							continue;
						delayHit(npc, 0, t, getRegularHit(npc, Utils.random(tile.getX() == t.getX() && tile.getY() == t.getY() ? 990 : 500)+1));
					}
					World.sendGraphics(npc, new Graphics(5157, 30 , 0), tile);
				}
			}, CombatScript.getDelay(msDelay)+1);
			break;
		}
		return defs.getAttackDelay();
	}

}
