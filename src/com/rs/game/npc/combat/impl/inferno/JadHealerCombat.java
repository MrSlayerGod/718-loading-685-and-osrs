package com.rs.game.npc.combat.impl.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.inferno.InfernoJad;
import com.rs.utils.Utils;

public class JadHealerCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27701 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (target instanceof InfernoJad) {
			npc.setNextAnimation(new Animation(22639));
			target.heal(Utils.random(npc.getMaxHit())+1);
			target.setNextGraphics(new Graphics(444, 0, 150));
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 2, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		}
		return defs.getAttackDelay();
	}
}
