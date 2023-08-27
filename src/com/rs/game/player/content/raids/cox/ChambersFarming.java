package com.rs.game.player.content.raids.cox;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.DropTable;
import com.rs.utils.DropTable.ItemDrop;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * @since Dec 03, 2020
 */
public enum ChambersFarming {

    NOXIFER(55, 20903, 20901, 29997, 30009),
    GOLPAR(27, 20906, 20904, 29998, 30010),
    BUCHU(39, 20909, 20907, 29999, 30011);

    public final int levelReq, seedId, harvestId, objectIdStart, objectIdEnd;

    ChambersFarming(int levelReq, int seedId, int harvestId, int objectIdStart, int objectIdEnd) {
        this.levelReq = levelReq;
        this.seedId = seedId + Settings.OSRS_ITEM_OFFSET;
        this.harvestId = harvestId + Settings.OSRS_ITEM_OFFSET;
        this.objectIdStart = objectIdStart + Settings.OSRS_OBJECTS_OFFSET;
        this.objectIdEnd = objectIdEnd + Settings.OSRS_OBJECTS_OFFSET;
    }

    public static void plantSeed(Player player, Item item, WorldObject object, ChambersFarming patch) {
        if(player.getSkills().getLevel(Skills.FARMING) < patch.levelReq) {
            player.sendMessage("You need a Farming levle of " + patch.levelReq + " to plant this seed.");
            return;
        }
        player.faceObject(object);
        player.lock(1);
        player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 2291);
        player.getInventory().deleteItem(patch.seedId, 1);
        WorldTasksManager.schedule(() -> {
            player.getSkills().addXp(Skills.FARMING, 6);
            player.getPackets().sendGameMessage("You plant the " + item.getDefinitions().getName() + " in the herb patch.");
            growHerbPatch(object, patch.objectIdStart);
        }, 1);
    }

    public static void growHerbPatch(WorldObject object, int patchId) {
        WorldTasksManager.schedule(new WorldTask() {
            int stage = 0;
            @Override
            public void run() {
                if(stage == 4) {
                    stop();
                } else {
                    stage++;
                    object.updateId(patchId + (3 * stage));
                }
            }
        }, 0, 0);
    }

    public static void pickHerbPatch(Player player, WorldObject object, ChambersFarming patch) {
        ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
        if(raid == null)
            return;

        /*if (player.getInventory().getFreeSlots() == 0) {
            player.sendMessage("You cannot hold any more herbs.");
            return;
        }*/

        player.getPackets().sendGameMessage("You begin to harvest the herb patch.");
        player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 2282);
        WorldTask task = new WorldTask() {
            int tick = 0;
            boolean delayTick = false;
            @Override
            public void run() {
                /* OSRS allows herbs to drop on the floor
                 if (player.getInventory().getFreeSlots() == 0) {
                    player.sendMessage("You cannot hold any more herbs.");
                    stop();
                    return;
                 }*/
                if(object.getId() != patch.objectIdEnd) {
                    object.updateId(129765);
                    player.sendMessage("The herb patch is now empty.");
                    player.getActionManager().forceStop();
                    stop();
                }

                if(delayTick) {
                    delayTick = false;
                    return;
                }

                if(tick++ %2 == 0 && tick > 1) {
                    player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 2282);
                    Item herb = new Item(patch.harvestId, Math.min(Utils.random(1,4), player.getInventory().getFreeSlots()));
                    if (!player.getInventory().hasFreeSlots()) {
                        for(int i = 0; i < herb.getAmount(); i++)
                            raid.trackFloorItem(World.addCoxFloorItem(new Item(herb.getId(), 1), new WorldTile(player), player));
                    } else {
                        player.getInventory().addItem(herb);
                    }
                    player.getSkills().addXp(Skills.FARMING, 15);
                    delayTick = true;
                    if (Utils.rollPercent(20) && tick > 6) {
                        object.updateId(129765);
                        player.sendMessage("The herb patch is now empty.");
                        player.getActionManager().forceStop();
                        stop();
                        return;
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

    public static void inspectHerbPatch(Player player, WorldObject object, ChambersFarming patch) {
        if (object.getId() == patch.objectIdEnd)
            player.getPackets().sendGameMessage("This herb patch is ready to be picked.");
        else
            player.getPackets().sendGameMessage("There's a " + ItemConfig.forID(patch.seedId).name + " growing here.");
    }

    public static void clearHerbPatch(Player player, WorldObject object) {
        player.lock(1);
        player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 830);
        WorldTasksManager.schedule(() -> {
            object.updateId(Settings.OSRS_OBJECTS_OFFSET + 29765);
            player.sendMessage("You clear the farming patch..");
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null && raid.getTeamSize() > 1) {
                raid.getTeam().forEach(player1 -> {
                    if(player != player1) {
                        player1.getPackets().sendGameMessage(player.getName() + " has cleared the farming patch.");
                    }
                });
            }
        });
    }



    /**
     * Weeds
     */
    public static final DropTable SEEDS = new DropTable(
            new ItemDrop(Settings.OSRS_ITEM_OFFSET + 20903, 1, 3, 1),   //Noxifer seed
            new ItemDrop(Settings.OSRS_ITEM_OFFSET + 20906, 1, 3, 1),  //Golpar seed
            new ItemDrop(Settings.OSRS_ITEM_OFFSET + 20909, 1, 3, 1)   //Buchu seed
    );

    public static void init() {
        ObjectHandler.register(129765, 2, (player, obj) -> {
            player.getPackets().sendGameMessage("The herb patch appears to be empty.");
        });

        ObjectHandler.register(129773, 1, (player, obj) -> {
            player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 2273);
            WorldTask task = new WorldTask() {
                int tick = 0;
                @Override
                public void run() {
                    if(player.getInventory().getFreeSlots() == 0) {
                        player.sendMessage("You cannot hold any more seeds.");
                        stop();
                    } else if(++tick %4 == 0) {
                        player.anim(Settings.OSRS_ANIMATIONS_OFFSET + 2273);
                        if(Utils.random(1.0) < 0.8) {
                            Item randomSeed = SEEDS.roll().get();
                            player.getInventory().addItem(randomSeed);
                            player.getPackets().sendGameMessage("You find " + (randomSeed.getAmount() > 1 ? "several " : "a ") +
                                    randomSeed.getDefinitions().name + (randomSeed.getAmount() > 1 ? "s!" : "!"));
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
        });
    }
}
