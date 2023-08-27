package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class KreearraCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 6222 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (/*!npc.isUnderCombat()*/Utils.isOnRange(npc, target, 0) && Utils.random(2) == 0) {
			npc.setNextAnimation(new Animation(17396));
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 260, NPCCombatDefinitions.MELEE, target)));
			return defs.getAttackDelay();
		}
		npc.setNextAnimation(new Animation(17397));
		for (final Entity t : npc.getPossibleTargets()) {
			if (Utils.random(2) == 0) {
				delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 210, NPCCombatDefinitions.MAGE, t)));
				World.sendProjectile(npc, t, 3388, 65, 41, 65, 35, 0, 5);
			} else {
				delayHit(npc, 1, t, getRangeHit(npc, getRandomMaxHit(npc, 720, NPCCombatDefinitions.RANGE, t)));
				World.sendProjectile(npc, t, 3349, 65, 41, 65, 35, 0, 5);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						for (int c = 0; c < 10; c++) {
							int dir = Utils.random(Utils.DIRECTION_DELTA_X.length);
							if (World.checkWalkStep(t.getPlane(), t.getX(), t.getY(), dir, 1)) {
								t.resetWalkSteps();
								t.setNextWorldTile(new WorldTile(t.getX() + Utils.DIRECTION_DELTA_X[dir], t.getY() + Utils.DIRECTION_DELTA_Y[dir], t.getPlane()));
								break;
							}
						}
					}
				}, 1);
			}
		}
		return defs.getAttackDelay();
	}
}
