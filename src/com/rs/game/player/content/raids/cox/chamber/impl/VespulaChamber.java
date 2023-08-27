package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.cox.impl.Vespula;
import com.rs.game.npc.cox.impl.VespulaGrub;
import com.rs.game.npc.cox.impl.VespulaPortal;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

import java.util.*;

/**
 * @author Simplex
 * @since Nov 02, 2020
 */
public class VespulaChamber extends Chamber {
    public static final int GRUB_MAX_HEALTH = 100;
    private static final int GRUB_BASE_HEALTH_DECAY = 2;
    private static final int GRUB_HEALTH_RESTORE = 25;

    private static WorldTile PORTAL_TILE = new WorldTile(11, 21, 2),
            VESPULA_SPAWN = new WorldTile(11, 8, 2),
            BLOCK_SPAWN = new WorldTile(27, 15, 2);

    private static WorldTile[] GRUB_SPAWNS = {
            new WorldTile(15, 13, 2),
            new WorldTile(8, 13, 2),
            new WorldTile(5, 9, 2),
            new WorldTile(8, 5, 2),
    };

    private static WorldTile[] GRUB_WAYPOINT = {
            new WorldTile(15, 10, 2),
            new WorldTile(8, 10, 2),
            new WorldTile(8, 9, 2),
            new WorldTile(8, 8, 2),
    };
    private static int[][] GRUBS_DIRECTIONS = {
            {0, -1},
            {0, -1},
            {1, 0},
            {0, 1},
    };

    private NPC portal;
    private Vespula vespula;
    private WorldObject crystal, portalObject;
    private int[] grubHealth = new int[4];
    private NPC[] grubs = new NPC[4];

    private List<WorldTile> protectedTiles = new ArrayList<>(2);
    private List<COXBoss> vespineSoldiers = new LinkedList<>();

