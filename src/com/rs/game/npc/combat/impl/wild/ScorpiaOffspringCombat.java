/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
public class ScorpiaOffspringCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {26616};
	}

	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.random(3) == 0) 
			target.getPoison().makePoisoned(60);
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		World.sendProjectile(npc, target, defs.getAttackProjectile(), 15, 20, 41, 25, 15, 0);
		delayHit(npc, 0, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), defs.getAttackStyle(), target)));
		if (target instanceof Player)
			((Player)target).getPrayer().drainPrayer(1);
		return defs.getAttackDelay();
	}

}
