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
public class CaveKraken extends NPC {

	private static final int ID = 20493, TRANSFORM_ID = 20492;

	public CaveKraken(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCantFollowUnderCombat(true);
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
		}
		if (!isUnderCombat()) {
			setNextNPCTransformation(ID);
			return;
		}
		super.processNPC();
	}

	@Override
	public void finish() {
		setNPC(ID);
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
