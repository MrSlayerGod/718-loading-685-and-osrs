/**
 * 
 */
package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

/**
 * @author dragonkk(Alex) Nov 8, 2017
 */
@SuppressWarnings("serial")
public class Kraken extends NPC {

    public static final int ID = 20496, TRANSFORM_ID = 20494;

	private KrakenTentacle[] tentacles = new KrakenTentacle[4];

	private boolean fishingExplosive;

	public Kraken(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCantFollowUnderCombat(true);
		setIntelligentRouteFinder(true);
		tentacles[0] = new KrakenTentacle(tile.transform(-3, 0, 0));
		tentacles[1] = new KrakenTentacle(tile.transform(6, 0, 0));
		tentacles[2] = new KrakenTentacle(tile.transform(-3, 4, 0));
		tentacles[3] = new KrakenTentacle(tile.transform(6, 4, 0));
		setDropRateFactor(2);
		setLureDelay(3000);
	}

	public void forceWakeUP() {
		fishingExplosive = true;
	}

	public boolean isReady() {
		if (fishingExplosive)
			return true;
		for (KrakenTentacle n : tentacles) {
			if (n == null || (!n.hasFinished() && !n.isTransformed()))
				return false;
		}
		return true;
	}

	private void spawnTentacles() {
		for (KrakenTentacle n : tentacles) {
			if (n == null)
				continue;
			n.spawn();
		}
	}
	
	public void forceAgro() {
		for (KrakenTentacle n : tentacles) {
			if (n == null)
				continue;
			n.setForceAgressive(true);
			n.setForceTargetDistance(64);
			n.checkAgressivity();
		}
	}

	private void killTentacles() {
		for (KrakenTentacle n : tentacles) {
			if (n == null)
				continue;
			n.reset();
			n.finish();
		}
	}

	public boolean isTransformed() {
		return getId() == TRANSFORM_ID;
	}

	@Override
	public void processNPC() {
		if (isDead())
			return;
		if (!isTransformed()) {
			if (isUnderCombat()) {
				setNextNPCTransformation(TRANSFORM_ID);
				setNextAnimation(new Animation(27135));
				getCombat().setCombatDelay(4); // transform anim delay
			}
			return; // doesnt process if not transformed. i mean why should it.
		} else {

		}
		if (!isUnderCombat()) {
			setNextNPCTransformation(ID);
			return;
		}
		super.processNPC();
	}

	@Override
	public void spawn() {
		spawnTentacles();
		super.spawn();
	}

	@Override
	public void finish() {
		setNPC(ID);
		killTentacles();
		super.finish();
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getLook() == HitLook.RANGE_DAMAGE) {
			hit.setDamage((int) (hit.getDamage() * 0.2));
			if (hit.getSource() instanceof Player)
				((Player) hit.getSource()).getPackets()
						.sendGameMessage("Your ranged attack has very little effect on the cave kraken.", true);
		}
		super.handleIngoingHit(hit);

	}

}
