package com.rs.game.npc.others;

import java.util.concurrent.TimeUnit;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Logger;

@SuppressWarnings("serial")
public class DoorSupport extends NPC {

	public DoorSupport(int id, WorldTile tile) {
		super(id, tile, -1, true, true);
		setCantFollowUnderCombat(true);
	}

	@Override
	public void processNPC() {
		cancelFaceEntityNoCheck();
	}
	
	public boolean canDestroy(Player player) {
		/*if(getId() == 2446)
			return player.getY() < getY();
		if(getId() == 2440)
			return player.getY() > getY();
		return player.getX() > getX();*/
		return true;
	}

	@Override
	public void sendDeath(Entity killer) {
		setNextNPCTransformation(getId() + 1);
		final WorldObject door = World.getObjectWithId(this, 8967);
		if(door != null)
			World.removeObject(door);
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					setNextNPCTransformation(getId() - 1);
					reset();
					if(door != null)
						World.spawnObject(door);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, 60, TimeUnit.SECONDS);
	}
	
}
