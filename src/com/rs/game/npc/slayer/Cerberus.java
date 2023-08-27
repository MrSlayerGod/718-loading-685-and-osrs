/**
 * 
 */
package com.rs.game.npc.slayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rs.executor.WorldThread;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.Default;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Nov 14, 2017
 */
@SuppressWarnings("serial")
public class Cerberus extends NPC {

	private static final Integer[] GHOSTS = { 25867, 25868, 25869 };
	private static final WorldTile GHOST_BASE_POSITION = new WorldTile(1239, 1265, 0);

	private static final WorldTile BASE = new WorldTile(1238, 1251, 0);

	public static final int ID = 25863, COMBAT_ID = 25862;

	private long lastSpecial, lastExplosion;
	private boolean resetEmote;

	public Cerberus(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCantFollowUnderCombat(true);
		setIntelligentRouteFinder(true);
		setForceAgressive(true);
		setRandomWalk(0);
		setDropRateFactor(4);
	}

	public boolean isTransformed() {
		return getId() == COMBAT_ID;
	}

	@Override
	public void processNPC() {
		if (isDead())
			return;
		if (!isTransformed()) {
			if (isUnderCombat()) {
				setNextNPCTransformation(COMBAT_ID);
				setNextAnimation(new Animation(24486));
				resetEmote = true;
				getCombat().setCombatDelay(3); // transform anim delay
			}
		} else {
			if (!isUnderCombat()) {
				setNextNPCTransformation(ID);
				setNextAnimation(new Animation(24487));
				setNextFaceWorldTile(getRespawnTile().transform(0, -1, 0));
				this.setNextFaceEntity(null);
				resetEmote = true;
				reset();
			}
		}
		if (getCombat().getCombatDelay() <= 1 && isResetEmote())
			return;
		super.processNPC();
	}

	public boolean canUseSpecial() {
		return lastSpecial + 66 < WorldThread.WORLD_CYCLE; // 66
	}

	public boolean canUseExplosion() {
		return lastExplosion + 60 < WorldThread.WORLD_CYCLE;
	}

	public void setSpecialUsed() {
		this.lastSpecial = WorldThread.WORLD_CYCLE;
	}

	public void setExplosionUsed() {
		this.lastExplosion = WorldThread.WORLD_CYCLE;
	}

	public void useExplosion(Entity target) {
		setExplosionUsed();
		setNextForceTalk(new ForceTalk("Grrrrrrrrrrrrrr"));
		setNextAnimation(new Animation(24494));

		WorldTile[] tiles = new WorldTile[3];
		tiles[0] = new WorldTile(target);
		tiles[1] = target.transform(-2 + Utils.random(5), -2 + Utils.random(5), 0);
		tiles[2] = target.transform(-2 + Utils.random(5), -2 + Utils.random(5), 0);

		for (WorldTile tile : tiles)
			World.sendGraphics(this, new Graphics(6246), tile);

		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (hasFinished())
					return;
				for (WorldTile tile : tiles) {
					World.sendGraphics(Cerberus.this, new Graphics(6247), tile);
					if (tile.withinDistance(target, 1))
						target.applyHit(new Hit(Cerberus.this,
								tile.getX() == target.getX() && tile.getY() == target.getY() ? (100 + Utils.random(50))
										: 70,
								HitLook.REGULAR_DAMAGE));
				}
			}
		}, 12);
	}

	public void useGhostSpecial(Entity target) {
		setSpecialUsed();
		setNextForceTalk(new ForceTalk("Aaarrrooooooo"));
		setNextAnimation(new Animation(24492));
		List<Integer> arrayData = Arrays.asList(GHOSTS);
		Collections.shuffle(arrayData);
		for (int i = 0; i < 3; i++) {
			int ghostID = arrayData.get(i);
			WorldTile tile = getTile(GHOST_BASE_POSITION.transform(i, 0, 0));
			NPC ghost = new NPC(ghostID, tile, -1, true, true);
			ghost.addWalkSteps(tile.getX(), tile.getY() - 10, 10, false);
			int ghostIndex = i;
			WorldTasksManager.schedule(new WorldTask() {

				int cycle;

				@Override
				public void run() {
					cycle++;
					if (cycle == ghostIndex + 1) { // attack
						if (hasFinished()) {// if cerberus is dead dont dmg
							stop();
							ghost.finish();
							return;
						}
						ghost.setNextFaceEntity(target);
						if (ghost.getId() == 25867) { // ranged
							ghost.setNextAnimation(new Animation(24503));
							World.sendProjectile(ghost, target, 15, 30, 20, 30, 5, 16, 32);
							Default.delayHit(Cerberus.this, 0, target,
									new Hit(Cerberus.this, 300, HitLook.RANGE_DAMAGE));
							if (target instanceof Player) {
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										if (isDead() || hasFinished() || target.isDead() || target.hasFinished()
												|| !((Player) target).getPrayer().isRangeProtecting())
											return;
										((Player) target).getPrayer().drainPrayer(300);
									}
								}, 0);
							}
						} else if (ghost.getId() == 25868) { // magic
							ghost.setNextAnimation(new Animation(24504));
							World.sendProjectile(ghost, target, 5100, 30, 20, 30, 5, 16, 32);
							Default.delayHit(Cerberus.this, 0, target,
									new Hit(Cerberus.this, 300, HitLook.MAGIC_DAMAGE));
							if (target instanceof Player) {
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										if (isDead() || hasFinished() || target.isDead() || target.hasFinished()
												|| !((Player) target).getPrayer().isMageProtecting())
											return;
										((Player) target).getPrayer().drainPrayer(300);
									}
								}, 0);
							}
						} else if (ghost.getId() == 25869) { // melee
							World.sendProjectile(ghost, target, 6248, 30, 20, 30, 5, 16, 32);
							target.setNextGraphics(new Graphics(5101, 60, 0));
							Default.delayHit(Cerberus.this, 0, target,
									new Hit(Cerberus.this, 300, HitLook.MELEE_DAMAGE));
							if (target instanceof Player) {
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										if (isDead() || hasFinished() || target.isDead() || target.hasFinished()
												|| !((Player) target).getPrayer().isMeleeProtecting())
											return;
										((Player) target).getPrayer().drainPrayer(300);
									}
								}, 0);
							}
						}
					} else if (cycle == ghostIndex + 2) { // walk back
						ghost.setNextFaceEntity(null);
						ghost.addWalkSteps(tile.getX(), tile.getY(), 10, false);
					} else if (cycle >= ghostIndex + 3) {
						stop();
						ghost.finish();
					}
				}
			}, 10, 1);
		}
	}

	private WorldTile getTile(WorldTile base) {
		return base.transform(-BASE.getX(), -BASE.getY(), 0).transform(getRespawnTile().getX(), getRespawnTile().getY(),
				0);
	}

	@Override
	public void finish() {
		setNPC(ID);
		super.finish();
	}

	/**
	 * @return the resetEmote
	 */
	public boolean isResetEmote() {
		if (resetEmote) {
			resetEmote = false;
			this.setNextAnimation(new Animation(-1));
			return true;
		}
		return false;
	}

}
