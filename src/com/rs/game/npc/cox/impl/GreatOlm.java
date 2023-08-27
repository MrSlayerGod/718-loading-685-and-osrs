package com.rs.game.npc.cox.impl;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.ChambersRewards;
import com.rs.game.player.content.raids.cox.chamber.impl.GreatOlmChamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.rs.game.npc.cox.impl.GreatOlm.Facing.*;
import static com.rs.game.tasks.WorldTasksManager.WorldTaskList;
import static com.rs.game.tasks.WorldTasksManager.schedule;

/**
 * @author Simplex
 * @since Jul 19, 2020
 */
public class GreatOlm extends COXBoss {

    private static final Projectile CRYSTAL_DROP_PROJECTILE = new Projectile(1357 + Settings.OSRS_GFX_OFFSET, 215, 0, 0, 1, 0, 127);
    private static final Projectile CRYSTAL_BOMB_PROJECTILE = new Projectile(1357 + Settings.OSRS_GFX_OFFSET, 90, 0, 30, 40, 16, 0);
    private static final Projectile CRYSTAL_SPIKE_PROJECTILE = new Projectile(1352 + Settings.OSRS_GFX_OFFSET, 200, 0, 0, 1, 0, 0);
    private static final Projectile ACID_POOL_PROJECTILE = new Projectile(1354 + Settings.OSRS_GFX_OFFSET, 90, 0, 30, 40, 16, 0);
    private static final Projectile ACID_DRIP_PROJECTILE = new Projectile(1354 + Settings.OSRS_GFX_OFFSET, 90, 43, 30, 25, 16, 0);
    private static final Projectile BURN_PROJECTILE = new Projectile(1350 + Settings.OSRS_GFX_OFFSET, 90, 43, 35, 20, 16, 192);
    private static final Projectile FLAME_WALL_PROJECTILE_1 = new Projectile(1347 + Settings.OSRS_GFX_OFFSET, 90, 0, 28, 70, 16, 127);
    private static final Projectile FLAME_WALL_PROJECTILE_2 = new Projectile(1348 + Settings.OSRS_GFX_OFFSET, 0, 0, 0, 60, 16, 0);
    private static final Projectile SIPHON_PROJECTILE = new Projectile(1355 + Settings.OSRS_GFX_OFFSET, 90, 0, 30, 60, 16, 0);
    private static final Projectile MAGIC_PROJECTILE = new Projectile(1339 + Settings.OSRS_GFX_OFFSET, 90, 43, 35, 40, 16, 192);
    private static final Projectile RANGED_PROJECTILE = new Projectile(1340 + Settings.OSRS_GFX_OFFSET, 90, 43, 35, 40, 16, 192);
    private static final Projectile MAGIC_SPHERE = new Projectile(1341 + Settings.OSRS_GFX_OFFSET, 90, 43, 40, 25, 16, 192);
    private static final Projectile RANGED_SPHERE = new Projectile(1343 + Settings.OSRS_GFX_OFFSET, 90, 43, 40, 25, 16, 192);
    private static final Projectile MELEE_SPHERE = new Projectile(1345 + Settings.OSRS_GFX_OFFSET, 90, 43, 40, 25, 16, 192);

    public static int OLM_HEAD_NPC = 7554 + Settings.OSRS_NPC_OFFSET;
    private int attackTimer = -4; // skip first attack
    private ChambersOfXeric raid;
    private GreatOlmLeftClaw leftClaw = null;
    private GreatOlmRightClaw rightClaw = null;
    private int clenchDamageCounter = 0;
    private boolean clenched;
    private boolean clawHealing;
    private int currentPhase = 0;
    private int lastPhase;
    private Facing facing = Facing.CENTER;
    private Bounds northTargetBounds, centerTargetBounds, southTargetBounds, arenaBounds;
    private int attackCounter = 0;
    private int specialCounter = 0;
    private int lastBasicAttackStyle = Utils.rollPercent(50) ? Combat.MAGIC_TYPE : Combat.RANGE_TYPE;
    private PhasePower phasePower = null;
    private boolean finalStand = false;
    private Stopwatch siphonDelay = new Stopwatch().delay(15);
    private boolean activated;
    private int phaseChangeStage = 0;

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    /**
     * OLM
     * <p>
     * obj ids:29880-29888
     * anims : 7334 - 7360 somewhere theres
     * npc ids : 7550-7555
     */
    public GreatOlm(ChambersOfXeric raid, WorldTile tile, GreatOlmChamber chamber) {
        super(raid, OLM_HEAD_NPC, tile, chamber);
        this.addFreezeDelay(Integer.MAX_VALUE);
        this.raid = raid;
        this.setCantFollowUnderCombat(true);
        this.setForceAgressive(false);
        initNPC();
    }

    @Override
    public List<Player> getTeam() {
        return getRaid().getTeam().stream().filter(Objects::nonNull)
                .filter(this::inChamber).collect(Collectors.toList());
    }

    private WorldTile getTile(int x, int y) {
        /*
         * hack until we decide to do a proper raids system
         */
        return raid.getTile(x + 96, y);
    }

