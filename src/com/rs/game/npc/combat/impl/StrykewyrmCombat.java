package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.Strykewyrm;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class StrykewyrmCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
				{ 9463, 9465, 9467 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.getRandom(10);
		if (attackStyle <= 7 && Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0)) { // melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			if (npc.getId() == 9467) {
				if (Utils.getRandom(10) == 0) {
					target.setNextGraphics(new Graphics(2309));
					target.getPoison().makePoisoned(44);
				}
			}
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
			return defs.getAttackDelay();
		}
		if (attackStyle <= 9) { // mage
			npc.setNextAnimation(new Animation(12794));
			final Hit hit = getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target));
			delayHit(npc, 1, target, hit);
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 16, 41, 30, 16, 0);
			if (npc.getId() == 9463) {
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						if (Utils.getRandom(10) == 0 && target.getFreezeDelay() < Utils.currentTimeMillis()) {
							target.addFreezeDelay(3000);
							target.setNextGraphics(new Graphics(369));
							if (target instanceof Player) {
								Player targetPlayer = (Player) target;
								targetPlayer.stopAll();
							}
						} else if (hit.getDamage() != 0)
							target.setNextGraphics(new Graphics(2315));
					}
				}, 1);
			} else if (npc.getId() == 9467) {
				if (Utils.getRandom(10) == 0) {
					target.setNextGraphics(new Graphics(2313));
					if (Utils.random(2) == 0)
						target.getPoison().makePoisoned(88);
				}
			}
		} else if (attackStyle == 10) { // bury
			final WorldTile tile = new WorldTile(target);
			tile.moveLocation(-1, -1, 0);
			npc.setNextAnimation(new Animation(12796));
			npc.setCantInteract(true);
			((Strykewyrm) npc).setEmerged(false);
			WorldTasksManager.schedule(new WorldTask() {

				int count;

				@Override
				public void run() {
					count++;
					if (count == 1) {
						npc.setNextNPCTransformation(npc.getId() - 1);
						npc.setForceWalk(tile);
					}else if (count == -1) {
						((Strykewyrm) npc).setEmerged(true);
						npc.setCantInteract(false);
						npc.setTarget(target);
						stop();
					} else if (!npc.hasForceWalk()) {
						count = -2;
						npc.setNextNPCTransformation(npc.getId() + 1);
						npc.setNextAnimation(new Animation(12795));
						if (Utils.colides(target.getX(), target.getY(), target.getSize(), npc.getX(), npc.getY(), npc.getSize())) {
							delayHit(npc, 0, target, new Hit(npc, 300, HitLook.REGULAR_DAMAGE));
							if (npc.getId() == 9467) {
								target.getPoison().makePoisoned(88);
							} else if (npc.getId() == 9465) {
								delayHit(npc, 0, target, new Hit(npc, 300, HitLook.REGULAR_DAMAGE));
								target.setNextGraphics(new Graphics(2311));
							}
						}
					}
				}
			}, 1, 1);
		}
		return defs.getAttackDelay();
	}
}
