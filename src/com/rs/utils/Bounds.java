package com.rs.utils;

import com.rs.game.WorldTile;

import java.util.function.Consumer;

/**
 * @author Simplex
 * @since Jul 25, 2020
 */
public class Bounds {

    public final int bottomLeftX, bottomLeftY;

    public final int topRightX, topRightY;

    public final int z;

    public Bounds(WorldTile WorldTile, int range) {
        this(WorldTile.getX(), WorldTile.getY(), WorldTile.getPlane(), range);
    }

    public Bounds(WorldTile swWorldTile, WorldTile neWorldTile, int z) {
        this(swWorldTile.getX(), swWorldTile.getY(), neWorldTile.getX(), neWorldTile.getY(), z);
    }

    public Bounds(int x, int y, int z, int range) {
        this(x - range, y - range, x + range, y + range, z);
    }

    public Bounds(int bottomLeftX, int bottomLeftY, int topRightX, int topRightY, int z) {
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.z = z;
    }

    public void forEachPos(Consumer<WorldTile> consumer) {
        int minZ, maxZ;
        if (z == -1) {
            minZ = 0;
            maxZ = 3;
        } else {
            minZ = z;
            maxZ = minZ;
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = bottomLeftX; x <= topRightX; x++) {
                for (int y = bottomLeftY; y <= topRightY; y++)
                    consumer.accept(new WorldTile(x, y, z));
            }
        }
    }

    public boolean inBounds(WorldTile tile) {
        return inBounds(tile.getX(), tile.getY(), tile.getPlane(), 0);
    }

    public boolean inBounds(WorldTile tile, int size) {
        return inBounds(tile.getX(), tile.getY(), tile.getPlane(), size);
    }

    public boolean inBounds(int x, int y, int z, int range) {
        return !(this.z != -1 && z != this.z) && x >= bottomLeftX - range && x <= topRightX + range && y >= bottomLeftY - range && y <= topRightY + range;
    }

    public boolean intersects(Bounds other) {
        return bottomLeftX < other.topRightX && topRightX > other.bottomLeftX && bottomLeftY < other.topRightY && topRightY > other.bottomLeftY;
    }

    public WorldTile randomPosition() {
        return new WorldTile(randomX(), randomY(), z == -1 ? Utils.random(0, 3) : z);
    }

    public int randomX() {
        return Utils.random(bottomLeftX, topRightX);
    }

    public int randomY() {
        return Utils.random(bottomLeftY, topRightY);
    }

}