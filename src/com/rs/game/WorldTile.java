package com.rs.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.rs.Settings;
import com.rs.game.npc.cox.impl.VasaNistirio;
import com.rs.utils.Bounds;
import com.rs.utils.Utils;

public class WorldTile implements Serializable {

	private static final long serialVersionUID = -6567346497259686765L;

	private short x, y;
	private byte plane;

	public WorldTile(int x, int y, int plane) {
		this.x = (short) x;
		this.y = (short) y;
		this.plane = (byte) plane;
	}

	public WorldTile(int x, int y, int plane, int size) {
		this.x = (short) getCoordFaceX(x, size, size, -1);
		this.y = (short) getCoordFaceY(y, size, size, -1);
		this.plane = (byte) plane;
	}

	public WorldTile clone() {
		return new WorldTile(getX(), getY(), getPlane());
	}

	public WorldTile(WorldTile tile) {
		this.x = tile.x;
		this.y = tile.y;
		this.plane = tile.plane;
	}

	public WorldTile(WorldTile tile, int randomize) {
		this.x = (short) (tile.x + Utils.random(randomize * 2 + 1) - randomize);
		this.y = (short) (tile.y + Utils.random(randomize * 2 + 1) - randomize);
		this.plane = tile.plane;
	}

	public WorldTile(WorldTile tile, int randomize, boolean check) {
		this.x = (short) (tile.x + Utils.random(randomize * 2 + 1) - randomize);
		this.y = (short) (tile.y + Utils.random(randomize * 2 + 1) - randomize);
		int i = 0;
		do {
			this.x = (short) (tile.x + Utils.random(randomize * 2 + 1) - randomize);
			this.y = (short) (tile.y + Utils.random(randomize * 2 + 1) - randomize);
		} while(!World.isTileFree(tile, 1) && i++<20);
		this.plane = tile.plane;
	}

