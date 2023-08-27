package com.rs.game.player.content.raids.cox.chamber;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.impl.GreatOlmChamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public abstract class Chamber {
    public static final int CHUNK_SIZE = 4;
    public static final int TILE_SIZE = CHUNK_SIZE * 8;

    protected int getTileSize() {
        return TILE_SIZE;
    }
    protected int getChunkSize() {
        return CHUNK_SIZE;
    }

    private ChambersOfXeric raid;
    private WorldTile baseTile;

    public boolean isActivated() {
        return activated;
    }


    public List<Player> getTeam() {
        WorldTile tile = raid.getMapTileBaseWorldTile(getBaseTile().getX(), getBaseTile().getY(), getBaseTile().getPlane());

        return raid.getTeam().stream().filter(player ->
                player.getPlane() == tile.getPlane()
                        && player.getX() > tile.getX()
                        && player.getX() < tile.getX() + 32
                        && player.getY() > tile.getY()
                        && player.getY() < tile.getY() + 32)
                .collect(Collectors.toList());
    }

    public void setActivated(boolean activated) {
        if(!this.activated && activated) {
            this.onActivation();
        }
        this.activated = activated;
    }

    public void onActivation() {}

    private boolean activated;

    public abstract void onRaidStart();

    private boolean init = false;

    public boolean initChamber() {
        if(init) return false;
        else {
            init = true;
            return true;
        }
    }

    public void setRaid(ChambersOfXeric raid) {
        this.raid = raid;
    }

    public ChambersOfXeric getRaid() {
        return raid;
    }

    public WorldTile getBaseTile() {
        return baseTile;
    }

    public Chamber(int x, int y, int z, ChambersOfXeric raid) {
        this.setRaid(raid);
        this.setBaseTile(x, y, z);
    }

    String debug = "N/A";

    public void setDebug(String s) {
        debug = s;
    }

    public String getDebug() {
        return getClass().getSimpleName() + ": " + debug;
    }

    public void setBaseTile(int x, int y, int z) {
        baseTile = new WorldTile(x, y, z);
    }

    public WorldObject spawnObject(int id, WorldTile tile, int type, int rotation) {
        WorldObject obj = new WorldObject(id, type, rotation, getWorldTile(tile));
        World.spawnObject(obj);
        return obj;
    }

    public WorldObject getObject(int id, WorldTile chamberTile) {
        WorldTile worldTile = getWorldTile(chamberTile);
        return World.getObjectWithId(worldTile, id);
    }

    public WorldObject getNormalObject(WorldTile chamberTile) {
        WorldTile worldTile = getWorldTile(chamberTile);
        return World.getObjectWithType(worldTile, 10);
    }

    public WorldObject getObject(int id, int x, int y, int z) {
        return getObject(id, new WorldTile(x, y, z));
    }

    public WorldTile getWorldTile(int x, int y) {
        return getWorldTile(new WorldTile(x, y, getBaseTile().getPlane()));
    }

    public WorldTile getWorldTile(WorldTile tile) {
        if (!(this instanceof GreatOlmChamber)
               && (tile.getX() < 0 || tile.getX() > getTileSize() || tile.getY() < 0 || tile.getY() > getTileSize())) {
            throw new IllegalArgumentException("Tile coords out of bounds - must be within chamber bounds [tile:" + tile +"]");
        }

        return raid.getMapTileBaseWorldTile(baseTile.getX(), baseTile.getY(), baseTile.getPlane())
                .transform(tile.getX(), tile.getY(), 0);
    }

    /**
     * remove blocks from doors
     */
    public void bossDeath() {
    }

    protected void setDefaultActivationoTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
            	if (raid.hasFinished()) {
            		stop();
            		return;
            	}
                setDebug("activated");
                if(getTeam().size() > 0) {
                    setActivated(true);
                    stop();
                }
            }
        }, 0, 0);
    }

    public boolean chamberCompleted(Player player) {
        return true;
    }
}
