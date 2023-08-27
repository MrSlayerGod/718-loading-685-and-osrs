package com.rs.utils;

import com.rs.game.WorldTile;

/**
 * @author Simplex
 * @since Nov 03, 2020
 */
public enum Direction {

    /**
     * DO NOT REORDER
     */

    NORTH_WEST(-1, 1, 768),     //0
    NORTH(0, 1, 1024),          //1
    NORTH_EAST(1, 1, 1280),     //2
    WEST(-1, 0, 512),           //3
    EAST(1, 0, 1536),           //4
    SOUTH_WEST(-1, -1, 256),    //5
    SOUTH(0, -1, 0),            //6
    SOUTH_EAST(1, -1, 1792);    //7

    public final int deltaX, deltaY;

    public final int clientValue;

    Direction(int deltaX, int deltaY, int clientValue) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.clientValue = clientValue;
    }

    public static Direction get(String cardinal) {
        switch(cardinal.toUpperCase()) {
            case "N":   return Direction.NORTH;
            case "NW":  return Direction.NORTH_WEST;
            case "NE":  return Direction.NORTH_EAST;
            case "W":   return Direction.WEST;
            case "E":   return Direction.EAST;
            case "SW":  return Direction.SOUTH_WEST;
            case "SE":  return Direction.SOUTH_EAST;
            default:    return Direction.SOUTH;
        }
    }


    public static Direction getFromObjectDirection(int direction) {
        switch (direction) {
            case 0:
                return SOUTH;
            case 1:
                return WEST;
            case 2:
                return NORTH;
            case 3:
                return EAST;
            default:
                return null;
        }
    }

    public static Direction fromDoorDirection(int direction) {
        switch (direction) {
            case 0:
                return WEST;
            case 1:
                return NORTH;
            case 2:
                return EAST;
            case 3:
                return SOUTH;
            default:
                return null;
        }
    }

    public static Direction getDirection(WorldTile src, WorldTile dest) {
        int deltaX = dest.getX() - src.getX();
        int deltaY = dest.getY() - src.getY();
        return getDirection(deltaX, deltaY);
    }

    public static Direction getDirection(int deltaX, int deltaY) {
        if (deltaX != 0)//normalize
            deltaX /= Math.abs(deltaX);
        if (deltaY != 0)
            deltaY /= Math.abs(deltaY);
        for (Direction d: Direction.values()) {
            if (d.deltaX == deltaX && d.deltaY == deltaY)
                return d;
        }
        return null;
    }


    public static final Direction[] CARDINAL_DIRECTIONS = {NORTH, EAST, SOUTH, WEST};

    public static Direction getRandomCardinal() {
        return Utils.random(CARDINAL_DIRECTIONS);
    }

    public Direction opposite() {
        switch(this){
            case NORTH_WEST: return SOUTH_EAST;
            case NORTH: return SOUTH;
            case NORTH_EAST: return SOUTH_WEST;
            case WEST: return EAST;
            case EAST: return WEST;
            case SOUTH_WEST: return NORTH_EAST;
            case SOUTH: return NORTH;
            case SOUTH_EAST: return NORTH_WEST;
        }
        return NORTH;
    }
}