package com.rs.game.npc.combat.impl.dung;

import java.util.LinkedList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.dungeonnering.NightGazerKhighorahk;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class NightGazerKhighorahkCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Night-gazer Khighorahk" };
	}

	public void sendRangeAoe(final NightGazerKhighorahk gazer) {
		if (gazer.isDead())
			return;
		gazer.setNextAnimation(new Animation(13425));
		for (Entity target : gazer.getPossibleTargets()) {
			World.sendProjectile(gazer, target, 2385, 60, 16, 41, 30, 0, 0);
			delayHit(gazer, 1, target, getRangeHit(gazer, getRandomMaxHit(gazer, (int) (gazer.getMaxHit() * 0.6), NPCCombatDefinitions.RANGE, target)));
		}

		if (!gazer.isSecondStage()) {
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (gazer.isDead())
						return;
					gazer.setNextAnimation(new Animation(13422));
				}

			}, 5);
		}
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();

		final NightGazerKhighorahk gazer = (NightGazerKhighorahk) npc;
		final DungeonManager manager = gazer.getManager();

		/*
		 * without this check its possible to lure him so that he always nukes
		 */
		if (!gazer.isUsedSpecial()) {
			final List<Entity> targets = gazer.getPossibleTargets();
			boolean success = false;
			for (Entity t : targets) {
				if (Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), t.getX(), t.getY(), t.getSize(), 1)) {
					if (!success)
						success = true;
					npc.setNextAnimation(new Animation(gazer.isSecondStage() ? 13427 : 13429));
					npc.setNextGraphics(new Graphics(/*gazer.isSecondStage() ? 2391 : */2390));
					gazer.setUsedSpecial(true);
				}
			}
			if (success) {
				WorldTasksManager.schedule(new WorldTask() {

					private int ticks;
					private List<WorldTile> tiles = new LinkedList<WorldTile>();

					@Override
					public void run() {
						ticks++;
						if (ticks == 1) {
							npc.setNextAnimation(new Animation(gazer.isSecondStage() ? 13426 : 13428));
						} else if (ticks == 3) {
							for (Entity t : targets) {
								if (Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), t.getX(), t.getY(), t.getSize(), 1)) {
									t.applyHit(new Hit(npc, Utils.random((int) (t.getMaxHitpoints() * 0.74)) + 1, HitLook.REGULAR_DAMAGE));
									if (t instanceof Player) {
										Player player = (Player) t;
										player.lock(2);
										player.stopAll();
									}
									byte[] dirs = Utils.getDirection(npc.getDirection());
									WorldTile tile = null;
									distanceLoop: for (int distance = 2; distance >= 0; distance--) {
										tile = new WorldTile(new WorldTile(t.getX() + (dirs[0] * distance), t.getY() + (dirs[1] * distance), t.getPlane()));
										if (World.isFloorFree(tile.getPlane(), tile.getX(), tile.getY()) && manager.isAtBossRoom(tile))
											break distanceLoop;
										else if (distance == 0)
											tile = new WorldTile(t);
									}
									tiles.add(tile);
									t.faceEntity(gazer);
									t.setNextAnimation(new Animation(10070));
									t.setNextForceMovement(new NewForceMovement(t, 0, tile, 2, t.getDirection()));
								}
							}
						} else if (ticks == 4) {
							for (int index = 0; index < tiles.size(); index++) {
								Entity t = targets.get(index);
								if (Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), t.getX(), t.getY(), t.getSize(), 1))
									t.setNextWorldTile(tiles.get(index));
							}
							stop();
							return;
						}
					}
				}, 0, 0);
				return 10;
			}
		} else
			gazer.setUsedSpecial(false);

		if (Utils.random(10) == 0) { //range aoe
			if (!gazer.isSecondStage()) {
				npc.setNextAnimation(new Animation(13423));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						sendRangeAoe(gazer);
					}

				}, 1);
				return defs.getAttackDelay() + 6;
			} else {
				sendRangeAoe(gazer);
				return defs.getAttackDelay() + 1;
			}
		} else {
			if (Utils.random(3) == 0) { //range single target
				npc.setNextAnimation(new Animation(gazer.isSecondStage() ? 13433 : 13434));
				World.sendProjectile(npc, target, 2385, gazer.isSecondStage() ? 60 : 40, 16, 41, 90, 0, 0);
				delayHit(npc, 3, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
				return defs.getAttackDelay() + 1;
			} else { //magic
				npc.setNextAnimation(new Animation(gazer.isSecondStage() ? 13430 : 13431));
				World.sendProjectile(npc, target, 2385, gazer.isSecondStage() ? 60 : 40, 16, 41, 30, 0, 0);
				target.setNextGraphics(new Graphics(2386, 70, 100));
				delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, (int) (npc.getMaxHit() * 0.6), NPCCombatDefinitions.MAGE, target)));
				return defs.getAttackDelay();
			}
		}
	}
}
