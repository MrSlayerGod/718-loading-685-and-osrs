package com.rs.game.player.content.agility;

import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.others.zalcano.Zalcano;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Utils;

import java.util.function.BiConsumer;

/**
 * @author Simplex
 * @since Sep 03, 2020
 */
public class PriffdinasAgility {

    public static final String OB_KEY = "PRIFF_OB_COMPL_";

    public static Portals activePortal = Utils.randomEnum(Portals.class);

    public enum Portals {
        P1(136241, new WorldTile(2233, 3359, 2), new WorldTile(2248, 3353, 2)),
        P2(136242, new WorldTile(2246, 3364, 0), new WorldTile(2269, 3389, 0)),
        P3(136243, new WorldTile(2258, 3386, 2), new WorldTile(2247, 3395, 2)),
        P4(136244, new WorldTile(2243, 3395, 2), new WorldTile(2246, 3406, 2)),
        P5(136245, new WorldTile(2248, 3405, 2), new WorldTile(2250, 3416, 2)),
        P6(136246, new WorldTile(2249, 3419, 2), new WorldTile(2260, 3425, 0));

        Portals(int objectId, WorldTile portalPos, WorldTile portalTelePos) {
            this.objectId = objectId;
            this.portalPos = portalPos;
            this.portalTelePos = portalTelePos;
        }

        int objectId;

        WorldTile portalPos, portalTelePos;

        public void enterPortal(Player player, WorldObject obj) {
            if(!Agility.hasLevel(player, 70))
                return;

            Agility.startObstacle(player, portalPos, () -> {
                if(player.isLocked())
                    return;
                if(activePortal == this) {
                    player.lock(2);
                    player.setNextWorldTile(portalTelePos.clone());
                    player.getSkills().addXp(Skills.AGILITY, 20 * Agility.getAgilityMultiplier(player));
                    player.getPackets().sendGameMessage("The portal transports you further in the course!");
                    player.resetWalkSteps();
                    movePortal();
                }
            });
        }

        public int getId() {
            return objectId;
        }

        public WorldTile getTile() {
            return portalPos;
        }
    }