	public WorldTile(int hash) {
		this.x = (short) (hash >> 14 & 0x3fff);
		this.y = (short) (hash & 0x3fff);
		this.plane = (byte) (hash >> 28);
	}

	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		x += xOffset;
		y += yOffset;
		plane += planeOffset;
	}

	public void setLocation(WorldTile tile) {
		setLocation(tile.x, tile.y, tile.plane);
	}

	public final void setLocation(int x, int y, int plane) {
		this.x = (short) x;
		this.y = (short) y;
		this.plane = (byte) plane;
	}

	public int getX() {
		return x;
	}

	public int getXInRegion() {
		return x & 0x3F;
	}

	public int getYInRegion() {
		return y & 0x3F;
	}

	public int getXInChunk() {
		return x & 0x7;
	}

	public int getYInChunk() {
		return y & 0x7;
	}

	public int getY() {
		return y;
	}

	public int getPlane() {
		if (plane > 3)
			return 3;
		return plane;
	}

	public int getChunkX() {
		return (x >> 3);
	}

	public int getChunkY() {
		return (y >> 3);
	}

	public int getRegionX() {
		return (x >> 6);
	}

	public int getRegionY() {
		return (y >> 6);
	}

	public int getRegionId() {
		return ((getRegionX() << 8) + getRegionY());
	}

	public int getLocalX(WorldTile tile, int mapSize) {
		return x - 8 * (tile.getChunkX() - (Settings.MAP_SIZES[mapSize] >> 4));
	}

	public int getLocalY(WorldTile tile, int mapSize) {
		return y - 8 * (tile.getChunkY() - (Settings.MAP_SIZES[mapSize] >> 4));
	}

	public int getChunkXInScene(Entity entity) {
		return getChunkX() - getChunkXSceneOffset(entity);
	}

	public int getChunkXSceneOffset(Entity entity) {
		return entity.getLastLoadedMapRegionTile().getChunkX() - Settings.MAP_SIZES[entity.getMapSize()] / 16;
	}

	public int getChunkYInScene(Entity entity) {
		return getChunkY() - getChunkYSceneOffset(entity);
	}

	public int getChunkYSceneOffset(Entity entity) {
		return entity.getLastLoadedMapRegionTile().getChunkY() - Settings.MAP_SIZES[entity.getMapSize()] / 16;
	}

	public int getLocalX(WorldTile tile) {
		return getLocalX(tile, 0);
	}

	public int getLocalY(WorldTile tile) {
		return getLocalY(tile, 0);
	}

	public int getLocalX() {
		return getLocalX(this);
	}

	public int getLocalY() {
		return getLocalY(this);
	}

	public int getRegionHash() {
		return getRegionY() + (getRegionX() << 8) + (plane << 16);
	}

	public int getTileHash() {
		return y + (x << 14) + (plane << 28);
	}

	public boolean withinDistance(WorldTile tile, int distance) {
		if (tile == this)
			return true;
		if (tile.plane != plane)
			return false;
		return Math.abs(tile.x - x) <= distance && Math.abs(tile.y - y) <= distance;
		/*int deltaX = tile.x - x, deltaY = tile.y - y;
		return deltaX <= distance && deltaX >= -distance && deltaY <= distance && deltaY >= -distance;
	*/}

	public boolean withinDistance(WorldTile tile) {
		return withinDistance(tile, 14);
		/*if (tile == this)
			return true;
		if (tile.plane != plane)
			return false;
		return Math.abs(tile.x - x) <= 14 && Math.abs(tile.y - y) <= 14;*/
	}


	public int getCoordFaceX(int sizeX) {
		return getCoordFaceX(sizeX, -1, -1);
	}

	public static final int getCoordFaceX(int x, int sizeX, int sizeY, int rotation) {
		return x + ((rotation == 1 || rotation == 3 ? sizeY : sizeX) - 1) / 2;
	}

	public static final int getCoordFaceY(int y, int sizeX, int sizeY, int rotation) {
		return y + ((rotation == 1 || rotation == 3 ? sizeX : sizeY) - 1) / 2;
	}

	public int getCoordFaceX(int sizeX, int sizeY, int rotation) {
		return x + ((rotation == 1 || rotation == 3 ? sizeY : sizeX) - 1) / 2;
	}

	public int getCoordFaceY(int sizeY) {
		return getCoordFaceY(-1, sizeY, -1);
	}

	public int getCoordFaceY(int sizeX, int sizeY, int rotation) {
		return y + ((rotation == 1 || rotation == 3 ? sizeX : sizeY) - 1) / 2;
	}

	public WorldTile transform(int x, int y, int plane) {
		return new WorldTile(this.x + x, this.y + y, this.plane + plane);
	}

	public WorldTile translate(int x, int y, int plane) {
		this.setLocation(this.x + x, this.y + y, this.plane + plane);
		return this;
	}

	/**
	 * Add all tiles iin given area (1=9 tiles, x-1 -> 1 )
	 * @param radius
	 * @return
	 */
	public List<WorldTile> area(int radius) {
		return area(radius, p -> true);
	}

	public List<WorldTile> area(int radius, Predicate<WorldTile> filter) {
		List<WorldTile> list = new ArrayList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= +radius; y++) {
				WorldTile pos = relative(x, y);
				if (filter.test(pos))
					list.add(pos);
			}
		}
		return list;
	}

	public WorldTile relative(int changeX, int changeY) {
		return relative(changeX, changeY, 0);
	}

	public WorldTile relative(int changeX, int changeY, int changeZ) {
		return transform(changeX, changeY, changeZ);
	}

	/**
	 * Checks if this world tile's coordinates match the other world tile.
	 * 
	 * @param other
	 *            The world tile to compare with.
	 * @return {@code True} if so.
	 */
	public boolean matches(WorldTile other) {
		return x == other.x && y == other.y && plane == other.plane;
	}

	public boolean withinArea(int a, int b, int c, int d) {
		return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
	}

	public boolean inBounds(Bounds bounds) {
		return bounds.inBounds(x, y, plane, 0);
	}
	@Override
	public String toString() {
		return "[ X: " + x + ", Y: " + y + ", Z: " + plane + " ]";
	}

    public boolean hasGameObject() {
		return !World.isTileFree(this, 1);
    }

    public boolean collides(Entity e) {
		return Utils.collides(this.getX(), this.getY(), 1, e.getX(), e.getY(), e.getSize());
    }

    public int distance(WorldTile position) {
		return Utils.getDistance(this, position);
    }
}
