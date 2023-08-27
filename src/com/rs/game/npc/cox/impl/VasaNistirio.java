package com.rs.game.npc.cox.impl;

import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.VasaChamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.tasks.WorldTasksManager.WorldTaskList;
import com.rs.utils.Direction;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Nov 06, 2020
 */
public class VasaNistirio extends COXBoss {

    private static final Projectile TELEPORT_ATTACK_PROJECTILE = new Projectile(6327, 90, 0, 0, 15, 24, 0);
    private static final Projectile ROCK_PROJECTILE = new Projectile(6329, 90, 0, 0, 40, 16, 0);
    private WorldTile[] tpAttackProjectiles; // WorldTiles where the teleport attack projectiles are fired
    private WorldTile spawnTile;
    private List<WorldTile> closeTeleportPositions;
    private List<WorldTile> farTeleportPositions;
    private VasaChamber vasaChamber;
    private WorldObject selectedCrystalObject = null;
    private VasaCrystal selectedCrystalNPC = null;

    private int charges;
    private WorldTaskList teleportAttackTasks = new WorldTaskList(), channelTasks = new WorldTaskList();
    private VasaCrystal prevSelection = null;
    private int kickRocksDelay;

    public VasaNistirio(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        vasaChamber = (VasaChamber) chamber;
        setDirection(Direction.WEST, true);
        tpAttackProjectiles = new WorldTile[]{
                tile.relative(-1, -1),
                tile.relative(2, -1),
                tile.relative(5, -1),
                tile.relative(5, 2),
                tile.relative(5, 5),
                tile.relative(2, 5),
                tile.relative(-1, 5),
                tile.relative(-1, 2),
        };

        closeTeleportPositions = tile.relative(2, 2).area(3, pos -> !pos.collides(this)); // edges of the boss
        farTeleportPositions = new ArrayList<>(36);
        farTeleportPositions.addAll(tile.relative(-6, 3).area(1));
        farTeleportPositions.addAll(tile.relative(2, 11).area(1));
        farTeleportPositions.addAll(tile.relative(10, 2).area(1));
        farTeleportPositions.addAll(tile.relative(1, -6).area(1));
        setForceAgressive(false);
        setCantSetTargetAutoRelatio(true);
        setCantFollowUnderCombat(true);
        setFrozeBlocked(Integer.MAX_VALUE);
        spawnTile = this.clone();


       setDrops();
    }

    @Override
    public boolean preAttackCheck(Player player) {
        if (getId() == 27567) {
            player.sendMessage("Vasa Nistirio is invulnerable to attacks while channeling power from the crystal!");
            return false;
        }
        return true;
    }

    @Override
    public void faceEntity(Entity target) {
        // no facing entity
    }

    @Override
    public void setNextFaceEntity(Entity entity) {
        // no facing entity
    }

    @Override
    public void faceEntity2(Entity target) {
        // no facing entity
    }

