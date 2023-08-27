package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.cox.impl.Tekton;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;

import java.util.Arrays;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class TektonChamber extends Chamber {
    private Tekton tekton;
    private WorldObject crystal;
    private WorldObject fire;

    public TektonChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static void init() {
        ObjectHandler.register(130021, 1, ((player, obj) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null && raid.getTektonChamber().getFire() != null) {
                if(obj.matches(raid.getTektonChamber().getFire())) {
                    //if(player.getX() < obj.getX()) {
                        player.applyHit(player, (int) Math.max(50, ((double)player.getHitpoints() * 0.25 - 20.0)), Hit.HitLook.REGULAR_DAMAGE);
                    //}
                    player.lock(3);
                    boolean run = player.isRunning();
                    player.setRun(false);
                    player.addWalkSteps(player.getX() < obj.getX() ? player.getX() + 3 : player.getX() - 3, player.getY(), 3, false);
                    WorldTasksManager.schedule(() -> player.setRun(run), 3);
                } else {
                    player.sendMessage("You see no way of getting through those.");
                }
            }
        }));

        ObjectHandler.register(130019, 1, ((player, obj) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null && raid.getVasaChamber().getFire()!= null) {
                if(obj.matches(raid.getVasaChamber().getFire())) {
                    //if(player.getX() < obj.getX()) {
                    player.applyHit(player, (int) Math.max(50, ((double)player.getHitpoints() * 0.25 - 20.0)), Hit.HitLook.REGULAR_DAMAGE);
                    //}
                    player.lock(3);
                    boolean run = player.isRunning();
                    player.setRun(false);
                    player.addWalkSteps(player.getX() < obj.getX() ? player.getX() + 3 : player.getX() - 3, player.getY(), 3, false);
                    WorldTasksManager.schedule(() -> player.setRun(run), 3);
                } else {
                    player.sendMessage("You see no way of getting through those.");
                }
            }
        }));
    }

    public WorldObject getFire() {
        return fire;
    }

    @Override
    public void onActivation() {
        fire = spawnObject(130021, new WorldTile(29, 15, 0), 10, 0);
    }

    @Override
    public void onRaidStart() {
        tekton = new Tekton(getRaid());
        crystal = spawnObject(130017, new WorldTile(7, 14, 0), 10, 0);
        //World.getRegion(crystal.getRegionId()).forceClip(true, crystal, 2, 2);
    }

    @Override
    public void bossDeath() {
        crystal.anim(27506);
        fire.remove();

        WorldTasksManager.schedule(() -> {
            crystal.remove();
        }, 3);
        super.bossDeath();
    }
}
