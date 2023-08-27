package com.rs.game.npc.combat.impl.dung;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class AnimatedBookCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 10744 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		boolean meleeAttack = Utils.random(2) == 0;
		if (meleeAttack) { // melee
			if (!Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0)) {
				magicAttack(npc, target);
				return defs.getAttackDelay();
			} else {
				meleeAttack(npc, target);
				return defs.getAttackDelay();
			}
		} else {
			magicAttack(npc, target);
			return defs.getAttackDelay();
		}
	}

	private void meleeAttack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(13479));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
	}

	private void magicAttack(NPC npc, final Entity target) {
		npc.setNextAnimation(new Animation(13480));
		npc.setNextGraphics(new Graphics(2728));
		delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
		World.sendProjectile(npc, target, 2731, 34, 16, 30, 35, 16, 0);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				target.setNextGraphics(new Graphics(2738, 0, 80));
			}
		}, 2);
	}
}
