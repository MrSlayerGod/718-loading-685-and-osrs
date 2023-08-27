/**
 * 
 */
package com.rs.game.npc.combat.impl.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.slayer.Cerberus;
import com.rs.utils.Utils;


public class CerberusCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] {25862, 25863};
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		Cerberus cerberus = (Cerberus) npc;
		if (npc.getHitpoints() <= 4000 && cerberus.canUseSpecial()) {
			cerberus.useGhostSpecial(target);
			return 7;
		}
		if (npc.getHitpoints() <= 2000 && cerberus.canUseExplosion()) {
			cerberus.useExplosion(target);
			return 7;
		}
		int attack = Utils.random(3);
		if (attack == 0 && !Utils.isOnRange(npc, target, 0))
			attack = Utils.random(2) + 1;
		switch (attack) {
		case 0: //melee
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			break;
		case 1: //ranged
			delayHit(npc, 3, target, getRangeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
			World.sendProjectile(npc, target, 6244, 60, 20, 30, 55, 5, 74);
			target.setNextGraphics(new Graphics(6245, 60,  100));
			npc.setNextAnimation(new Animation(24490));
			break;
		case 2: //magic
			delayHit(npc, 3, target, getMagicHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
			World.sendProjectile(npc, target, 6242, 60, 20, 30, 55, 5, 74);	
			target.setNextGraphics(new Graphics(6243, 60,  100));
			npc.setNextAnimation(new Animation(24490));
			break;
		}
		return 7;
	}

}
