package com.rs.game.npc.cox.impl;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.tasks.WorldTasksManager.WorldTaskList;
import com.rs.utils.Direction;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Oct 17, 2020
 */
public class Tekton extends COXBoss {
    public static final WorldTile CHAMBER_COORDS = new WorldTile(2, 1, 3);

    public static final int ID = Settings.OSRS_NPC_OFFSET + 7540;

    public static final WorldTile TEKTON_SPAWN_TILE = new WorldTile(78, 43, 3); // must use map.getTile
    //74, 16, 30, 65, 16, 64);
    private static final Projectile LAVA_PROJECTILE = new Projectile(660, 0, 0, 0, 40, 45, 0);
    //public static WorldTile anvilTile;
    private static Drop[] ALWAYS_DROPS =
            {
                    new Drop(50890, 1, 1),    // Tekton's journal
                    //new Drop(50910, 5, 5),    // Stinkhorn mushroom
                    new Drop(50996, 2, 2),    // Overload (+)(4)
            };
    private static Drop[] COMMON_DROPS =
            {
                    new Drop(50972, 1, 1),    // Prayer enhance (+)(4)
                    new Drop(50960, 1, 1)    // Revitalisation (+)(4)
            };
    private List<WorldObject> bubbles = new ArrayList<>(20);

    private WorldTile[] SMOKE_TILES = {
            new WorldTile(12, 22, 0),
            new WorldTile(11, 22, 0),
            new WorldTile(11, 23, 0),
            new WorldTile(12, 23, 0),

            new WorldTile(22, 21, 0),
            new WorldTile(22, 22, 0),
            new WorldTile(21, 22, 0),
            new WorldTile(21, 21, 0),

            new WorldTile(23, 11, 0),
            new WorldTile(22, 11, 0),
            new WorldTile(22, 12, 0),
            new WorldTile(23, 12, 0),

            new WorldTile(11, 11, 0),
            new WorldTile(10, 11, 0),
            new WorldTile(10, 13, 0),
            new WorldTile(11, 13, 0),
    };

    private long lastAttack = 0;
    private int attackCounter = 0, attackFinishCount = 6 + Utils.random(8);
    private boolean enraged = false;
    private Player target = null;
    private boolean smithing = false;
    private WorldTaskList events = null;
    private int attackRotation = 0;


    public Tekton(ChambersOfXeric raid) {
        super(raid, ID, raid.getTile(TEKTON_SPAWN_TILE.getX(), TEKTON_SPAWN_TILE.getY(), TEKTON_SPAWN_TILE.getPlane()), raid.getTektonChamber());
        setLureDelay(Integer.MAX_VALUE);
        setFreezeDelay(Integer.MAX_VALUE);
        setIntelligentRouteFinder(false);
        setCantFollowUnderCombat(true);
        setDrops();
    }

