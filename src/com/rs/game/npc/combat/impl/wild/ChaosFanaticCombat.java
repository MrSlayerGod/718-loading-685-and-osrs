/**
 * 
 */
package com.rs.game.npc.combat.impl.wild;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Oct 31, 2017
 */
public class ChaosFanaticCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] {26619};
	}

	private static final String[] ATTACKS = new String[] {"WEUGH!", "I shall call him squidgy and he shall be my squidgy!", "Burn!", "Develish Oxen Roll!", "AhehHeheuhHhahueHuUEehEahAH"};

	
	private void doExplosion(NPC npc, int count, WorldTile from) {
		for (int i = 0; i < count; i++) {
			final WorldTile newTile = new WorldTile(from, 1);
			if (!World.isTileFree(newTile.getPlane(), newTile.getX(), newTile.getY(), 1))
				continue;
			World.sendProjectile(npc, count == 2 ? from : npc, newTile, 5551, 41, 30, 15, 0, 30, 0);
			boolean send = count == 3 && i == 0;
			WorldTasksManager.schedule(new WorldTask() {
				
				boolean send2;
				@Override
				public void run() {
					if (send2) {
						doExplosion(npc, 2, newTile);
						stop();
					} else {
						for (Entity t : npc.getPossibleTargets()) {
							if (t.hasWalkSteps() || Utils.getDistance(newTile.getX(), newTile.getY(), t.getX(), t.getY()) > 1 || !t.clipedProjectile(newTile, false))
								continue;
							delayHit(npc, 0, t, getRegularHit(npc, Utils.random(240)+1));
						}
						World.sendGraphics(npc, new Graphics(5157, 30 , 0), newTile);
						if (send) 
							send2 = true;
						else
							stop();
					}
				}
			}, count == 3 ? (true || Utils.getDistance(npc, from) > 3 ? 2 : 1) : 0, 1);
		}
	}
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attack = Utils.random(ATTACKS.length);
		npc.setNextForceTalk(new ForceTalk(ATTACKS[attack]));
		if (attack == 0) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			doExplosion(npc, 3, new WorldTile(target));
			return 8;
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, attack == 1 ? 240 : npc.getMaxHit(), defs.getAttackStyle(), target)));
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 30, 41, 40, 16, 0);
			if (attack == 1) { //special attack1
				target.setNextGraphics(new Graphics(5305, 75, 100));
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
			}
		}
		return defs.getAttackDelay();
	}

}
