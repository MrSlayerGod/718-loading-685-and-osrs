package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.Bork;

public class BorkCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Bork" };
	}


	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions cdef = npc.getCombatDefinitions();
		if (npc instanceof Bork) {
			Bork bork = (Bork) npc;
			if (npc.getHitpoints() <= (cdef.getHitpoints() * 0.6) && !bork.isSpawnedMinions()) {
				bork.spawnMinions();
				return 0;
			}
		}
		npc.setNextAnimation(new Animation(cdef.getAttackEmote()));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		return cdef.getAttackDelay();
	}

}
