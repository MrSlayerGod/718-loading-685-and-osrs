package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.impl.Vanguard;
import com.rs.game.player.Player;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colour;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class VanguardChamber extends Chamber {
    private static final double HEAL_THRESHOLD = 0.4;

    public static final int
            MELEE = Settings.OSRS_NPC_OFFSET + 7527,
            MAGE = Settings.OSRS_NPC_OFFSET + 7529,
            RANGED = Settings.OSRS_NPC_OFFSET + 7528;

    private static int[] SPAWN_ANIM = {27438, 27433, 27443};
    private static int[] VANGUARD_ID = {MELEE, MAGE, RANGED};

    private Vanguard[] vanguard;

    private WorldTile[] focusTiles;

    private WorldObject crystal = null;

    private int[] lastTile = {0, 1, 2};
    private int[] hp = new int[3];

    public boolean walking = false;

    public VanguardChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    @Override
    public void onActivation() {
        for(int i = 0; i < vanguard.length; i++) {
            Vanguard n = vanguard[i];
            n.lock();
            n.anim(SPAWN_ANIM[i]);
            final int I = i;
            WorldTasksManager.schedule(() -> {
                n.setNextNPCTransformation(VANGUARD_ID[I]);
                n.setHitpoints(n.getMaxHitpoints());
                n.unlock();
            });
        }
    }

    @Override
    public void onRaidStart() {
        setBaseTile(3, 1, 2);
        ChambersOfXeric raid = getRaid();
        crystal = new WorldObject(130017, 10, 0, getWorldTile(3, 15));
        World.spawnObject(crystal);

        focusTiles = new WorldTile[3];
        focusTiles[0] = raid.getTile(103, 45, 2);
        focusTiles[1] = raid.getTile(113, 45, 2);
        focusTiles[2] = raid.getTile(108, 51, 2);

        vanguard = new Vanguard[3];
        vanguard[0] = new Vanguard(VANGUARD_ID[0], raid.getTile(103, 45, 2), raid);
        vanguard[1] = new Vanguard(VANGUARD_ID[1], raid.getTile(113, 45, 2), raid);
        vanguard[2] = new Vanguard(VANGUARD_ID[2], raid.getTile(108, 51, 2), raid);

        World.addFloor(getWorldTile(4, 16));
        World.addFloor(getWorldTile(4, 15)); // clip under crystal

        for(NPC n : vanguard)
            n.setNextNPCTransformation(27525);

        WorldTasksManager.schedule(new WorldTask() {
            int tick = -1;
            @Override
            public void run() {
                if(!isActivated()) {
                    checkActivation();
                    return;
                }

                if(vanguard[0].isLocked()) {
                    // only happens during activation
                    return;
                }
                if(Arrays.stream(vanguard).allMatch(v->v.isDead() || v.hasFinished())) {
                    World.sendObjectAnimation(crystal, new Animation(27506));
                    WorldTasksManager.schedule(()
                            -> crystal.remove(), 3);
                    World.unclipTile(getWorldTile(4, 16));
                    World.unclipTile(getWorldTile(4, 15)); // clip under crystal
                    stop();
                    return;
                }

                List<Vanguard> aliveList = Arrays.stream(vanguard).filter(v->!v.isDead() && !v.hasFinished()).collect(Collectors.toList());

                if(aliveList.size() > 1) {
                    boolean heal = checkHeal(aliveList);

                    if(heal) {
                        aliveList.forEach(v -> {
                            //v.forceTalk("Healing - dif > " +(int)  (HEAL_THRESHOLD * 100) + "%");
                            v.setHitpoints(v.getMaxHitpoints());
                        });
                    }
                }

                if(tick++ % 25 != 0)
                    return;

                delayWalk();
            }
        }, 0, 0);
    }

    private void checkActivation() {
        for(Vanguard v : vanguard) {
            if(v != null) {
                if (getTeam().stream().anyMatch(p->p.distance(v) < 11)) {
                    setActivated(true);
                }
            }
        }
    }

    /**
     * Delay walk to allow attacks to finish
     */
    private void delayWalk() {
        walking = true;

        WorldTasksManager.schedule(() -> {
            walk();
        }, 2);
    }
    private void walk() {
        walking = true;

        for(int i = 0; i < 3; i ++) {
            lastTile[i]++;
            WorldTile tile = focusTiles[lastTile[i]%3];
            Vanguard v = vanguard[i];
            v.resetWalkSteps();
            v.getCombat().reset();
            v.setNextFaceEntity(null);
            v.setCantInteract(true);
            //v.setLocked(true);
            hp[i] = vanguard[i].getHitpoints();
            v.anim(v.getCombatDefinitions().getDeathEmote());
            v.setNextNPCTransformation(27526);
            v.setHitpoints(hp[i]);
            v.addWalkSteps(tile.getX(), tile.getY(), -1, false);
            v.setRespawnTile(tile);
            //v.forceTalk("transform -> move to " + lastTile[i] % 3 + " => " + lastTile[i]);
        }

        // reset after movement
        WorldTasksManager.schedule(new WorldTask() {
            int failSafe = 15;
            @Override
            public void run() {
                if(failSafe-- > 0) {
                    for(int i = 0; i < vanguard.length; i++) {
                        if(!vanguard[i].isDead() && !vanguard[i].hasFinished() && !vanguard[i].matches(focusTiles[lastTile[i] % 3])) {
                            return;
                        }
                    }
                } else {
                    // failsafe - set position manually
                    for(int i = 0; i < vanguard.length; i++) {
                        if(!vanguard[i].isDead() && !vanguard[i].hasFinished()) {
                            vanguard[i].setNextWorldTile(focusTiles[lastTile[i] % 3]);
                        }
                    }
                }
                for(int i = 0; i < vanguard.length; i++) {
                    if(vanguard[i].isDead() || vanguard[i].hasFinished())
                        continue;
                    vanguard[i].anim(SPAWN_ANIM[i]);
                    hp[i] = vanguard[i].getHitpoints();
                    vanguard[i].setNextNPCTransformation(VANGUARD_ID[i]);
                    vanguard[i].setHitpoints(hp[i]);
                    vanguard[i].setCantInteract(false);
                    vanguard[i].setup();
                    walking = false;
                    stop();
                };
            }
        }, 0, 0);
    }

    private boolean checkHeal(List<Vanguard> list) {
        double highestRatio = -1;
        for(Vanguard vanguard : list) {
            double dif = getHealthRatio(vanguard);
            if(highestRatio == -1 || dif > highestRatio) {
                highestRatio = dif;
            }
        }

        for(Vanguard vanguard : list) {
            double ratio = highestRatio - getHealthRatio(vanguard);
            //vanguard.forceTalk("" + (int) (ratio * 100) + "%");
            if(ratio > HEAL_THRESHOLD) {
                return true;
            } else if(ratio > HEAL_THRESHOLD - 0.1) {
                vanguard.gfx(1314);
            }
        }

        return false;
    }

    private double getHealthRatio(Vanguard v) {
        return (double)v.getHitpoints() / v.getMaxHitpoints();
    }
}
