package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class SaradominWizard extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 1264 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		switch (Utils.random(3)) {
		case 0:
			magicAttack(npc, target);
			break;
		case 1:
		case 2:
		default:
			if (Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0)) {
				npc.setNextAnimation(new Animation(376));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 160, NPCCombatDefinitions.MELEE, target)));
				if (Utils.random(3) == 0)
					target.getPoison().makePoisoned(80);
			} else
				magicAttack(npc, target);
			break;
		}
		return defs.getAttackDelay();
	}

	private void magicAttack(final NPC npc, final Entity target) {
		final int damage = getRandomMaxHit(npc, 200, NPCCombatDefinitions.MAGE, target);
		delayHit(npc, 2, target, getMagicHit(npc, damage));
		npc.setNextAnimation(new Animation(811));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (damage > 0)
					target.setNextGraphics(new Graphics(98));
				else
					target.setNextGraphics(new Graphics(76));
			}
		});
	}
}
