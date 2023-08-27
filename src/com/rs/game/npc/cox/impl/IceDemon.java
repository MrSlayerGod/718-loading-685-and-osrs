package com.rs.game.npc.cox.impl;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.tasks.WorldTasksManager.WorldTaskList;
import com.rs.utils.NPCCombatDefinitionsL;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Oct 17, 2020
 */
public class IceDemon extends COXBoss {
    public static final int KINDLING = Settings.OSRS_ITEM_OFFSET + 20799;
    public static final WorldTile DEMON_SPAWN_TILE = new WorldTile(88, 80, 3);
    public static final int[][] FIEND_TILES = {
            {88, 83},
            {85, 83},
            {85, 78},
            {88, 78}
    };
    public static final int[][] BRAZIER_TILES = {
            {88, 84},
            {85, 82},
            {85, 79},
            {88, 77}
    };
    public static final int ICE_DEMON_FROZEN = Settings.OSRS_NPC_OFFSET + 7584; // initial state (not melted)
    public static final int ICE_DEMON = Settings.OSRS_NPC_OFFSET + 7585; // active fight
    private static final int ICE_FIEND = Settings.OSRS_NPC_OFFSET + 7586;
    public static final int UNLIT_BRAZIER = Settings.OSRS_OBJECTS_OFFSET + 29747;
    public static final int LIT_BRAZIER = Settings.OSRS_OBJECTS_OFFSET + 29748;
    private static final int SNOW = Settings.OSRS_OBJECTS_OFFSET + 29876;
    private static final Projectile RANGED_PROJECTILE = new Projectile(Settings.OSRS_GFX_OFFSET + 1324, 60, 0, 50, 30, /*60*/ 0, 0);
    private static final Projectile MAGIC_PROJECTILE = new Projectile(Settings.OSRS_GFX_OFFSET + 366, 60, 38, 51, 30, /*60*/ 0, 0);
    private ArrayList<WorldObject> braziers = new ArrayList<>(4);
    private ArrayList<NPC> fiends = new ArrayList<>(4);
    private WorldTaskList dropShieldTask;
    private boolean encasedInIce = false;
    private int shieldHealth = 1000;
    private int[] brazierKindling = new int[4];
    private WorldObject snow; // snow object blocks exit
    private int tick = 0;

    public IceDemon(ChambersOfXeric raid) {
        super(raid, ICE_DEMON_FROZEN, raid.getTile(DEMON_SPAWN_TILE.getX(), DEMON_SPAWN_TILE.getY(), DEMON_SPAWN_TILE.getPlane()), raid.getIceDemonChamber());
        setCantInteract(true);
        setIntelligentRouteFinder(false);
        setCantFollowUnderCombat(true);
        setCombatScript();
    }

    public void onActivation() {
        if(!isInit()) {
            scale(getRaid().getTeamSize());
        }
        shieldHealth = getMaxHitpoints();
        snow = new WorldObject(SNOW, 10, 0, this.clone());

        //World.addFloor(getChamber().getWorldTile(25, 16));
        //World.addFloor(getChamber().getWorldTile(25, 17)); // block exit
        World.spawnObject(snow);

        setForceAgressive(true);
        setDirection(Utils.getAngle(-1, 0));

        WorldTasksManager.schedule(() -> {
            // wait for map to be built
            for (int[] offsets : BRAZIER_TILES) {
                WorldObject obj = World.getObjectWithId(raid.getTile(offsets[0], offsets[1], getPlane()), UNLIT_BRAZIER);
                braziers.add(obj);
            }
            // ice fiends
            for (int[] offsets : FIEND_TILES) {
                NPC n = World.spawnNPC(ICE_FIEND, raid.getTile(offsets[0], offsets[1], getPlane()), -1, false);
                n.setRandomWalk(0);
                fiends.add(n);
                int index = fiends.size() - 1;
                fiends.get(index).setNextFaceWorldTile(braziers.get(index));
            }
        }, 2, 0);

        dropShieldTask = new WorldTaskList();
        IceDemon iceDemon = this;

        // tile for demon to walk to after unfrozen
        WorldTile walkTile = raid.getTile(DEMON_SPAWN_TILE.getX() - 6, DEMON_SPAWN_TILE.getY(), 3);

        dropShieldTask.add(() -> {
            fiends.stream().forEach(npc -> npc.anim(21580));
            addWalkSteps(walkTile.getX(), walkTile.getY(), -1, false);
        });
        dropShieldTask.add(() -> {
            // remove fiends & braziers
            fiends.stream().forEach(npc -> npc.finish());
            braziers.stream().forEach(object -> object.updateId(UNLIT_BRAZIER));
        });
        dropShieldTask.add(new WorldTask() {
            @Override
            public void run() {
                resetWalkSteps();
                if (!iceDemon.matches(walkTile)) {
                    iceDemon.addWalkSteps(walkTile.getX(), walkTile.getY(), -1, false);
                } else {
                    debug("dropShieldTask completed");
                    iceDemon.scale(getRaid().getTeamSize());
                    iceDemon.setCantInteract(false);
                    iceDemon.setForceAgressive(true);
                    stop();
                }
            }
        });
    }

