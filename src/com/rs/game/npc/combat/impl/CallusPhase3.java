package com.rs.game.npc.combat.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.utils.Utils;

public class CallusPhase3 extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 21202 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		return ((CallusFrostborne) npc).standardAttack(this);
	}
}
