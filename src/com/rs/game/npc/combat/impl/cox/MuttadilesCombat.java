package com.rs.game.npc.combat.impl.cox;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.cox.impl.MuttadileChild;
import com.rs.game.npc.cox.impl.MuttadileMother;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.raids.cox.chamber.impl.MuttadileChamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Stopwatch;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Nov 10, 2020
 */
public class MuttadilesCombat extends CombatScript {
    private static final Projectile RANGED_PROJECTILE = new Projectile(6291, 20, 31, 0, 25, 15, 10);
    private static final Projectile MAGIC_PROJECTILE = new Projectile(5393, 20, 31, 20, 15, 15, 10);

    @Override
    public Object[] getKeys() {
        return new Object[]{MuttadileChamber.MOTHER_SWIMMING_ID, MuttadileChamber.MOTHER_ID, MuttadileChamber.CHILD_ID};
    }

    @Override
    public int attack(NPC npc, Entity target) {
        if(npc.isLocked()) {
            return 0;
        }
        if (npc.getId() == MuttadileChamber.MOTHER_SWIMMING_ID) {
            target = Utils.get(((COXBoss) npc).getTeam());
            motherAttack((MuttadileMother) npc, target);
            return npc.getCombatDefinitions().getAttackDelay();
        }
        COXBoss coxNPC = (COXBoss) npc;
        if(((MuttadileChamber) coxNPC.getChamber()).eatingTree) {
            return 0;
        }
        COXBoss muttadile = (COXBoss) npc;
        NPC tree = ((MuttadileChamber) muttadile.getChamber()).getTree();
        if (!tree.hasFinished() && Utils.random(1.0) >= .3 && npc.getHitpoints() < npc.getMaxHitpoints() / 2
            && lastEat.finished()) {
            // walk to tree and heal
            npc.getCombat().reset();
            eatTree(muttadile, tree);
            npc.forceTalk("Weeat");
            return 0;
        } else {
            //npc.forceTalk("lasteat " + lastEat.remaining() + " tree fin " + tree.hasFinished());
        }

        if (npc.getId() == MuttadileChamber.MOTHER_ID) {
            motherAttack((MuttadileMother) npc, target);
        } else {
            childAttack((MuttadileChild) npc, target);
        }

        if (npc.getId() != MuttadileChamber.MOTHER_SWIMMING_ID && !Utils.isOnRange(target, npc, 1) && Utils.random(8) == 0) {
            // walk to target to engage melee

            npc.lock();
            npc.resetWalkSteps();
            npc.calcFollow(target,false);
            //npc.addWalkSteps(target.getX(), target.getY(), -1, true);
            npc.lock();
            WorldTasksManager.schedule(() -> {
                npc.unlock();
            }, Utils.getDistance(npc, target) > 5 ? 5 : 5);
        }

        return npc.getCombatDefinitions().getAttackDelay();
    }

    public void childAttack(MuttadileChild npc, Entity target) {
        if(Utils.isOnRange(npc, target, 0) && Utils.random(3) > 0) {
            meleeAttack(npc, target);
        } else {
            rangeAttack(npc, target);
        }
    }


    public void motherAttack(MuttadileMother npc, Entity target) {
        if (npc.getId() == MuttadileChamber.MOTHER_SWIMMING_ID) {
            // in water
            if(Utils.rollDie(4, 1))
                // attack all
                for(Player player : npc.getTeam())
                    magicAttack(npc, player);
                else
                    magicAttack(npc, target);
        } else {
            // if not fire a reg attack
            if (Utils.isOnRange(npc, target, 0) && Utils.random(3) != 1) {
                if (Utils.random(2) == 1)
                    chompAttack(npc, target);
                else
                    meleeAttack(npc, target);
            } else {
                rangeAttack(npc, target);
            }
        }
    }

    private void meleeAttack(NPC npc, Entity target) {
        int max = target.isPlayer() && target.asPlayer().getPrayer().isMeleeProtecting() ? 200 : 400;
        npc.anim(27420);
        delayHit(npc, 0, target, getMeleeHit(npc, Utils.random(max)));
    }

    private void chompAttack(NPC npc, Entity target) {
        int max = 1100;
        npc.anim(27424);
        if (Utils.random(0xabc) == 1)
            npc.forceTalk("*CHOMP*");
        delayHit(npc, 0, target, getMeleeHit(npc, Utils.random(max)));
    }

    Stopwatch lastEat = new Stopwatch().delayMS(10000);

    private void eatTree(COXBoss muttadile, NPC tree) {
        lastEat.delayMS(10000);
        muttadile.resetWalkSteps();
        muttadile.getCombat().setTarget(tree);
        muttadile.setLocked(true);
        MuttadileChamber chamber = (MuttadileChamber) muttadile.getChamber();
        chamber.eatingTree = true;

        WorldTasksManager.schedule(new WorldTask() {
            final WorldTile feedTile = muttadile.getChamber().getWorldTile(new WorldTile(9, 21, 1));
            int timer = 50;
            int startHP = muttadile.getHitpoints();
            int feedDelay = 0;
            int attacks = 0;
            @Override
            public void run() {
                timer--;
                if(timer == 22) {
                    // after 20 seconds set attackable if mutta hasn't reached tree
                    // TODO
                }

                if (attacks > 3 || tree.hasFinished() || tree.isDead() || muttadile.isDead() || muttadile.hasFinished()) {
                    stop();
                    muttadile.unlock();
                    chamber.eatingTree = false;
                    muttadile.swapTarget();
                    return;
                }
                if (muttadile.matches(feedTile)) {
                    if(feedDelay-- <= 0) {
                        attacks++;
                        feedDelay = 3;
                        muttadile.anim(27420);
                        int heal = ((MuttadileChamber) muttadile.getChamber()).damageTree(100);
                        muttadile.heal(heal * 3);
                    }
                } else {
                    muttadile.addWalkSteps(feedTile.getX(), feedTile.getY(), -1, false);
                    if (timer < 0) {
                        chamber.eatingTree = false;
                        stop();
                        muttadile.unlock();
                        muttadile.swapTarget();
                    }
                }
            }
        }, 0, 0);
    }

    private void rangeAttack(NPC npc, Entity target) {
        int maxDamage = npc instanceof MuttadileMother ? 300 : 200;
       //if (target.isPlayer() && target.asPlayer().getPrayer().isRangeProtecting())
            //maxDamage *= 0.6;
        npc.anim(27421);
        int delay = RANGED_PROJECTILE.fire(npc, target);
        delayHit(npc, CombatScript.getDelay(delay), target, getRangeHit(npc, getRandomMaxHit(npc, maxDamage, Combat.RANGE_TYPE, target)));
    }

    private void magicAttack(NPC npc, Entity target) {
        int maxDamage = 300;
        int startHeight = MAGIC_PROJECTILE.getStartHeight();
        //if (target.isPlayer() && target.asPlayer().getPrayer().isMageProtecting()) {
            //maxDamage *= 0.6;
        //}
        if (npc.getId() != MuttadileChamber.MOTHER_SWIMMING_ID) {
            npc.anim(27421);
            startHeight = 0;
        }

        int delay = World.sendProjectile(npc, target, MAGIC_PROJECTILE.getGfx(), startHeight, MAGIC_PROJECTILE.getEndHeight(), 41, 25, 25, 140);
        //int delay = MAGIC_PROJECTILE.fire(npc, target);
        delayHit(npc, CombatScript.getDelay(delay), target, getMagicHit(npc, getRandomMaxHit(npc, maxDamage, Combat.MAGIC_TYPE, target)));
    }
}
