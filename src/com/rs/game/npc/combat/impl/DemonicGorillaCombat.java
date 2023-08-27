/**
 * 
 */
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
import com.rs.game.npc.gorrilas.TorturedGorilla;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Nov 17, 2017
 */
public class DemonicGorillaCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 27144, 27145, 27146, 27150 };
	}

	// 856
	private void useSpecial(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(27238));
		WorldTile tile = new WorldTile(target);
		World.sendProjectile(npc, tile.transform(0, -3, 0), tile, 856, 150, 0, 1, 25, 5, 128);

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (npc.hasFinished())
					return;
				World.sendGraphics(npc, new Graphics(5166), tile);
				for (Entity target : World.getNearbyPlayers(target, true)) {
					if (target.withinDistance(tile, 1))
						target.applyHit(new Hit(npc, Utils.random(npc.getId() == 27150 ? 200 : 400) + 1,
								HitLook.REGULAR_DAMAGE));
				}
			}

		}, 3);
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		TorturedGorilla gorilla = (TorturedGorilla) npc;
		switch (gorilla.getAttackStyle()) {
		case 0: // melee
			if (!Utils.isOnRange(npc, target, 0)) {
				if (!npc.hasWalkSteps() && !npc.clipedProjectile(target, true)) {
					gorilla.miss();
					return defs.getAttackDelay();
				}
				return 0;
			}
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target);
			delayHit(npc, 0, target, getMeleeHit(npc, damage));
			if (damage == 0 || (target instanceof Player) && ((Player) target).getPrayer().isMeleeProtecting())
				gorilla.miss();
			break;
		case 1: // range
			if (Utils.random(8) == 0) {
				useSpecial(npc, target);
				break;
			}
			npc.setNextAnimation(new Animation(27227));
			target.setNextGraphics(new Graphics(6303));
			damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			if (damage == 0 || (target instanceof Player) && ((Player) target).getPrayer().isRangeProtecting())
				gorilla.miss();
			break;
		case 2: // mage
			if (Utils.random(8) == 0) {
				useSpecial(npc, target);
				break;
			}
			npc.setNextAnimation(new Animation(27238));
			World.sendProjectile(npc, target, 6305, 35, 20, 41, 25, 5, 128);
			damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target);
			target.setNextGraphics(new Graphics(damage == 0 ? 85 : 6304, 60, 100));
			delayHit(npc, 1, target, getMagicHit(npc, damage));
			if (damage == 0 || (target instanceof Player) && ((Player) target).getPrayer().isMageProtecting())
				gorilla.miss();
			break;
		}
		return defs.getAttackDelay();
	}

}
