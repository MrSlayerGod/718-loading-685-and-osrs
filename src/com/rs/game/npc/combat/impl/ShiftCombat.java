package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;

public class ShiftCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 3732, 3733, 3734, 3735, 3736, 3737, 3738, 3739, 3740, 3741 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		if (npc.withinDistance(target, 2)) {
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), def.getAttackStyle(), target)));
			return def.getAttackDelay();
		}
		return 1;
	}
}
