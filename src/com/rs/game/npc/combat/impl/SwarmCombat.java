package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.utils.Utils;

public class SwarmCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 411 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		delayHit(npc, 0, target, getRegularHit(npc, Utils.random(10)));
		return 6;
	}
}
