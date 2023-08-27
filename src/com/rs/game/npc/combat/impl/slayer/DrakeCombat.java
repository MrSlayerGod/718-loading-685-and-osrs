package com.rs.game.npc.combat.impl.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.slayer.Drake;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class DrakeCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 28612 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		Drake drake = (Drake) npc;
		if (drake.useSpecial()) {
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			WorldTile tile = new WorldTile(target);
			int msDelay = World.sendProjectile(npc, tile, 6637, 25, 20, 19, 45, 16, npc.getSize() * 64);		
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (npc.isDead() || npc.hasFinished())
						return;
					World.sendGraphics(npc, new Graphics(6638, 30 , 0), tile);
					if (target.hasWalkSteps() || Utils.getDistance(tile.getX(), tile.getY(), target.getX(), target.getY()) > 0)
						return;
					delayHit(npc, 0, target, getRegularHit(npc, (Combat.hasAntiDragProtection(target) ? Utils.random(30, 40) : Utils.random(60, 80)) + 1));
					delayHit(npc, 1, target, getRegularHit(npc, (Combat.hasAntiDragProtection(target) ? Utils.random(30, 40) : Utils.random(60, 80)) + 1));
					delayHit(npc, 2, target, getRegularHit(npc, (Combat.hasAntiDragProtection(target) ? Utils.random(30, 40) : Utils.random(60, 80)) + 1));
					delayHit(npc, 3, target, getRegularHit(npc, (Combat.hasAntiDragProtection(target) ? Utils.random(30, 40) : Utils.random(60, 80)) + 1));
				}
			}, CombatScript.getDelay(msDelay)/*+1*/);
			return def.getAttackDelay();
		}
		if (Utils.random(2) == 0 && Utils.isOnRange(npc, target, 0)) { //melee
			npc.setNextAnimation(new Animation(28275));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			return def.getAttackDelay();
		}
		//range
		npc.setNextAnimation(new Animation(def.getAttackEmote()));
		int msDelay = World.sendProjectile(npc, target, def.getAttackProjectile(), 25, 20, 40, 45, 16, 92);		
		delayHitMS(npc, msDelay, target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), def.getAttackStyle(), target)));
		return def.getAttackDelay();
	}

}
