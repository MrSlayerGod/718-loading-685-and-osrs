package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

public class HatiCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 13460, 14836 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		int attackStyle = Utils.random( !Utils.isOnRange(npc, target, 0) ? 2 : 3);
		if (attackStyle == 0) {
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target)));
			World.sendProjectile(npc, target, 500, 41, 30, 41, 40, 16, 96);
		} else if (attackStyle == 1) {
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target)));
			World.sendProjectile(npc, target, 100, 41, 30, 41, 40, 16, 96);
		} else if (attackStyle == 2) 
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target)));
		return defs.getAttackDelay();
	}
}
