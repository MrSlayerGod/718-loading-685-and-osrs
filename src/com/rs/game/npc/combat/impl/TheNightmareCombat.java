package com.rs.game.npc.combat.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.nightmare.Husk;
import com.rs.game.npc.nightmare.Parasite;
import com.rs.game.npc.nightmare.TheNightmare;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.TheNightmareInstance;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class TheNightmareCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "The Nightmare" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions cf = npc.getCombatDefinitions();
		
		
		TheNightmare boss = (TheNightmare) npc;
		if (boss.isShadowSpecialReady()) {
			boss.resetShadowSpecial();
			boss.resetWalkSteps();
			boss.setCantFollowUnderCombat(true); //cant w during special
			List<Integer> tiles = new LinkedList<Integer>();
			
			List<Entity> players = World.getNearbyPlayers(npc, false);
			
			for (int x = 3862; x < 3882; x++) {
				skip: for (int y = 9941; y < 9961; y++) {
					if (World.isFloorFree(boss.getPlane(), x, y) && Utils.random(10) == 0
							&& !Utils.colides(x, y,1, npc.getX(), npc.getY(), npc.getSize())) {
						
						for (Entity target2 : players) 
							if (target2.getX() == x && target2.getY() == y && target2.isFrozen())
								continue skip;
						
						WorldTile tile = new WorldTile(x, y, boss.getPlane());
						World.sendGraphics(boss, new Graphics(6767, 0, 70), tile);
						tiles.add(tile.getTileHash());
					}
				}
			}
			
			for (Entity target2 : players) {
				if (!target2.isFrozen() && Utils.random(5) == 0 && !Utils.collides(target2, boss)
						&& !tiles.contains(target2.getTileHash())) {
					WorldTile tile = new WorldTile(target2.getX(), target2.getY(), boss.getPlane());
					World.sendGraphics(boss, new Graphics(6767, 0, 70), tile);
					tiles.add(tile.getTileHash());
				}
			}
			
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished() || !boss.isAwaken())
						return;
					for (Entity target2 : World.getNearbyPlayers(npc, false)) 
						if (Utils.collides(npc, target2) || tiles.contains(target2.getTileHash())) 
							delayHit(npc, -1, target2, getRegularHit(npc, Utils.random(199, 500)+1));
					boss.setCantFollowUnderCombat(false);
				}
			}, 4);
			boss.anim(28598);
			return cf.getAttackDelay() + 5;
		}
		if (boss.isPhaseSpecialReady()) {
			boss.resetPhaseSpecial();
			switch (boss.getPhase()) { //boss.getPhase()
			case 2:
				if (boss.isFirstSpecial()) { //no escape
					WorldTile min = null;
					WorldTile max = null;
					List<Entity> players = World.getNearbyPlayers(npc, false);
					find:for (int i = 0; i < 1000; i++) {
						switch (Utils.random(2)) {
						case 0://up
							min = new WorldTile(3866 + Utils.random(9), 9942, 3);
							max = new WorldTile(min.getX(), 9956, 3);
							break;
						case 1://left
							min = new WorldTile(3863, 9945  + Utils.random(9) , 3);
							max = new WorldTile(3877, min.getY(), 3);
							break;
						}
						for (int x = min.getX(); x <= max.getX(); x++) {
							for (int y = min.getY(); y <= max.getY(); y++) {
								for (Entity target2 : players) 
									if (Utils.colides(target2.getX(), target2.getY(), 1, x, y, boss.getSize()))
										break find;
							}
						}
					}
					
					boolean reverse = Utils.random(2) == 0;
					WorldTile from = reverse ? max : min;
					WorldTile to = reverse ? min : max;

					boss.anim(28607);
					boss.setCantFollowUnderCombat(true);
					boss.setCantInteract(true);
					boss.setNextFaceEntity(null);//stop facing tank
					
					WorldTile minF = min;
					WorldTile maxF = max;
					
					WorldTasksManager.schedule(new WorldTask() {

						boolean special = false;
						
						@Override
						public void run() {
							if (npc.isDead() || npc.hasFinished() || !boss.isAwaken()) {
								stop();
								return;
							}
							if (special) {
								stop();
								boss.anim(28597);
								npc.setNextForceMovement(new NewForceMovement(new WorldTile(from), 0, to, 1, Utils.getAngle(to.getX() - from.getX(), to.getY() - from.getY())));
								
								WorldTasksManager.schedule(new WorldTask() {

									boolean finish = false;
									
									@Override
									public void run() {
										if (npc.isDead() || npc.hasFinished() || !boss.isAwaken()) {
											stop();
											return;
										}
										if (finish) {
											boss.setCantFollowUnderCombat(false);
											boss.setCantInteract(false);
											stop();
										} else {
											finish = true;
											boss.setNextWorldTile(to);
											List<Entity> players = World.getNearbyPlayers(npc, false);
											
											for (int x = minF.getX(); x <= maxF.getX(); x++) {
												for (int y = minF.getY(); y <= maxF.getY(); y++) {
													for (Entity target2 : players.toArray(new Entity[players.size()])) {
														if (Utils.colides(target2.getX(), target2.getY(), 1, x, y, boss.getSize())) {
															delayHit(npc, -1, target2, getRegularHit(npc, Utils.random(300, 600)+1));
															((Player)target2).getPackets().sendGameMessage("<col=D80000>The Nightmare surges across the room, damaging you as she does!");
															players.remove(target2);
														}
													}
												}
											}
										}
									}
								}, 0, 1);
							} else {
								boss.setNextWorldTile(from);
								boss.anim(28609);
								boss.setNextFaceWorldTile(to);
								special = true;
							}
						}
						
					}, 1, 3);
				return cf.getAttackDelay();
				} else {
					boss.anim(28599);
					boss.setCantFollowUnderCombat(true);
					
					for (WorldTile tile : TheNightmare.SPORES) 
						World.spawnObject(new WorldObject(137738, 10, 0, tile));
					
					WorldTasksManager.schedule(new WorldTask() {

						int cycle;
						
						@Override
						public void run() {
							if (npc.isDead() || npc.hasFinished() || !boss.isAwaken()) {
								stop();
								for (WorldTile tile : TheNightmare.SPORES)
									World.removeObject(World.getObjectWithType(tile, 10));
								return;
							}
							cycle++;
							if (cycle == 1) {
								TheNightmareInstance.sendMessage("<col=D80000>The Nightmare summons some infectious spores!");
								boss.setCantFollowUnderCombat(false);
								for (WorldTile tile : TheNightmare.SPORES)
									World.sendObjectAnimation(boss, World.getObjectWithType(tile, 10), new Animation(28630));
							} else if (cycle == 2) {
								for (WorldTile tile : TheNightmare.SPORES) 
									World.spawnObject(new WorldObject(137739, 10, 0, tile));
							} else if (cycle == 8) {

								List<Entity> players = World.getNearbyPlayers(npc, false);
								List<Entity> infected = new LinkedList<Entity>();
								
								for (WorldTile tile : TheNightmare.SPORES) {
									
									
									World.sendObjectAnimation(boss, World.getObjectWithType(tile, 10), new Animation(28632));
								
									for (Entity target2 : players) {
										if (target2.withinDistance(tile, 1) && !infected.contains(target2)) {
											Player player = (Player) target2;
											infected.add(target2);
											player.getPackets().sendGameMessage("<col=D80000>You have been infected!");
											player.setRun(false);
											player.getTemporaryAttributtes().put(Key.SPORE_INFECTED, Boolean.TRUE);
										}
									}
								}
								
								WorldTasksManager.schedule(new WorldTask() {

									int cycle = 0;
									@Override
									public void run() {
										cycle++;
										if (npc.isDead() || npc.hasFinished() || !boss.isAwaken() || cycle >= 33) {
											stop();
											for (Entity target2 : infected) {
												if (target2.isDead())
													continue;
												Player infected = (Player) target2;
												infected.getTemporaryAttributtes().remove(Key.SPORE_INFECTED);
												infected.getPackets().sendGameMessage("<col=00FF00>The Nightmare's infection has worn off.");
											}
											return;
										}
										a:for (Entity entity : World.getNearbyPlayers(npc, false)) {
											if (!infected.contains(entity)) {
												for (Entity target2 : infected) {
													if (target2.withinDistance(entity, 1)) {
														infected.add(entity);
														Player infected = (Player) entity;
														infected.getTemporaryAttributtes().remove(Key.SPORE_INFECTED);
														infected.getPackets().sendGameMessage("<col=D80000>The Nightmare's infection has been passed on to you, making you feel drowsy!");
														((Player) target2).getPackets().sendGameMessage("<col=D80000>You have passed on the Nightmare's infection.");
														continue a;
													}
												}
											}
										}
										
									}
								}, 0, 0);//3
							} else if (cycle >= 10) {
								stop();
								for (WorldTile tile : TheNightmare.SPORES)
									World.removeObject(World.getObjectWithType(tile, 10));
							}
							
						}
					}, 2, 0);
					
					return cf.getAttackDelay();
				}
			case 1:
				if (boss.isFirstSpecial()) { //curse
					boss.anim(28599);
					List<Entity> players = World.getNearbyPlayers(npc, false);
					for (Entity target2 : players) {
						Player player = (Player) target2;
						player.getPackets().sendGameMessage("<col=D80000>The Nightmare has cursed you, shuffling your prayers!");
						player.getTemporaryAttributtes().put(Key.SHUFFLE_PRAYERS, Boolean.TRUE);
						player.getAppearence().generateAppearenceData();
						player.getInterfaceManager().setFadingInterface(170);
						GameExecutorManager.fastExecutor.schedule(new TimerTask() {
							@Override
							public void run() {
								try {
									player.getInterfaceManager().closeFadingInterface();
								} catch (Throwable e) {
									Logger.handle(e);
								}
							}
						}, 2000);
					}
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (npc.isDead() || npc.hasFinished())
								return;
							for (Entity target2 : players) {
								if (target2.isDead())
									continue;
								Player player = (Player) target2;
								player.getTemporaryAttributtes().remove(Key.SHUFFLE_PRAYERS);
								player.getAppearence().generateAppearenceData();
								player.getPackets().sendGameMessage("<col=00FF00>You feel the effects of the Nightmare's curse wear off.");
							}
						}
					}, 33);
					return cf.getAttackDelay();
				} else { //parasite
					boss.anim(28601);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (npc.isDead() || npc.hasFinished() || !boss.isAwaken() || npc.getId() == TheNightmare.NIGHTMARE_BOSS_SLEEPWALKER)
								return;
							List<Entity> players = World.getNearbyPlayers(npc, false);
							Entity forceT = players.isEmpty() ? null : players.get(Utils.random(players.size()));
							for (Entity target2 : players) {
								if (target2 == forceT || Utils.random(3) == 0) {
									int msDelay = World.sendProjectile(npc, target2, 6770, 90, 36, 50, 0, 5, 64);
									
									WorldTasksManager.schedule(new WorldTask() {

										boolean spawn;
										
										@Override
										public void run() {
											if (target2.isDead() || !target2.withinDistance(boss) || npc.isDead() || npc.hasFinished() || !boss.isAwaken() || npc.getId() == TheNightmare.NIGHTMARE_BOSS_SLEEPWALKER) {
												stop();
												return;
											}

											Player player = (Player) target2;
											if (!spawn) {
												spawn = true;
												player.getPackets().sendGameMessage("<col=D80000>The Nightmare has impregnated you with a deadly parasite!");
												player.getTemporaryAttributtes().put(Key.BIG_PARATISE, Boolean.TRUE);
											} else {
												stop();
												boolean big = player.getTemporaryAttributtes().remove(Key.BIG_PARATISE) != null;
												WorldTile tile = player;
												l: for (int i = 0; i < 1000; i++) {
													WorldTile tile2 = target2.transform(-1 + Utils.random(3), -1 + Utils.random(3), 0);
													if (!tile2.matches(target2) && World.isFloorFree(tile2.getPlane(), tile2.getX(), tile2.getY())
															&&	!Utils.collides(tile2.getX(), tile2.getY(), 1, boss.getX(), boss.getY(), boss.getSize())
															&& (tile2.getX() == target2.getX() || tile2.getY() == target2.getY())) {
														tile = tile2;
														break l;
													}
												}
												Parasite parasite = new Parasite(big, boss, tile);
												player.getPackets().sendGameMessage("<col=D80000>The parasite bursts out of you, fully grown!");
												delayHit(parasite, -1, target2, new Hit(parasite, big ? 100 : 50, HitLook.REGULAR_DAMAGE));
											}
											
											
										}
									}, getDelay(msDelay),25);
									
									
								}
							}
						}
					}, 1);
					return cf.getAttackDelay();
				}
			case 0:
				if (boss.hasHusks())
					break;
				if (boss.isFirstSpecial()) {
					boss.anim(28599);
					World.sendProjectile(npc, target, 6781, 90, 36, 50, 90, 5, 64);
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							if (npc.isDead() || npc.hasFinished() || !boss.isAwaken() || npc.getId() == TheNightmare.NIGHTMARE_BOSS_SLEEPWALKER)
								return;
							List<Entity> players = World.getNearbyPlayers(npc, false);
							Entity forceT = players.isEmpty() ? null : players.get(Utils.random(players.size()));
							for (Entity target2 : players) {
								if (target2 == forceT || Utils.random(3) == 0) {
									WorldTile tile = null;
									l: for (int i = 0; i < 1000; i++) {
										WorldTile tile2 = target2.transform(-1 + Utils.random(3), -1 + Utils.random(3), 0);
										if (!tile2.matches(target2) && World.isFloorFree(tile2.getPlane(), tile2.getX(), tile2.getY())
												&&	!Utils.collides(tile2.getX(), tile2.getY(), 1, boss.getX(), boss.getY(), boss.getSize())
												&& (tile2.getX() == target2.getX() || tile2.getY() == target2.getY())) {
											tile = tile2;
											break l;
										}
									}
									if (tile != null) {
										new Husk(target2, 29454, tile);
										((Player)target2).getPackets().sendGameMessage("<col=D80000>The Nightmare puts you in a strange trance, preventing you from moving!");
									
										WorldTile tile3 = null;
										l: for (int i = 0; i < 1000; i++) {
											WorldTile tile2 = target2.transform(-1 + Utils.random(3), -1 + Utils.random(3), 0);
											if (!tile2.matches(target2) && !tile2.matches(tile) && World.isFloorFree(tile2.getPlane(), tile2.getX(), tile2.getY())
												&&	!Utils.collides(tile2.getX(), tile2.getY(), 1, boss.getX(), boss.getY(), boss.getSize())
												&& (tile2.getX() == target2.getX() || tile2.getY() == target2.getY())) {
												tile3 = tile2;
												break l;
											}
										}
										if (tile3 != null) 
											new Husk(target2, 29455, tile3);
									
									}
									
								}
							}
						}
					}, 3);
					return cf.getAttackDelay();
				} else {
					boss.anim(28607);
					boss.setCantFollowUnderCombat(true);
					WorldTasksManager.schedule(new WorldTask() {

						boolean special = false;
						
						@Override
						public void run() {
							if (npc.isDead() || npc.hasFinished() || !boss.isAwaken()) {
								stop();
								return;
							}
							if (special) {
								stop();
								boss.anim(28601);
								
								int xs = Utils.random(2) == 1 ? -1 : 1;
								int ys = Utils.random(2) == 1 ? -1 : 1;
								int coordX = 3872 + (xs * 10);
								int coordY = 9951 + (ys * 10);
								int minX = Math.min(3872, coordX);
								int minY = Math.min(9951, coordY);
								int maxX = Math.max(3872, coordX);
								int maxY = Math.max(9951, coordY);
								
								for (int i2 = 0; i2 <= 10; i2++) { //good flowers
									World.spawnObject(new WorldObject(137743, 10, 0, 3872 + (xs * i2), 9951, 3));
									World.spawnObject(new WorldObject(137743, 10, 0, 3872, 9951 + (ys * i2), 3));
								}
								for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
									World.spawnObject(new WorldObject(137740, 10, 0, 3872 + (-xs * i2), 9951, 3));
									World.spawnObject(new WorldObject(137740, 10, 0, 3872, 9951 + (-ys * i2), 3));
								}
								
								
								WorldTasksManager.schedule(new WorldTask() {

									int cycle = 0;
									
									@Override
									public void run() {
										if (npc.isDead() || npc.hasFinished() || !boss.isAwaken()/* || npc.getId() == TheNightmare.NIGHTMARE_BOSS_SLEEPWALKER*/) {
											stop();
											boss.setFlowerPower(null);
											for (int i2 = 0; i2 <= 10; i2++) { //good flowers
												World.removeObject(World.getObjectWithType(new WorldTile(3872 + (xs * i2), 9951, 3), 10));
												World.removeObject(World.getObjectWithType(new WorldTile(3872, 9951 + (ys * i2), 3), 10));
											}
											for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
												World.removeObject(World.getObjectWithType(new WorldTile(3872 + (-xs * i2), 9951, 3), 10));
												World.removeObject(World.getObjectWithType(new WorldTile(3872, 9951 + (-ys * i2), 3), 10));
											}
											
											return;
										}
										cycle++;
										 if (cycle == 1) {
												TheNightmareInstance.sendMessage("<col=D80000>The Nightmare splits the area into segments!");
												boss.setCantFollowUnderCombat(false);
												boss.setFlowerPower(minX, maxX, minY, maxY);
												for (int i2 = 0; i2 <= 10; i2++) { //good flowers
													World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872 + (xs * i2), 9951, 3), 10), new Animation(28617));
													World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872, 9951 + (ys * i2), 3), 10), new Animation(28617));
												}
												for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
													World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872 + (-xs * i2), 9951, 3), 10), new Animation(28623));
													World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872, 9951 + (-ys * i2), 3), 10), new Animation(28623));
												}
										 } else if (cycle == 2) {
											for (int i2 = 0; i2 <= 10; i2++) { //good flowers
												World.spawnObject(new WorldObject(137744, 10, 0, 3872 + (xs * i2), 9951, 3));
												World.spawnObject( new WorldObject(137744, 10, 0, 3872, 9951 + (ys * i2), 3));
											}
											for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
												World.spawnObject(new WorldObject(137741, 10, 0, 3872 + (-xs * i2), 9951, 3));
												World.spawnObject(new WorldObject(137741, 10, 0, 3872, 9951 + (-ys * i2), 3));
											}
										} else if (cycle == 5) {
											for (int i2 = 0; i2 <= 10; i2++) { //good flowers
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872 + (xs * i2), 9951, 3), 10), new Animation(28619));
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872, 9951 + (ys * i2), 3), 10), new Animation(28619));
											}
											for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872 + (-xs * i2), 9951, 3), 10), new Animation(28625));
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872, 9951 + (-ys * i2), 3), 10), new Animation(28625));
											}
										} else if (cycle == 6) {
											for (int i2 = 0; i2 <= 10; i2++) { //good flowers
												World.spawnObject(new WorldObject(137745, 10, 0, 3872 + (xs * i2), 9951, 3));
												World.spawnObject(new WorldObject(137745, 10, 0, 3872, 9951 + (ys * i2), 3));
											}
											for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
												World.spawnObject( new WorldObject(137742, 10, 0, 3872 + (-xs * i2), 9951, 3));
												World.spawnObject( new WorldObject(137742, 10, 0, 3872, 9951 + (-ys * i2), 3));
											}
										} else if (cycle == 25) { //stop also if move into next phase
											for (int i2 = 0; i2 <= 10; i2++) { //good flowers
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872 + (xs * i2), 9951, 3), 10), new Animation(28621));
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872, 9951 + (ys * i2), 3), 10), new Animation(28621));
											}
											for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872 + (-xs * i2), 9951, 3), 10), new Animation(28627));
												World.sendObjectAnimation(boss, World.getObjectWithType(new WorldTile(3872, 9951 + (-ys * i2), 3), 10), new Animation(28627));
											}
										} else if (cycle >= 26) { //stop also if move into next phase
											stop();
											boss.setFlowerPower(null);
											for (int i2 = 0; i2 <= 10; i2++) { //good flowers
												World.removeObject(World.getObjectWithType(new WorldTile(3872 + (xs * i2), 9951, 3), 10));
												World.removeObject(World.getObjectWithType(new WorldTile(3872, 9951 + (ys * i2), 3), 10));
											}
											for (int i2 = 1; i2 <= 10; i2++) { //bad flowers
												World.removeObject(World.getObjectWithType(new WorldTile(3872 + (-xs * i2), 9951, 3), 10));
												World.removeObject(World.getObjectWithType(new WorldTile(3872, 9951 + (-ys * i2), 3), 10));
											}
										} else  if (cycle >= 7) {
											if (npc.getId() == TheNightmare.NIGHTMARE_BOSS_SLEEPWALKER) //skip to end
												cycle = 24;
											else {
												for (Entity target2 : World.getNearbyPlayers(npc, false)) {
													if (!(target2.getX() >= minX && target2.getX() <= maxX
															&& target2.getY() >= minY && target2.getY() <= maxY)) {
														delayHit(npc, -1, target2, new Hit(boss, 200, HitLook.REGULAR_DAMAGE));
													}
												}
											}

										}
									}
									
								}, 2, 0);
								
								
								//special here
							} else {
								boss.setNextWorldTile(boss.getRespawnTile());
								boss.anim(28609);
								special = true;
							}
						}
						
					}, 1, 2);
					return cf.getAttackDelay() +  5;
				}
			}
		}
		
		//default attacks
		boolean meleeDistance = Utils.isOnRange(npc, target, 0);
		int attackStyle = Utils.random(meleeDistance ? 3 : 2);
		switch (attackStyle) {
		case 2: //melee standard
			
			double angle = Math.toDegrees(Math.atan2((target.getX()+target.getSize()/2) - (npc.getX()+npc.getSize()/2), (target.getY()+target.getSize()/2) - (npc.getY()+npc.getSize()/2)));
			boss.resetWalkSteps();
			boss.setCantFollowUnderCombat(true); //cant w during special
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished() || !boss.isAwaken())
						return;
					for (Entity target2 : World.getNearbyPlayers(npc, false)) {
						double xOffset = (target2.getX() + target2.getSize()/2) - (npc.getX()+npc.getSize()/2);
						double yOffset = (target2.getY() + target2.getSize()/2) - (npc.getY()+npc.getSize()/2);
						double targetAngle = Math.toDegrees(Math.atan2(xOffset, yOffset));
						double ratioAngle = 45;
						if(!Utils.collides(npc, target2) && (!Utils.isOnRange(npc, target2, 0) || (targetAngle < angle-ratioAngle || targetAngle > angle+ratioAngle)))
							continue;
						//-1 means instant
						delayHit(npc, -1, target2, getMeleeHit(npc, Utils.random(npc.getMaxHit())+1));
					}
					boss.setCantFollowUnderCombat(false); //cant w during special
				}
				
			}, 1);
			
			
			npc.anim(cf.getAttackEmote());
				break;
		case 0: // mage
			for (Entity target2 : npc.getPossibleTargets()) {// checks clip.
				int msDelay = World.sendProjectile(npc, target2, 6764, 80, 36, 50, 90, 0, 64);
				Hit hit = getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target2));
				target2.setNextGraphics(new Graphics(hit.getDamage() == 0 ? 85 : 6765, msDelay / 20, 100));
				delayHit(npc, getDelay(msDelay), target2, hit);
			}
			npc.anim(28595);
			break;
		case 1: //ranged
			npc.anim(28596);
			for (Entity target2 : npc.getPossibleTargets()) {// checks clip.
				int msDelay = World.sendProjectile(npc, target2, 6766, 90, 36, 50, 90, 5, 64);
				delayHit(npc, getDelay(msDelay), target2, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target2)));
			}
				break;
		}
		return cf.getAttackDelay();
	}

}
