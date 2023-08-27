package com.rs.game.npc.combat.impl;

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

public class MysteriousShadeCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Mysterious shade" };
	}

	@Override
	public int attack(NPC npc, final Entity target) {

		final boolean rangeAttack = Utils.random(1) == 0;

		npc.setNextAnimation(new Animation(rangeAttack ? 13396 : 13398));
		npc.setNextGraphics(new Graphics(rangeAttack ? 2514 : 2515));
		World.sendProjectile(npc, target, rangeAttack ? 2510 : 2511, 18, 18, 50, 25, 0, 0);

		if (rangeAttack)
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
		else
			delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				target.setNextGraphics(new Graphics(rangeAttack ? 2512 : 2513));
			}
		}, 2);
		return 5;
	}
}
