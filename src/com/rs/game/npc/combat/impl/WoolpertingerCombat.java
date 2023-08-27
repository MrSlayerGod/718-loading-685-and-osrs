package com.rs.game.npc.combat.impl;

import java.util.ArrayList;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.familiar.Familiar;
import com.rs.utils.Utils;

public class WoolpertingerCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
				{ 16025, 16026 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		
		for (Entity t : npc.getPossibleTargets(true, true)) {
			if (t instanceof Familiar) {
				t.heal(npc.getHitpoints());
				t.sendDeath(npc);
				continue;
			}
		}
		final ArrayList<Entity> possibleTargets = npc.getPossibleTargets();
		//boolean stomp = false;
		int size = npc.getSize();
		for (Entity t : possibleTargets) {
			if (Utils.colides(t.getX(), t.getY(), t.getSize(), npc.getX(), npc.getY(), size)) {
			//	stomp = true;
				delayHit(npc, 0, t, getRegularHit(npc, Utils.random(npc.getMaxHit())));
			}
		}
		
		if (npc.getId() == 16026) {
			possibleTargets.clear();
			possibleTargets.add(target);
		}
		
		int nextAttack = Utils.random(5);
		if (nextAttack == 0 && npc.getId() == 16025) {
			npc.setNextGraphics(new Graphics(2600));	
			npc.setNextForceTalk(new ForceTalk("Brrrrrrrrrr"));
			for (Entity t : possibleTargets) 
				delayHit(npc, 3, t, getRegularHit(npc, 100+Utils.random((int) (npc.getMaxHit() * 0.4))));
		} else {
			for (Entity t : possibleTargets) { 
				if (Utils.random(2) == 0) {
					t.setNextGraphics(new Graphics(1896));	
					delayHit(npc, 4, t, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, t)));
				} else {
					int msDelay = World.sendProjectile(npc, t, 1835, 35, 20, 40, 80, 16, 64);		
					delayHit(npc, Math.max(4, getDelay(msDelay)), t, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, t)));
				}
			}
		}
		
		npc.setNextGraphics(new Graphics(1834));
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		return defs.getAttackDelay();
	}

}
