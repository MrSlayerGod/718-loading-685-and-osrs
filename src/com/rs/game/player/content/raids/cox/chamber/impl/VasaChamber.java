package com.rs.game.player.content.raids.cox.chamber.impl;

import java.util.LinkedList;
import java.util.List;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.cox.impl.VasaCrystal;
import com.rs.game.npc.cox.impl.VasaNistirio;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class VasaChamber extends Chamber {
    private static final int ACTIVE_CRYSTAL = 129774;
    private static final int DRAINED_CRYSTAL = 129775;
    private static final int BLOCK_CRYSTAL = 130016;
    private static final int CRYSTAL_NPC = 27568;

    public static final WorldTile BLOCK_CRSTAL_SPAWN1 = new WorldTile(4, 15, 1);
    public static final WorldTile BLOCK_CRSTAL_SPAWN2 = new WorldTile(4, 16, 1);

    public static final WorldTile[] CRYSTAL_SPAWNS = {
            new WorldTile(6, 23, 1), //nw
            new WorldTile(23, 23, 1), //ne
            new WorldTile(6, 5, 1), //sw
            new WorldTile(23, 5, 1), //se
    };

    public static final WorldTile[][] CRYSTAL_WAYPOINTS = {
            {new WorldTile(14, 23, 1), new WorldTile(10, 23, 1)},
            {new WorldTile(23, 13, 1), new WorldTile(23, 18, 1)},
            {new WorldTile(5, 14, 1), new WorldTile(5, 9, 1)},
            {new WorldTile(13, 3, 1), new WorldTile(18, 3, 1)},
    };
    private VasaNistirio vasa;

    public static final WorldTile VASA_SPAWN = new WorldTile(14, 13, 1);

    public WorldObject crystal1, crystal2;
    public List<WorldObject> crystalObjects = new LinkedList<>();
    public List<VasaCrystal> crystalNPCs = new LinkedList<>();

    public VasaChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    private WorldObject fire;

    @Override
    public void onActivation() {
        fire = spawnObject(130019, new WorldTile(30, 15, 1), 10, 0);
    }

    @Override
    public void onRaidStart() {
        vasa = new VasaNistirio(getRaid(), 27565, getRaid().getTile(78, 77, 1), this);

        // block enterance
        crystal1 = new WorldObject(BLOCK_CRYSTAL, 10, 0, getWorldTile(BLOCK_CRSTAL_SPAWN1));
        crystal2 = new WorldObject(BLOCK_CRYSTAL, 10, 0, getWorldTile(BLOCK_CRSTAL_SPAWN2));
        World.spawnObject(crystal1);
        World.spawnObject(crystal2);

        // spawn crystals
        for (int i = 0; i < 4; i++) {
            crystalObjects.add(spawnObject(DRAINED_CRYSTAL, CRYSTAL_SPAWNS[i], 10, 0));
            crystalNPCs.add(new VasaCrystal(getRaid(), CRYSTAL_NPC, getWorldTile(CRYSTAL_SPAWNS[i]), this));
        }
        crystalNPCs.forEach(npc -> {
            npc.setForceMultiArea(true);
            npc.setForceMultiAttacked(true);
        });
    }

    @Override
    public void bossDeath() {
        World.unclipTile(crystal1);
        World.unclipTile(crystal2);
        crystal1.remove();
        crystal2.remove();
        fire.remove();
    }

    public VasaNistirio getVasa() {
        return vasa;
    }

    public WorldObject getFire() {
        return fire;
    }
}