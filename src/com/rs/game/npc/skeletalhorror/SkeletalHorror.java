package com.rs.game.npc.skeletalhorror;

import com.rs.Settings;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.Mimic;
import com.rs.game.player.Projectile;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Lamps;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.controllers.SkeletalHorrorController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Direction;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simplex
 * created on 2021-01-31
 */
public class SkeletalHorror extends NPC {
    private static final Projectile MAGE_PROJECTILE = new Projectile(2126, 70, 31, 50, 56, 16, 127);

    public static final WorldTile OUTSIDE = new WorldTile(3367, 3513, 0);

    public static final int ENTER_INSTANCE_STILE = 44378, ENTER_FIGHT_STILE = 44379;

    public static final int P1_NPC_ID = 9177, // normal
            P2_NPC_ID = 9178, // missing right arm
            P3_NPC_ID = 9179, // missing both arms
            P4_NPC_ID = 9180, // missing both arms and tail
            RIGHT_NPC_HAND_ID = 9181,
            LEFT_NPC_HAND_ID = 9182,
            TAIL_NPC_ID = 9183;

    public static final int RIGHT_HAND_ITEM_ID = 15217,
            LEFT_HAND_ITEM_ID = 15218,
            TAIL_ITEM_ID = 15219;

    public static final int RIGHT_ARM_PROJECTILE = 2130,
            LEFT_ARM_PROJECTILE = 2131,
            TAIL_PROJECTILE = 2132;

    private static final String[] MINION_MESSAGES = {"RRaaaahh", "Aaaaargh", "Grraaarrgh"};

    private final SkeletalHorrorController controller;

    private int phaseDamage = 0;
    private boolean extremityActive = false;

    public SkeletalHorror(WorldTile tile, SkeletalHorrorController controller) {
        super(P1_NPC_ID, tile, -1, true, true);
        setCantInteract(true);
        setDirection(Direction.WEST, true);
        setForceMultiArea(true);
        setNoDistanceCheck(true);
        setForceAgressive(true);
        anim(12060);
        throwPositions = new WorldTile[] {
               tile.relative(6, 6),
                tile.relative(6, 0),
                tile.relative(6, -6),
                tile.relative(0, -6),
                tile.relative(-6, -6),
                tile.relative(-6, 6),
                tile.relative(0, 6),
        };
        this.controller = controller;
        setCombat();
        setDrops();
    }