    @Override
    public void faceEntity(Entity target) {
    }

    @Override
    public void faceEntity2(Entity target) {
        super.faceEntity2(target);
    }

    @Override
    public void setNextFaceEntity(Entity entity) {
    }

    private void setCombatScript() {
        setCustomCombatScript(new CombatScript() {
            @Override
            public Object[] getKeys() {
                return new Object[0];
            }

            public void fireAttack(NPC npc, Entity target) {
                IceDemon iceDemon = (IceDemon) npc;

                if (target.isPlayer() && npc.withinDistance(target, 10)) {
                    Player player = target.asPlayer();
                    if (player.getPrayer().isRangeProtecting()) {
                        iceDemon.rangedAttack(player);
                    } else if (player.getPrayer().isMageProtecting()) {
                        iceDemon.magicAttack(player);
                    } else if (Utils.random(1) == 0)
                        iceDemon.magicAttack(player);
                    else
                        iceDemon.rangedAttack(player);
                }
            }
            @Override
            public int attack(NPC npc, Entity target) {
                if(npc.isDead() || npc.hasFinished())
                    return 0;

                NPCCombatDefinitions def = npc.getCombatDefinitions();

                target = getClosestPlayer();

                if(target == null)
                    return 0;

                npc.faceEntity2(target);

                fireAttack(npc, target);

                // attack everyone
                if(Utils.rollDie(4, 1)) {
                    for(Player player : getTeam()) {
                        if(player == target) continue;
                        fireAttack(npc, player);
                    }
                }

                return npc.getCombatDefinitions().getAttackDelay();
            }
        });
    }

    public void magicAttack(Player target) {
        NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(getId());
        anim(def.getAttackEmote());

        WorldTile targetPosition = target.clone();
        //int delay = MAGIC_PROJECTILE.fire(this, target);
        int delay = World.sendProjectile(this.clone(), targetPosition.clone(), MAGIC_PROJECTILE.getGfx(), MAGIC_PROJECTILE.getStartHeight(), MAGIC_PROJECTILE.getEndHeight(), MAGIC_PROJECTILE.getSpeed(), MAGIC_PROJECTILE.getDelay(), MAGIC_PROJECTILE.getCurve(), MAGIC_PROJECTILE.getStartDistanceOffset());

        WorldTasksManager.schedule(() -> {
            // burst 1 tile around target tile
            targetPosition.area(1).forEach(pos -> World.sendGraphics(Settings.OSRS_GFX_OFFSET + 363, pos));

            getTeam().forEach((player -> {
                if (player.withinDistance(targetPosition, 1)) {
                    int damage = Utils.random(100, 400);
                    if(!player.isFrozen())
                        player.addFreezeDelay(3000);
                    player.applyHit(new Hit(this, damage, Hit.HitLook.MAGIC_DAMAGE));
                }
            }));
        }, CombatScript.getDelay(delay), 0);
    }

    public void rangedAttack(Player target) {
        NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(getId());
        anim(def.getAttackEmote());

        WorldTile targetPosition = target.clone();
        int delay = World.sendProjectile(this.clone(), targetPosition.clone(), RANGED_PROJECTILE.getGfx(), RANGED_PROJECTILE.getStartHeight(), RANGED_PROJECTILE.getEndHeight(), RANGED_PROJECTILE.getSpeed(), RANGED_PROJECTILE.getDelay(), RANGED_PROJECTILE.getCurve(), RANGED_PROJECTILE.getStartDistanceOffset());

        WorldTasksManager.schedule(() -> {
            World.sendGraphics(Settings.OSRS_GFX_OFFSET + 1325, targetPosition);
            getTeam().forEach((player -> {
                if (player.matches(targetPosition)) {
                    int damage = Utils.random(100, 400);
                    player.applyHit(new Hit(this, damage, Hit.HitLook.RANGE_DAMAGE));
                }
            }));
        }, CombatScript.getDelay(delay), 0);
    }

