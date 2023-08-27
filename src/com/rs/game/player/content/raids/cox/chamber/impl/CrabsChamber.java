package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.utils.Direction;
import com.rs.utils.Stopwatch;
import com.rs.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Nov 05, 2020
 */
public class CrabsChamber extends Chamber {

    public static final int[] STUN_WEAPONS = {43576, 51003, 4747, 4958, 4959, 4960, 4961, 4962, 4154, 51742, 7441, 7449, 25640, 25547, 25630, 13902};
    private static final WorldTile ENERGY_SPAWN_TILE = new WorldTile(17, 18, 0);

    private static final WorldTile[] CRYSTAL_TILES = {
            new WorldTile(8, 12, 0),
            new WorldTile(12, 12, 0),
            new WorldTile(20, 13, 0),
            new WorldTile(21, 9, 0)
    };
    private static final WorldTile[] CRAB_TILES = {
            new WorldTile(8, 7, 0),
            new WorldTile(7, 14, 0),
            new WorldTile(10, 10, 0),
            new WorldTile(14, 10, 0),
            new WorldTile(16, 13, 0),
            new WorldTile(19, 7, 0)
    };

    private static final int ENERGY_FOCUS_WHITE_OBJ = 27580;
    private static final int ENERGY_FOCUS_RED_OBJ = 27581;
    private static final int ENERGY_FOCUS_GREEN_OBJ = 27582;
    private static final int ENERGY_FOCUS_BLUE_OBJ = 27583;

    private static final int CRYSTAL_BLACK_NPC = 129758;
    private static final int CRYSTAL_CYAN_NPC = 129759;
    private static final int CRYSTAL_MAGENTA_NPC = 129760;
    private static final int CRYSTAL_YELLOW_NPC = 129761;
    private static final int CRYSTAL_WHITE_NPC = 129762;

    private static final int DIM_EXIT_CRYSTAL_ID = 129756;
    private static final int BRIGHT_EXIT_CRYSTAL_ID = 129757;
    private static final int RED_CRAB = 27577, BLUE_CRAB = 27579, GREEN_CRAB = 27578, WHITE_CRAB = 27576;
    private static int[][] CRYSTAL_SOLVE = {
            {CRYSTAL_BLACK_NPC, ENERGY_FOCUS_WHITE_OBJ},
            {CRYSTAL_YELLOW_NPC, ENERGY_FOCUS_BLUE_OBJ},
            {CRYSTAL_CYAN_NPC, ENERGY_FOCUS_RED_OBJ},
            {CRYSTAL_MAGENTA_NPC, ENERGY_FOCUS_GREEN_OBJ},
    };
    private NPC energyFocus = null;
    private NPC[] crabs;
    private WorldObject[] crystals;
    private WorldObject exitCrystal;
    private int crystalsSolved = 0;

    private boolean puzzleCompleted = false;

    public CrabsChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static void init() {
        for (int i = 27576; i <= 27579; i++) {
            //for(int j = 0; j < 5; j++)
            NPCHandler.register(i, 2, ((player, npc) -> {
                ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
                player.faceEntity(npc);
                if (raid != null) {
                    raid.getCrabsChamber().stunCrab(player, npc);
                }
            }));
        }
    }

    @Override
    public void onRaidStart() {
        crabs = new NPC[6];
        for (int i = 0; i < crabs.length; i++) {
            crabs[i] = new NPC(WHITE_CRAB, getWorldTile(CRAB_TILES[i]), -1, true) {
                Stopwatch resetColor = null;

                @Override
                public void handleIngoingHit(Hit hit) {
                    resetColor.reset();
                    resetColor.delayMS(10000);

                    //stunCrab(hit.getSource(), this);

                    if (hit.getLook() == Hit.HitLook.MELEE_DAMAGE) {
                        setNextNPCTransformation(RED_CRAB);
                    } else if (hit.getLook() == Hit.HitLook.MAGIC_DAMAGE) {
                        setNextNPCTransformation(BLUE_CRAB);
                    } else if (hit.getLook() == Hit.HitLook.RANGE_DAMAGE) {
                        setNextNPCTransformation(GREEN_CRAB);
                    }

                    hit.setDamage(0);
                }

                @Override
                public void processNPC() {
                    if (resetColor == null) {
                        resetColor = new Stopwatch();
                        resetColor.delayMS(10000);
                    } else if (resetColor.finished()) {
                        setNextNPCTransformation(WHITE_CRAB);
                    }
                    super.processNPC();
                }
            };
            crabs[i].setSpawned(true);
            crabs[i].setForceMultiAttacked(true);
            crabs[i].setAtMultiArea(true);
            crabs[i].setForceMultiArea(true);
        }
        exitCrystal = new WorldObject(DIM_EXIT_CRYSTAL_ID, 10, 0, getWorldTile(8, 21));
        setDefaultActivationoTask();
        World.spawnObject(exitCrystal);
    }

    @Override
    public boolean chamberCompleted(Player player) {
        if(!puzzleCompleted) {
            return false;
        }
        return super.chamberCompleted(player);
    }