    public static void init() {

        ChambersRewards.init();
        ObjectHandler.register(130028, 1, ((player, obj) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                ChambersRewards.openRewards(player, obj);
            }
        }));
    }

    private void initNPC() {
        setIntelligentRouteFinder(true);// classifies boss in some code
        northTargetBounds = new Bounds(getTile(RIGHT.swX, RIGHT.swY), getTile(RIGHT.neX, RIGHT.neY), 0);
        centerTargetBounds = new Bounds(getTile(CENTER.swX, CENTER.swY), getTile(CENTER.neX, CENTER.neY), 0);
        southTargetBounds = new Bounds(getTile(LEFT.swX, LEFT.swY), getTile(LEFT.neX, LEFT.neY), 0);
        arenaBounds = new Bounds(getTile(28, 35), getTile(37, 51), 0);
        lastPhase = 2; //0,1,2  = 3 phases default

        //if (raid != null)
        //   lastPhase += raid.getTeam().size() / 8; // 1 extra phase for every 8 party members
        startAcidPoolTask();
    }

    private boolean isEmpowered() {
        return currentPhase == lastPhase;
    }

    public boolean leftHandDown() {
        return leftClaw.getId() == 27552;
    }

    public boolean rightHandDown() {
        return rightClaw.getId() == 27550;
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if(hit.getLook() == Hit.HitLook.MELEE_DAMAGE) {
            if(hit.getSource().isPlayer())
                hit.getSource().asPlayer().sendMessage("Your attack has no effect on Great olm!");
            hit.setDamage(0);
        }
        if (isEmpowered() && leftHandDown() && rightHandDown()) {
            // allow damage
        } else {
            hit.setDamage(0);
        }
        super.handleIngoingHit(hit);
    }

    private boolean turn() {
        Facing dest = getTurnDestination();
        if (dest == null || dest == facing)
            return false;
        getChamber().setDebug("Turning " + dest.name());
        if ((facing == LEFT && dest == RIGHT) || (facing == RIGHT && dest == LEFT) || (facing == LEFT && dest == CENTER)) { // 'far' transition
            animate(this, dest.getFarTransitionAnim(isEmpowered()));
            delayedAnimation(this, dest.getIdleAnim(isEmpowered()), 1);
        } else { // 'close' transition
            animate(this, dest.getCloseTransitionAnim(isEmpowered()));
            delayedAnimation(this, dest.getIdleAnim(isEmpowered()), 1);
        }
        facing = dest;
        return true;
    }

    private boolean isOnEastSide() {
        return this.getRespawnTile().matches(this);
    }

    private static final int SOUTH_TURN_ZONE_Y = 38, NORTH_TURN_ZONE_Y = 50;

    private static final int MAX_EQUALIBRIUM_ATTACKS = 6;
    private int equilibriumForceTurn = Utils.random(3, MAX_EQUALIBRIUM_ATTACKS);
    private Facing equilibriumDirection = Utils.rollPercent(50) ? LEFT : RIGHT;

    private Facing getZonePriorityFaceDirection(int plrsAtCenter, List<Player> targetsLeft, List<Player> targetsRight) {
        int plrsFar, plrsClose;

        if(targetsLeft.size() == targetsRight.size()) {
            if(equilibriumForceTurn-- > 0) {
                // do nothing
            } else {
                equilibriumDirection = Utils.rollPercent(50) ? LEFT : RIGHT;
                equilibriumForceTurn = Utils.random(3, MAX_EQUALIBRIUM_ATTACKS);
                //forceTalk("equalibrium=" + equilibriumDirection + " x " + equilibriumForceTurn);
            }

            return equilibriumDirection;
        }

        // randomize so players can't determine which way head will turn
        equilibriumDirection = Utils.rollPercent(50) ? LEFT : RIGHT;
        equilibriumForceTurn = Utils.random(3, MAX_EQUALIBRIUM_ATTACKS);

        if(plrsAtCenter > targetsLeft.size() && plrsAtCenter > targetsRight.size()
                && (facing == Facing.LEFT || facing == Facing.RIGHT)) {
            // everyone is standing on center tile, continue facing same direction
            return facing;
        } else if (targetsLeft.size() > targetsRight.size()) {
            // collect # of players in the far zone of the left side
            plrsFar = (int) targetsLeft.stream().filter(player -> {
                int y = raid.getInstanceTile(player).getY();
                return ((isOnEastSide() && y <= SOUTH_TURN_ZONE_Y) || (!isOnEastSide() && y >= NORTH_TURN_ZONE_Y));
            }).count();
            plrsClose = targetsLeft.size() - plrsFar;

            // turn left only if the majority of players are in the far zone
            // don't turn center if majority of players are on left side but not majority far
            return plrsFar > plrsClose ? LEFT : facing == LEFT ? LEFT : CENTER;
        } else if (targetsLeft.size() < targetsRight.size()) {
            // collect # of players in the far zone of the right side
            plrsFar = (int) targetsRight.stream().filter(player -> {
                int y = raid.getInstanceTile(player).getY();
                return ((isOnEastSide() && y >= NORTH_TURN_ZONE_Y) || (!isOnEastSide() && y <= SOUTH_TURN_ZONE_Y));
            }).count();

            plrsClose = targetsRight.size() - plrsFar;

            // turn right only if the majority of players are in the far zone
            // don't turn center if majority of players are on right side but not majority far
            return plrsFar > plrsClose ? RIGHT : facing == RIGHT ? RIGHT : CENTER;
        } else {
            return CENTER;
        }
    }

    private Facing getTurnDestination() {
        List<Player> targetsLeft = getFarTargets(LEFT);
        List<Player> targetsRight = getFarTargets(RIGHT);

        int plrsAtCenter = (int) getTeam().stream()
                .filter(p->raid.getInstanceTile(p).getY() == 44).count();

        // no movement if players are all standing on center tile
        if(plrsAtCenter == getTeam().size())
            return facing;

        Facing dest = getZonePriorityFaceDirection(plrsAtCenter, targetsLeft, targetsRight);

        //forceTalk("zone priority = "+ dest + " ");
        /**
         * Olm will skip turning center regardless of placement on dest side
         * if the hand on dest side was damaged this attack rotation
         */
        if(dest == CENTER && facing != CENTER) {
            if(facing == RIGHT && leftDamageThisTick > 0 && targetsLeft.size() > targetsRight.size()) {
                dest = LEFT;
            }
            if(facing == LEFT && rightDamageThisTick > 0 && targetsRight.size() > targetsLeft.size()) {
                dest = RIGHT;
            }
        }
        // south = 38
        // north = 50f

        return dest;
    }


    private void animate(NPC npc, int animationId) {
        WorldObject obj = getObject(npc);
        if (obj != null)
            objectAnim(obj, animationId);
    }

    private void objectAnim(WorldObject obj, int animationId) {
        getTeam().forEach(player -> {
            player.getPackets().sendObjectAnimation(obj, new Animation(animationId));
        });
    }

    private void delayedAnimation(NPC npc, int animationId, int delay) {
        schedule(new WorldTask() {
            @Override
            public void run() {
                animate(npc, animationId);
                stop();
            }
        }, delay);
    }

    private WorldObject getObject(NPC npc) {
        WorldObject object = null;

        if (isOnEastSide())
            object = World.getObjectWithType(npc, 10);
        else
            object = World.getObjectWithType(new WorldTile(npc.getX() - 3, npc.getY(), npc.getPlane()), 10);

        if (object == null) {
            System.out.println("Warning: Object was null at " + ((WorldTile) npc).toString());
            object = new WorldObject(0, 10, 1, npc);
        }

        return object;
    }

    public void rise() {
        if(!isActivated()) {
            setActivated(true);
            leftClaw = new GreatOlmLeftClaw(raid, 7555 + Settings.OSRS_NPC_OFFSET, getTile(37 + 1, 37), getChamber());
            rightClaw = new GreatOlmRightClaw(raid, 7553 + Settings.OSRS_NPC_OFFSET, getTile(37 + 1, 47), getChamber());

            this.setCustomCombatScript(CombatScript.DO_NOTHING);
            leftClaw.setCustomCombatScript(CombatScript.DO_NOTHING);
            rightClaw.setCustomCombatScript(CombatScript.DO_NOTHING);
            /*if(Utils.rollDie(2, 1)) {
                // 50% chance to start on west side
                setNextWorldTile(getTile(23, 42));
                leftClaw.setNextWorldTile(getTile(23, 47));
                rightClaw.setNextWorldTile(getTile(23, 37));
                leftClaw.setDirection(Direction.EAST, true);
                rightClaw.setDirection(Direction.EAST, true);
                setDirection(Direction.EAST, true);
            }*/
        }

        rightClaw.lock();
        leftClaw.lock();
        lock();

        WorldObject head = getObject(this);
        WorldObject left = getObject(leftClaw);
        WorldObject right = getObject(rightClaw);

        right.updateId(29886 + Settings.OSRS_OBJECTS_OFFSET);
        head.updateId(29880 + Settings.OSRS_OBJECTS_OFFSET);
        left.updateId(29883 + Settings.OSRS_OBJECTS_OFFSET);

        GreatOlm OLM = this;

        schedule(new WorldTask() {
            int cycle = 0;

            @Override
            public void run() {
                if (cycle == 1) {
                    animate(rightClaw, 7350 + Settings.OSRS_ANIMATIONS_OFFSET);
                    animate(OLM, isEmpowered() ? 7383 + Settings.OSRS_ANIMATIONS_OFFSET : 7335 + Settings.OSRS_ANIMATIONS_OFFSET);
                    animate(leftClaw, 7354 + Settings.OSRS_ANIMATIONS_OFFSET);
                }

                if (cycle == 6) {
                    getObject(rightClaw).updateId(29887 + Settings.OSRS_OBJECTS_OFFSET);
                    getObject(OLM).updateId(29881 + Settings.OSRS_OBJECTS_OFFSET);
                    getObject(leftClaw).updateId(29884 + Settings.OSRS_OBJECTS_OFFSET);
                    setLocked(false);
                    restore(leftClaw);
                    restore(rightClaw);
                    stop();
                }

                cycle++;
            }
        }, 0, 0);
    }

    private void forAllTargets(Consumer<Player> action) {
        getTeam().forEach(action);
    }

    ArrayList<WorldObject> acidPools = new ArrayList<>();

    private void startAcidPoolTask() {
        final GreatOlm OLM = this;
        schedule(new WorldTask() {
            @Override
            public void run() {
                if(OLM.hasFinished()) {
                    stop();
                    return;
                }
                forAllTargets(p -> {
                    if (acidPools.stream().anyMatch(obj->obj.matches(p))) {
                        p.applyHit(new Hit(OLM, Utils.random(30, 60), Hit.HitLook.POISON_DAMAGE));
                        p.getPoison().makePoisoned(40);
                    }
                });
            }
        }, 0, 1);
    }

    int t = 0;

    @Override
    public void processNPC() {
        if(!isActivated() || isDead() || hasFinished()) {
            super.processNPC();
            return;
        }

        attackTimer++;

        if(phaseChangeStage == 1) {
            phaseChangeStage = 2;
            WorldTasksManager.schedule(() -> nextPhase(), 1);
            debug("Commencing phase " + (currentPhase + 2));
            return;
        }

        if(attackTimer >= 4 && phaseChangeStage != 2) {
            unlock();
        }

        if(isLocked() || phaseChangeStage == 3) {
            // can change phase while locked so need to separate
            return;
        }

        attackTimer = 0;
        attackCounter++;


        if(attackCounter % 16 == 0) {
            // empty event (same as OSRS)
            // forceTalk("EMPTY EVENT");
            leftDamageThisTick = 0;
            rightDamageThisTick = 0;
            lastDamageTick = attackTimer;
            lock();
            return;
        }

        if ((attackCounter % 4) == 1) {
            if (++specialCounter == (isEmpowered() ? 5 : 4)) {
                specialCounter = 1;
            }
        }

        Facing dest = getTurnDestination();
        turnRequired = dest != null && dest != facing;

        if(turnRequired)
            startTurn();
        else attack();

        lock();

        leftDamageThisTick = 0;
        rightDamageThisTick = 0;
        lastDamageTick = attackTimer;
        super.processNPC();
    }

    @Override
    public void unlock() {
        super.unlock();
    }

    /**
     * @return ticks between next attack
     */
    public void startTurn() {
        // if head needs to turn after the attack, queue it immediately after that attack
        // and lock olm until turn completes
                turn();
    }

    @Override
    public void sendDeath(Entity source) {
        resetWalkSteps();
        getCombat().removeTarget();
        animate(this, Settings.OSRS_ANIMATIONS_OFFSET + 7348);
        phaseChangeStage = 3;
        schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    forAllTargets(p -> p.getPackets().sendStopCameraShake());
                } else if (loop >= World.getAnimTicks(27348)) {
                    olmDeathEnd();
                    reset();
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    private void olmDeathEnd() {
        phaseChangeStage = 3;
        getObject(this).updateId(Settings.OSRS_OBJECTS_OFFSET + 29882);
        if(leftClaw != null)
            leftClaw.finish();
        if(rightClaw != null)
            rightClaw.finish();
        getChamber().bossDeath();
    }

    public void clawDeathStart(COXBoss claw) {
        animate(claw, (claw == leftClaw ? 7370 : 7352) + Settings.OSRS_ANIMATIONS_OFFSET);
        schedule(() -> {
            getObject(claw).setId((claw == leftClaw ? 29885 : 29888) + Settings.OSRS_OBJECTS_OFFSET);
        }, 2);
    }

    public void clawDeathEnd(COXBoss claw) {
        claw.setHitpoints(claw.getMaxHitpoints());
        claw.setNextNPCTransformation(claw.getId() - 3);

        if(claw == leftClaw) {
            getObject(leftClaw).updateId(129885);
        }

        if (leftHandDown() && rightHandDown()) {
                phaseChangeStage = 1;
            if(currentPhase != lastPhase) {
                // only lock when transitioning, last phase goes into final stand - no transition
                lock();
            }
        } else if (currentPhase == lastPhase) {
            if(claw == leftClaw && !rightHandDown() || claw == rightClaw && !leftHandDown()) {
                startClawReviveTimer(claw);
            }
        } else {
            debug("Both hands not dead left: " + leftClaw.getId() + " right: " + rightClaw.getId());
        }
    }

    private COXBoss restoreClaw = null;

    private void startClawReviveTimer(COXBoss claw) {
        if(restoreClaw != null) // other claw already died
            return;
        restoreClaw = claw;
        COXBoss otherClaw = claw == leftClaw ? rightClaw : leftClaw;
        AtomicInteger revive = new AtomicInteger(0);
        claw.lock();
        claw.setHitpoints(1);
        WorldTaskList taskList = new WorldTaskList();
        taskList.add(new WorldTask() {
            @Override
            public void run() {
                if ((otherClaw == rightClaw && rightHandDown()) || (otherClaw == leftClaw && leftHandDown())) {
                    stop();
                    restoreClaw = null;
                    return;
                } else if(restoreClaw != null && restoreClaw.getHitpoints() >= restoreClaw.getMaxHitpoints()) {
                    // failed, revive claw
                    ((COXBoss) claw).setRewardNoPoints(true);
                    restore(claw);
                    getObject(claw).updateId((claw == rightClaw ? 29886 : 29883) + Settings.OSRS_OBJECTS_OFFSET);
                    revive.set(1);
                    stop();
                    restoreClaw = null;
                }
                //} else {
                //animate(this, facing.getIdleAnim(isEmpowered()));
            }
        });
        taskList.add(() -> {
            if (revive.get() == 1) {
                animate(claw, Settings.OSRS_ANIMATIONS_OFFSET + (claw == rightClaw ? 7350 : 7354));
                WorldTasksManager.schedule(() -> {
                    getObject(claw).updateId((claw == rightClaw ? 29887 : 29884) + Settings.OSRS_OBJECTS_OFFSET);
                }, 2);
            }
        });
        taskList.execute2t();
    }

    private void dropCeilingCrystals(int duration) {
        WorldTasksManager.scheduleRevolving(event -> {
            forAllTargets(p -> {
                p.getPackets().sendCameraShake(3, 8, 8, 8, 8);
            });
            event.add(new WorldTask() {
                int ticks = 0;

                private void end() {
                    stop();
                    WorldTasksManager.schedule(() -> {
                        raid.getTeam().forEach(p -> p.getPackets().sendStopCameraShake());
                    });
                }

                @Override
                public void run() {
                    ticks++;

                    if(currentPhase != lastPhase ) {
                        if (ticks >= duration || phaseChangeStage == 0) { // expired or risen
                            end();
                            return;
                        }
                    } else {
                        if(!leftHandDown() || !rightHandDown()) { // intermission bleed into last phase
                            end();
                            return;
                        }
                        if(phaseChangeStage != 0) {
                            end();
                            return;
                        }
                    }

                    if(ticks %6 == 0) {
                        dropCeilingCrystals(false);
                    } else {
                        if (Utils.rollPercent(50)) {
                            dropCeilingCrystals(true);
                        }
                    }
                }
            });
        });
    }

    private void dropCeilingCrystals(boolean random) {
        if(isDead() || hasFinished())
            return;
        for (int i = 0; i < (random ? 1 : raid.getTeamSize()); i++) {
            Player target = Utils.rollPercent(70) && i < getTeam().size() ? getTeam().get(i) : null;
            WorldTile position;
            if (target != null && !random)
                position = target.clone();
            else
                position = arenaBounds.randomPosition();

            int delay = World.sendProjectile(GreatOlm.this, position.transform(0,2,0), position, CRYSTAL_DROP_PROJECTILE.getGfx(), CRYSTAL_DROP_PROJECTILE.getStartHeight(), CRYSTAL_DROP_PROJECTILE.getEndHeight(), CRYSTAL_DROP_PROJECTILE.getSpeed(), CRYSTAL_DROP_PROJECTILE.getDelay(), CRYSTAL_DROP_PROJECTILE.getCurve(), CRYSTAL_DROP_PROJECTILE.getStartDistanceOffset());
            WorldTasksManager.schedule(() -> {
                World.sendGraphics(Settings.OSRS_GFX_OFFSET + 1358, position);
                WorldTasksManager.schedule(() -> {
                    forAllTargets(p -> {
                        int distance = p.clone().distance(position);
                        if (distance <= 1) {
                            p.applyHit(GreatOlm.this, Utils.get(distance == 0 ? 250 : 150));
                        }
                    });
                });
            }, 1);
        }
    }

    private void ceilingCrystals(int delay, int duration) {
        WorldTasksManager.schedule(() -> dropCeilingCrystals(duration), delay);
    }

    private void nextPhase() {
        phaseChangeStage = 3;

        if (currentPhase >= lastPhase && leftHandDown() && rightHandDown()) {
            phaseChangeStage = 0;
            unlock();
            if (!finalStand) {
                finalStand = true;
                ceilingCrystals(0, 150);
                forAllTargets(p -> p.sendMessage("The Great Olm is giving its all. This is its final stand."));
                this.setRewardNoPoints(false);
                this.currentPhase = lastPhase;
            }
            return;
        }
        lock();
        leftClaw.lock();
        rightClaw.lock();
        ceilingCrystals(3, 30);
        facing = CENTER;
        WorldTasksManager.scheduleRevolving(event -> {
            //go down
            event.setCancelCondition(this::hasFinished);
            event.add(() -> {
                animate(this, Settings.OSRS_ANIMATIONS_OFFSET + 7348);
                animate(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7370);
                animate(rightClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7352);
            });
            event.add(() -> {
                getObject(this).updateId(129882);
                //getObject(leftClaw).updateId(129885);
                getObject(rightClaw).updateId(129888);

                if (isOnEastSide()) { // to west
                    setNextWorldTile(getTile(23, 42));
                    leftClaw.setNextWorldTile(getTile(23, 47));
                    rightClaw.setNextWorldTile(getTile(23, 37));
                    leftClaw.setDirection(Direction.EAST, true);
                    rightClaw.setDirection(Direction.EAST, true);
                    setDirection(Direction.EAST, true);
                } else {
                    setNextWorldTile(getRespawnTile().clone());
                    leftClaw.setNextWorldTile(leftClaw.getRespawnTile().clone());
                    rightClaw.setNextWorldTile(rightClaw.getRespawnTile().clone());
                    leftClaw.setDirection(Direction.WEST, true);
                    rightClaw.setDirection(Direction.WEST, true);
                    setDirection(Direction.WEST, true);
                }
            });


            event.delay(25);
            event.add(() -> {
                if (currentPhase + 1 != lastPhase) {
                    phasePower = Utils.get(PhasePower.values());
                    forAllTargets(p -> p.sendMessage("The Great Olm rises with the power of " + phasePower.name + "."));
                } else {
                    phasePower = null;
                }
                phaseChangeStage = 0;
                rise();
                currentPhase++;
            });
        });
    }

    private void restore(COXBoss claw) {
        if(currentPhase != 0 && claw.getId() != 27553 && claw.getId() != 27555) {
            claw.setNextNPCTransformation(claw.getId() + 3);
        }
        claw.setHitpoints(claw.getMaxHitpoints());
        claw.unlock();
    }

    public void zio(int amt) {
        for (int i = 0; i < amt; i++) {
            WorldTile tile = arenaBounds.randomPosition();
            int delay = World.sendProjectile(this, tile, 6047, 60, 20, 30, 35, 16, 74);
            WorldTasksManager.schedule(() -> {
                NPC npc = new NPC(27519, tile, -1, false);
                WorldTasksManager.schedule(() -> {
                    npc.setRandomWalk(0);
                    World.sendGraphics(5157, npc);
                    getTeam().stream().filter(player->player.matches(npc)).forEach(player -> player.applyHit(npc, 50 + Utils.random(150, 250)));
                    WorldTasksManager.schedule(() -> {
                        npc.finish();
                    });
                });
            }, CombatScript.getDelay(delay));
        }
    }

    boolean turnRequired = false;

    public boolean attack() {
        if (isLocked()) {
            return true;
        }

        List<Player> targets = getAttackableTargets(facing);

        if(facing == CENTER) {
            // remove targets in far zones if facing center
            targets = targets.stream().filter(player -> {
                int y = raid.getInstanceTile(player).getY();
                return y > SOUTH_TURN_ZONE_Y && y < NORTH_TURN_ZONE_Y;
            }).collect(Collectors.toList());
        }


        if(targets.size() == 0) {
            debug("Nobody to attack | zone: " + facing + "");
            return true;
        }

        int attackType = (attackCounter - 1) % 3;

        // don't override heal
        if(clawHealing && attackType == 1)
            attackType = 2;

        if (attackType == 0 || attackType == 2) {
            //forceTalk("base attack / siphons " + (attackType == 0 ? "1" : "2"));
            if (finalStand && siphonDelay.finished() && Utils.get(5) == 0) {
                siphonAttack();
                siphonDelay.delay(20);
            } else {
                PhasePower power = isEmpowered() ? Utils.get(PhasePower.values()) : phasePower;
                if (power == PhasePower.ACID && Utils.rollPercent(20)) {
                    if (Utils.rollPercent(50) && Utils.rollPercent(18))
                        acidPoolsAttack();
                    else
                        acidDrip();
                } else if (power == PhasePower.FLAME && Utils.rollPercent(20)) {
                    if (Utils.rollPercent(50) && Utils.rollPercent(50))
                        flameWall(targets);
                    else
                        burnAttack(targets);
                } else if (power == PhasePower.CRYSTAL && Utils.rollPercent(15)) {
                    if (Utils.rollPercent(50) && Utils.rollPercent(50))
                        crystalBomb();
                    else
                        crystalMark(targets);
                } else {
                    if (Utils.random(1.0) < 0.9)
                        basicAttack(targets);
                    else
                        sphereAttack(targets);
                }
            }
        } else if (attackType == 1) {
            int specialType = (specialCounter - 1) % (isEmpowered() ? 4 : 3);
            //forceTalk("spec " + (specialType == 0 ? "burst" : specialType == 1 ? "lightning" : specialType == 2 ? "teleport" : "claw healing"));

            if(lastPhase != currentPhase && specialType >= 3) {
                // skip healing on all phases except last
                specialCounter++;
                specialType = 0;
            }

            if (specialType == 0)
                crystalBurst();
            else if (specialType == 1)
                lightningAttack();
            else if (specialType == 2)
                teleportAttack();
            else
                clawHealing();
        }
        return true;
    }

    private void clawHealing() {
        if (leftClaw.isDead() || leftHandDown() || clenched || clawHealing)
            return;
        clawHealing = true;
        animate(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7358);
        delayedAnimation(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7357, 2);
        WorldTasksManager.scheduleRevolving(event -> {
            for(int i = 0; i < 16; i++) {
                event.add(() -> {/* empty */});
            }
            event.add(() -> {
                clawHealing = false;
                animate(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7355);
            });
        });
    }

    private void basicAttack(List<Player> targets) {
        getChamber().setDebug("Basic attack - tick=" + attackTimer);
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        lastBasicAttackStyle = Utils.rollPercent(75) ? lastBasicAttackStyle : (lastBasicAttackStyle == Combat.RANGE_TYPE ? Combat.MAGIC_TYPE : Combat.RANGE_TYPE);
        targets.forEach(p -> {
            int delay = (lastBasicAttackStyle == Combat.RANGE_TYPE ? RANGED_PROJECTILE : MAGIC_PROJECTILE).fire(this, p);
            int d = 300;
            //if ((p.getPrayer().isMageProtecting() && lastBasicAttackStyle == Combat.MAGIC_TYPE)
            //       || (p.getPrayer().isRangeProtecting() && lastBasicAttackStyle == Combat.RANGE_TYPE))
            //d /= 3;
            int finalDmg = d;
            WorldTasksManager.schedule(() -> {
                if(changingPhase() || isDead() || hasFinished()) return;
                p.applyHit(lastBasicAttackStyle == Combat.RANGE_TYPE
                                ? CombatScript.getRangeHit(this, Utils.random(140, finalDmg))
                                : CombatScript.getMagicHit(this, Utils.random(140, finalDmg)));
            }, CombatScript.getDelay(delay));
        });
    }

    private void siphonAttack() {
        getChamber().setDebug("Siphon");
        GreatOlm olm = this;
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        WorldTasksManager.scheduleRevolving(event -> {
            event.setCancelCondition(() -> this.changingPhase() || this.isDead() || this.hasFinished());
            WorldTile[] siphons = new WorldTile[2];
            for (int i = 0; i < siphons.length; i++) {
                siphons[i] = centerTargetBounds.randomPosition();
                SIPHON_PROJECTILE.fire(this, siphons[i]);
            }
            event.add(()-> {
                for (WorldTile siphon : siphons) {
                    World.sendGraphics(olm, new Graphics(Settings.OSRS_GFX_OFFSET + 1363, 0, 40), siphon);
                }
            });
            event.delay(3);
            event.add(() -> {
                int damageDealt = 0;
                for (Player player : getTeam()) {
                    boolean safe = false;
                    for (WorldTile siphon : siphons) {
                        if (player.matches(siphon)) {
                            safe = true;
                            break;
                        }
                    }
                    if (!safe) {
                        int dmg = Utils.random(100, 150);
                        damageDealt += dmg;
                        player.applyHit(olm, dmg, Hit.HitLook.MAGIC_DAMAGE);
                    }
                };

                if(damageDealt != 0)
                    olm.applyHit(olm, damageDealt * 5, Hit.HitLook.HEALED_DAMAGE);
            });
        });
    }

    public void resetOlmHeadAnim() {
        if(facing != null && !hasFinished() && !isDead())
            facing.getIdleAnim(isEmpowered());
    }

    private void crystalMark(List<Player> potentialTargets) {
        getChamber().setDebug("Mark");
        GreatOlm olm = this;
        forAllTargets(p -> p.sendMessage("The Great Olm sounds a cry..."));
        if (potentialTargets.size() == 0)
            return;

        Player target = Utils.get(potentialTargets);
        target.sendMessage(Colour.RED.wrap("The Great Olm has chosen you as its target - watch out!"));
        target.gfx(Settings.OSRS_GFX_OFFSET + 246);
        WorldTasksManager.scheduleRevolving(event -> {
            event.setCancelCondition(() -> this.changingPhase() || this.isDead() || this.hasFinished() || target == null || target.isDead() || !inChamber(target));

            int crystals = 0;
            while (crystals++ < 10) {
                event.add(() -> {
                    WorldTile pos = target.clone();
                    int delay = CRYSTAL_SPIKE_PROJECTILE.fire(olm, pos.relative(0, 1), pos);
                    World.sendGraphics(Settings.OSRS_GFX_OFFSET + 1353, pos);
                    target.gfx(Settings.OSRS_GFX_OFFSET + 246);
                    WorldTasksManager.schedule(() -> {
                        getTeam().forEach(player -> {
                            if(player.matches(pos))
                                player.applyHit(olm, Utils.random(100, 150), Hit.HitLook.REGULAR_DAMAGE);
                        });
                    });
                });
            }
        });
    }

    private void crystalBomb() {
        getChamber().setDebug("Crystal bomb");
        GreatOlm olm = this;
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        int bombCount;
        if (getRaid().getTeamSize() >= 30)
            bombCount = 3;
        else if (getRaid().getTeamSize() >= 15)
            bombCount = 2;
        else bombCount = 1;
        for (int i = 0; i < bombCount; i++) {
            WorldTasksManager.scheduleRevolving(event -> {
                event.setCancelCondition(() -> this.changingPhase());
                WorldTile bombPos = arenaBounds.randomPosition();
                int delay = CRYSTAL_BOMB_PROJECTILE.fire(this, bombPos);
                event.add(() -> {});
                WorldObject bomb = new WorldObject(Settings.OSRS_OBJECTS_OFFSET + 29766, 10, 0, bombPos);
                event.add(() -> World.spawnObject(bomb));
                event.delay(8);
                event.add(() -> {
                    bomb.remove();
                    World.sendGraphics( Settings.OSRS_GFX_OFFSET + 40, bombPos);
                    forAllTargets(p -> {
                        int distance = p.distance(bombPos);
                        if (distance > 3)
                            return;
                        p.applyHit(this, 600 - (distance * 150), Hit.HitLook.REGULAR_DAMAGE);
                    });
                });
            });
        }
    }

    private void burnAttack(List<Player> potentialTargets) {
        getChamber().setDebug("Burn");
        if (potentialTargets.size() == 0)
            return;
        Player target = Utils.get(potentialTargets);
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        BURN_PROJECTILE.fire(this, target);
        WorldTasksManager.schedule(() ->
            burnPlayer(target, false), 2);
    }

    private void burnPlayer(Player player, boolean wasSpread) {
        if (player.getTemporaryAttributtes().get("OLM_BURN_EFFECT") != null)
            return;
        if (wasSpread)
            player.forceTalk("I will burn with you!");
        GreatOlm olm = this;
        player.getTemporaryAttributtes().put("OLM_BURN_EFFECT", true);

        WorldTasksManager.scheduleRevolving(event -> {
            event.setCancelCondition(() -> this.changingPhase());
            for (int i = 0; i < 5; i++) {
                int ATTACK = i;
                event.add(() -> {
                    if (!player.inBounds(arenaBounds))
                        return;
                    if (!wasSpread || ATTACK > 0)
                        player.forceTalk("Burn with me!");
                    player.applyHit(this, 50, Hit.HitLook.MAGIC_DAMAGE);
                    player.getSkills().drainLevel(Skills.ATTACK, 2);
                    player.getSkills().drainLevel(Skills.STRENGTH, 2);
                    player.getSkills().drainLevel(Skills.DEFENCE, 2);
                    player.getSkills().drainLevel(Skills.RANGE, 2);
                    player.getSkills().drainLevel(Skills.MAGIC, 2);

                    // burn players within 1 tile
                    getTeam().stream().filter(other -> other.distance(player) <= 1).forEach(other ->
                            burnPlayer(other, true));
                });
                event.delay(5);
            }

            // after 5 burns remove
            event.add(() ->
                    player.getTemporaryAttributtes().remove("OLM_BURN_EFFECT"));
        });
    }

    private void flameWall(List<Player> potentialTargets) {
        getChamber().setDebug("Flamewall");

        if (potentialTargets.size() == 0)
            return;
        GreatOlm olm = this;
        Player target = Utils.get(potentialTargets);
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        int targetY = target.getY();
        int localY = targetY & 63;
        if (localY <= 36 || localY >= 51) // fail
            return;
        int projectileX = isOnEastSide() ? getRespawnTile().getX() - 10 : getRespawnTile().getX() - 1;
        WorldTile src1 = new WorldTile(projectileX, targetY + 1, 0);
        FLAME_WALL_PROJECTILE_1.fire(this, src1);
        WorldTile src2 = new WorldTile(projectileX, targetY - 1, 0);
        FLAME_WALL_PROJECTILE_1.fire(this, src2);
        List<WorldObject> fires = new ArrayList<>(20);

        WorldTasksManager.scheduleRevolving(event -> {
            event.setCancelCondition(() -> {
                boolean cancel = this.changingPhase();
                if(cancel) {
                    fires.forEach(fire -> {
                        fire.remove();
                        World.unclipTile(fire);
                    });
                }
                return cancel;
            });
            event.add(() -> {
                if (isOnEastSide()) {
                    int projStep = 5;
                    for (int x = projectileX; x < projectileX + 10; x++) {
                        if(x%2==0)projStep+=5;else projStep+=10;
                        Projectile p = new Projectile(1348 + Settings.OSRS_GFX_OFFSET, 0, 0, 0, projStep, 16, 0);
                        //FLAME_WALL_PROJECTILE_2.fire(olm, new WorldTile(src1.getX(), src1.getY(), 0), new WorldTile(x, targetY + 1, 0));
                        //FLAME_WALL_PROJECTILE_2.fire(olm, new WorldTile(src2.getX(), src2.getY(), 0), new WorldTile(x, targetY - 1, 0));
                        p.fire(olm, new WorldTile(src2.getX(), src2.getY(), 0), new WorldTile(x, targetY - 1, 0));
                        p.fire(olm, new WorldTile(src1.getX(), src1.getY(), 0), new WorldTile(x, targetY + 1, 0));
                    }
                } else {
                    for (int x = projectileX; x > projectileX - 10; x--) {
                        FLAME_WALL_PROJECTILE_2.fire(olm, new WorldTile(src1.getX(), src1.getY(), 0), new WorldTile(x, targetY + 1, 0));
                        FLAME_WALL_PROJECTILE_2.fire(olm, new WorldTile(src2.getX(), src2.getY(), 0), new WorldTile(x, targetY - 1, 0));
                    }
                }
            });

            event.add(() -> {
                if (isOnEastSide()) {
                    for (int x = projectileX; x < projectileX + 10; x++) {
                        fires.add(World.spawnObject(Settings.OSRS_OBJECTS_OFFSET + 32297, x, targetY + 1, 0, 0, 10));
                        fires.add(World.spawnObject(Settings.OSRS_OBJECTS_OFFSET + 32297, x, targetY - 1, 0, 0, 10));
                    }
                } else {
                    for (int x = projectileX; x > projectileX - 10; x--) {
                        fires.add(World.spawnObject(Settings.OSRS_OBJECTS_OFFSET + 32297, x, targetY + 1, 0, 0, 10));
                        fires.add(World.spawnObject(Settings.OSRS_OBJECTS_OFFSET + 32297, x, targetY - 1, 0, 0, 10));
                    }
                }
                for(WorldObject fire : fires) {
                    World.addFloor(fire);
                }
            });
            event.delay(8);
            event.add(() -> {
                getTeam().forEach(player -> {
                    if(player.getY() == targetY)
                        player.applyHit(this, Utils.random(350, 600), Hit.HitLook.MAGIC_DAMAGE);
                });
            });
            event.add(() -> {
                fires.forEach(fire -> {
                    fire.remove();
                    World.unclipTile(fire);
                });
            });
        });
    }

    private void spawnAcidPool(WorldTile position, int i) {
        i *= 2;
        WorldObject pool = new WorldObject(Settings.OSRS_OBJECTS_OFFSET + 30032, 10, 0, position);
        World.spawnObject(pool);
        acidPools.add(pool);
        WorldTasksManager.schedule(() -> {
            acidPools.remove(pool);
            pool.remove();
        }, i);
    }

    private void acidPoolsAttack() {
        getChamber().setDebug("Pools");
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        int poisonPools = 6 + (getRaid().getTeamSize() / 3);
        for (int i = 0; i < poisonPools; i++) {
            WorldTile pos = arenaBounds.randomPosition();
            int delay = ACID_POOL_PROJECTILE.fire(this, pos);
            WorldTasksManager.schedule(() -> {
                WorldObject pool = new WorldObject(Settings.OSRS_OBJECTS_OFFSET + 30032, 10, 0, pos);
                //World.sAcidPoolpawnObjectTemporary(pool, 10000);
                spawnAcidPool(pool, 10);
            }, /*3*/CombatScript.getDelay(delay) + 1);
        }
    }

    private void acidDrip() {
        getChamber().setDebug("Acid drip");
        GreatOlm olm = this;
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        List<Player> potentialTargets = getAttackableTargets(facing);
        if (potentialTargets.size() == 0)
            return;
        Player target = Utils.get(potentialTargets);
        ACID_DRIP_PROJECTILE.fire(this, target);
        target.sendMessage(Colour.RED.wrap("The Great Olm has smothered you in acid. It starts to drip off slowly."));
        WorldTasksManager.scheduleRevolving(event -> {
            Predicate<Player> cancelCondition = (player) -> olm.isDead() || olm.hasFinished() || target.isDead() || !inChamber(target);
            event.setCancelCondition(() -> this.changingPhase() || cancelCondition.test(target));
            event.delay(1);
            event.add(new WorldTask() {
                int ticks = 0;

                @Override
                public void run() {
                    if(ticks++ >= 20 || cancelCondition.test(target)) {
                        stop();
                        return;
                    }

                    if(target.skippedRunStep != null) {
                        if (World.getObjectWithType(target.skippedRunStep, 10) == null) {
                            spawnAcidPool(target.skippedRunStep, 10);
                        }
                    }
                    if (World.getObjectWithType(target.clone(), 10) == null) {
                        spawnAcidPool(target.clone(), 10);
                    }
                }
            });
        });
    }

    private void sphereAttack(List<Player> targets) {
        getChamber().setDebug("Spheres");
        animate(this, facing.getAttackAnim(isEmpowered()));
        delayedAnimation(this, facing.getIdleAnim(isEmpowered()), 1);
        for (int i = 0; i < 3 && targets.size() > 0; i++) {
            Player target = targets.remove(Utils.get(targets.size() - 1));
            Hit.HitLook style = Utils.random(1.0) < 1d / 3 ? Hit.HitLook.MAGIC_DAMAGE : (Utils.random(1.0) < 0.5 ? Hit.HitLook.RANGE_DAMAGE : Hit.HitLook.MELEE_DAMAGE);
            String message;
            Projectile projectile;
            int hitGfx;
            switch (style) {
                case MAGIC_DAMAGE:
                    message = Colour.PURPLE.wrap("The Great Olm fires a sphere of magical power your way.");
                    projectile = MAGIC_SPHERE;
                    hitGfx = Settings.OSRS_GFX_OFFSET + 1342;
                    break;
                case RANGE_DAMAGE:
                    message = Colour.DARK_GREEN.wrap("The Great Olm fires a sphere of accuracy and dexterity your way.");
                    projectile = RANGED_SPHERE;
                    hitGfx = Settings.OSRS_GFX_OFFSET + 1344;
                    break;
                case MELEE_DAMAGE:
                    message = Colour.RED.wrap("The Great Olm fires a sphere of aggression your way.");
                    projectile = MELEE_SPHERE;
                    hitGfx = Settings.OSRS_GFX_OFFSET + 1346;
                    break;
                default:
                    return;
            }

            if ((target.getPrayer().isMeleeProtecting())
                    || (target.getPrayer().isRangeProtecting())
                    || (target.getPrayer().isMageProtecting())) {
                if (target.getPrayer().getPrayerpoints() > 0) {
                    target.getPrayer().drainPrayer(target.getPrayer().getPrayerpoints() / 2);
                    target.getPrayer().closeProtectionPrayers();
                    message += " Your prayers have been sapped.";
                }
            }

            projectile.setSpeed(distance(target) < 3 ? 10 : 25);
            int delay = projectile.fire(this, target);
            target.sendMessage(message);
            target.setNextGraphics(new Graphics(hitGfx, 0, 100));
            GreatOlm olm = this;

            WorldTasksManager.schedule(() -> {
                if(this.changingPhase()) return;

                if(!olm.isDead() && !olm.hasFinished() && inChamber(target)) {
                    if ((!target.getPrayer().isMeleeProtecting() && style == Hit.HitLook.MELEE_DAMAGE)
                            || (!target.getPrayer().isRangeProtecting() && style == Hit.HitLook.RANGE_DAMAGE)
                            || (!target.getPrayer().isMageProtecting() && style == Hit.HitLook.MAGIC_DAMAGE)) {
                        if (target.getPrayer().getPrayerpoints() > 0) {
                            target.applyHit(new Hit(this, Utils.random(300, 350), style));
                        }
                    }
                }
            }, CombatScript.getDelay(delay));
        }
    }

    private void crystalBurst() {
        if (leftClaw != null && leftClaw.isDead() || leftHandDown() || clenched)
            return;
        getChamber().setDebug("Crystal burst");
        animate(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7356);
        delayedAnimation(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7355, 2);
        getTeam().forEach(p -> {
            WorldTile pos = p.clone();
            WorldObject crystal = new WorldObject(Settings.OSRS_OBJECTS_OFFSET + 30033, 10, 0, pos);
            World.spawnObject(crystal);
            WorldTasksManager.scheduleRevolving(event -> {
                for(int i = 0; i < 2; i++)
                    event.add(() -> {});//skip tick
                event.add(() -> {
                    event.add(() -> {});//skip tick
                    crystal.updateId(Settings.OSRS_OBJECTS_OFFSET + 30034);
                    if (!changingPhase() && !p.isDead() && p.matches(pos)) {
                        p.anim(424);
                        p.applyHit(this, 350);
                    }
                });
                event.add(() -> {});//skip tick
                event.add(() -> {
                    crystal.remove();
                });
            });

        });
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(phaseChangeStage != 0) {
            return false;
        }

        WeaponTypesLoader.WeaponType type = WeaponTypesLoader.getWeaponDefinition(attacker.getEquipment().getWeaponId());
        if(type == null || type.getStyle() == Combat.MELEE_TYPE) {
            // Can't attack olm with melee
            attacker.sendMessage("I can't reach that!");
            return false;
        }

        if(lastPhase != currentPhase || !leftHandDown() || !rightHandDown()) {
            attacker.sendMessage("Great Olm is too powerful to attack now!");
            return false;
        }

        return super.preAttackCheck(attacker);
    }

    private void lightningAttack() {
        if (leftClaw.isDead() || clenched)
            return;
        GreatOlm olm = this;
        animate(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7358);
        delayedAnimation(leftClaw, Settings.OSRS_ANIMATIONS_OFFSET + 7355, 2);
        for (int x = 30; x <= 36; x++) {
            if (!Utils.rollPercent(45))
                continue;
            final int X = x;
            int yStep = Utils.rollPercent(50) ? -1 : 1;
            WorldTile lightningPos = getTile(X, yStep == -1 ? 52 : 35);
            WorldTasksManager.schedule(new WorldTask() {
                int i = 0;

                @Override
                public void run() {
                    // todo: check if event gets canceled in intermissions
                    if (olm.changingPhase() || olm.isDead() || olm.hasFinished() || i++ == 17) {
                        stop();
                        return;
                    }
                    World.sendGraphics(Settings.OSRS_GFX_OFFSET + 1356, lightningPos);
                    forAllTargets(player -> {
                        if (player.matches(lightningPos)) {
                            player.applyHit(olm, 50 + Utils.random(200));
                            player.addFreezeDelay(1200, false);
                            player.setPrayerDelay(1200);
                            player.sendMessage(Colour.RED.wrap("You've been electrocuted to the spot!"));
                        }
                    });
                    lightningPos.translate(0, yStep, 0);
                }
            }, 0, 0);
        }
    }

    public boolean inChamber(Player player) {
        return raid.getCurrentChamber(player) == raid.getGreatOlmChamber();
    }

    public boolean changingPhase() {
        return phaseChangeStage != 0;
    }
    private void teleportAttack() {
        if (leftClaw.isDead() || clenched)
            return;
        GreatOlm olm = this;
        animate(leftClaw, 7359 + Settings.OSRS_ANIMATIONS_OFFSET);
        delayedAnimation(leftClaw, 7355 + Settings.OSRS_ANIMATIONS_OFFSET, 2);
        LinkedList<Player> targets = new LinkedList<>(getTeam());
        Collections.shuffle(targets);
        for (int i = 0; i < 4; i++) {
            if (targets.isEmpty())
                return;
            Player player = targets.pop();
            Player other = null;
            WorldTile tile = null;
            if (!targets.isEmpty())
                other = targets.pop();
            if (other != null) {
                player.sendMessage("The Great Olm has paired you with " + Colour.RED.wrap(other.getName()) + "! The magical power will enact soon...");
                other.sendMessage("The Great Olm has paired you with " + Colour.RED.wrap(player.getName()) + "! The magical power will enact soon...");
            } else {
                if(getTeam().size() != 1) {
                    // odd player in ground gets skipped
                    return;
                }
                player.sendMessage("The Great Olm had no one to pair you with! The magical power will enact soon...");
                tile = centerTargetBounds.randomPosition();
            }
            int gfxId = Settings.OSRS_GFX_OFFSET + 1359 + i;
            Player finalOther = other;
            WorldTile finalTile = tile;
            WorldTasksManager.scheduleRevolving(event -> {
                if(changingPhase()) return;
                event.setCancelCondition(()->!olm.isDead() && !olm.hasFinished() && !inChamber(player) || (finalOther != null && !inChamber(finalOther)));
                for (int ticks = 0; ticks < 10; ticks++) {
                    event.add(() -> {
                        if(changingPhase()) return;
                        player.gfx(gfxId);
                        if (finalOther != null)
                            finalOther.gfx(gfxId);
                        else
                            World.sendGraphics(gfxId, finalTile);
                    });
                }

                event.add(() -> {
                    if(changingPhase()) return;
                    int distance = player.distance(finalOther != null ? finalOther : finalTile);
                    if (distance > 20)
                        return;
                    if (distance == 0) {
                        player.sendMessage("The teleport attack has no effect!");
                        if (finalOther != null)
                            finalOther.sendMessage("The teleport attack has no effect!");
                    } else {
                        player.setNextWorldTile(finalOther != null ? finalOther.clone() : finalTile.clone());
                        player.gfx(Settings.OSRS_GFX_OFFSET + 1039);
                        player.applyHit(olm, distance * 50);
                        if (finalOther != null) {
                            finalOther.setNextWorldTile(player.clone());
                            finalOther.gfx(Settings.OSRS_GFX_OFFSET + 1039);
                            finalOther.applyHit(olm, distance * 50);
                        }
                    }
                });
            });
        }
    }

    private List<Player> getAttackableTargets(Facing f) {
        Predicate<Player> targetable = player -> {
            int y = raid.getInstanceTile(player).getY();
            return f == CENTER || y == 44 // attack everyone if facing center or player at center tile
                    || (f == RIGHT && ((isOnEastSide() && y > 44) || (!isOnEastSide() && y < 44)))
                    || (f == LEFT && ((isOnEastSide() && y < 44) || (!isOnEastSide() && y > 44)));
        };

        return getTeam().stream()
                .filter(targetable::test)
                .collect(Collectors.toList());
    }

    private List<Player> getFarTargets(Facing f) {
        Predicate<Player> targetable = player -> {
            int y = raid.getInstanceTile(player).getY();
            return (f == RIGHT && ((isOnEastSide() && y > 44) || (!isOnEastSide() && y < 44)))
                    || (f == LEFT && ((isOnEastSide() && y < 44) || (!isOnEastSide() && y > 44)));
        };

        return getTeam().stream()
                .filter(targetable::test)
                .collect(Collectors.toList());
    }
    public void checkClench(Hit hit) {
        if (rightClaw.isDead() || leftClaw.isDead() || leftHandDown() || rightHandDown()|| clenched || currentPhase == lastPhase)
            return;
        clenchDamageCounter += hit.getDamage();
        if ((hit.getDamage() >= 300 && Utils.random(0, 5) == 1)  || clenchDamageCounter >= leftClaw.getMaxHitpoints() / 5) {
            forAllTargets(p -> p.sendMessage("The Great Olm's left claw clenches to protect itself temporarily."));
            clenchDamageCounter = 0;
            clenched = true;
            animate(leftClaw, 7360 + Settings.OSRS_ANIMATIONS_OFFSET);
            delayedAnimation(leftClaw, 7361 + Settings.OSRS_ANIMATIONS_OFFSET, 1);
            schedule(() -> {
                forAllTargets(p -> p.sendMessage("The Great Olm regains control of its left claw!"));
                clenched = false;
                animate(leftClaw, 7362 + Settings.OSRS_ANIMATIONS_OFFSET);
                delayedAnimation(leftClaw, 7355 + Settings.OSRS_ANIMATIONS_OFFSET, 2);
            }, 20);
        }
    }

    public static final int[][] SPELL_RUNES = {
            {Magic.WATER_RUNE, 1, Magic.AIR_RUNE, 1, Magic.MIND_RUNE, 1}, // strike
            {Magic.WATER_RUNE, 3, Magic.AIR_RUNE, 2, Magic.CHAOS_RUNE, 1}, // bolt
            {Magic.WATER_RUNE, 4, Magic.AIR_RUNE, 3, Magic.DEATH_RUNE, 1}, // blast
            {Magic.WATER_RUNE, 7, Magic.AIR_RUNE, 5, Magic.BLOOD_RUNE, 1}, // wave
            {Magic.WATER_RUNE, 10, Magic.AIR_RUNE, 7, Magic.BLOOD_RUNE, 1, Magic.DEATH_RUNE, 1}, // surge
    };

    public static final int[][] SPELL_DATA = {
            {21163, 5093}, // strike
            {21163, 5121}, // bolt
            {21163, 5135}, // blast
            {21167, 5161}, // wave
            {21165, 6458}, // surge
            {4947, 1061}, // humidify
    };

    public void douseFlameWall(Player player, WorldObject obj) {
        GreatOlm olm = this;
        int spell = -1;
        for(int i = 0; i < SPELL_RUNES.length; i++) {
            if(Magic.checkRunes(player, false, false, false, SPELL_RUNES[i])) {
                spell = i;
            }
        }

        if(spell == -1) {
            player.sendMessage("You do not have enough runes to cast any water spells at the flame!");
            return;
        }

        int SPELL = spell;

        if(spell != -1 && Magic.checkRunes(player, true, false, false, SPELL_RUNES[SPELL])) {
            player.setNextFaceWorldTile(obj.clone());
            player.anim(SPELL_DATA[spell][0]);
            player.setNextGraphics(new Graphics(SPELL_DATA[spell][1], 0, 124));
            WorldTasksManager.schedule(() -> {
                World.sendProjectile(player, obj, SPELL_DATA[SPELL][1] + 1, 60, 20, 40, 50, 0, 0);
                WorldTasksManager.schedule(() -> {
                    World.sendGraphics(player, new Graphics(SPELL_DATA[SPELL][1] + 2, 40, 124), obj);
                    WorldTasksManager.schedule(() -> {
                        World.unclipTile(obj);
                        obj.remove();
                    });
                });
            });
        }
    }

    public boolean isClenched() {
        return clenched;
    }

    private static final int OAO = Settings.OSRS_ANIMATIONS_OFFSET;

    public GreatOlmRightClaw getRightHand() {
        return rightClaw;
    }

    public COXBoss getRestoreClaw() {
        return restoreClaw;
    }

    private int leftDamageThisTick = 0, rightDamageThisTick, lastDamageTick = 0;

    /**
     * Used for head turn mechanic
     */
    public void incomingDamage(Hit hit, boolean leftClaw) {
        if(hit.getDamage() < 10) return;

        if(leftClaw && clawHealing) {
            hit.setHealHit();
        }

        if(leftClaw)
            leftDamageThisTick += hit.getDamage();
        else
            rightDamageThisTick += hit.getDamage();
    }

    public int getPhase() {
        return currentPhase;
    }

    enum Facing {
        RIGHT(7337 + OAO, 7376 + OAO, 7346 + OAO, 7373 + OAO,
                7339 + OAO, 7343 + OAO, 7381 + OAO, 7379 + OAO,
                28, 44,
                37, 52),
        CENTER(7336 + OAO, 7374 + OAO, 7345 + OAO, 7371 + OAO,
                7340 + OAO, 7342 + OAO, 7382 + OAO, 7378 + OAO,
                28, 39,
                37, 49),
        LEFT(7338 + OAO, 7375 + OAO, 7347 + OAO, 7372 + OAO,
                7341 + OAO, 7344 + OAO, 7377 + OAO, 7380 + OAO,
                28, 35,
                37, 45),
        ;

        int idleAnim, empoweredIdleAnim;
        int attackAnim, empoweredAttackAnim;
        int closeTransitionAnim, farTransitionAnim;
        int empoweredCloseTransitionAnim, empoweredFarTransitionAnim;
        int swX, swY;
        int neX, neY; // local targeting area, assuming olm is on the east side. must be flipped if on west

        Facing(int idleAnim, int empoweredIdleAnim, int attackAnim, int empoweredAttackAnim, int closeTransitionAnim, int farTransitionAnim, int empoweredCloseTransitionAnim, int empoweredFarTransitionAnim, int swX, int swY, int neX, int neY) {
            this.idleAnim = idleAnim;
            this.empoweredIdleAnim = empoweredIdleAnim;
            this.attackAnim = attackAnim;
            this.empoweredAttackAnim = empoweredAttackAnim;
            this.closeTransitionAnim = closeTransitionAnim;
            this.farTransitionAnim = farTransitionAnim;
            this.empoweredCloseTransitionAnim = empoweredCloseTransitionAnim;
            this.empoweredFarTransitionAnim = empoweredFarTransitionAnim;
            this.swX = swX;
            this.swY = swY;
            this.neX = neX;
            this.neY = neY;
        }

        int getIdleAnim(boolean empowered) {
            return empowered ? empoweredIdleAnim : idleAnim;
        }

        int getAttackAnim(boolean empowered) {
            return empowered ? empoweredAttackAnim : attackAnim;
        }

        int getCloseTransitionAnim(boolean empowered) {
            return empowered ? empoweredCloseTransitionAnim : closeTransitionAnim;
        }

        int getFarTransitionAnim(boolean empowered) {
            return empowered ? empoweredFarTransitionAnim : farTransitionAnim;
        }

    }

    enum PhasePower {
        ACID(Colour.DARK_GREEN.wrap("acid")),
        FLAME(Colour.RED.wrap("flame")),
        CRYSTAL(Colour.RAID_PURPLE.wrap("crystal"));

        String name;

        PhasePower(String name) {
            this.name = name;
        }
    }
}