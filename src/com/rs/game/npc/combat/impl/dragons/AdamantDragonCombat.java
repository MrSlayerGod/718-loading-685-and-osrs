package com.rs.game.npc.combat.impl.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.DragonfireShield;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class AdamantDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 28030 };
	}
	
	private void doExplosion(NPC npc, int count, WorldTile from) {
		for (int i = 0; i < count; i++) {
			final WorldTile newTile = new WorldTile(from, 1);
			if (!World.isTileFree(newTile.getPlane(), newTile.getX(), newTile.getY(), 1))
				continue;
			World.sendProjectile(npc, count == 2 ? from : npc, newTile, 6486, 41, 30, 15, 0, 30, 0);
			boolean send = count == 3 && i == 0;
			WorldTasksManager.schedule(new WorldTask() {
				
				boolean send2;
				@Override
				public void run() {
					if (send2) {
						doExplosion(npc, 2, newTile);
						stop();
					} else {
						for (Entity t :  World.getNearbyPlayers(npc, false)) {
							if (t.hasWalkSteps() || Utils.getDistance(newTile.getX(), newTile.getY(), t.getX(), t.getY()) > 1/* || !t.clipedProjectile(newTile, false)*/)
								continue;
							delayHit(npc, 0, t, new Hit(npc, Utils.random(t instanceof Player && ((Player)t).getPoison().isImmune() ? 120 : 240)+1, HitLook.POISON_DAMAGE));
						}
						World.sendGraphics(npc, new Graphics(6487, 30 , 0), newTile);
						if (send) 
							send2 = true;
						else
							stop();
					}
				}
			}, count == 3 ? (true || Utils.getDistance(npc, from) > 3 ? 2 : 1) : 0, 1);
		}
	}

	public int attack(NPC npc, Entity target) {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		switch (Utils.random(Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(), 0) ? 8 : 7)) {
		case 7: //melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			break;
		case 6: //bolt
			npc.setNextAnimation(new Animation(81));
			World.sendProjectile(npc, target, 27, 38, 36, 40, 40, 5, 128);
			target.setNextGraphics(new Graphics(754, 40, 0));
			delayHit(npc, 1, target, getRegularHit(npc, (int) (target.getHitpoints() * 0.2)));
			break;
		case 5: //splash aoe
			npc.setNextAnimation(new Animation(81));
			doExplosion(npc, 3, new WorldTile(target));
			break;
		case 4: //magic
			npc.setNextAnimation(new Animation(81));
			World.sendProjectile(npc, target, 5165, 28, 16, 35, 45, 16, 128);
			int damage = getRandomMaxHit(npc, 195, NPCCombatDefinitions.MAGE, target);
			target.setNextGraphics(new Graphics(damage == 0 ? 85 : 5166, 60,  100));
			delayHit(npc, 1, target, getMagicHit(npc, damage));
			break;
		case 3: //range
			npc.setNextAnimation(new Animation(81));
			World.sendProjectile(npc, target, 6476, 28, 16, 35, 45, 16, 128);
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 195, NPCCombatDefinitions.RANGE, target)));
			break;
		case 0: //dragonfire
		case 1:
		case 2:
			damage = 100 + Utils.getRandom(550);
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
			npc.setNextAnimation(new Animation(81));
			World.sendProjectile(npc, target, 2464, 28, 16, 35, 45, 16, 128);
			delayHit(npc, 1, target, getRegularHit(npc, damage));
			break;
		}

		return defs.getAttackDelay();

	}

}
