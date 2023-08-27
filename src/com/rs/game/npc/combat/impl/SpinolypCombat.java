package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

public class SpinolypCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Spinolyp" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		switch (Utils.random(2)) {
		case 0:
			int hit = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			World.sendProjectile(npc, target, 2705, 34, 16, 30, 35, 16, 0);
			delayHit(npc, 2, target, getMagicHit(npc, hit));
			break;
		case 1:
			hit = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			World.sendProjectile(npc, target, 473, 34, 16, 30, 35, 16, 0);
			delayHit(npc, 2, target, getRangeHit(npc, hit));
			break;
		}
		if (Utils.random(10) == 0)
			target.getPoison().makePoisoned(68);
		return defs.getAttackDelay();
	}
}
