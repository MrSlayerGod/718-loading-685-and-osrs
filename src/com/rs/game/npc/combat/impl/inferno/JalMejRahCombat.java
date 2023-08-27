package com.rs.game.npc.combat.impl.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;

public class JalMejRahCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27692 };
	}

	@Override
	public int attack(NPC npc, Entity target) {// yoa
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		if (target instanceof Player)
			((Player) target).setRunEnergy(((Player) target).getRunEnergy() > 3 ? ((Player) target).getRunEnergy() - 3 : 0);
		World.sendProjectile(npc, target, 6382, 44, 30, 30, 20, 5, 32);
		delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
		return defs.getAttackDelay();
	}
}
