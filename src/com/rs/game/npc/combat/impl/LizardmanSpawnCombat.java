package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;

public class LizardmanSpawnCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 26768  };
	}

	@Override
	public int attack(NPC npc, final Entity target) {
		return 0;
	}
}
