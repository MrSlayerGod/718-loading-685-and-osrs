package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

public class LizardmanCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Lizardman", "Lizardman brute"  };
	}

	@Override
	public int attack(NPC npc, final Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		/*if (Utils.isOnRange(npc, target, 0)) {
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		} else {
			npc.setNextAnimation(new Animation(27193));
			World.sendProjectile(npc, target, 2181, 41, 16, 41, 35, 16, 0);
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), def.getAttackStyle(), target)));
		}*/
		npc.setNextAnimation(new Animation(def.getAttackEmote()));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		if (Utils.random(3) == 0) 
			target.getPoison().makePoisoned(30);
		return def.getAttackDelay();
	}
}
