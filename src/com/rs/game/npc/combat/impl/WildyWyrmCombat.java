package com.rs.game.npc.combat.impl;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewForceMovement;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.WildyWyrm;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class WildyWyrmCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		// TODO Auto-generated method stub
		return new Object[]
		{ 3334 };
	}
	
	public static void attackMageTarget(final WorldTile originalTarget, final List<Player> arrayList, Entity fromEntity, final NPC startTile, Entity t, final int projectile, final int gfx, final int damage) {
		if(damage < 20)
			return;
		final Entity target = t == null ? KalphiteQueenCombat.getTarget(arrayList, fromEntity, startTile) : t;
		if (target == null)
			return;
		
		if (target instanceof Player)
			arrayList.add((Player) target);
		World.sendProjectile(fromEntity, target, projectile, fromEntity == startTile ? 70 : 20, 20, 30, 30, 0, 0);
		delayHit(startTile, 1, target, getMagicHit(startTile, damage));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				target.setNextGraphics(new Graphics(gfx));
				attackMageTarget(originalTarget, arrayList, target, startTile, null, projectile, gfx, damage / 2);
			}
		}, 1);
	}
	
	
	

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		
		final List<Entity> possibleTargets = npc.getPossibleTargets();
		int size = npc.getSize();
		for(Entity t : possibleTargets) {
			if(Utils.colides(t.getX(), t.getY(), t.getSize(), npc.getX(), npc.getY(), size))
				delayHit(npc, 0, t, getRegularHit(npc, Utils.random(50) + 60));
		}
		
		if(npc instanceof WildyWyrm && Utils.random(10) == 0) {
			((WildyWyrm)npc).emerge();
			final WorldTile to = npc.getMiddleWorldTile();
			for(final Entity t : possibleTargets) {
				if(t.withinDistance(target, 1)) {
				//	t.setNextAnimation(new Animation(14388));
					//t.setNextGraphics(new Graphics(2767));
					t.setNextForceMovement(new NewForceMovement(t, 0, to, 2, Utils.getAngle(to.getX() - t.getX(), to.getY() - t.getY())));
					t.addFreezeDelay(1200, true);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							t.setNextWorldTile(to);
						}
					}, 2);
				}
			}
			return defs.getAttackDelay();
		}
		int attackStyle = Utils.random(Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), target.getSize(), 0) ? 3 : 2);
		switch (attackStyle) {
		case 0: //magic
			npc.setNextAnimation(new Animation(12794));
			attackMageTarget(new WorldTile(target), new ArrayList<Player>(), npc, npc, target, 2731, 2738, Utils.random(100) + 100);
			break;
		case 1://range
			final WorldTile tile = new WorldTile(target);
			World.sendProjectile(npc, tile, 2735, 70, 16, 30, 0, 16, 0);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					for (Entity t : possibleTargets) {
						if (!t.withinDistance(tile, 5)) 
							continue;
						World.sendProjectile(npc, tile, t, 2735, 70, 0, 25, 0, 30, 0);
						delayHit(npc, 1, t, getRangeHit(npc, Utils.random(50) + 50));
					}
				}
			}, 1);
			
			
			npc.setNextAnimation(new Animation(12794));
			break;
		default: //melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			for(Entity t : possibleTargets) {
				if(t.withinDistance(target, 1))
					delayHit(npc, 0, t, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, t)));
			}
			break;
		}
		return defs.getAttackDelay();
	}

}
