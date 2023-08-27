package com.rs.game.npc.combat.impl.inferno;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class BlobCombat extends CombatScript {

	@Override
	public Object[] getKeys() {

		return new Object[]
		{ 27693 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(3);
		if (attackStyle == 2 && !Utils.isOnRange(npc, target, 0))
			attackStyle = Utils.random(2);
		if (target instanceof Player) {
			Player pTarget = (Player) target;
			if (attackStyle == 0 && pTarget.getPrayer().isMageProtecting())
				attackStyle = 1;
			else if (attackStyle == 1 && pTarget.getPrayer().isRangeProtecting())
				attackStyle = 0;
		}
		if (attackStyle == 2) { // melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			return defs.getAttackDelay();
		}else if (attackStyle == 1) { // range
			npc.setNextAnimation(new Animation(27581));
			World.sendProjectile(npc, target, 6382, 44, 30, 30, 20, 5, 64);
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
		} else {
			npc.setNextAnimation(new Animation(27583));
			World.sendProjectile(npc, target, 6378, 44, 30, 30, 20, 5, 64);
			delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
		}
		return defs.getAttackDelay();
	}

}
