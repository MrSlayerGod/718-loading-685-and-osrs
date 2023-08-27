package com.rs.game.npc.combat.impl.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.DragonfireShield;
import com.rs.utils.Utils;

public class OSRSLeatherDragon extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27273, 27274, 27275, 26593 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0) && Utils.getRandom(2) != 0) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		} else {
			npc.setNextAnimation(new Animation(81));
			npc.setNextGraphics(new Graphics(1, 0, 100));
			int damage = 100 + Utils.getRandom(550);
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
					if (damage >= 50) {
						damage = Utils.random(50);
						player.getPackets().sendGameMessage("Your " + (hasShield ? "shield" : "prayer") + " absorbs most of the dragon's breath!", true);
					} else
						damage = 0;
				} else if (!hasPot)
					player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
				DragonfireShield.chargeDFS(player, false);
			}
			delayHit(npc, 1, target, getRegularHit(npc, damage));
		}
		return defs.getAttackDelay();
	}
}