    public void addKindling(Player player, WorldObject gameObject) {
        if(shieldHealth < 1)
            return;

        int kindling = player.getInventory().getAmountOf(KINDLING);
        kindling*=2;//buff for lazy players
        if (kindling == 0) {
            player.sendMessage("You'll need some kindling to light the brazier.");
            return;
        }

        int brazierIndex = -1;
        for (int i = 0; i < braziers.size(); i++)
            if (braziers.get(i).matches(gameObject))
                brazierIndex = i;

        player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 832);
        player.sendMessage("You add some kindling to the brazier.");
        player.getInventory().deleteItem(KINDLING, kindling);
        brazierKindling[brazierIndex] += kindling;
        if (gameObject.getId() == UNLIT_BRAZIER)
            gameObject.updateId(LIT_BRAZIER);
        raid.addPoints(player, kindling * 10);
    }

    public Player target = null;
    public long lastAttack = 0;

    @Override
    public void processNPC() {
        tick++;

        if (isEncasedInIce()) {
            // fiend consumes kindling
            checkKindling();
            applyHit(this, 0); // show hp bar always
        } else {
            if(!dropShieldTask.finished())
                return;
            if(lastAttack < System.currentTimeMillis())
                lastAttack = System.currentTimeMillis() + 1800;
            else return;

            if(target == null || !clipedProjectile(target, false)) {
                List<Player> targets = getTeam().stream().filter(player -> player != target).collect(Collectors.toList());
                if(targets.size() > 0)
                    target = Utils.get(targets);
            }

            if(target != null) {
                getCustomCombatScript().attack(this, target);
                target.setAttackedBy(this);
                target.setAttackedByDelay(3000);
                faceEntity(target);
            }
        }

        super.processNPC();
    }

    /*
     * check kindling in each brazier
     */
    private void checkKindling() {
        // task list will execute on death
        if (dropShieldTask == null || dropShieldTask.isExecuted())
            return;

        for (int i = 0; i < braziers.size(); i++) {
            if (brazierKindling[i] > 0) {
                int damage = 0;
                int remove = Math.min(2, getRaid().getTeamSize());
                int kindling = brazierKindling[i];
                if (kindling >= remove) {
                    damage += 10;
                    //if (i < Math.min(3, getRaid().getTeamSize())) {
                    // if guarded by icefiend, consume more
                    if (tick % 2 == 0) {
                        //remove *= 1.5;
                        fiends.get(i).anim(Settings.OSRS_ANIMATIONS_OFFSET + 7820);
                    }
                    //}
                    kindling -= remove;
                    if (kindling < 0) kindling = 0;
                    brazierKindling[i] = kindling;
                    if (kindling == 0) {
                        braziers.get(i).updateId(UNLIT_BRAZIER);
                    }
                }

                shieldHealth -= Math.min(damage, shieldHealth);
                debug("removedKindling=" + remove + " damage=" + damage + " shield=" + shieldHealth);
            }
        }

        //debug(shieldHealth + " / " + Arrays.toString(brazierKindling));
        setHitpoints(shieldHealth);

        if (shieldHealth <= 0) {
            setHitpoints(getMaxHitpoints());
            setNextNPCTransformation(ICE_DEMON);
            WorldTasksManager.schedule(() ->
                applyHit(this, 0)); // show new hp bar
            dropShieldTask.execute2t();
        }
    }

    public boolean isEncasedInIce() {
        return (getId() == ICE_DEMON_FROZEN);
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if(hit.getSource().isPlayer()) {
            Player player = hit.getSource().asPlayer();
            int spell = (int) player.getTemporaryAttributtes().getOrDefault("lastSpellCast", -1);
            player.getTemporaryAttributtes().remove("lastSpellCast");
            boolean fireSpell = spell == 45 || spell == 63 || spell == 80 || spell == 91 || spell == 68;
            if(fireSpell)
                hit.setDamage((int) ((double) hit.getDamage() * 1.5));
            else
                hit.setDamage((int) ((double) hit.getDamage() * .667));
        }

        if (getId() == ICE_DEMON_FROZEN) {
            if (hit.getSource().isPlayer())
                hit.getSource().asPlayer().sendMessage("The demon is protected by ice, your attacks would be ineffective!");
            hit.getSource().resetCombat();
            return;
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public void sendDeath(Entity source) {
        if (getId() == ICE_DEMON) {
            World.unclipTile(getChamber().getWorldTile(25, 16));
            World.unclipTile(getChamber().getWorldTile(25, 17));
            snow.remove();
            super.sendDeath(source);
        }
    }

    @Override
    public int getHitbarSprite(Player player) {
        return getId() == ICE_DEMON_FROZEN ? 21416 : 22191;
    }

    public void fillBraziers() {
        for(int i = 0; i < brazierKindling.length; i++) {
            brazierKindling[i] = 200;
            braziers.get(i).updateId(LIT_BRAZIER);
        }
    }
}
