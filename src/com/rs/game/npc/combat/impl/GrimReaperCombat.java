package com.rs.game.npc.combat.impl;

import java.util.*;

import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.GrimReaper;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class GrimReaperCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 16031 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(5);
		if (attackStyle == 4 && Utils.random(2) == 0)
			attackStyle = Utils.random(4);

		if(Utils.random(10) == 1) {
			fearReaperAttack(npc, target);
		}

		if (Utils.random(10) == 0) {
			List<Entity> possibleTargets = World.getNearbyPlayers(npc, false);
			final HashMap<String, int[]> tiles = new HashMap<String, int[]>();
			for (Entity t : possibleTargets) {
				String key = t.getX() + "_" + t.getY();
				if (!tiles.containsKey(t.getX() + "_" + t.getY())) {
					tiles.put(key, new int[]
					{ t.getX(), t.getY() });
					World.sendProjectile(npc, new WorldTile(t.getX(), t.getY(), npc.getPlane()), 1900, 34, 0, 30, 35, 16, 0);
				}
			}
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					List<Entity> possibleTargets = World.getNearbyPlayers(npc, false);
					for (int[] tile : tiles.values()) {

						World.sendGraphics(null, new Graphics(1896), new WorldTile(tile[0], tile[1], 0));
						for (Entity t : possibleTargets)
							if (t.getX() == tile[0] && t.getY() == tile[1])
								t.applyHit(new Hit(npc, Utils.random(200) + 200, HitLook.REGULAR_DAMAGE));
					}
					stop();
				}

			}, 5);
		} else if (Utils.random(50) == 0) {
			npc.setNextGraphics(new Graphics(444));
			npc.heal(1000);
		}
		if (attackStyle == 0) { // normal mage move
			delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
			World.sendProjectile(npc, target, 2963, 34, 16, 40, 35, 16, 0);
		} else if (attackStyle == 1) { // normal mage move
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
			World.sendProjectile(npc, target, 1904, 34, 16, 30, 35, 16, 0);

			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					target.setNextGraphics(new Graphics(1910));
				}

			}, 2);

		} else if (attackStyle == 2) {
			npc.setNextGraphics(new Graphics(1901));
			World.sendProjectile(npc, target, 1899, 34, 16, 30, 95, 16, 0);
			delayHit(npc, 4, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
		} else if (attackStyle == 3) {
			npc.setNextGraphics(new Graphics(1898));
			target.setNextGraphics(new Graphics(2954));
			delayHit(npc, 2, target, getRegularHit(npc, target.getMaxHitpoints() - 1 > 400 ? 400 : target.getMaxHitpoints() - 1));
		} else if (attackStyle == 4) {
			npc.setNextGraphics(new Graphics(2600));
			npc.setCantInteract(true);
			npc.getCombat().removeTarget();
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished())
						return;
					for (Entity t : World.getNearbyPlayers(npc, false)) {
						t.applyHit(new Hit(npc, (int) (t.getHitpoints() * Math.random() * 0.4) + 1, HitLook.REGULAR_DAMAGE, 0));
					}
					npc.getCombat().addCombatDelay(3);
					npc.setCantInteract(false);
					npc.setTarget(target);
				}

			}, 4);
		}
		return defs.getAttackDelay();
	}

	private static final WorldTile baseTile = new WorldTile(4087, 5235, 0);
	private ArrayList<NPC> reapers = new ArrayList<>();

	private void fearReaperAttack(NPC npc, Entity target) {
		if(reapers.size() > 0)
			return;

		npc.forceTalk("Reapers of death, come forth..");
		WorldTasksManager.schedule(new WorldTask() {
			ArrayList<WorldTile> gasTiles = new ArrayList<>();
			int tick = 0;
			final int[] spawnTick = {1, 2, 3, 4, 3, 2, 1};
			@Override
			public void run() {
				tick++;
				//npc.forceTalk("tick=" + tick);
				for(int i = 0; i < spawnTick.length; i++) {
					if(tick == spawnTick[i]) {
						NPC n = World.spawnNPC(3649, baseTile.transform(1 + i * 2, 0, 0), 0-1, false);
						n.setCantInteract(true); // wait for all to spawn
						n.setDirection(ForceMovement.SOUTH);
						n.setRandomWalk(0);
						n.setSpawned(true);
						reapers.add(n);
						WorldTasksManager.schedule(() -> {
							n.setCantInteract(false);
							n.addWalkSteps(n.getX(), n.getY() - 9, -1,false);
							n.forceTalk("ARGH");
						});
					}
				}

				reapers.forEach(n -> {
					if(!n.isDead()) {
						WorldTile endTile = n.getRespawnTile().transform(0, -9, 0);

						if(n.matches(endTile)) {
							n.applyHit(new Hit(n, n.getHitpoints(), HitLook.POISON_DAMAGE));
						} else {
							WorldTile tile = n.clone();

							if(!gasTiles.contains(tile)) {
								gasTiles.add(tile);
								gasTiles.add(tile.transform(-1, 0, 0));
								gasTiles.add(tile.transform(1, 0, 0));
							}

							World.sendGraphics(2312, n);

							npc.getPossibleTargets().stream().filter(Objects::nonNull).forEach(player -> {
								if(Utils.isOnRange(player, n, 0) && tick %2 == 0) {
									player.asPlayer().sendMessage("A great fear comes over you.");
									player.asPlayer().forceTalk("Noooo!");
									player.applyHit(new Hit(n, 250 + Utils.random(100), HitLook.DESEASE_DAMAGE));
								}
							});
						}
					}
				});

				if(tick >= 14 || !reapers.stream().anyMatch(n -> !n.isDead())) {
					// all dead
					reapers.forEach(npc1 -> npc1.finish());
					reapers.clear();
					stop();
				}
			}
		}, 0, 0);
	}
}
