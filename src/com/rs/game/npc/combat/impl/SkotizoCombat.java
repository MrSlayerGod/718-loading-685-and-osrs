
/**
 * 
 */
package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.skotizo.Skotizo;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) Nov 17, 2017
 */
public class SkotizoCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 27286 };
	}


	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		Skotizo boss = (Skotizo) npc;
		boss.spawnSpawns();
		boss.activateAltar();
		int attackStyle = Utils.random(2);
		if (attackStyle == 0 && !Utils.isOnRange(npc, target, 0))
			attackStyle = 1;
		switch (attackStyle) {
		case 0: // melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		case 1: // mage
			npc.setNextAnimation(new Animation(69));
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 65, 35, 41, 35, 5, 128);
			delayHit(npc, 2, target, getMagicHit(npc,  getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
			break;
		}
		return defs.getAttackDelay();
	}

}
