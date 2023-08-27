package com.rs.game.npc.combat.impl.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class InfernoJadCombat extends CombatScript {

	@Override
	public Object[] getKeys() {

		return new Object[]
		{ 27700 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(3);
		if (attackStyle == 2) { // melee
			if (!Utils.isOnRange(npc, target, 0))
				attackStyle = Utils.random(2); // set mage
			else {
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
				return defs.getAttackDelay();
			}
		}
		if (attackStyle == 1) { // range
			npc.setNextAnimation(new Animation(27593));
			npc.setNextGraphics(new Graphics(1625));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					target.setNextGraphics(new Graphics(5451));
					delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit() - 2, NPCCombatDefinitions.RANGE, target)));
				}
			}, 3);
		} else {
			npc.setNextAnimation(new Animation(27592));
		//	npc.setNextGraphics(new Graphics(6226));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					World.sendProjectile(npc, target, 5448, 130, 30, 30, 20, 5, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(5157, 0, 100));
							delayHit(npc, 0, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit() - 2, NPCCombatDefinitions.MAGE, target)));
						}

					}, 2);
				}
			}, 1);
		}

		return 12;
	}

}
