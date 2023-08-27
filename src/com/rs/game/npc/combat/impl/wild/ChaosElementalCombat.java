package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.utils.Utils;

public class ChaosElementalCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		// TODO Auto-generated method stub
		return new Object[]
		{ 3200 };
	}

	@Override
	public int attack(NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(8);
		if (attackStyle == 0 && (target instanceof NPC || !((Player) target).getInventory().hasFreeSlots()))
			attackStyle = 1 + Utils.random(2);
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		switch (attackStyle) {
		case 0: //remove item
			World.sendProjectile(npc, target, 558, 41, 41, 20, 15, 0, 0);

			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					Player player = (Player) target;
					int freeSlots = player.getInventory().getFreeSlots();
					if (freeSlots > 4)
						freeSlots = 4;
					for (int i = 0; i < player.getEquipment().getItems().getSize() && freeSlots > 0; i++) {
						Item item = player.getEquipment().getItem(i);
						if (item != null) {
							freeSlots--;
							ButtonHandler.sendRemove(player, i);
						}
					}
				}

			}, 1);
			break;
		case 1: //teleport
			World.sendProjectile(npc, target, 2947, 41, 41, 20, 15, 0, 0);

			//projectile here
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					WorldTile tile = new WorldTile(target, 15);
					if (!World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 1) || !Wilderness.isAtWild(tile))
						return;
					target.setNextWorldTile(tile);
				}

			}, 1);
			break;
		default: //attack
			int attack = Utils.random(3); 		//	melee range mage
			if (target instanceof Player) {
				Player player = (Player) target;
				int prayer = player.getPrayer().isMeleeProtecting() ? 0 : player.getPrayer().isRangeProtecting() ? 1 : player.getPrayer().isMageProtecting() ? 2 : -1;
				if (prayer == attack)
					attack = (attack - 1) & 0x3; //to make sure its positive between 0 and 2 lol
			}
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), attack, target);
			World.sendProjectile(npc, target, 552, 41, 41, 20, 15, 0, 0);
			delayHit(npc, 1, target, attack == NPCCombatDefinitions.MELEE ? getMeleeHit(npc, damage) : attack == NPCCombatDefinitions.RANGE ? getRangeHit(npc, damage) : getMagicHit(npc, damage));
			break;
		}
		return defs.getAttackDelay();
	}

}