    public enum Obstacles {
        START_LADDER(136221, 1,
                ((player, object) -> {
                    player.lock(1);
                    player.getPackets().sendGameMessage("You climb the ladder.", true);
                    player.useStairs(828, new WorldTile(2231, 3357, 2), 1, 2);
                    player.startPriffCourse();
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            setKey(player, object.getId());
                            player.getSkills().addXp(Skills.AGILITY, 10 * Agility.getAgilityMultiplier(player));
                        }
                    }, 1);
                })
        ),
        TIGHT_ROPE(136225, 1,
                ((player, object) -> {
                    Agility.tightropeWalk(player, new WorldTile(2233, 3353, 2), new WorldTile(2248, 3353, 2), 20);
                })
        ),

        CHIMNEY(136227, 1,
                ((player, object) -> {

                    if(player.isLocked())
                        return;
                    player.lock(3);
                    Agility.leapAcross(player, new WorldTile(2249, 3353, 2), new WorldTile(2249, 3355, 2), 12);
                    WorldTasksManager.schedule(() -> Agility.leapDown(player, new WorldTile(2249, 3355, 2), new WorldTile(2245, 3360, 2), 15), 1);
                })
        ),

        ROOF_EDGE(136228, 1,
                ((player, object) -> {
                    if(player.isLocked())
                        return;
                    player.lock(3);
                    Agility.leapDown(player, new WorldTile(2245, 3363, 2), new WorldTile(2245, 3365, 0), 15);
                })
        ),

        DARK_HOLE_1(136229, 1,
                ((player, object) -> {
                    player.getStopwatch().delay(10);
                    player.getPackets().sendGameMessage("You climb into the hole..", true);
                    player.anim(20827);
                    FadingScreen.fade(player, 600, () -> {
                        player.lock(3);
                        player.useStairs(-1, new WorldTile(2269, 3389, 0), 0, 0);
                        FadingScreen.unfade(player, 600, () -> {
                            player.lock(5);
                            setKey(player, object.getId());
                            player.getPackets().sendGameMessage("..and emerge on the other side.", true);
                            player.getSkills().addXp(Skills.AGILITY, 50 * Agility.getAgilityMultiplier(player));
                        });
                    });
                })
        ),

        LADDER_2(136231, 1,
                ((player, object) -> {
                    if(player.isLocked())
                        return;
                    player.lock(1);
                    player.getPackets().sendGameMessage("You climb the ladder.", true);
                    player.useStairs(828, new WorldTile(2269, 3393, 2), 1, 2);

                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            setKey(player, object.getId());
                            player.getSkills().addXp(Skills.AGILITY, 10 * Agility.getAgilityMultiplier(player));
                            //player.addWalkSteps(2233, 3357);
                        }
                    }, 1);
                })
        ),
        ROPE_BRIDGE(136233, 1,
                ((player, object) -> {
                    Agility.tightropeWalk(player, new WorldTile(2264, 3390, 2), new WorldTile(2257, 3390, 2), 12);
                })
        ),
        TIGHT_ROPE_2(136234, 1,
                ((player, object) -> {
                    // tightrope is missing, broken map
                    // Agility.tightropeWalk(player, new WorldTile(2253, 3390, 2), new WorldTile(2247, 3395, 2));
                    Agility.startObstacle(player, new WorldTile(2253, 3390, 2), () -> {
                        player.getStopwatch().delay(20);
                        //player.useStairs(24380, new WorldTile(2251, 3392, 2), 1, 2);
                        WorldTile endTile = new WorldTile(2247, 3395, 2);
                        player.setNextFaceWorldTile(endTile);
                        player.anim(24380);
                        player.setNextForceMovement(new ForceMovement(player, 1, endTile, 5, ForceMovement.NORTH_WEST));
                        player.getPackets().sendGameMessage("You attempt a massive leap..", true);

                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                player.lock(2);
                                player.getStopwatch().reset();
                                setKey(player, object.getId());
                                player.setNextWorldTile(endTile);
                                player.getSkills().addXp(Skills.AGILITY, 25 * Agility.getAgilityMultiplier(player));
                                player.getPackets().sendGameMessage(".. and land it!", true);
                            }
                        }, 5);
                    });
                })
        ),
        ROPE_BRIDGE_2(136235, 1,
                ((player, object) -> {
                    Agility.tightropeWalk(player, new WorldTile(2246, 3399, 2), new WorldTile(2246, 3406, 2), 20);
                })
        ),
        TIGHT_ROPE_3(136236, 1,
                ((player, object) -> {
                    Agility.tightropeWalk(player, new WorldTile(2243, 3409, 2), new WorldTile(2250, 3416, 2), 20);
                })
        ),
        TIGHT_ROPE_4(136237, 1,
                ((player, object) -> {
                    Agility.tightropeWalk(player, new WorldTile(2253, 3418, 2), new WorldTile(2260, 3425, 0), 20);
                })
        ),
        DARK_HOLE_2(136238, 1,
                ((player, object) -> {
                    player.getStopwatch().delay(10);
                    player.lock(18);
                    player.getPackets().sendGameMessage("You climb into the hole..", true);
                    player.anim(20827);
                    FadingScreen.fade(player, 600, () -> {
                        player.useStairs(-1, new WorldTile(2216, 3357, 0), 0, 0);
                        FadingScreen.unfade(player, 600, () -> {
                            setKey(player, object.getId());
                            player.getPackets().sendGameMessage("..and emerge on the other side.", true);
                            player.getSkills().addXp(Skills.AGILITY, 1000 * Agility.getAgilityMultiplier(player));
                            player.increasePriffCourseCompletions();
                        });
                    });
                })
        );

        Obstacles(int id, int actionIndex, BiConsumer<Player, WorldObject> traversalEvent) {
            this.id = id;
            this.actionIndex = actionIndex;
            this.traversalEvent = traversalEvent;
        }

        int id;
        int actionIndex;
        BiConsumer<Player, WorldObject> traversalEvent;

        public int getId() {
            return id;
        }

        public int getActionIndex() {
            return actionIndex;
        }

        public void traverse(Player player, WorldObject object) {
            if(!Agility.hasLevel(player, 70))
                return;
            if(Utils.random(14) == 0) {
                Item shard = new Item(Zalcano.CRYSTAL_SHARD, 1);
                if(player.getInventory().hasFreeSlots(shard)) {
                    player.getInventory().addItem(shard);
                    player.sendMessage("<col=00ffff>A crystal shard appears in your inventory.");
                } else {
                    player.getInventory().addItemDrop(shard.getId(), shard.getAmount());
                    player.sendMessage("<col=00ffff>A crystal shard falls to the ground.");
                }
            }
            traversalEvent.accept(player, object);
        }
    }

    /**
     * Marks Obstacle corresponding to obstacleID completed
     */
    public static void setKey(Player player, int obstacleID) {
        int idx = -1;
        for (int i = 0; i < Obstacles.values().length; i++)
            if (obstacleID == Obstacles.values()[i].getId())
                idx = i;
        if (idx != -1)
            player.getTemporaryAttributtes().put(OB_KEY + idx, Boolean.TRUE);
    }

    public static void movePortal() {
        World.spawnObject(new WorldObject(-1, 10, 0, activePortal.getTile()));
        Portals lastPortal = activePortal;
        do {
            activePortal = Utils.randomEnum(Portals.class);
        } while(lastPortal == activePortal);
        World.spawnObject(new WorldObject(activePortal.getId(), 10, 0, activePortal.getTile()));
    }

    public static void init() {
        // register obstacle actions
        for (Obstacles ob : Obstacles.values()) {
            ObjectHandler.register(
                    ob.getId(), ob.getActionIndex(), (ob::traverse));
        }

        // register portal actions
        for (Portals p : Portals.values()) {
            if(p != activePortal)
                World.spawnObject(new WorldObject(-1, 10, 0, p.getTile()));
            ObjectHandler.register(
                    p.getId(), 1, (p::enterPortal));
        }
    }
}
