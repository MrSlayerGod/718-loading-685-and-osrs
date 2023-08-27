/**
 * 
 */
package com.rs.game.npc.zulrah;

import com.rs.game.WorldTile;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
public enum ZulrahSpawnPosition {
	
	SOUTH_WEST(new WorldTile(2262, 3069, 0), new WorldTile(2265, 3068, 0)),
	SOUTH_EAST(new WorldTile(2268, 3068, 0), new WorldTile(2271, 3069, 0)),
	EAST(new WorldTile(2272, 3071, 0), new WorldTile(2272, 3074, 0)),
	WEST(new WorldTile(2262, 3071, 0), new WorldTile(2262, 3074, 0)),
	NEW_LOC(new WorldTile(2263, 3076, 0), new WorldTile(2273, 3076, 0));
	
	private WorldTile[] tiles;
	
	private ZulrahSpawnPosition(WorldTile... tiles) {
		this.tiles = tiles;
	}
	
	public WorldTile[] getTiles() {
		return tiles;
	}
}
