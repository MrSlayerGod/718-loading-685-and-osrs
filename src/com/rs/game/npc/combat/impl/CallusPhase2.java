package com.rs.game.npc.combat.impl;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CallusPhase2 extends CombatScript {

	public static final Projectile SIPHON_PROJECTILE = new Projectile(1355 + Settings.OSRS_GFX_OFFSET, 90, 0, 30, 100, 16, 0);
	public static final Graphics SIPHON = new Graphics(1363 + Settings.OSRS_GFX_OFFSET, 0, 25);

	public static void siphonAttack(CallusFrostborne callus) {
		callus.forceTalk("Run and hide, puny mortals..");
		callus.setNextAnimation(new Animation(9964));
		List<WorldTile> tiles = new ArrayList<WorldTile>();
		int size = 12;
		int i = 0;

		for(Entity e : callus.getPossibleTargets(true, false)) {
			tiles.add(e);
			i++;
			siphonTile(callus, e);
		}

		for (; i < 100;) {
			final WorldTile tile = new WorldTile(callus).transform(Utils.random(22 + size) - 9, Utils.random(22 + size) - 9, 0);
			if (CallusFrostborne.siphonTiles.stream().filter(worldTile -> worldTile.matches(tile)).findAny().isPresent())
				continue;
			tiles.add(tile);
			i++;
			siphonTile(callus, tile);
		}
	}

	private static void siphonTile(CallusFrostborne callus, WorldTile entityTile) {
		final WorldTile tile = entityTile.clone();

		int ms = World.sendProjectile(callus, tile, SIPHON_PROJECTILE.getGfx(), 120, 26, 25, 36, 35, 0);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {

				World.sendGraphics(callus, SIPHON, tile);

				WorldTasksManager.schedule(new WorldTask() {

					int tick = 0;
					final int MAX_SIPHONS = 5;
					@Override
					public void run() {
						int siphons = 0;

						if (tick++ == 4) {
							callus.getSiphonTiles().remove(tile);
							stop();
							return;
						}

						for(Entity player : World.getNearbyPlayers(callus, false)) {
							if(tile.matches(player)) {
								int dmg = 50 + Utils.random(150);
								if(player instanceof Player) {
									player.setFreezeDelay(0);
									Hit hit2 = new Hit(callus, dmg, Hit.HitLook.POISON_DAMAGE);
									((Player) player).sendMessage("<col=00ffff><shad=000000>Callus siphons your hitpoints through the pool below you!");
									player.applyHit(hit2);
									player.gfx(3019);
								}
								if(siphons++ < MAX_SIPHONS) {
									callus.processHit(new Hit(callus, dmg, Hit.HitLook.HEALED_DAMAGE));
								}
							}
						}
					}
				}, 0, 0);
			}
		}, CombatScript.getDelay(ms)-1);
	}

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 21201 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		CallusFrostborne callus = (CallusFrostborne) npc;
		NPCCombatDefinitions defs = npc.getCombatDefinitions();

		if(Utils.random(12) == 1) {
			siphonAttack(callus);
			return 6;
		}

		if(Utils.random(CallusFrostborne.ARENA_CLEAR_CHANCE) == 0) {
			return ((CallusFrostborne) npc).arenaClearAttack();
		}
		if(Utils.random(CallusFrostborne.ICE_BALL_CHANCE) == 0) {
			return ((CallusFrostborne) npc).iceballBarrageAttack();
		}
		if(Utils.random(CallusFrostborne.SNOW_STORM_CHANCE) == 0) {
			return ((CallusFrostborne) npc).snowScreenAttack();
		}

		return callus.standardAttack(this);
	}

}
