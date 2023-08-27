package com.rs.game.player.content.teleportation;

import com.rs.game.WorldTile;

import java.io.Serializable;

/**
 * @author Simplex
 * @since Sep 26, 2020
 */
public class
Teleport implements Serializable {
    public static final long serialVersionUID = 9222339617569112134L;

    public Teleport(String name, int category, WorldTile tile, boolean wild, int modelType, int entityID, String description) {
        this.name = name;
        this.tile = tile;
        this.category = category;
        this.wild = wild;
        this.modelType = modelType;
        this.entityID = entityID;
        this.description = description;
    }

    public String name;
    public WorldTile tile;
    public int category;
    public int modelType;
    public int entityID;
    public String description;
    public boolean wild;
}