    public void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        for(Drop drop : DROPS) {
            dList[Drops.ALWAYS].add(drop);
        }
        drops.addDrops(dList);
        NPCDrops.addDrops(27565, drops);
        NPCDrops.addDrops(27566, drops);
        NPCDrops.addDrops(27567, drops);
        NPCDrops.addDrops(27566, drops);
    }

    private static Drop[] DROPS =
    {
        new Drop(50888, 1, 1), // Nistirio's manifesto
        new Drop(50911, 5, 5),    // Endarkened juice
        new Drop(50984, 2, 2),    // Xeric's aid (+)(4)
        new Drop(50936, 2, 2)    // Twisted (+)(4)
    };

    public WorldTaskList teleportAttackTaskList() {
        charges = 0;
        WorldTaskList tasks = new WorldTaskList();
        VasaNistirio vasa = this;

        tasks.setCancelCondition(() -> isDead() || hasFinished());

        if (getId() == 27565) {
            anim(27408);
            tasks.add(() -> {
                setNextNPCTransformation(27566);
            });
            tasks.add(() -> {
                anim(27409);
            });
        } else {
            anim(27409);
        }

        debug("starting teleport attack");

        // mark all players within distance, these players will be hit
        List<Player> players = getTeam();

        if(players.size() == 0) {
            return new WorldTaskList();
        }

        tasks.add(() -> {
            List<Player> selectedPlayers = new ArrayList<>();
            selectedPlayers.addAll(getTeam());
            if (selectedPlayers.size() > 1) {
                // remove half the players at random
                Collections.shuffle(selectedPlayers);
                for (int i = 0; i < selectedPlayers.size() / 2; i++)
                    selectedPlayers.remove(0);
            }

            int totalDamage = 0;
            for (Player player : selectedPlayers)
                totalDamage += player.getHitpoints() > 50 ? player.getHitpoints() - 50 : 50;
            final int damage = selectedPlayers.size() == 0 ? 0 : totalDamage / selectedPlayers.size();

            getTeam().stream().filter(p->selectedPlayers.contains(p)).forEach(player -> {
                //teleport far
                player.setNextWorldTile(Utils.get(farTeleportPositions).clone());
            });

            selectedPlayers.forEach((player -> {
                //decimation
                WorldTasksManager.schedule(() -> {
                    if(!player.isDead() && getRaid().getCurrentChamber(player) == vasaChamber)
                        player.anim(1816);
                });
                player.gfx(6296);

                // world task fired inside task list allows vasa to start next attack before damage (same on osrs)
                // this delay will stop vasa from sdtacking huge damage
                kickRocksDelay = 10;

                if(vasa.isDead() || vasa.hasFinished()) {
                    return;
                }
                //teleport close
                player.stopAll();
                player.addFreezeDelay(6000, false);
                //player.setNextAnimation(new Animation(424));
                WorldTasksManager.schedule(() -> {
                    if(raid.getCurrentChamber(player) == vasaChamber && !player.isDead()) {
                        // hit / extended stun gfx
                        for (int i = 0; i < 2; i++)
                            WorldTasksManager.schedule(() -> player.setNextGraphics(new Graphics(80, 5, 60)), i * 3);
                        player.setNextWorldTile(Utils.get(closeTeleportPositions).clone());
                        player.anim(-1);

                        WorldTasksManager.schedule(() -> {
                            if(vasa.isDead() || vasa.hasFinished()) {
                                return;
                            }
                            player.applyHit(vasa, damage);
                        }, 7);
                    }
                }, 2);
            }));
        });

        tasks.delay(2);

        tasks.add(() -> {
            // fire projectiles
            //int lms = 1800;
            for (WorldTile pos : tpAttackProjectiles) {
                int ms = TELEPORT_ATTACK_PROJECTILE.fire(vasa, pos);
                //lms = lms > ms ? lms : ms;
                WorldTasksManager.schedule(() -> World.sendGraphics(6328, pos), 2);
            }
        });

        //tasks.delay(2);
        tasks.add(() -> anim(-1));

        return tasks;
    }

    public void restart() {
        channelTasks.cancel();
        ;
        teleportAttackTaskList().cancel();
        setNextWorldTile(spawnTile.clone());
        setNextNPCTransformation(27565);
    }

    @Override
    public void sendDeath(Entity killer) {
        getChamber().bossDeath();
        anim(27415);
        for (NPC npc : ((VasaChamber) getChamber()).crystalNPCs) {
            setCrystalVulnerable(npc, false);
            npc.finish();
        }

        setHitpoints(0);

        WorldTasksManager.schedule(() -> {
            super.sendDeath(killer);
        }, 2);
    }

    public WorldTile[] getWaypoints(WorldObject o) {
        int i;
        List<WorldObject> objects = ((VasaChamber) getChamber()).crystalObjects;
        for (i = 0; i < objects.size(); i++)
            if (objects.get(i).matches(o))
                break;
        WorldTile[] waypoints = {VasaChamber.CRYSTAL_WAYPOINTS[i][0].clone(), VasaChamber.CRYSTAL_WAYPOINTS[i][1].clone()};

        // convert from chamber coords to real coords
        for (int j = 0; j < waypoints.length; j++)
            waypoints[j] = getChamber().getWorldTile(waypoints[j]);

        return waypoints;
    }

    public WorldTask waypointTask(boolean toSpawn, WorldObject channelCrystal) {
        return new WorldTask() {
            WorldTile[] waypoints = getWaypoints(channelCrystal);
            WorldTile endTile = toSpawn ? spawnTile.clone() : waypoints[1];
            boolean atWaypoint;

            @Override
            public void run() {
                if (isDead()) {
                    stop();
                    return;
                }
                if (kickRocksDelay-- <= 0 && kickRocksDelay % 3 == 0)
                    kickRocks();

                if (!atWaypoint) {
                    if (!matches(waypoints[0])) {
                        addWalkSteps(waypoints[0].getX(), waypoints[0].getY(), -1, false);
                        return;
                    } else {
                        atWaypoint = true;
                    }
                }

                resetWalkSteps();
                addWalkSteps(endTile.getX(), endTile.getY(), -1, false);
                if (matches(endTile)) {
                    if (!toSpawn && atWaypoint) {
                        faceObject(getCrystalObject(selectedCrystalNPC));
                    }
                    stop();
                }
            }
        };
    }

    public WorldTaskList channelCrystalTaskList() {
        WorldTaskList tasks = new WorldTaskList();
        VasaNistirio vasa = this;

        if (vasa.isDead())
            return new WorldTaskList();
        selectedCrystalNPC = Utils.get(((VasaChamber) getChamber()).crystalNPCs.stream().filter(n -> n != prevSelection && !n.hasFinished() && !n.isDead()).collect(Collectors.toList()));
        if (selectedCrystalNPC == null)
            return new WorldTaskList(); // return empty task list - shouldn't happen
        selectedCrystalNPC.setHitpoints(selectedCrystalNPC.getMaxHitpoints());
        selectedCrystalObject = getCrystalObject(selectedCrystalNPC);
        prevSelection = selectedCrystalNPC;
        debug("starting crystal channel");

        setCrystalVulnerable(selectedCrystalNPC, true);

        tasks.setCancelCondition(() -> {
            if (isDead() || hasFinished()) {
                return true;
            }
            return false;
        });

        // in case crystal dies before vasa walks to it
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (vasa.isDead() || vasa.hasFinished() || selectedCrystalNPC.isDead()) {
                    setCrystalVulnerable(selectedCrystalNPC, false);
                    stop();
                }
            }
        });

        // walk to crystal
        tasks.add(waypointTask(false, selectedCrystalObject));
        tasks.add(() -> setNextNPCTransformation(27567));

        tasks.add(new WorldTask() {
            @Override
            public void run() {
                if (!isCrystalVulnerable(selectedCrystalNPC) || charges++ > 65) {
                    selectedCrystalNPC.resetAttackers();
                    stop();
                }
                faceObject(getCrystalObject(selectedCrystalNPC));
                debug("charges=" + charges + " end=66");
                vasa.heal(15); // heal 1.5 hp per tick
            }
        });

        tasks.add(() -> {
            setCrystalVulnerable(selectedCrystalNPC, false);
            anim(27414);
            setNextNPCTransformation(27566);
            setNextFaceEntity(null);
            selectedCrystalNPC = null;
        });

        tasks.add(waypointTask(true, selectedCrystalObject));

        tasks.add(() -> {
            debug("channel completed");
        });
        return tasks;
    }

    private void kickRocks() {
        getTeam().forEach(p -> {
            if (p.isDead() || isDead() || hasFinished() || p.isFrozen())
                return;
            WorldTile targetPos = p.clone();
            int delay = ROCK_PROJECTILE.fire(this, targetPos);
            World.sendGraphics(p, new Graphics(5305, 30, 35), targetPos);
            WorldTasksManager.schedule(() -> {
                if (p.withinDistance(targetPos, 1)) {
                    int dmg = 440;
                    //if (p.getPrayer().isRangeProtecting())
                    //    dmg /= 2;
                    Hit hit = CombatScript.getRangeHit(this, dmg);
                    if(hit.getDamage() == 0)
                        hit.setDamage(50);
                    p.applyHit(hit);
                }
            }, CombatScript.getDelay(delay));
        });
    }

    private WorldObject getCrystalObject(NPC crystalNPC) {
        Optional<WorldObject> crystal = vasaChamber.crystalObjects.stream().filter(o -> o.matches(crystalNPC)).findFirst();
        return crystal.isPresent() ? crystal.get() : World.getObjectWithType(crystalNPC, 10);
    }

    public void setCrystalVulnerable(NPC npc, boolean vulnerable) {
        getCrystalObject(npc).updateId(!vulnerable ? 129775 : 129774);
    }

    private boolean isCrystalVulnerable(NPC npc) {
        return getCrystalObject(npc).getId() != 129775;
    }

    private void attack() {
        if (teleportAttackTasks.finished() && channelTasks.finished()) {
            if (getId() == 27565 || charges >= 65) { // initial form
                debug("starting teleport attack charges=" + charges);
                (teleportAttackTasks = teleportAttackTaskList()).execute2t();
            } else {
                debug("starting channel crystal");
                (channelTasks = channelCrystalTaskList()).execute2t();
            }
        }
    }

    @Override
    public void processNPC() {

        if (!getChamber().isActivated()) {
            if (getTeam().stream().anyMatch((p) -> Utils.isOnRange(p, this, 6))) {
                getChamber().setActivated(true);
            }
        }
        if (getChamber().isActivated()) {
            attack();

            if(channelTasks != null) {
                getTeam().forEach(player -> {
                    if (Utils.collides(player, this) && !player.isFrozen()) {
                        player.applyHit(new Hit(this, Utils.random(150), Hit.HitLook.MELEE_DAMAGE));
                    }
                });
            }
        }

        resetCombat();

        // super.processNPC();
    }

    public WorldObject getSelectedCrystalObject() {
        return selectedCrystalObject;
    }
}
