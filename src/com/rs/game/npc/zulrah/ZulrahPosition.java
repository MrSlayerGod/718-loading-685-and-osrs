/**
 * 
 */
package com.rs.game.npc.zulrah;

import com.rs.game.WorldTile;

/**
 * @author dragonkk(Alex)
 * Nov 5, 2017
 */
public enum ZulrahPosition {

	NORTH(new WorldTile(2266, 3072, 0)),
	SOUTH(new WorldTile(2266, 3063, 0)),
	WEST(new WorldTile(2257, 3072, 0)),
	EAST(new WorldTile(2275, 3072, 0));
	
	private WorldTile tile;
	
	private ZulrahPosition(WorldTile tile) {
		this.tile = tile;
	}
	
	public WorldTile getTile() {
		return tile;
	}
}
