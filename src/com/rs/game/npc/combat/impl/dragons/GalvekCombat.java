package com.rs.game.npc.combat.impl.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.DragonfireShield;
import com.rs.utils.Utils;

public class GalvekCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 28097 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		for (Entity t : npc.getPossibleTargets()) {
			if (t instanceof Player && !((Player) t).hasSkull()) 
				((Player) t).setGalvekSkull();
		}
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int damage = 100 + Utils.random(550);
		final Player player = target instanceof Player ? (Player) target : null;
		if (player != null) {
			boolean hasShield = Combat.hasAntiDragProtection(target);
			boolean hasPrayer = player.getPrayer().isMageProtecting();
			boolean hasPot = player.hasFireImmunity();
			if (hasPot) {
				damage = player.isSuperAntiFire() ? 0 : Utils.random(50);
				player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
			}
			if (hasPrayer || hasShield) {
				if (damage > 50) {
					damage = Utils.random(50);
					player.getPackets().sendGameMessage("Your " + (hasShield ? "shield" : "prayer") + " absorbs most of the dragon's breath!", true);
				} else
					damage = 0;
			} else if (!hasPot)
				player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
			DragonfireShield.chargeDFS(player, false);
		}
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		World.sendProjectile(npc, target, 2464, 60, 16, 35, 35, 16, 96);
		delayHit(npc, 1, target, getRegularHit(npc, damage));
		return defs.getAttackDelay();
	}
}
