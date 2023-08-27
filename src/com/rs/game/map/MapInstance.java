package com.rs.game.map;

import java.util.concurrent.TimeUnit;

import com.rs.executor.GameExecutorManager;
import com.rs.game.WorldTile;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.utils.Logger;


public class MapInstance {

	public static enum Stages {
		LOADING, RUNNING, DESTROYING
	}

	private Stages stage;

	/*
	 * instanced position on map to overwrite
	 */
	private int[] instancePos;

	/*
	 * physical position on map to grab
	 */
	private int[] originalPos;

	private int ratioX, ratioY;

	public MapInstance(int x, int y) {
		this(x, y, 1, 1);
	}

	public MapInstance(int x, int y, int ratioX, int ratioY) {
		originalPos = new int[] {x, y};
		this.ratioX = ratioX;
		this.ratioY = ratioY;
	}


	public void load(final Runnable run) {
		stage = Stages.LOADING;
		GameExecutorManager.slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// finds empty map bounds
					if(instancePos == null)
						instancePos = MapBuilder.findEmptyChunkBound(ratioX * 8, ratioY * 8);
					buildMap();
					if(run != null)
						run.run();
					stage = Stages.RUNNING;
				}catch(Throwable e) {
					Logger.handle(e);
				}
			}
		});
	}
	
	//override this if u wanna customize behaviour for an instance map
	protected void buildMap() {
		// copies real map into the empty map
		MapBuilder.copyAllPlanesMap(originalPos[0], originalPos[1], instancePos[0], instancePos[1], ratioX * 8, ratioY * 8);
	}

	public void destroy(final Runnable run) {
		stage = Stages.DESTROYING;
		GameExecutorManager.slowExecutor.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					MapBuilder.destroyMap(instancePos[0], instancePos[1], ratioX * 8, ratioY * 8);
					if(run != null)
						run.run();
				}catch(Throwable e) {
					Logger.handle(e);
				}
			}
		}, 1800, TimeUnit.MILLISECONDS);
	}

	public void destroyInstant() {
		MapBuilder.destroyMap(instancePos[0], instancePos[1], ratioX * 8, ratioY * 8);
	}

	//local x, y
	public WorldTile getTile(int x, int y) {
		return getTile(x, y, 0);
	}
	
	public WorldTile getTile(int x, int y, int z) {
		return new WorldTile(instancePos[0] * 8 + x, instancePos[1] * 8 + y, z);
	}
	public int getInstanceMapID() {
		return (((instancePos[0]/8) << 8) + (instancePos[1]/8));
	}


	public WorldTile getInstanceTile(WorldTile original) {
		return new WorldTile(
				original.getX() - getInstancePos()[0] * 8,
				original.getY() - getInstancePos()[1] * 8,
				original.getPlane());
	}
	
	//real map x, y
	public WorldTile getTile(WorldTile original) {
		return getTile(original.getX() - originalPos[0] * 8, original.getY() - originalPos[1] * 8).transform(0, 0, original.getPlane());
	}

	public int[] getOriginalPos() {
		return originalPos;
	}
	
	public int[] getInstancePos() {
		return instancePos;
	}

	public int getRatioX() {
		return ratioX;
	}

	public int getRatioY() {
		return ratioY;
	}

	public Stages getStage() {
		return stage;
	}

}
