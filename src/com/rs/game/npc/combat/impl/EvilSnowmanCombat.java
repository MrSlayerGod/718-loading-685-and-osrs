package com.rs.game.npc.combat.impl;

import java.util.HashMap;
import java.util.List;

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
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class EvilSnowmanCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 16032 };
	}


	private void doExplosion(NPC npc, int count, WorldTile from) {
		for (int i = 0; i < count; i++) {
			final WorldTile newTile = new WorldTile(from, 1);
			if (!World.isTileFree(newTile.getPlane(), newTile.getX(), newTile.getY(), 1))
				continue;
			World.sendProjectile(npc, count == 2 ? from : npc, newTile, 6260, 41, 30, 15, 0, 30, 0);
			boolean send = count == 3 && i == 0;
			WorldTasksManager.schedule(new WorldTask() {
				
				boolean send2;
				@Override
				public void run() {
					if (send2) {
						doExplosion(npc, 2, newTile);
						stop();
					} else {
						for (Entity t : World.getNearbyPlayers(npc, false)) {
							if (t.hasWalkSteps() || Utils.getDistance(newTile.getX(), newTile.getY(), t.getX(), t.getY()) > 1 || !t.clipedProjectile(newTile, false))
								continue;
							delayHit(npc, 0, t, getRegularHit(npc, Utils.random(200)+1));
						}
						World.sendGraphics(npc, new Graphics(5157, 30 , 0), newTile);
						if (send) 
							send2 = true;
						else
							stop();
					}
				}
			}, count == 3 ? (true || Utils.getDistance(npc, from) > 3 ? 2 : 1) : 0, 1);
		}
	}
	
	
	
	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(4);
		if (attackStyle == 3 && Utils.random(4) != 0)
			attackStyle = Utils.random(3);
		if (Utils.random(70) == 0) {
			npc.setNextGraphics(new Graphics(444));
			npc.heal(100);
		}
		if (attackStyle == 0) { // normal mage move
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		} else if (attackStyle == 1) {
			target.setNextGraphics(new Graphics(1910));
			delayHit(npc, 0, target, getRegularHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		} else if (attackStyle == 2) {
			doExplosion(npc, 3, new WorldTile(target));
		} else if (attackStyle == 3) {
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
}
