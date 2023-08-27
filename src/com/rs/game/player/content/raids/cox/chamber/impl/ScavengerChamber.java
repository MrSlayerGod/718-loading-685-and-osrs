package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.item.Item;
import com.rs.game.npc.Drop;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitions;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.DropTable;
import com.rs.utils.DropTable.*;
import com.rs.utils.NPCDrops;
import com.rs.utils.Utils;

import java.util.*;

/**
 * @author Simplex
 * @since Nov 05, 2020
 */
public class ScavengerChamber extends Chamber {
    public ScavengerChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    public static final int[][][] SCAV_SPAWNS = {
            {},
            {
                    {6, 16},
                    {10, 18},
                    {13, 20},
                    {19, 15},
                    {16, 13},
            },
            {
                    {17, 26},
                    {18, 23},
                    {17, 20},
                    {22, 14},
                    {11, 11},
            },
            {
                    {13, 7},
                    {11, 10},
                    {9, 16},
                    {11, 19},
                    {15, 16},
                    {19, 13},
            },
    };

    private static DropTable SCAVENGER_DROP_TABLE = new DropTable(
            new ItemDrop(51036, 1, 1, 1), // Malignum root plank
            new ItemDrop(50910, 5, 25, 1), // Stinkhorn mushrooms
            new ItemDrop(50911, 5, 25, 1), // Endarkened juice
            new ItemDrop(50912, 5, 25, 1),// Cicely
            new ItemDrop(Settings.OSRS_ITEM_OFFSET + 20903, 1, 3, 1),   //Noxifer seed
            new ItemDrop(Settings.OSRS_ITEM_OFFSET + 20906, 1, 3, 1),  //Golpar seed
            new ItemDrop(Settings.OSRS_ITEM_OFFSET + 20909, 1, 3, 1)   //Buchu seed
    );

    public static void init() {
        Drops drops = new Drops(false);
        @SuppressWarnings("unchecked")
        List<Drop>[] dList = new ArrayList[Drops.VERY_RARE + 1];
        for (int i = 0; i < dList.length; i++)
            dList[i] = new ArrayList<Drop>();
        dList[Drops.ALWAYS].add(new Drop(532, 1, 1));
        dList[Drops.COMMOM].add(new Drop(51036, 2, 2));
        dList[Drops.COMMOM].add(new Drop(50910, 10, 25));
        dList[Drops.COMMOM].add(new Drop(50911, 10, 25));
        dList[Drops.COMMOM].add(new Drop(50912, 10, 25));
        dList[Drops.UNCOMMON].add(new Drop(50903, 1, 3)); //Noxifer seed
        dList[Drops.UNCOMMON].add(new Drop(50906, 1, 3)); //Golpar seed
        dList[Drops.UNCOMMON].add(new Drop(50909, 1, 3)); //Buchu seed
        drops.addDrops(dList);
        NPCDrops.addDrops(27548, drops);
        NPCDrops.addDrops(27549, drops);
    }

    public static boolean isScav(int id) {
        return id == 27548 || id == 27549;
    }
    @Override
    public void onRaidStart() {
        LinkedList<int[]> spawns = new LinkedList<>(Arrays.asList(SCAV_SPAWNS[getBaseTile().getPlane()]));
        Collections.shuffle(spawns);
        int spawnCount = getSpawnCount();
        for (int i = 0; i < spawnCount; i++) {
            int[] spawnPoint = spawns.pop();
            COXBoss scav = new COXBoss(getRaid(), Utils.random(1.0) >= 0.5 ? 27548 : 27549 , getWorldTile(spawnPoint[0], spawnPoint[1]), this) {
                @Override
                public void sendDeath(Entity source) {
                    final NPCCombatDefinitions defs = getCombatDefinitions();
                    resetWalkSteps();
                    getCombat().removeTarget();
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                    getRaid().killedScav();
                    WorldTasksManager.schedule(new WorldTask() {
                        int loop;

                        @Override
                        public void run() {
                            if (loop == 2) {
                                drop();
                                drop();
                                setRespawnTask();
                                finish();
                            } else if(loop >= 3) {
                                anim(-1);
                                stop();
                                reset();
                                setLocation(getRespawnTile());
                            }
                            loop++;
                        }
                    }, 0, 0);
                }
            };

            scav.setRandomWalk(NPC.NORMAL_WALK);
            scav.setForceMultiArea(true);
            scav.setForceMultiAttacked(true);
            scav.setSpawned(false);
            scav.setCustomCombatScript(new CombatScript() {
                @Override public Object[] getKeys() { return new Object[0]; }

                @Override
                public int attack(NPC npc, Entity target) {
                    int max = (target.isPlayer() && target.asPlayer().getPrayer().isMeleeProtecting() ? 0 : 110);
                    delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, max, NPCCombatDefinitions.MELEE, target)));
                    npc.anim(npc.getCombatDefinitions().getAttackEmote());
                    return 5;
                }
            });
        }
    }

    private int getSpawnCount() {
        if (getRaid().getTeamSize() > 15)
            return 6;
        else if (getRaid().getTeamSize() > 9)
            return 4;
        else if (getRaid().getTeamSize() > 4)
            return 3;
        return 2;
    }
}