    public VespulaChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static void init() {
        // Picking root
        ObjectHandler.register(Settings.OSRS_OBJECTS_OFFSET + 30068, 1, ((player, obj) -> {
            if(ChambersOfXeric.getRaid(player) == null)
                return;
            if(player.getInventory().getFreeSlots() < 2) {
                player.sendMessage("Not enough space in your inventory.");
                return;
            }
            player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 827);
            player.getInventory().addItem(Settings.OSRS_ITEM_OFFSET + 20892, 1);
            WorldTasksManager.schedule(() -> {
                if(obj.isRemoved())
                    obj.updateId(Settings.OSRS_OBJECTS_OFFSET + 30068);
            }, 8);
        }));
    }

    public static void feedGrub(Player player, NPC npc) {
        if (!player.getInventory().containsItem(50892, 1)) {
            player.sendMessage("You'll need a medivaemia blossom to feed the grub!");
            return;
        }
        player.anim(20827);
        player.getInventory().deleteItem(50892, 1);
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid != null) {
            raid.getVespulaChamber().feedGrub(npc);
        }
        player.sendMessage("You feed the grub.");
    }

    private void feedGrub(NPC n) {
        for(int i = 0; i < GRUB_SPAWNS.length; i++) {
            if(getWorldTile(GRUB_SPAWNS[i]).matches(n)) {
                if(!grubs[i].isDead() && !portal.isDead()) {
                    grubHealth[i] += GRUB_HEALTH_RESTORE;
                    grubs[i].setHitpoints(grubHealth[i]);
                    grubs[i].applyHit(grubs[i], 0); // refresh hp
                }
            }
        }
    }

    @Override
    public void bossDeath() {
        vespula.applyHit(null, vespula.getHitpoints());
        Arrays.stream(grubs).forEach(grub -> {
            if(!grub.hasFinished())
                grub.sendDeath(grub);
        });
        portalObject.anim(27500);
        crystal.anim(27506);
        WorldTasksManager.schedule(() -> {
            crystal.remove();
            portalObject.updateId(130073);
        }, 3);

        vespineSoldiers.stream().filter(npc -> !npc.hasFinished() && !npc.isDead()).forEach(npc->npc.sendDeath(npc));
    }

    @Override
    public void onActivation() {
        portalObject = getObject(130072, PORTAL_TILE);
        protectedTiles.add(getWorldTile(13, 13));
        protectedTiles.add(getWorldTile(12, 13));
    }

    @Override
    public void onRaidStart() {
        setDefaultActivationoTask();
        // block crystal
        crystal = spawnObject(130018, BLOCK_SPAWN, 10, 0);

        vespula = new Vespula(getRaid(), 27530, getWorldTile(VESPULA_SPAWN), this);

        portal = new VespulaPortal(getRaid(), 27533, getWorldTile(PORTAL_TILE), this, vespula);

        for (int i = 0; i < GRUB_SPAWNS.length; i++) {
            int grubId = i;
            grubHealth[grubId] = (GRUB_MAX_HEALTH / 3) + (GRUB_BASE_HEALTH_DECAY * Utils.random(4, 8));

            grubs[i] = new VespulaGrub(getRaid(), 27535, getWorldTile(GRUB_SPAWNS[i]), this, i);
            grubs[i].setHitpoints(grubHealth[grubId]);
            grubs[i].setDirection(Utils.getAngle(GRUBS_DIRECTIONS[i][0], GRUBS_DIRECTIONS[i][1]));
        }

        // set roots
        WorldTasksManager.schedule(() -> {
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int y = 0; y < TILE_SIZE; y++) {
                    Optional.ofNullable(getObject(130069, x, y, getBaseTile().getPlane())).ifPresent(o -> {
                        o.updateId(130068);
                    });
                }
            }
        }, 3);

        World.addFloor(getWorldTile(4, 16));
        World.addFloor(getWorldTile(4, 15)); // clip under crystal
    }

    private void transformGrub(NPC grub, int grubId) {
        if(grub.getId() == 27537)
            return;

        grub.setNextNPCTransformation(27537);
        grub.gfx(6365);
        grub.setCantInteract(true);

        WorldTasksManager.schedule(() -> {
            grub.finish();
            COXBoss soldier = new COXBoss(getRaid(), 27539, getWorldTile(GRUB_SPAWNS[grubId]), this);
            soldier.setCanWalkNPC(true);
            vespineSoldiers.add(soldier);
            soldier.anim(27452);
            WorldTasksManager.schedule(() -> {
                soldier.setNextNPCTransformation(27538);
                soldier.setup();
                if(vespula.isDead() || vespula.hasFinished()) {
                    // still show anim but since vesp died, instant kill
                    soldier.sendDeath(vespula);
                    return;
                } else {
                    WorldTile waypoint = getWorldTile(GRUB_WAYPOINT[grubId]);
                    soldier.addWalkSteps(waypoint.getX(), waypoint.getY(), 3, false);
                    soldier.lock();
                    WorldTasksManager.schedule(() -> soldier.unlock(), 3);
                }
            });
        }, 2);

        vespula.setHitpoints(vespula.getMaxHitpoints());
        portal.setHitpoints(portal.getMaxHitpoints());

        if (vespula.getId() == 27532) {
            vespula.anim( 27452);
            vespula.setNextNPCTransformation(27530);
        }
    }

    public List<WorldTile> getProtectedTiles() {
        return protectedTiles;
    }

    public void grubMetamorphisis(VespulaGrub g) {
        if(g.hasFinished() || portal.hasFinished() || vespula.getProtectionTicks() > 0) {
            return;
        }

        int grubId = g.getGrubId();

        grubHealth[g.getGrubId()] -= GRUB_BASE_HEALTH_DECAY + (getRaid().getTeamSize() / 5);

        if(grubHealth[grubId]>0) {
            g.setHitpoints(grubHealth[grubId]);
            g.applyHit(g, 0); // refresh hp bar
        } else {
            transformGrub(g, grubId);
        }
    }
}