    private void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        dList[Drops.ALWAYS].add(new Drop(532, 1, 1));
        dList[Drops.ALWAYS].add(new Drop(995, 1000000, 5000000));
        dList[Drops.ALWAYS].add(new Drop(19043, 1, 1)); // elite clue
        dList[Drops.ALWAYS].add(new Drop(23714, 1, 1)); // medium xp lamp
        dList[Drops.RARE].add(new Drop(54034, 1, 1)); // dragonstone helm
        dList[Drops.RARE].add(new Drop(54037, 1, 1)); // dragonstone plate
        dList[Drops.RARE].add(new Drop(54040, 1, 1)); // dragonstone legs
        dList[Drops.RARE].add(new Drop(54043, 1, 1)); // dragonstone gaunts
        drops.addDrops(dList);
        NPCDrops.addDrops(P1_NPC_ID, drops);
        NPCDrops.addDrops(P2_NPC_ID, drops);
        NPCDrops.addDrops(P3_NPC_ID, drops);
        NPCDrops.addDrops(P4_NPC_ID, drops);
    }

    public void setCombat() {
        this.setCustomCombatScript(new CombatScript() {

            @Override
            public Object[] getKeys() { return new Object[0]; };

            @Override
            public int attack(NPC npc, Entity target) {
                boolean ood = !Utils.isOnRange(npc, target, 0);
                boolean meleePrayer = target.isPlayer() && target.asPlayer().getPrayer().isMeleeProtecting();

                if(ood && (Utils.random(5) == 0 || meleePrayer && Utils.random(2) == 1)) {
                    // aggress target
                    calcFollow(target, false);
                }

                if(getId() != P4_NPC_ID && (ood || Utils.random(2) == 1))
                    mageAttack(npc, target);
                else
                    meleeAttack(npc, target);
                return 5;
            }

            private void mageAttack(NPC npc, Entity target) {
                npc.anim(12060);
                //npc.gfx(VULN_GFX);
                int delay = CombatScript.getDelay(MAGE_PROJECTILE.fire(npc, target));
                int maxDamage = 250;
                //if (target != null && target.getPrayer().isMageProtecting())
                //maxDamage /= 2;
                int damage = getRandomMaxHit(npc, maxDamage, NPCCombatDefinitions.MAGE, target);
                if(target.isPlayer() && damage > 10) {
                    int defDrain = (int) ((double) damage * 0.02);
                    target.asPlayer().getSkills().drainLevel(Skills.DEFENCE, defDrain);
                }
                WorldTasksManager.schedule(() -> {
                    target.setNextGraphics(new Graphics(2128, 0, 0));
                    target.applyHit(npc, damage, Hit.HitLook.MAGIC_DAMAGE);
                }, delay-1);
            }

            private void meleeAttack(NPC npc, Entity target) {
                npc.anim(getCombatDefinitions().getAttackEmote());
                delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
            }
        });
    }


    @Override
    public void drop() {
        controller.getPlayer().getSkills().addXp(Skills.PRAYER, 100000, true);
        controller.getPlayer().getSkills().addXp(Skills.SLAYER, 100000, true);
        checkSlayer(controller.getPlayer());
        super.drop();
    }

    public void processNPC() {
        if(!isLocked())
            super.processNPC();
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if(hit.getLook() != Hit.HitLook.HEALED_DAMAGE) {
            if(extremityActive)
                hit.setDamage((int) ((double)hit.getDamage() * 0.25));
            phaseDamage += hit.getDamage();
        }

        if(phaseDamage >= 1250 && !extremityActive) {
            switchPhase();
            phaseDamage = 0;
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public void sendDeath(Entity source) {
        if(getId() != P4_NPC_ID) {
            // failsafe
            switchPhase();
            setHitpoints(1250);
            return;
        }

        LuckyPets.checkPet(source.asPlayer(), LuckyPets.LuckyPet.HORROR_1);

        controller.completed();
        super.sendDeath(source);
    }

    WorldTile[] throwPositions;

    private void switchPhase() {
        if(getId() == P4_NPC_ID) {
            return;
        }

        lock();

        int newId = P2_NPC_ID;
        int gfxId = RIGHT_ARM_PROJECTILE;
        int spawnNpc = RIGHT_NPC_HAND_ID;
        int prevId = this.getId();
        int prevMaxHP = this.getMaxHitpoints();

        switch(getId()) {
            case P2_NPC_ID:
                newId = P3_NPC_ID;
                gfxId = LEFT_ARM_PROJECTILE;
                spawnNpc = LEFT_NPC_HAND_ID;
                break;
            case P3_NPC_ID:
                newId = P4_NPC_ID;
                gfxId = TAIL_PROJECTILE;
                spawnNpc = TAIL_NPC_ID;
                break;
        }

        // throw extremity, face that pos, lock until hits the ground, start walk back/head task list
        WorldTile throwPos = Utils.get(throwPositions);
        anim(getCombatDefinitions().getAttackEmote());
        setNextFaceWorldTile(throwPos);
        setNextNPCTransformation(newId);
        lock();
        Entity target = getCombat().getTarget();
        setTarget(null);
        setNextFaceEntity(null);

        // send projectile
        int delay = World.sendProjectile(this, throwPos, gfxId, 70, 0, 30, 20/*60*/, 0, 0);

        int npc = spawnNpc;
        WorldTasksManager.schedule(() -> {
            extremityActive = true;
            unlock();
            setTarget(target);
            setNextFaceEntity(target);

            NPC extremity = World.spawnNPC(npc, throwPos, -1, true, true);

            // walk extremity back to horror
            WorldTasksManager.schedule(event -> {
                event.setCancelCondition(() -> SkeletalHorror.this.isDead());
                event.delay(1);
                event.add(() -> {
                    extremity.addWalkSteps(SkeletalHorror.this.getX(), SkeletalHorror.this.getY(), -1, false);
                });

                // wait for the extremity to reach the horror, if it does, heal and reverse a phase
                event.add(new WorldTask() {
                    int t = 0;
                    @Override
                    public void run() {
                        if(extremity.hasFinished()) {
                            extremityActive = false;
                            stop();
                            return;
                        }

                        extremity.addWalkSteps(SkeletalHorror.this.getX(), SkeletalHorror.this.getY(), -1, false);

                        if(t++ > 14 || Utils.isOnRange(extremity, SkeletalHorror.this, 1)) {
                            extremityActive = false;
                            extremity.finish();
                            SkeletalHorror.this.anim(12060);
                            SkeletalHorror.this.setNextNPCTransformation(prevId);
                            SkeletalHorror.this.applyHit(SkeletalHorror.this, prevMaxHP - getHitpoints(), Hit.HitLook.HEALED_DAMAGE);
                            stop();
                        }
                    }
                });
            });
        }, CombatScript.getDelay(delay) - 1);

    }
}
