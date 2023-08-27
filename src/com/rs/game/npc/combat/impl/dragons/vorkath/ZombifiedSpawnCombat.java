/**
 * 
 */
package com.rs.game.npc.combat.impl.dragons.vorkath;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Jan 11, 2018
 */
public class ZombifiedSpawnCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {28063};
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(npc, 0, target, getRegularHit(npc, Utils.random(npc.getHitpoints() * npc.getMaxHit() / npc.getMaxHitpoints())+1));
		delayHit(npc, 0, npc,  getRegularHit(npc, npc.getHitpoints()));
		return defs.getAttackDelay();
	}

}
