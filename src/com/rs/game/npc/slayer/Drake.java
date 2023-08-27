/**
 * 
 */
package com.rs.game.npc.slayer;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author dragonkk(Alex) Nov 8, 2017
 */
@SuppressWarnings("serial")
public class Drake extends NPC {

	private static final int ID = 28612, TRANSFORM_ID = 28613;
	
	private int attackCount;

	public Drake(WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(ID, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}



	
	public void sendDeath(Entity killer) {
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (hasFinished())
					return;
				setNextNPCTransformation(TRANSFORM_ID);
			}
			
		});
		super.sendDeath(killer);
	}
	
	@Override
	public void finish() {
		setNPC(ID);
		attackCount = 0;
		super.finish();
	}
	
	public boolean useSpecial() {
		return ++attackCount % 7 == 0;
	}

}
