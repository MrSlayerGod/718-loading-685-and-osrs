package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.Skills;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Dec 05, 2020
 */
public class MysticsChamber extends Chamber {
    public MysticsChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static WorldTile SPAWNS[] = {
            new WorldTile(15, 13, 1),
            new WorldTile(5, 7, 1),
            new WorldTile(16, 7, 1),
    };

    private COXBoss[] mystics = new COXBoss[3];
    private int mysticsAlive = 3;
    private WorldObject block;

    public static boolean isMysticNPC(int id) {
        return id == 27604 || id == 27605 || id == 27606;
    }

    @Override
    public void bossDeath() {
        mysticsAlive--;
        if(mysticsAlive == 0) {
            block.anim(27506);
            WorldTasksManager.schedule(() -> {
                World.unclipTile(getWorldTile(24, 14));
                World.unclipTile(getWorldTile(24, 15)); // clip under crystal
                block.remove();
            }, 3);
        }
    }

    @Override
    public void onRaidStart() {
        block = spawnObject(129796, new WorldTile(24, 14, 1), 10, 0);
        for (int i = 0; i < mystics.length; i++) {
            mystics[i] = new SkeletalMystic(getRaid(), 27604 + (i % 3), getWorldTile(SPAWNS[i]), this);
        }
    }
}

class SkeletalMystic extends COXBoss {
    private static final int VULN_GFX = 6321;
    private static final int FIRE_GFX = 6322;
    private static final int VULN_HIT_GFX = 5169;
    private static final int FIRE_HIT_GFX = 5131;

    private static final Projectile VULN_PROJECTILE = new Projectile(5168, 70, 31, 34, 56, 16, 127);
    private static final Projectile FIRE_PROJECTILE = new Projectile(5130, 70, 31, 34, 56, 16, 127);

    public SkeletalMystic(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        setCombat();
        setUndeadNPC(true);
    }

    private static final Drop[] drops = {
            new Drop(50909, 5, 10, 1), 	// Buchu Seed
            new Drop(50906, 5, 10, 1), 	// Golpar Seed
            new Drop(50903, 5, 10, 1)	// Noxifer Seed
    };

    @Override
    public void drop() {
        for(Drop drop : drops)
            sendDrop(getMostDamageReceivedSourcePlayer(), drop);
    }
    
    @Override
    public void sendDeath(Entity killer) {
        getChamber().bossDeath();
        super.sendDeath(killer);
    }

    private void setCombat() {
        setCustomCombatScript(new CombatScript() {
            @Override
            public Object[] getKeys() { return new Object[0]; }

            @Override
            public int attack(NPC npc, final Entity target) {
                if(!target.isPlayer())
                    return 0;

                Player player = target.asPlayer();
                if (Utils.isOnRange(npc, target, 0) && Utils.rollDie(2, 1)) {
                    meleeAttack(npc, player);
                } else {
                    if (Utils.rollDie(3, 1))
                        vulnAttack(npc, player);
                    else
                        fireAttack(npc, player);;
                }
                return npc.getCombatDefinitions().getAttackDelay();
            }

            public void fireAttack(NPC npc, Player target) {
                npc.anim(npc.getCombatDefinitions().getAttackEmote());
                npc.gfx(FIRE_GFX);
                int delay = CombatScript.getDelay(FIRE_PROJECTILE.fire(npc, target));
                int maxDamage = 350;
                //if (target != null && target.getPrayer().isMageProtecting())
                    //maxDamage /= 2;
                int damage = getRandomMaxHit(npc, maxDamage, NPCCombatDefinitions.MAGE, target);
                WorldTasksManager.schedule(() -> {
                    target.setNextGraphics(new Graphics(FIRE_HIT_GFX, 0, 124));
                    target.applyHit(npc, damage, Hit.HitLook.MAGIC_DAMAGE);
                }, delay-1);
            }

            private void vulnAttack(NPC npc, Player target) {
                npc.anim(npc.getCombatDefinitions().getAttackEmote());
                npc.gfx(VULN_GFX);
                int delay = CombatScript.getDelay(VULN_PROJECTILE.fire(npc, target));
                int maxDamage = 250;
                //if (target != null && target.getPrayer().isMageProtecting())
                    //maxDamage /= 2;
                int damage = getRandomMaxHit(npc, maxDamage, NPCCombatDefinitions.MAGE, target);
                if(damage > 10) {
                    int defDrain = (int) ((double) damage * 0.01);
                    target.getSkills().drainLevel(Skills.DEFENCE, defDrain);
                }
                WorldTasksManager.schedule(() -> {
                    target.setNextGraphics(new Graphics(VULN_HIT_GFX, 0, 124));
                    target.applyHit(npc, damage, Hit.HitLook.MAGIC_DAMAGE);
                }, delay-1);
            }

            private void meleeAttack(NPC npc, Player target) {
                npc.anim(25487);
                delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
            }
        });
    }

}
