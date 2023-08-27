package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.Player;
import com.rs.game.player.Projectile;
import com.rs.game.player.content.agility.Agility;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Dec 5, 2020
 */
public class TightropeChamber extends Chamber {

    private static final int BARRIER = 129749;
    private static final int CRYSTAL_ITEM = 50884;
    private static final int CRYSTAL_OBJECT = 129751;
    private static final int TIGHTROPE = 129750;
    private static final int RANGER = 27559;
    private static final int MAGE = 27560;

    private COXBoss[] npcs = new COXBoss[8];

    public static final WorldTile[] MAGE_TILES = {
        new WorldTile(20, 9, 2), new WorldTile(19, 10, 2),
        new WorldTile(20, 10, 2), new WorldTile(19, 8, 2),
    };
    public static final WorldTile[] RANGER_TILES = {
        new WorldTile(25, 10, 2), new WorldTile(26, 11, 2),
        new WorldTile(25, 11, 2), new WorldTile(26, 10, 2),
    };

    public TightropeChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static void init() {
        ObjectHandler.register(CRYSTAL_OBJECT, 1, TightropeChamber::takeCrystal);
        ObjectHandler.register(TIGHTROPE, 1, TightropeChamber::cross);
        ObjectHandler.register(BARRIER, 1, TightropeChamber::removeBarrier);
    }

    private static void removeBarrier(Player player, WorldObject object) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid != null) {
            if (!player.getInventory().containsItem(CRYSTAL_ITEM, 1)) {
                player.sendMessage("You'll need the keystone crystal to dispel this barrier.");
                return;
            }
            World.unclipTile(raid.getTightropeChamber().getWorldTile(24, 18));
            World.unclipTile(raid.getTightropeChamber().getWorldTile(24, 17)); // clip under crystal
            raid.getTightropeChamber().completePuzzle(player, object);
        }
    }

    private void completePuzzle(Player player, WorldObject object) {
        if(barrier == null && object != null) {
            player.getInventory().deleteItem(CRYSTAL_ITEM, 1);
            object.remove();
            return;
        }
        player.anim(20832);
        object.remove();
        player.getInventory().deleteItem(CRYSTAL_ITEM, 1);
        getRaid().addPoints(player.getUsername(), 1200);
        player.sendMessage("You dispel the barrier. The path is now open.");
        for(NPC npc : npcs)
            if(npc != null && !npc.isDead() && !npc.hasFinished())
                npc.finish();
    }

    private static void takeCrystal(Player player, WorldObject object) {
        if(player.getInventory().getFreeSlots() == 0) {
            player.sendMessage("You do not have enough inventory space.");
        } else if(ChambersOfXeric.getRaid(player) != null) {
            player.addWalkSteps(object.getX(), object.getY(), -1, false);
            WorldTasksManager.schedule(() -> {
                player.getInventory().addItem(CRYSTAL_ITEM, 1);
                object.remove();
            });
        }
    }

    private static void cross(Player player, WorldObject object) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid != null) {
            Chamber c = raid.getCurrentChamber(player);
            WorldTile point1 = c.getWorldTile(23, 13);
            WorldTile point2 = c.getWorldTile(23, 5);
            boolean back = player.getY() > point1.getY();
            Agility.tightropeWalk(player, back ? point1 : point2, back ? point2 : point1, 10);
            raid.getTightropeChamber().target(player);
        }
    }


    public void target(Player player) {
        for(NPC n : npcs) {
            if(n != null && !n.isDead()) {
                n.getCombat().setTarget(player);
                n.faceEntity(player);
                if(Utils.rollDie(2, 1))
                    n.forceTalk("Protect the keystone!");
            }
        }
    }

    private WorldObject barrier;

    @Override
    public void onRaidStart() {
        boolean large = getRaid().getTeamSize() > 3;
        for (int i = 0; i < (large ? 4 : 2); i++)
            npcs[i] = new DeathlyMage(getRaid(), MAGE, getWorldTile(MAGE_TILES[i]), this);
        for (int i = 0; i < (large ? 4 : 2); i++) {
            npcs[(large ? 4 : 2) + i] = new COXBoss(getRaid(), RANGER, getWorldTile(RANGER_TILES[i]), this);
        }

        for(COXBoss npc : npcs) {
            if(npc != null) {
                npc.setRandomWalk(NPC.NORMAL_WALK);
                npc.setForceAgressive(false);
            }
        }
        barrier = new WorldObject(BARRIER, 10, 0, getWorldTile(24, 17));
        World.spawnObject(barrier);
    }
}

class DeathlyMage extends COXBoss {
    private static final Projectile PROJECTILE = new Projectile(5130, 43, 31, 51, 56, 16, 64);

    public DeathlyMage(ChambersOfXeric raid, int id, WorldTile tile, Chamber chamber) {
        super(raid, id, tile, chamber);
        setCombat();
    }

    public void setCombat() {
        setCustomCombatScript(new CombatScript() {
            @Override
            public Object[] getKeys() { return new Object[0]; }

            @Override
            public int attack(NPC npc, Entity target) {
                NPCCombatDefinitions def = npc.getCombatDefinitions();
                npc.anim(def.getAttackEmote());
                npc.setNextGraphics(new Graphics(5129, 0, 92));
                int delay = CombatScript.getDelay(PROJECTILE.fire(npc, target));
                int maxDamage = 350;
                //if (target.isPlayer() && target.asPlayer().getPrayer().isMageProtecting())
               //     maxDamage /= 2;
                int damage = getRandomMaxHit(npc, maxDamage, NPCCombatDefinitions.MAGE, target);
                WorldTasksManager.schedule(() -> {
                    target.setNextGraphics(new Graphics(5131, 0, 124));
                    target.applyHit(npc, damage, Hit.HitLook.MAGIC_DAMAGE);
                }, delay);
                return getCombatDefinitions().getAttackDelay();
            }
        });
    }
}