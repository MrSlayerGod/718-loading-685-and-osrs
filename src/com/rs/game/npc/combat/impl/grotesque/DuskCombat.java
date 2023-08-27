/**
 * 
 */
package com.rs.game.npc.combat.impl.grotesque;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.grotesque.Dusk;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Mar 20, 2018
 */
public class DuskCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 27882, 27888 };
	}

	@Override
	public int attack(NPC npc, Entity target) {// yoa
		Dusk dusk = (Dusk) npc;
		final NPCCombatDefinitions def = npc.getCombatDefinitions();
		if (npc.getId() == Dusk.ID_PHASE_4) {
			if (dusk.useSpecial()) { //TODO
				npc.setNextAnimation(new Animation(27799));
				npc.setCantFollowUnderCombat(true);
				WorldTile from = new WorldTile(target);
				List<WorldTile> clippedTiles = new ArrayList<WorldTile>();
				target.setNextForceTalk(new ForceTalk("Arghhh!"));
				if (target instanceof Player) {
					target.setNextAnimation(new Animation(10070));
					((Player) target).stopAll();
					((Player) target).lock(6);
				}
				WorldTasksManager.schedule(new WorldTask() {
					boolean secondPart;
					@Override
					public void run() {
						if (!dusk.isRunning()) {
							stop();
							return;
						}
						if (!secondPart) {
							if (dusk.isDead() || dusk.hasFinished()) {
								stop();
								return;
							}
							secondPart = true;
							int[][] offsets = Utils.getCoordOffsetsNear(3);
							int skip = 4 + Utils.random(offsets[0].length-4);
							for(int i = 0; i < offsets[0].length; i++) {
								WorldTile tile = from.transform(offsets[0][i]+1, offsets[1][i]+1, 0);
								if (i == skip || !World.isFloorFree(0, tile.getX(), tile.getY()))
									continue;
								World.addFloor(tile);
								clippedTiles.add(tile);
								World.sendGraphics(npc, new Graphics(6434), tile);
							}
						} else {
							stop();
							npc.setCantFollowUnderCombat(false);
							for (WorldTile tile : clippedTiles)
								World.removeFloor(tile);
							if (dusk.isDead() || dusk.hasFinished()) 
								return;
							int[][] offsetsHit = Utils.getCoordOffsetsNear(1);
							World.sendGraphics(npc, new Graphics(6434), from);
							for(int i = 0; i < offsetsHit[0].length; i++) {
								WorldTile tile = from.transform(offsetsHit[0][i], offsetsHit[1][i], 0);
								if (!World.isFloorFree(0, tile.getX(), tile.getY()))
									continue;
								World.sendGraphics(npc, new Graphics(6434), tile);
							}
							if (target.withinDistance(from, 1)) {
								int damage = 600 + Utils.random(30);
								target.applyHit(new Hit(npc, damage, HitLook.REGULAR_DAMAGE));
								npc.heal(damage);
							}
						}
					}
				}, 4, 4);
				return 12;
			}
			int attackStyle = Utils.random(Utils.isOnRange(npc, target, 0) ? 2 : 1);
			switch (attackStyle) {
			case 1: //melee;
				npc.setNextAnimation(new Animation(def.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
				break;
			case 0: //range
				npc.setNextAnimation(new Animation(27801));
				World.sendProjectile(npc.transform(1, 2, 0), target, 6444, 74, 16, 30, 45, 16, 64);
				World.sendProjectile(npc.transform(1, 0, 0), target, 6444, 74, 16, 30, 65, 16, 64);
				delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
				delayHit(npc, 3, target, getRangeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
				break;
			}
			return def.getAttackDelay();
		} else {
			if (dusk.phase3())
				return 0;
			if (dusk.useSpecial()) {
				npc.setNextAnimation(new Animation(27802));
				npc.setCantFollowUnderCombat(true);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						if (!dusk.isRunning())
							return;
						npc.setCantFollowUnderCombat(false);
						if (Utils.isOnRange(npc, target, 0))
							target.applyHit(new Hit(npc, 300 + Utils.random(30), HitLook.REGULAR_DAMAGE));
					}
				}, 4);
				return 10;
			}
			npc.setNextAnimation(new Animation(def.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, def.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
			return def.getAttackDelay();
		}
	}

}