    private void stunCrab(Entity source, NPC crab) {
        if (source.isPlayer()) {
            int wep = source.asPlayer().getEquipment().getWeaponId();
            if ((source.asPlayer().getInventory().containsItem(2347, 1) || Arrays.stream(STUN_WEAPONS).anyMatch(i -> i == wep))) {
                source.anim(PlayerCombat.getWeaponAttackEmote(wep, 0));
                crab.freeze(Utils.currentTimeMillis() + 25000);
                Entity prevTarget = crab.getCombat().getTarget();
                WorldTasksManager.schedule(new WorldTask() {
                    int tick = 0;

                    @Override
                    public void run() {
                        if (crab.isDead() || crab.hasFinished()) {
                            stop();
                        } else if (!crab.isFrozen()) {
                            if (prevTarget != null) {
                                // sometimes after freeze doesnt hold agro
                                crab.getCombat().setTarget(prevTarget);
                            }
                            crab.gfx(-1);
                            stop();
                        } else {
                            if (tick++ % 13 == 0) {
                                crab.setNextGraphics(new Graphics(5080, 0, 60));
                            }
                        }
                    }
                }, 0, 0);
            } else {
                source.asPlayer().sendMessage("You need a smash weapon or a hammer to stun the crab!");
            }
        }
    }

    @Override
    public void onActivation() {
        crystals = new WorldObject[4];
        for (int i = 0; i < crystals.length; i++) {
            crystals[i] = World.getObjectWithType(getWorldTile(CRYSTAL_TILES[i]), 10);
            ;
        }

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if(getRaid() == null || getRaid().hasFinished()) {
                    stop();
                    return;
                }
                setDebug("solved=" + crystalsSolved);
                puzzleCompleted = crystalsSolved >= 4;

                if (puzzleCompleted) {
                    removeExitCrystal();
                    stop();
                } else {
                    if (energyFocus == null || energyFocus.hasFinished()) {
                        spawnEnergyFocus();
                    }
                }
            }
        }, 0, 0);
    }

    private void removeExitCrystal() {
        exitCrystal.updateId(BRIGHT_EXIT_CRYSTAL_ID);
        WorldTasksManager.schedule(() -> {
            exitCrystal.remove();
        }, 3);
        Arrays.stream(crabs).forEach(crab -> crab.sendDeath(crab));
    }

    private void spawnEnergyFocus() {
        WorldTile spawnTile = getWorldTile(ENERGY_SPAWN_TILE);
        energyFocus = new NPC(ENERGY_FOCUS_WHITE_OBJ, spawnTile, -1, false) {
            Direction walkDir = Direction.SOUTH;

            public void switchDir() {
                if (walkDir == Direction.SOUTH) {
                    walkDir = Direction.WEST;
                } else if (walkDir == Direction.WEST) {
                    walkDir = Direction.NORTH;
                } else if (walkDir == Direction.NORTH) {
                    walkDir = Direction.EAST;
                } else {
                    walkDir = Direction.SOUTH;
                }
            }

            private void explode() {
                int id = energyFocus.getId();
                int gfx = id == ENERGY_FOCUS_WHITE_OBJ ? 5160 : id == ENERGY_FOCUS_BLUE_OBJ ? 5163 : id == ENERGY_FOCUS_GREEN_OBJ ? 5166 : 5157;
                World.sendGraphics(energyFocus, new Graphics(gfx, 1, 124), energyFocus.clone());
                finish();
            }

            private void transform(NPC crab) {
                int npc = -1;
                if (crab.getId() == RED_CRAB)
                    npc = ENERGY_FOCUS_RED_OBJ;
                else if (crab.getId() == GREEN_CRAB)
                    npc = ENERGY_FOCUS_GREEN_OBJ;
                else if (crab.getId() == BLUE_CRAB)
                    npc = ENERGY_FOCUS_BLUE_OBJ;
                //osrs doesn't change color on white
                //else npc = ENERGY_FOCUS_WHITE_OBJ;
                if (npc != -1)
                    setNextNPCTransformation(npc);
            }

            @Override
            public void processNPC() {
                resetWalkSteps();
                WorldTile nextTile = new WorldTile(getX() + walkDir.deltaX, getY() + walkDir.deltaY, 3);
                boolean continueWalking = false;
                for (int i = 0; i < 4; i++) {
                    WorldTile T = nextTile;
                    World.sendGraphics(2322, T.clone());
                    Optional<NPC> crab = Arrays.stream(crabs).filter(npc -> npc.matches(T)).findAny();
                    if (crab.isPresent()) {
                        switchDir();
                        transform(crab.get());
                        nextTile = new WorldTile(getX() + walkDir.deltaX, getY() + walkDir.deltaY, 3);
                    } else {
                        List<Player> plrs = getTeam().stream().filter(player -> player.matches(T)).collect(Collectors.toList());
                        WorldObject obj = World.getObjectWithType(T, 10);
                        if (plrs.size() != 0) {
                            continueWalking = false;
                            setDebug("explode - player obstructing");
                            plrs.forEach(player -> player.applyHit(energyFocus, 250, Hit.HitLook.MAGIC_DAMAGE));
                        } else if (obj != null && Arrays.stream(CRYSTAL_SOLVE).anyMatch(ints -> obj.getId() == ints[0])) {
                            int solve = -1;
                            for (int[] cs : CRYSTAL_SOLVE) {
                                if (cs[0] == obj.getId()) {
                                    solve = cs[1];
                                    break;
                                }
                            }
                            if (solve == getId()) {
                                getTeam().forEach((player -> getRaid().addPoints(player, 100)));
                                obj.updateId(CRYSTAL_WHITE_NPC);
                                crystalsSolved++;
                            }
                        } else if (World.isTileFree(T, 1)) {
                            continueWalking = true;
                        }
                        break;
                    }
                }

                if (!continueWalking) {
                    explode();
                } else {
                    addWalkSteps(nextTile.getX(), nextTile.getY(), 1);
                }
            }
        };
        energyFocus.setSpawned(true);
        energyFocus.setRandomWalk(0);
    }

    // admin-only method to move focus to their position (easily solve crabs puzzle)
    public void moveFocus(Player player) {
        energyFocus.setNextWorldTile(player.clone());
        player.sendMessage("Cheat activated.");
    }
}
