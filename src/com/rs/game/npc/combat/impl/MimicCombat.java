package com.rs.game.npc.combat.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.others.Mimic;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MimicCombat extends CombatScript {

    @Override
    public Object[] getKeys() {
        return new Object[]
                {28633, /*28635, 28636,*/ 28637};
    }

    @Override
    public int attack(NPC npc, Entity target) {
        switch (npc.getId()) {
            case Mimic.MIMIC_ID:
                Mimic mimic = (Mimic) npc;
                if(Utils.isOnRange(npc, target, 1)) {
                    // is in range
                    // attack with melee - candy attack 1/10

                    if(Utils.random(10) == 0) {
                        // mimic can walk over (collide) the player to cause damage
                        return chaseTarget(mimic);
                    }
                    return Utils.random(10) != 1 ? candyAttack((Mimic) npc, target) : mimicMelee(npc, target);
                } else {
                    // out of range
                    // move closer 1/4 chance & no attack
                    if(Utils.random(3) == 1) {
                        return chaseTarget(mimic);
                    }
                    return candyAttack((Mimic) npc, target);
                }
            case Mimic.MAGE_ID:
                return thirdAgeMagicianAttack(npc, target);
        }

        return 5;
    }

    public int chaseTarget(Mimic mimic) {
        mimic.chaseTargetTicks = 5;
        return mimic.chaseTargetTicks;
    }

    public static int thirdAgeMagicianAttack(NPC npc, Entity target) {
        final NPCCombatDefinitions defs = npc.getCombatDefinitions();
        npc.setNextAnimation(new Animation(defs.getAttackEmote()));
        int damage = getRandomMaxHit(npc, 190, NPCCombatDefinitions.MAGE, target);
        npc.setNextGraphics(new Graphics(5164, 0, 92));
        final int ms = World.sendProjectile(npc, target, 5165, 30, 30, 50, 42, 0, 0);

        // land projectiles
        WorldTasksManager.schedule(() -> {
            target.setNextGraphics(new Graphics(damage > 0 ? 5166 : 5085, 0, 92));
            target.applyHit(getMagicHit(npc, damage));
        }, CombatScript.getDelay(ms));
        return defs.getAttackDelay();
    }

    public static int mimicMelee(NPC npc, Entity target) {
        final NPCCombatDefinitions defs = npc.getCombatDefinitions();
        npc.setNextAnimation(new Animation(defs.getAttackEmote()));

        npc.getPossibleTargets().forEach(entity -> {
            if (entity.withinDistance(npc, 1)) {
                int damage = getRandomMaxHit(npc, 230, NPCCombatDefinitions.MELEE, entity);
                delayHit(npc, 0, entity, getMeleeHit(npc, damage));
            }
        });
        return defs.getAttackDelay();
    }

    public int candyAttack(Mimic mimic, Entity target) {
        final NPCCombatDefinitions defs = mimic.getCombatDefinitions();
        mimic.setNextAnimation(Mimic.CANDY_ATTACK_ANIM);

        // fire candy at end of anim
        WorldTasksManager.schedule(() -> {
            List<WorldTile> candyTiles = new ArrayList<WorldTile>();
            for (int i = 0; i < 6; i++) {
                WorldTile tile;
                int threadLockFailsafe = 0;
                do {
                    // select open tile
                    tile = target.transform(Utils.random(3), Utils.random(3), 0);

                    // if this tile is obstructed re-roll
                    for (WorldTile t2 : candyTiles) {
                        if (tile !=null && t2.matches(tile)) {
                            tile = null;
                        }
                    }
                } while (threadLockFailsafe++<15 && (tile == null || tile.hasGameObject()));

                if(tile != null)
                    candyTiles.add(tile);
            }

            // first 3 candies spawn mage/melee/ranger
            // list is already randomized so can use first 3
            int candy = 0;

            // fire projectiles
            for (WorldTile candyTile : candyTiles) {
                final boolean[] CANDY_ATK = {candy == 0, candy == 1, candy == 2};

                final Graphics proj =
                        CANDY_ATK[0] ? Mimic.ORANGE_CANDY_PROJ :
                        CANDY_ATK[1] ? Mimic.GREEN_CANDY_PROJ :
                        CANDY_ATK[2] ? Mimic.CYAN_CANDY_PROJ : Mimic.PURP_CANDY_PROJ;

                final Graphics impact =
                        CANDY_ATK[0] ? Mimic.RED_CANDY_OPEN :
                        CANDY_ATK[1] ? Mimic.GREEN_CANDY_OPEN :
                        CANDY_ATK[2] ? Mimic.CYAN_CANDY_OPEN : Mimic.PURP_CANDY_OPEN;

                final int ms = World.sendProjectile(mimic, candyTile, proj.getId(), 120, 26, 25, 36, 35, 0);
                final int index = candy;

                // land projectiles
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        World.sendGraphics(mimic, impact, candyTile);

                        mimic.getPossibleTargets().forEach(entity -> {
                            if (entity.getHitpoints()>0 && entity.withinDistance(candyTile, 1)) {
                                entity.applyHit(new Hit(mimic, 100, Hit.HitLook.MAGIC_DAMAGE));
                            }
                        });

                        if (index < 3)
                            mimic.spawnMinion(candyTile, index);
                    }
                }, CombatScript.getDelay(ms));

                candy++;
            }
        });

        return 9;
    }
}