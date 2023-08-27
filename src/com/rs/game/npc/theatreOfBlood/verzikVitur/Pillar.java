package com.rs.game.npc.theatreOfBlood.verzikVitur;

import com.rs.executor.GameExecutorManager;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.MapInstance.Stages;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.TheatreOfBlood;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Pillar extends NPC {
	
	private WorldObject object;
	private TheatreOfBlood raids;
	
	public Pillar(WorldTile tile, TheatreOfBlood raids) {
		super(28379, tile, -1, true, true);
		this.raids = raids;
		object = new WorldObject(132687, 10, 0, tile);
		World.getRegion(getRegionId(), true);
		GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (raids != null && raids.getStage() == Stages.RUNNING)
					World.spawnObject(object);
			}
		});
	}

	
	@Override
	public void sendDeath(Entity killer) {
		super.sendDeath(killer);
		World.removeObject(object);
		World.spawnObject(object = new WorldObject(new WorldObject(132688, 10, 0, object.transform(-1, -1, 0))));
	}
	
	public void finish() {
		super.finish();
		if (raids != null && raids.getStage() == Stages.RUNNING && object != null) {
			for (Player player : raids.getTargets(this))
				if (Utils.isOnRange(player.getX(), player.getY(), 1, object.getX(), object.getY(), 3, 0))
					player.applyHit(new Hit(this, Utils.random(500)+300, HitLook.REGULAR_DAMAGE));
			World.removeObject(object);
		//	clip(false);
		}
	}
	
	@Override
	public void setNextFaceWorldTile(WorldTile tile) {
		
	}

	@Override
	public void setNextFaceEntity(Entity target) {
		
	}
	
	@Override
	public void setTarget(Entity target) {
		
	}
	
	@Override
	public int getMaxHitpoints() {
		return 2000;
	}
	
	

}
