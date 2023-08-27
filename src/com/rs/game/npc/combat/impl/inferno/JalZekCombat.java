package com.rs.game.npc.combat.impl.inferno;

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

public class JalZekCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27699};
	}// anims: DeathEmote: 9257 DefEmote: 9253 AttackAnim: 9252 gfxs: healing:
		// 444 - healer

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		int hit = 0;
		if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1) {
			commenceMagicAttack(npc, target, hit);
			return defs.getAttackDelay();
		}
		int attackStyle = Utils.random(2);
		switch (attackStyle) {
		case 0:
			hit = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, hit));
			break;
		case 1:
			commenceMagicAttack(npc, target, hit);
			break;
		}
		return defs.getAttackDelay();
	}

	private void commenceMagicAttack(final NPC npc, final Entity target, int hit) {
		hit = getRandomMaxHit(npc, npc.getMaxHit() - 50, NPCCombatDefinitions.MAGE, target);
		npc.setNextAnimation(new Animation(27610));
		// npc.setNextGraphics(new Graphics(1622, 0, 96 << 16));
		World.sendProjectile(npc, target, 6376, 74, 30, 30, 35, 16, 0);
		delayHit(npc, 2, target, getMagicHit(npc, hit));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				target.setNextGraphics(new Graphics(5157, 0, 100));
			}
		}, 2);
	}
}
