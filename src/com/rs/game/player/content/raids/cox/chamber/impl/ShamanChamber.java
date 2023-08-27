package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class ShamanChamber extends Chamber {
    public WorldTile[] LIZARDMEN_TILES = {
            new WorldTile(115, 74, 3),
            new WorldTile(115, 83, 3),
            new WorldTile(110, 79, 3)
    };

    private NPC[] lizardmen;
    private int killed;

    public WorldObject[] getEntranceTendrils() {
        return entranceTendrils;
    }

    private WorldObject[] entranceTendrils = new WorldObject[2], exitTendrils = new WorldObject[2];

    public ShamanChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static void init() {
        ObjectHandler.register(129768, 1, (ShamanChamber::passTendrils));
    }

    private static void passTendrils(Player player, WorldObject obj) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);

        if(raid != null) {
            if(Arrays.stream(raid.getShamanChamber().getEntranceTendrils()).filter(Objects::nonNull).anyMatch(object -> obj.matches(object))) {
                if(player.getX() > obj.getX()) {
                    player.applyHit(player, (int) Math.max(50, ((double)player.getHitpoints() * 0.25 - 20.0)), Hit.HitLook.POISON_DAMAGE);
                }
                player.lock(3);
                boolean run = player.isRunning();
                player.setRun(false);
                player.addWalkSteps(player.getX() < obj.getX() ? player.getX() + 2 : player.getX() - 2, player.getY(), 2, false);
                WorldTasksManager.schedule(() -> player.setRun(run), 3);
            } else if(Arrays.stream(raid.getMuttadileChamber().getEntranceTendrils()).filter(Objects::nonNull).anyMatch(object -> obj.matches(object))) {
                if(player.getX() > obj.getX()) {
                    player.applyHit(player, (int) Math.max(50, ((double)player.getHitpoints() * 0.25 - 20.0)), Hit.HitLook.POISON_DAMAGE);
                }
                player.lock(3);
                boolean run = player.isRunning();
                player.setRun(false);
                player.addWalkSteps(player.getX() < obj.getX() ? player.getX() + 2 : player.getX() - 2, player.getY(), 2, false);
                WorldTasksManager.schedule(() -> player.setRun(run), 3);
            } else {
                player.sendMessage("You see no way of getting through those.");
            }
        }
    }

    @Override
    public void bossDeath() {
        killed++;
        if(killed == lizardmen.length) {
            removeBarriers();
        }
    }

    private void removeBarriers() {
        for(WorldObject o : entranceTendrils)
            o.remove();
        for(WorldObject o : exitTendrils)
            o.remove();
    }

    @Override
    public void onActivation() {
        entranceTendrils[0] = new WorldObject(129768, 10, 0, getWorldTile(3, 16));
        entranceTendrils[1] = new WorldObject(129768, 10, 0, getWorldTile(3, 17));
        exitTendrils[0] = new WorldObject(129768, 10, 0, getWorldTile(27, 16));
        exitTendrils[1] = new WorldObject(129768, 10, 0, getWorldTile(27, 15));
        for(WorldObject o : entranceTendrils)
            World.spawnObject(o);
        for(WorldObject o : exitTendrils)
            World.spawnObject(o);
    }


    static final Drop[] drops = {
            new Drop(50909, 5, 10),    // Buchu Seed
            new Drop(50906, 5, 10),    // Golpar Seed
            new Drop(50903, 5, 10)    // Noxifer Seed
    };

    @Override
    public void onRaidStart() {
        setDefaultActivationoTask();
        lizardmen = new COXBoss[3];
        for(int i = 0; i < lizardmen.length; i++) {
            lizardmen[i] = new COXBoss(getRaid(), 26766, getRaid().getTile(LIZARDMEN_TILES[i].getX(), LIZARDMEN_TILES[i].getY(), 3), this) {
                @Override
                public void sendDeath(Entity source) {
                    bossDeath();
                    super.sendDeath(source);
                }

                @Override
                public void processNPC() {
                    super.processNPC();
                    if(getCombat().getTarget() != null && getCombat().getTarget().distance(this) > 5)
                        swapTarget();
                }

                @Override
                public void drop() {
                    for(Drop drop : drops)
                        sendDrop(getMostDamageReceivedSourcePlayer(), drop);
                }
            };
            lizardmen[i].setForceMultiAttacked(true);
            lizardmen[i].setForceMultiArea(true);
            lizardmen[i].setSpawned(true);
        }
        // TODO door object
    }
}
