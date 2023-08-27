package com.rs.game.player.content.raids.cox.chamber.impl;

import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.impl.MuttadileChild;
import com.rs.game.npc.cox.impl.MuttadileMother;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingBase;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.utils.Direction;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Nov 01, 2020
 */
public class MuttadileChamber extends Chamber {

    public WorldTile TREE_SPAWN = new WorldTile(8, 25, 1),
            CHILD_SPAWN = new WorldTile(14, 19, 1),
            MOTHER_SPAWN = new WorldTile(19, 9, 1),
            BLOCK_SPAWN = new WorldTile(23, 18, 1);

    public static final int CHILD_ID = 27562;
    public static final int MOTHER_SWIMMING_ID = 27561, MOTHER_ID = 27563;

    private static final int LIVE_TREE_OBJ = 130012;
    private static final int DEAD_TREE_OBJ = 130013;
    private static final int BLOCK_CRYSTAL = 130018;
    public static final int TREE_NPC = 27564;
    public boolean eatingTree;

    public static void init() {
        NPCHandler.register(TREE_NPC, 1, MuttadileChamber::chopTree);
    }

    private NPC tree;

    public NPC getTree() {
        return tree;
    }

    public int damageTree(int damage) {
        damage = Math.min(damage, tree.getHitpoints());
        tree.applyHit(tree, damage);

        //System.out.println("Damage tree " + damage + " / " + tree.getHitpoints());

        if (tree.isDead() || damage == tree.getHitpoints()) {
            WorldObject treeObject = getObject(LIVE_TREE_OBJ, TREE_SPAWN);
            if(treeObject != null) {
                treeObject.updateId(DEAD_TREE_OBJ);
            } else {
                System.out.println("Error: Could not find mutta tree, can't update ID");
            }

            WorldTasksManager.schedule(() -> tree.finish());
        }
        return damage;
    }

    public MuttadileChamber(int x, int y, int z, ChambersOfXeric raid) {
        super(x, y, z, raid);
    }

    private MuttadileMother muttadileMother;
    private MuttadileChild muttadileChild;

    public MuttadileMother getMuttadileMother() {
        return muttadileMother;
    }

    private WorldObject crystal;

    private WorldObject[] entranceTendrils = new WorldObject[2];

    public WorldObject[] getEntranceTendrils() {
        return entranceTendrils;
    }

    @Override
    public void onRaidStart() {
        setDefaultActivationoTask();

        entranceTendrils[0] = new WorldObject(129768, 10, 0, getWorldTile(0, 15));
        entranceTendrils[1] = new WorldObject(129768, 10, 0, getWorldTile(0, 16));
        for(WorldObject o : entranceTendrils)
            World.spawnObject(o);

        spawnObject(LIVE_TREE_OBJ, TREE_SPAWN, 10, 1);
        muttadileMother = new MuttadileMother(getRaid(), MOTHER_SWIMMING_ID, getWorldTile(MOTHER_SPAWN), this);
        muttadileChild = new MuttadileChild(getRaid(), CHILD_ID, getWorldTile(CHILD_SPAWN), this);
        crystal = spawnObject(BLOCK_CRYSTAL, BLOCK_SPAWN, 10, 0);
        ;
        tree = new NPC(TREE_NPC, getWorldTile(TREE_SPAWN), -1, false) {
            @Override
            public int getMaxHitpoints() {
                int hp = getRaid().getTotalWCLevel() * 5;
                return hp < 1000 ? 1000 : hp;
            }
        };
        World.addFloor(getWorldTile(23, 19));
        World.addFloor(getWorldTile(23, 18)); // clip under crystal
        //tree.setFreezeDelay(Integer.MAX_VALUE);
        tree.setLureDelay(Integer.MAX_VALUE);
        tree.setCantFollowUnderCombat(true);
        tree.setSpawned(true);
    }

    @Override
    public void bossDeath() {
        World.sendObjectAnimation(crystal, new Animation(27506));
        WorldTasksManager.schedule(() -> {
            World.unclipTile(getWorldTile(23, 19));
            World.unclipTile(getWorldTile(23, 18)); // clip under crystal
            crystal.remove();
        }, 3);
    }

    public void activateMother() {
        Entity target = getMuttadileMother().getCombat().getTarget();
        getMuttadileMother().setNextNPCTransformation(27563);
        getMuttadileMother().setup();
        getMuttadileMother().anim(27423);
        getMuttadileMother().setCantFollowUnderCombat(false);
        getMuttadileMother().addWalkSteps(getMuttadileMother().getX(), getMuttadileMother().getY() + 6, -1, false);
        getMuttadileMother().resetCombat();
        getMuttadileMother().setNextFaceEntity(null);
        getMuttadileMother().setDirection(Direction.NORTH, true);
        getMuttadileMother().setLocked(true);
        WorldTasksManager.schedule(() -> {
            getMuttadileMother().setLocked(false);
            getMuttadileMother().faceEntity(target);
            getMuttadileMother().setTarget(target);
        }, 5);
    }

    public static void chopTree(Player player, NPC npc) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid != null) {
            raid.getMuttadileChamber().startCuttingTree(player, npc);
        }
    }

    private void startCuttingTree(Player player, NPC npc) {
        if(!tree.isDead()) {
            WoodcuttingBase.HatchetDefinitions hatchet = Woodcutting.getHatchet(player, false);
            if(hatchet == null) {
                player.sendMessage("You must have an axe to chop this tree.");
                return;
            } else {
                WorldTask task = new WorldTask() {
                    int tick = 0;
                    @Override
                    public void run() {
                        if(tree.isDead() ||  getMuttadileMother().isDead()) {
                            stop();
                            player.anim(-1);
                            player.setNextFaceEntity(null);
                            player.getActionManager().forceStop();
                            return;
                        }

                        player.faceEntity(npc);
                        player.anim(hatchet.getEmoteId());

                        if(tick%2 == 0) {
                            if(Utils.random(33) <= hatchet.getAxeTime()) {
                                if(damageTree(75) != 75) {
                                    player.anim(-1);
                                    player.setNextFaceEntity(null);
                                    player.getActionManager().forceStop();
                                }
                                player.getSkills().addXp(Skills.WOODCUTTING, 5);
                            }
                        }
                    }
                };

                WorldTasksManager.schedule(task, 0, 0);

                player.getActionManager().createSkillingLock(()->{
                    // on interrupt
                    player.anim(-1);
                    player.setNextFaceEntity(null);
                    task.stop();
                });
            }
        }
    }
}
