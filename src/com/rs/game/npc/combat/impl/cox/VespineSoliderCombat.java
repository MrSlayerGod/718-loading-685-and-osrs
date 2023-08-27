package com.rs.game.npc.combat.impl.cox;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Projectile;
import com.rs.utils.Utils;

public class VespineSoliderCombat extends CombatScript {

	//private static final Projectile PROJECTILE = new Projectile(Settings.OSRS_GFX_OFFSET + 1486, 40, 43, 0, 50, 30, 0);

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27538 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions def = npc.getCombatDefinitions();
		npc.anim(def.getAttackEmote());
		//int delay = PROJECTILE.fire(npc, target);
		int dmg = target.asPlayer().getPrayer().isMeleeProtecting() ? def.getMaxHit() : def.getMaxHit() / 2;
		delayHit(npc, 0, target, new Hit(npc, Utils.random(dmg), Hit.HitLook.MELEE_DAMAGE));
		return def.getAttackDelay();
	}

}
