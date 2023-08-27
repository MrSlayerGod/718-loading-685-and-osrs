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

public class MetalDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Bronze dragon", "Iron dragon", "Steel dragon" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0) && Utils.random(2) == 0) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		} else {
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
			npc.setNextAnimation(new Animation(13160));
			World.sendProjectile(npc, target, 2464, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRegularHit(npc, damage));
		}
		return defs.getAttackDelay();
	}

}
