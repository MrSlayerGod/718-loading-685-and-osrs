package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.utils.Utils;

public class GeneralGraardorCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 6260 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.random(5) == 0) {
			switch (Utils.random(11)) {
			case 0:
				npc.setNextForceTalk(new ForceTalk("Death to our enemies!"));
				npc.playSound(3219, 2);
				break;
			case 1:
				npc.setNextForceTalk(new ForceTalk("Brargh!"));
				npc.playSound(3209, 2);
				break;
			case 2:
				npc.setNextForceTalk(new ForceTalk("Break their bones!"));
				npc.playSound(3221, 2);
				break;
			case 3:
				npc.setNextForceTalk(new ForceTalk("For the glory of Bandos!"));
				npc.playSound(3205, 2);
				break;
			case 4:
				npc.setNextForceTalk(new ForceTalk("Split their skulls!"));
				npc.playSound(3229, 2);
				break;
			case 5:
				npc.setNextForceTalk(new ForceTalk("We feast on the bones of our enemies tonight!"));
				npc.playSound(3206, 2);
				break;
			case 6:
				npc.setNextForceTalk(new ForceTalk("CHAAARGE!"));
				npc.playSound(3220, 2);
				break;
			case 7:
				npc.setNextForceTalk(new ForceTalk("Crush them underfoot!"));
				npc.playSound(3224, 2);
				break;
			case 8:
				npc.setNextForceTalk(new ForceTalk("All glory to Bandos!"));
				npc.playSound(3205, 2);
				break;
			case 9:
				npc.setNextForceTalk(new ForceTalk("GRAAAAAAAAAR!"));
				npc.playSound(3207, 2);
				break;
			case 10:
				npc.setNextForceTalk(new ForceTalk("FOR THE GLORY OF THE BIG HIGH WAR GOD!"));
				npc.playSound(3228, 2);
				break;
			}
		}
		if (Utils.random(2) == 0) { // range magical attack
			npc.setNextAnimation(new Animation(17391));
			for (Entity t : npc.getPossibleTargets()) {
				delayHit(npc, 1, t, getRangeHit(npc, getRandomMaxHit(npc, 335, NPCCombatDefinitions.RANGE, t)));
				World.sendProjectile(npc, t, 3352, 41, 16, 41, 35, 16, 0);
			}
		} else { // melee attack
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		}
		return defs.getAttackDelay();
	}
}
