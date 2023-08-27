/**
 * 
 */
package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author dragonkk(Alex) Nov 8, 2017
 */
@SuppressWarnings("serial")
public class Wyrn extends NPC {

	private static final int ID = 28610, TRANSFORM_ID = 28611;

	public Wyrn(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public boolean isTransformed() {
		return getId() == TRANSFORM_ID;
	}

	@Override
	public void processNPC() {
		if (isDead() || isLocked())
			return;
		if (!isTransformed()) {
			if (isUnderCombat()) {
				setNextNPCTransformation(TRANSFORM_ID);
				setNextAnimation(new Animation(28268));
				getCombat().setCombatDelay(4); // transform anim delay
			}
			return; // doesnt process if not transformed. i mean why should it.
		}
		if (!isUnderCombat()) {
			setNextAnimation(new Animation(28269));
			setLocked(true);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (isDead() || hasFinished())
						return;
					setLocked(false);
					setNextNPCTransformation(ID);
				}
				
			}, 1);
			return;
		}
		super.processNPC();
	}

	@Override
	public void finish() {
		setNPC(ID);
		setLocked(false);
		super.finish();
	}

}
