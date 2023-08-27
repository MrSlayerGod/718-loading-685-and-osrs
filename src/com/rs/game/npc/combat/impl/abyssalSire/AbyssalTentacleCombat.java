package com.rs.game.npc.combat.impl.abyssalSire;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.abyssalNexus.Tentacle;
import com.rs.game.npc.combat.CombatScript;
import com.rs.utils.Utils;

public class AbyssalTentacleCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ Tentacle.AWAKEN_ID};
	}

	@Override
	public int attack(NPC npc, Entity target) {
		if (Utils.collides(npc, target)) { //is on range
			delayHit(npc, 0, target, getRegularHit(npc, Utils.random(300)+1));
			npc.setNextAnimation(new Animation(npc.getCombatDefinitions().getAttackEmote()));
			return 4;
		}
		return 0;
	}

}
