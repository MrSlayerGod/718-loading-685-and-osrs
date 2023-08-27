package com.rs.game.npc.combat.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.dungeonnering.LuminscentIcefiend;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Undying
 */
public class CallusPhase4 extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
		{ 21212 };
	}

	public boolean iciclesActive = false;

	public int icicleAttack(CallusFrostborne callus) {
		iciclesActive = true;
		List<Player> localPlayers = World.getLocalPlayers(callus);
		Collections.shuffle(localPlayers);

		for(int i = 0; i < (localPlayers.size() < 10 ? localPlayers.size() : 10); i++) {
			WorldTile icicleTile = new WorldTile(localPlayers.get(i));
			callus.icicles.add(icicleTile);
			World.sendGraphics(callus, LuminscentIcefiend.ICE_SHARDS, icicleTile);
		}

		WorldTasksManager.schedule(new WorldTask() {
			int count = 0;

			@Override
			public void run() {

				callus.gfx(2598);
				if (count == 16 || callus.isDead()) {
					stop();
					iciclesActive = false;
					callus.icicles.clear();
					for (Player player : localPlayers) {
						player.setCantWalk(false);
					}
					return;
				}
				count++;

				if (count < 3 || count %2 != 0) {
					return;
				}

				for (int idx = 0; idx < localPlayers.size(); idx++) {
					Player player = localPlayers.get(idx);
					if (player == null || player.isDead() || player.hasFinished())
						continue;
					WorldTile currentTile = player.getLastWorldTile();
					for (int i = 0; i < callus.icicles.size(); i++) {
						WorldTile tile = callus.icicles.remove(i);
						player.getPackets().sendGraphics(LuminscentIcefiend.ICE_SHARDS, tile);
						if (player.getX() == tile.getX() && player.getY() == tile.getY())
							player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.FIEND_FLAGGED, true);
					}
					callus.icicles.add(currentTile);
				}

				for (Iterator<WorldTile> it = callus.icicles.iterator(); it.hasNext();) {
					WorldTile tile = it.next();

					for (Entity t : callus.getPossibleTargets()) {
						Player player = (Player) t;

						if (player.getTemporaryAttributtes().get(TemporaryAtributtes.Key.FIEND_FLAGGED) == null)
							continue;

						//WorldTile nextTile = Utils.getFreeTile(player, 1);

						if (!player.isCantWalk())
							player.setCantWalk(true);
						if (player.getActionManager().getAction() != null)
							player.getActionManager().forceStop();
						player.setNextAnimation(LuminscentIcefiend.KNOCKBACK);
						// player.setNextWorldTile(nextTile);
						// player.setNextForceMovement(new NewForceMovement(tile, 0, nextTile, 1, Utils.getAngle(tile.getX() - nextTile.getX(), tile.getY() - nextTile.getY())));
						int damageCap = (int) (player.getMaxHitpoints() * .10);
						if (player.getHitpoints() < damageCap)// If has 10% of HP.
							continue;
						int damage = Utils.random(20, 100);
						if (player.getHitpoints() - damage <= damageCap)
							damage = damageCap;
						player.applyHit(new Hit(callus, damage, Hit.HitLook.REGULAR_DAMAGE));
					}
				}
			}
		}, 0, 0);

		return 8;
	}

	private int killAreaEffect = 0;
	private long last5kEndMS = -1;
	@Override
	public int attack(final NPC npc, final Entity target) {
		CallusFrostborne callus = (CallusFrostborne) npc;

		if(npc.getHitpoints() < 12500) {
			if(finalStandTimeout(callus))
				return 8;
			finalStandZone(callus);
		} else {
			if(last5kEndMS > 0) {
				last5kEndMS = -1;
			}
		}

		if(Utils.random(16) == 1) {
			return ((CallusFrostborne) npc).snowScreenAttack();
		}

		if(npc.getHitpoints() > 12500) {

			if(Utils.random(CallusFrostborne.ARENA_CLEAR_CHANCE / 2) == 0) {
				return ((CallusFrostborne) npc).arenaClearAttack();
			}
			if(Utils.random(CallusFrostborne.ICE_BALL_CHANCE / 2) == 0) {
				return ((CallusFrostborne) npc).iceballBarrageAttack();
			}
			if(Utils.random(CallusFrostborne.SNOW_STORM_CHANCE) == 0) {
				return ((CallusFrostborne) npc).snowScreenAttack();
			}

			/*if (!iciclesActive && Utils.random(10) == 0) {
				return icicleAttack(callus);
			}*/
		}
		return ((CallusFrostborne) npc).standardAttack(this);
	}

	private boolean finalStandTimeout(CallusFrostborne callus) {

		if(last5kEndMS > -1) {
			if(last5kEndMS - System.currentTimeMillis() <= 0) {
				// failed to kill boss in allotted time
				callus.yell("Callus: <col=00FFFF><shad=000000>Fools.. Now you'll experience the true extent of my wrath..");

				WorldTasksManager.schedule(new WorldTask() {
					int tick = 0;
					@Override
					public void run() {
						if(tick == 0)
							callus.anim(9968);
						else if(tick == 1)
							World.sendGraphics(callus, new Graphics(2820), callus);
						else if(tick == 4)
							callus.anim(9967);
						if(tick++ < 5) return;
						for(Entity e : callus.getPossibleTargets()) {
							if(e instanceof Player) {
								((Player) e).sendMessage("Callus: <col=00FFFF><shad=000000>Your time has come!");
								e.applyHit(callus, 13337);
								stop();
							}
						}
					}
				}, 0, 0);

				last5kEndMS = -1;
				callus.setHitpoints(7500);
				return true;
			}
		}

		return false;
	}

	private void finalStandZone(CallusFrostborne callus) {
		if(last5kEndMS == -1) {
			last5kEndMS = System.currentTimeMillis() + (4 * 60 * 1000);
			callus.yell("<col=ff0000>Callus is attempting to freeze the core of the Earth, kill him quickly before all life is frozen! Stand together to increase your power!");
		}
		if (killAreaEffect++ %4 == 0) {
			final int GFX = 2826, DELAY = 7;

			WorldTile[] area = CallusFrostborne.FINAL_AREA[callus.finalKillArea];
			int delay = 0;
			for (int x = area[0].getX(); x < area[1].getX(); x++) {
				int y = area[0].getY();
				World.sendGraphics(callus, new Graphics(GFX, delay += DELAY, 1), new WorldTile(x, y, 0));
			}
			for (int y = area[0].getY(); y <= area[1].getY(); y++) {
				int x = area[1].getX();
				World.sendGraphics(callus, new Graphics(GFX, delay += DELAY, 1), new WorldTile(x, y, 0));
			}
			for (int x = area[1].getX(); x >= area[0].getX(); x--) {
				int y = area[1].getY();
				World.sendGraphics(callus, new Graphics(GFX, delay += DELAY, 1), new WorldTile(x, y, 0));
			}
			for (int y = area[1].getY(); y > area[0].getY(); y--) {
				int x = area[0].getX();
				World.sendGraphics(callus, new Graphics(GFX, delay += DELAY, 1), new WorldTile(x, y, 0));
			}
		}
	}

}
