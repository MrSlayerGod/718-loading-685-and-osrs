package com.rs.game.npc.cox.impl;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.*;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.GuardianChamber;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Direction;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ChambersGuardian extends COXBoss {

    private static final Projectile PROJECTILE = new Projectile(5856, 150, 0, 0, 50,  0, 0);

    private int originalId;

    public ChambersGuardian(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        setCantFollowUnderCombat(true);
        originalId = id;
        setCustomCombatScript(customScript);
        setNoDistanceCheck(true);
        setDrops();
    }

    public void setDrops() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        for(Drop drop : ALWAYS_DROPS) {
            dList[Drops.ALWAYS].add(drop);
        }
        drops.addDrops(dList);
        NPCDrops.addDrops(27569, drops);
        NPCDrops.addDrops(27570, drops);
    }

    private static Drop[] ALWAYS_DROPS =
            {
                    new Drop(50909, 3, 5, 1), 	// Buchu Seed
                    new Drop(50906, 3, 5, 1), 	// Golpar Seed
                    new Drop(50903, 3, 5, 1)	// Noxifer Seed
            };
    private CombatScript customScript = new CombatScript() {
        @Override
        public Object[] getKeys() { return new Object[0]; }

        public void launchAttack(NPC npc, Entity target, WorldTile targetPos) {
            if (Utils.rollDie(4,1))
                dropBoulder(npc, target.asPlayer(), targetPos);
            else
                meleeAttack(npc, target.asPlayer(), targetPos);
        }
        @Override
        public int attack(NPC npc, Entity target) {
            Player t = getClosestPlayer();
            if(t != target)
                target = t;

            WorldTile targetPos = target.clone();
            if(Utils.rollDie(5, 1)) {
                launchAttack(npc, target, targetPos);
                //npc.forceTalk("instant attack");
            } else {
                WorldTasksManager.schedule(() -> {
                    if(!npc.isDead()) {
                        launchAttack(npc, t, targetPos);
                        //npc.forceTalk("delayed attack");
                    }
                }, 3);
                return getCombatDefinitions().getAttackDelay() + 3;
            }
            return getCombatDefinitions().getAttackDelay();
        }

        private void meleeAttack(NPC npc, Player target, WorldTile targetPos) {
            npc.anim(npc.getCombatDefinitions().getAttackEmote());
            getTeam().forEach(player -> {
                if(Utils.isOnRange(player, npc, 0)) {
                    int dmg = npc.getCombatDefinitions().getMaxHit();
                    if (player.getPrayer().isMeleeProtecting())
                        dmg /= 2;
                    if(!npc.isDead())
                        player.applyHit(npc, Utils.random(dmg), Hit.HitLook.MELEE_DAMAGE);
                }
            });
        }

        private void dropBoulder(NPC npc, Player target, WorldTile targetPos) {
            npc.anim(24278);
            int delay = CombatScript.getDelay(PROJECTILE.fire(npc, targetPos));
            WorldTasksManager.schedule(() -> {
                World.sendGraphics(npc, new Graphics(5305, 0, 35), targetPos);
                if(!npc.isDead())
                    getTeam().stream().filter(player -> player.matches(targetPos)).forEach(player
                        -> player.applyHit(npc, Utils.random(400), Hit.HitLook.RANGE_DAMAGE));
            }, delay);
        }
    };

    @Override
    public void setNextFaceEntity(Entity entity) {
        // don't face anywhere
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        Player player = hit.getSource().isPlayer() ? hit.getSource().asPlayer() : null;
        if(player != null) {
            int wep = player.getEquipment().getWeaponId();
            if(wep == -1 || !ItemConfig.forID(wep).getName().toLowerCase().contains("pickaxe")) {
                hit.setDamage(0);
                player.sendMessage("The Guardian resists your attack.");
            } else {
                MiningBase.PickAxeDefinitions def = Mining.getPickAxeDefinitions(player, false);
                int p = def.getLevelRequried(),
                    m = player.getSkills().getLevel(Skills.MINING);
                double mult = (50.0 + p + m) / 150;
                hit.setDamage((int) (hit.getDamage() * mult));
            }
        }
        super.handleIngoingHit(hit);
    }


    static final Drop[] drops = {
            new Drop(50909, 5, 10),    // Buchu Seed
            new Drop(50906, 5, 10),    // Golpar Seed
            new Drop(50903, 5, 10)    // Noxifer Seed
    };

    @Override
    public void sendDeath(Entity killer) {
        ChambersGuardian n = this;
        anim(getCombatDefinitions().getDeathEmote());
        WorldTasksManager.schedule(() -> {
            drop();
            if(n.getId() == originalId)
                n.setNextNPCTransformation(getId() + 2);
        }, 3);
    }

    @Override
    public boolean preAttackCheck(Player attacker) {
        if(getId() > originalId) {
            attacker.sendMessage("It's already destroyed.");
            return false;
        }
        return super.preAttackCheck(attacker);
    }

    @Override
    public void processNPC() {
        setDirection(Direction.EAST, true);
        super.processNPC();
    }
}
