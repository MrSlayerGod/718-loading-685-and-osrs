/**
 * 
 */
package com.rs.game.npc.zulrah.action;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.zulrah.Snakeling;
import com.rs.game.npc.zulrah.Zulrah;
import com.rs.game.npc.zulrah.ZulrahSpawnPosition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
public class SpawnSpawn extends ZulrahAction {

	private ZulrahSpawnPosition position;
	
	public SpawnSpawn(ZulrahSpawnPosition position) {
		this.position = position;
	}
	
	@Override
	public int use(Zulrah zulrah) {
		zulrah.setNextAnimation(new Animation(25068));
		zulrah.setNextFaceEntity(null);
		zulrah.setNextFaceWorldTile(zulrah.getShrine().getWorldTileReal(position.getTiles()[0]));
		for (WorldTile cloud : position.getTiles()) 
			World.sendProjectile(zulrah, zulrah.getShrine().getWorldTileReal(cloud).transform(1, 1, 0), 6047, 60, 20, 30, 35, 16, 74);		
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (zulrah.hasFinished()|| !zulrah.getShrine().isRunning())
					return;
				for (WorldTile cloud : position.getTiles()) 
					new Snakeling(zulrah, zulrah.getShrine().getWorldTileReal(cloud.transform(1, 1, 0)));
			}
		}, 2);
		return 4;
	}

}
