package com.rs.game.npc.combat.impl.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

public class WyrmCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 28611 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		if (Utils.random(2) == 0 && Utils.isOnRange(npc, target, 0)) { //melee
			npc.setNextAnimation(new Animation(28270));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			return def.getAttackDelay();
		}
		//magic
		npc.setNextAnimation(new Animation(def.getAttackEmote()));
		int msDelay = World.sendProjectile(npc, target, def.getAttackProjectile(), 30, 20, 40, 35, 16, 64);		
		delayHitMS(npc, msDelay, target, getMagicHit(npc, getRandomMaxHit(npc, def.getMaxHit(), def.getAttackStyle(), target)));
		target.setNextGraphics(new Graphics(6635, msDelay / 10,  0));
		return def.getAttackDelay();
	}

}
