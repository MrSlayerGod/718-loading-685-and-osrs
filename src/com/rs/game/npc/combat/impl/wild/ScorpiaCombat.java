/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
public class ScorpiaCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {26615};
	}

	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.random(3) == 0) 
			target.getPoison().makePoisoned(200);
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target)));
		return defs.getAttackDelay();
	}

}
