package com.rs.game.npc.combat.impl.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.DragonfireShield;
import com.rs.utils.Utils;

public class MithrilDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ "Mithril dragon" };
	}

	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		switch (Utils.random(Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0) ? 4 : 3)) {
		case 3: //melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		case 2: //magic
			npc.setNextAnimation(new Animation(13160));
			World.sendProjectile(npc, target, 2705, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 180, NPCCombatDefinitions.MAGE, target)));
			break;
		case 1: //range
			npc.setNextAnimation(new Animation(13160));
			World.sendProjectile(npc, target, 16, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 180, NPCCombatDefinitions.RANGE, target)));
			break;
		case 0: //dragonfire
			int damage = 100 + Utils.getRandom(550);
			final Player player = target instanceof Player ? (Player) target : null;
			if (player != null) {
				boolean hasShield = Combat.hasAntiDragProtection(target);
				boolean hasPrayer = player.getPrayer().isMageProtecting();
				boolean hasPot = player.hasFireImmunity();
				if (hasPot) {
					damage = player.isSuperAntiFire() ? 0 : Utils.random(100);
					player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
				}
				if (hasPrayer || hasShield) {
					if (damage > 100) {
						damage = Utils.random(100);
						player.getPackets().sendGameMessage("Your " + (hasShield ? "shield" : "prayer") + " absorbs most of the dragon's breath!", true);
					} else
						damage = 0;
				} else if (!hasPot)
					player.getPackets().sendGameMessage("You are hit by the dragon's fiery breath!", true);
				DragonfireShield.chargeDFS(player, false);
			}
			npc.setNextAnimation(new Animation(13164));
			npc.setNextGraphics(new Graphics(1, 0, 100));
			delayHit(npc, 1, target, getRegularHit(npc, damage));
			break;
		}

		return defs.getAttackDelay();

	}

}