    public void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        for (Drop drop : ALWAYS_DROPS) {
            dList[Drops.ALWAYS].add(drop);
        }
        for (Drop drop : COMMON_DROPS) {
            dList[Drops.COMMOM].add(drop);
        }
        dList[Drops.RARE].add(new Drop(6571, 1, 1));
        drops.addDrops(dList);
        for (int i = 0; i < 6; i++)
            NPCDrops.addDrops(27540 + i, drops);
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(smithing && matches(getRespawnTile())) {
            attacker.sendMessage("Tekton is protected by the magnetic field of his anvil!");
            return false;
        }
        return super.preAttackCheck(attacker);
    }

    @Override
    public void faceEntity(Entity target) {
        // do nothing
    }

    @Override
    public void faceEntity2(Entity target) {

    }

    @Override
    public void setNextFaceEntity(Entity entity) {

    }

    @Override
    public void drop() {
        super.drop();
    }

    private boolean mulligan = false;

    public void attack() {
        if (lastAttack < System.currentTimeMillis())
            lastAttack = System.currentTimeMillis() + 3600;
        else return;

        boolean attackCancelled = false;

        Tekton tekton = this;

        setNextFaceEntity(null);

        if (target != null && Utils.isOnRange(target, tekton, 0)) {
            //Player p = Utils.get(targets);
            Player p = target;
            mulligan = false;

            int dX, dY;
            if (p.getX() == tekton.getX() - 1 || p.getX() == tekton.getX() + tekton.getSize()) {
                dX = p.getX();
                dY = 0;
            } else {
                dX = 0;
                dY = p.getY();
            }

            tekton.setNextFaceWorldTile(new WorldTile(dX > 0 ? dX : tekton.getX(), dY > 0 ? dY : tekton.getY(), tekton.getPlane()));

            // FIRST HIT
            // SWORD
            WorldTasksManager.schedule(() -> {
                if(isDead()) return;
                anim(27482 + Utils.random(0, 1) + (isEnraged() ? 10 : 0));
                WorldTasksManager.schedule(() -> {
                    if(isDead()) return;
                    for (Player player : tekton.getTeam()) {
                        if ((dX != 0 && player.getX() == dX) || (dY != 0 && player.getY() == dY)) {
                            if (Utils.isOnRange(player, tekton, 1)) {
                                int maxDamage = 450;
                                if(isEnraged() && player.getPrayer().isMeleeProtecting())
                                    maxDamage *= 2;
                                player.applyHit(new Hit(tekton, Utils.random(maxDamage), Hit.HitLook.MELEE_DAMAGE));
                            } else debug(player.getName() + " out of range");
                        }
                    }
                });
            });

            // SECOND HIT
            // HAND
            WorldTasksManager.schedule(() -> {
                if(isDead()) return;
                anim(27484 + (isEnraged() ? 10 : 0));
                WorldTasksManager.schedule(() -> {
                    if(isDead()) return;
                    for (Player player : tekton.getTeam()) {
                        if ((dX != 0 && player.getX() == dX) || (dY != 0 && player.getY() == dY)) {
                            if (Utils.isOnRange(player, tekton, 1)) {
                                int maxDamage = 350;
                                if(isEnraged() && player.getPrayer().isMeleeProtecting())
                                    maxDamage *= 2;
                                player.applyHit(new Hit(tekton, Utils.random(maxDamage), Hit.HitLook.MELEE_DAMAGE));
                            } else debug(player.getName() + " out of range");
                        }
                    }
                });
            }, 3);
        } else {
            if(!mulligan) {
                // allow players 1 tick to fail (one time) per Tekton's attack rotation
                mulligan = true;
                lastAttack = System.currentTimeMillis() + 1200;
            } else {
                attackCancelled = true;
                debug("Cancel attack, target out of range");
            }
        }


        if (attackCancelled || attackCounter++ > attackFinishCount) {
            if (events != null)
                events.cancel();
            mulligan = false;
            attackCounter = 0;
            attackFinishCount = Utils.random(8) + 6;
            smith();
            smithing = true;
            enraged = false;

        }

    }

    @Override
    public void processNPC() {
        Arrays.stream(SMOKE_TILES).forEach(t -> {
            WorldTile worldTile = getChamber().getWorldTile(t);
            for(Player player : getTeam()) {
                if(player.matches(worldTile)) {
                    player.applyHit(this, Utils.random(10, 30));
                }
            }
        });

        if (!smithing) {
            attack();
        }
        getTeam().forEach(player -> {
            if (Utils.collides(player, this)) {
                player.applyHit(new Hit(this, Utils.random(150), Hit.HitLook.MELEE_DAMAGE));
            }
        });
    }

    public boolean isAggressive() {
        return getId() != Settings.OSRS_NPC_OFFSET + 7540
                && getId() != Settings.OSRS_NPC_OFFSET + 7545
                && !isLocked();
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (smithing && matches(getRespawnTile()))
            hit.setDamage(0);
        else if (hit.getLook() != Hit.HitLook.MELEE_DAMAGE)
            hit.setDamage(0);
        super.handleIngoingHit(hit);
    }

    public void debug(String s) {
        if (Settings.DEBUG) {
            //
            // forceTalk(s);
            getTeam().forEach(plr -> plr.asPlayer().sendMessage("<col=ff981f><shad=0>[DEBUG]: <col=ffffff><shad=0>" + s));
        }
    }

    public void smith() {
        if (events != null && !events.finished())
            events.cancel();
        events = new WorldTaskList();
        events.setCancelCondition(this::isDead);
        getCombat().reset();
        setNextFaceEntity(null);
        Tekton tekton = this;
        smithing = true;

        debug("Starting smithing..");

        getTeam().forEach(entity -> {
            if (entity != null) {
                entity.resetCombat();
            }
        });

        if (getChamber().isActivated()) {
            // transform into walking tekton
            tekton.anim(Settings.OSRS_ANIMATIONS_OFFSET + 7479);
            tekton.setNextNPCTransformation(Settings.OSRS_NPC_OFFSET + 7541);
            //setCantSetTargetAutoRelatio(true);

            // walk to anvil
            tekton.addWalkSteps(getRespawnTile().getX(), getRespawnTile().getY(), -1, true);
            if (!tekton.matches(getRespawnTile())) {
                events.add(new WorldTask() {
                    @Override
                    public void run() {
                        debug("Walking to anvil");
                        //setNextFaceWorldTile(getRespawnTile());
                        setDirection(Direction.SOUTH, true);
                        if (tekton.matches(getRespawnTile())) {
                            resetWalkSteps();
                            stop();
                        }
                    }
                });
            }
        }

        events.setCancelCondition(() -> this.isDead() || this.hasFinished());

        // smith sword / enrage
        events.add(() -> {

            tekton.setCantInteract(true);

            debug("smithing..");
            tekton.anim(Settings.OSRS_ANIMATIONS_OFFSET + 7475);
            WorldTasksManager.schedule(() ->
                    tekton.setNextNPCTransformation(Settings.OSRS_NPC_OFFSET + 7545));
            events.delay(1);
        });

        if (getChamber().isActivated()) {
            // fire lava rocks
            for (int i = 0; i < 6; i++) {
                int ROCK = i;
                events.add(new WorldTask() {
                    @Override
                    public void run() {
                        debug("firing rock # " + ROCK);
                        tekton.fireLavaRock();
                        stop();
                    }
                });
                events.delay(1);
                int hpAdd = (Math.max((int) (tekton.getMaxHitpoints() * 0.005), 10));
                tekton.setHitpoints(tekton.getHitpoints() + hpAdd);
            }
        }


        events.add(() -> {
            tekton.setCantInteract(false);
            debug("smithing finished");
            if (attackRotation++ > 0) {
                enraged = true;
                debug("Tekton is enraged");
            }
            final int rotation = attackRotation;

            WorldTasksManager.schedule(() -> {
                if(attackRotation == rotation && !smithing) {
                    // same rotation, 15 seconds passed
                    enraged = false;
                    tekton.setNextNPCTransformation(Settings.OSRS_NPC_OFFSET + 7542);
                }
            }, 21);
        });

        events.add(new WorldTask() {
            @Override
            public void run() {
                List<Player> plrs = tekton.getTeam().stream()
                        .filter(player -> Utils.isOnRange(player, tekton, getChamber().isActivated() ? 32 : 6)).collect(Collectors.toList());
                if (plrs.size() > 0) {
                    if (!getChamber().isActivated())
                        activate();
                    debug("getting up");
                    tekton.anim(Settings.OSRS_ANIMATIONS_OFFSET + (isEnraged() ? 7487 : 7474));
                    //tekton.setNextNPCTransformation(Settings.OSRS_NPC_OFFSET + 7541);
                    tekton.setNextNPCTransformation(Settings.OSRS_NPC_OFFSET + (isEnraged() ? 7544 : 7542));
                    tekton.setCantInteract(false);
                    stop();
                }

                // reset target after anvil
                target = null;
            }
        });

        events.add(new WorldTask() {
            int failsafe = 15;

            @Override
            public void run() {
                List<Player> plrs = getTeam().stream()
                        // only target players Tekton can walk to
                        .filter(player ->
                                player.getX() < raid.getTektonChamber().getFire().getX())
                        .collect(Collectors.toList());

                if (target == null && plrs.size() > 0) {
                    plrs.sort((a, b) -> (a.distance(tekton) > b.distance(tekton) ? 1 : 0));
                    target = plrs.get(0).asPlayer(); // closest plr
                }

                if (target != null) {
                    // get up from smithing
                    if (!getChamber().isActivated()) {
                        activate();
                    }
                    tekton.calcFollow(target, false);
                    if (Utils.isOnRange(tekton, target, 0)) {
                        // go into attack stance
                        int emote = Settings.OSRS_ANIMATIONS_OFFSET + 7478 + (isEnraged() ? 10 : 0);
                        tekton.anim(emote);
                        //tekton.setNextNPCTransformation(Settings.OSRS_NPC_OFFSET + 7542);
                        tekton.target = target;
                        tekton.smithing = false;
                        debug("targeted " + target.getName());
                        resetWalkSteps();

                        // wait until transition anim is finished to attack
                        WorldTasksManager.schedule(() -> {
                            attack();
                        }, World.getAnimTicks(emote));
                        stop();
                    } else if (failsafe-- == 0) {
                        debug("tekton cannot reach  " + target.getName());
                        smith();
                        stop();
                    }
                } else {
                    // continue smithing
                    // this is random walk on osrs
                    anim(27473);
                }
            }
        });

        events.execute2t();
    }

    private void activate() {
        getChamber().setActivated(true);

        WorldTile base = getRaid().getMapTileBaseWorldTile(getChamberBaseTile().getX(), getChamberBaseTile().getY(), getChamberBaseTile().getPlane());
        int baseX = base.getX(); // base of tekton chamber (tekton @ chunk 2,1)
        int baseY = base.getY();

        for (int x = baseX; x < baseX + 32; x++) {
            for (int y = baseY; y < baseY + 32; y++) {
                WorldObject obj2 = World.getObjectWithType(new WorldTile(x, y, base.getPlane()), 10);
                if (obj2 != null) {
                    if (obj2.getId() == Settings.OSRS_OBJECTS_OFFSET + 29890) {
                        bubbles.add(obj2);
                    }
                }
            }
        }
    }

    private boolean isEnraged() {
        return enraged;
    }

    private void fireLavaRock() {
        for (Entity e : getTeam()
                .stream()
                .filter(player ->
                        player.getX() < raid.getTektonChamber().getFire().getX())
                .collect(Collectors.toList())) {
            if (!e.isPlayer())
                continue;
            Player p = e.asPlayer();

            for (int i = 0; i < 2; i++) {
                if (bubbles.size() == 0) {
                    debug("ERROR: bubbles not found!");
                    return;
                }
                WorldObject source = Utils.get(bubbles);
                WorldTile landTile = p.clone();
                int delay = World.sendProjectile(source, source.clone(), p.clone(), LAVA_PROJECTILE.getGfx(), LAVA_PROJECTILE.getStartHeight(), LAVA_PROJECTILE.getEndHeight(), LAVA_PROJECTILE.getSpeed(), LAVA_PROJECTILE.getDelay(), LAVA_PROJECTILE.getCurve(), LAVA_PROJECTILE.getStartDistanceOffset());
                WorldTasksManager.schedule(() -> {
                    // land graphics after delay
                    World.sendGraphics(p, new Graphics(659, 0, 0), landTile);
                    if (isDead() || hasFinished())
                        return;
                    int distance = Utils.getDistance(p, landTile);
                    debug("delay " + delay + " distance to land tile (0-1 == hit) " + distance);
                    if (distance <= 1) {
                        p.applyHit(new Hit(this, Utils.random(distance == 1 ? 120 : 200), Hit.HitLook.REGULAR_DAMAGE));
                    }
                }, CombatScript.getDelay(delay), 0);
            }
        }
    }

    @Override
    public void sendDeath(Entity killer) {
        getChamber().bossDeath();
        super.sendDeath(killer);
    }

    public boolean isSmithing() {
        return smithing;
    }
}
