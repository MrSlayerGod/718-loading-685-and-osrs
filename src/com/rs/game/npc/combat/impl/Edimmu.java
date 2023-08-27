package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Edimmu extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 10704 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		boolean melee = Utils.random(2) == 0;
		int damage = getRandomMaxHit(npc, npc.getMaxHit(), melee ? NPCCombatDefinitions.MELEE : NPCCombatDefinitions.MAGE, target);

		if (!Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0))
			sendMagicAttack(npc, target, damage);
		else {
			if (melee) {
				if (Utils.random(2) == 0) {
					delayHit(npc, 0, target, getMeleeHit(npc, damage));
					delayHit(npc, 1, target, getMeleeHit(npc, damage / 2));
					delayHit(npc, 2, target, getMeleeHit(npc, damage / 4));
					npc.setNextAnimation(new Animation(13735));
				} else {
					npc.setNextAnimation(new Animation(13734));
					delayHit(npc, 0, target, getMeleeHit(npc, damage));
				}
			} else
				sendMagicAttack(npc, target, damage);
		}
		return 5;
	}

	private void sendMagicAttack(final NPC npc, Entity target, final int damage) {
		npc.setNextAnimation(new Animation(13734));
		World.sendProjectile(npc, target, 2615, 41, 16, 41, 35, 16, 0);
		delayHit(npc, 2, target, getMagicHit(npc, damage));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				npc.heal(damage);
			}
		}, 2);
	}
}
