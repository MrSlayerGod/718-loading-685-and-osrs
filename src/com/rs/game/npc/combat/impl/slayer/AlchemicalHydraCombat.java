package com.rs.game.npc.combat.impl.slayer;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.slayer.AlchemicalHydra;
import com.rs.game.player.controllers.HydraLair;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class AlchemicalHydraCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 28615, 28619, 28620, 28621 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		AlchemicalHydra hydra = (AlchemicalHydra) npc;
		if (hydra.useSpecial()) { //special
			if (hydra.getId() == AlchemicalHydra.POISON_ID) {
				npc.setNextAnimation(new Animation(def.getAttackEmote()));
				hydra.sendSplash(target, new WorldTile(target));
				hydra.sendSplash(target, new WorldTile(target).transform(-2 + Utils.random(5), -2 + Utils.random(5), 0));
				hydra.sendSplash(target, new WorldTile(target).transform(-2 + Utils.random(5), -2 + Utils.random(5), 0));
				hydra.sendSplash(target, new WorldTile(target).transform(-2 + Utils.random(5), -2 + Utils.random(5), 0));
				hydra.sendSplash(target, new WorldTile(target).transform(-2 + Utils.random(5), -2 + Utils.random(5), 0));
			} else 	if (hydra.getId() == AlchemicalHydra.ENRAGE_ID) {
				npc.setNextAnimation(new Animation(def.getAttackEmote()));
				hydra.sendSplash(target, new WorldTile(target));
			} else if (hydra.getId() == AlchemicalHydra.LIGHTNING_ID) {
				npc.setNextAnimation(new Animation(def.getAttackEmote()));
				WorldTile orbTile = hydra.getLair().getMap().getTile(new WorldTile(1365, 10266, 0));
				int msDelay = World.sendProjectile(npc, orbTile, 6665, 40, 20, 40, 40, 8, npc.getSize() * 32 - 32);	
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (hydra.hasFinished() || !hydra.isRunning())
							return;
						World.sendGraphics(hydra, new Graphics(6664), orbTile);
						List<WorldTile> lights = new ArrayList<WorldTile>();
						for (WorldTile light : HydraLair.ORB_SPAWNS)
							lights.add(light);
						for (int i = 0; i < HydraLair.ORB_SPAWNS.length; i++) {
							WorldTile nextLight = lights.get(Utils.random(lights.size()));
							lights.remove(nextLight);
							WorldTasksManager.schedule(new WorldTask() {

								@Override
								public void run() {
									if (hydra.hasFinished() || !hydra.isRunning())
										return;
									int msDelay = World.sendProjectile(npc, orbTile, hydra.getLair().getMap().getTile(nextLight), 6665, 40, 40, 40, 40, 8, 0);
							
									WorldTasksManager.schedule(new WorldTask() {

										@Override
										public void run() {
											if (hydra.hasFinished() || !hydra.isRunning())
												return;
											hydra.getLair().addLight(hydra.getLair().getMap().getTile(nextLight));
										}
									}, getDelay(msDelay));
									
								}
							}, i);
						}
					}
					
				}, getDelay(msDelay));
			} else if (hydra.getId() == AlchemicalHydra.FLAME_ID) {
				hydra.setCantInteract(true);
				hydra.setNextFaceEntity(null);
				hydra.setForceWalk(hydra.getRespawnTile());
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						if (hydra.hasFinished() || !hydra.isRunning()) {
							stop();
							return;
						}
						if (hydra.getX() != hydra.getRespawnTile().getX()
							|| hydra.getY() != hydra.getRespawnTile().getY()) {
							hydra.setForceWalk(hydra.getRespawnTile());
							return;
						}
						stop();
						hydra.getLair().getPlayer().getPackets().sendGameMessage("The Alchemical Hydra temporarily stuns you.");
						hydra.getLair().getPlayer().stopAll();
						hydra.getLair().getPlayer().addFreezeDelay(60000);
						npc.setNextAnimation(new Animation(def.getAttackEmote()));
						hydra.getLair().setFire(45);
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								if (hydra.hasFinished() || !hydra.isRunning() || hydra.getId() != AlchemicalHydra.FLAME_ID) 
									return;
								npc.setNextAnimation(new Animation(def.getAttackEmote()));
								hydra.getLair().setFire(-45);
							}
						}, 1);
						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								if (hydra.hasFinished() || !hydra.isRunning()) 
									return;
								hydra.setCantInteract(false);
								hydra.getCombat().setTarget(target);
								hydra.addFreezeDelay(10000);
								hydra.getCombat().setCombatDelay(7);
								hydra.getLair().getPlayer().setFreezeDelay(0);
								if (hydra.getId() == AlchemicalHydra.FLAME_ID)
									hydra.getLair().setFlame();
							}
							
						}, 3);
					}
					
				}, 0, 0);
			}
			return def.getAttackDelay();
		}
		if (hydra.useRange()) { //range
			npc.setNextAnimation(new Animation(def.getAttackEmote()+ (npc.getId() == AlchemicalHydra.ENRAGE_ID ? 0 : 2)));
			int msDelay = World.sendProjectile(npc, target, 6663, 40, 20, 40, 40, 8, npc.getSize() * 32 - 32);		
			if (npc.getId() == AlchemicalHydra.POISON_ID) 
				delayHitMS(npc, msDelay, target, getRangeHit(npc, getRandomMaxHit(npc, hydra.getMaxHit()/2, NPCCombatDefinitions.RANGE, target)),
						getRangeHit(npc, getRandomMaxHit(npc, hydra.getMaxHit()/2, NPCCombatDefinitions.RANGE, target)));
			else
				delayHitMS(npc, msDelay, target, getRangeHit(npc, getRandomMaxHit(npc, hydra.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
		} else { //magic
			npc.setNextAnimation(new Animation(def.getAttackEmote()+ 1));
			int msDelay = World.sendProjectile(npc, target, def.getAttackProjectile(), 40, 20, 40, 40, 8, npc.getSize() * 32 - 32);		
			if (npc.getId() == AlchemicalHydra.POISON_ID || npc.getId() == AlchemicalHydra.LIGHTNING_ID) 
				delayHitMS(npc, msDelay, target, getMagicHit(npc, getRandomMaxHit(npc, hydra.getMaxHit()/2, def.getAttackStyle(), target)),
						getMagicHit(npc, getRandomMaxHit(npc, hydra.getMaxHit()/2, def.getAttackStyle(), target)));
			else
				delayHitMS(npc, msDelay, target, getMagicHit(npc, getRandomMaxHit(npc, hydra.getMaxHit(), def.getAttackStyle(), target)));

		}
		return def.getAttackDelay();
	}

}
