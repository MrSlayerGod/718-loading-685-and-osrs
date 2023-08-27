package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class FlowerPoker {

    public static final int RED_FLOWERS = 2462;
    public static final int BLUE_FLOWERS = 2464;
    public static final int YELLOW_FLOWERS = 2466;
    public static final int PURPLE_FLOWERS = 2468;
    public static final int ORANGE_FLOWERS = 2470;
    public static final int MIXED_FLOWERS = 2472;
    public static final int WHITE_FLOWERS = 2474;
    public static final int BLACK_FLOWERS = 2476;
    public static final int ASSORTED_FLOWERS = 2460;
    public static final int MITHRIL_SEEDS = 299;

    public enum Flowers {

        ASSORTED(2980, ASSORTED_FLOWERS),
        RED(2981, RED_FLOWERS),
        BLUE(2982, BLUE_FLOWERS),
        YELLOW(2983, YELLOW_FLOWERS),
        PURPLE(2984, PURPLE_FLOWERS),
        ORANGE(2985, ORANGE_FLOWERS),
        MIXED(2986, MIXED_FLOWERS),
        WHITE(2987, WHITE_FLOWERS),
        BLACK(2988, BLACK_FLOWERS);

        Flowers(int objectId, int itemId) {
            this.objectId = objectId;
            this.itemId = itemId;
        }

        public final int objectId;
        public final int itemId;

        public static Flowers random() {
            final int roll = Utils.random(128);
            Flowers next = MIXED;
            // Black and white are both 1/128
            if      (roll < 1)   next = BLACK;
            else if (roll < 2)   next = WHITE;
                // All the other varieties are 18/128.
            else if (roll < 20)  next = ASSORTED;
            else if (roll < 38)  next = RED;
            else if (roll < 56)  next = BLUE;
            else if (roll < 74)  next = YELLOW;
            else if (roll < 92)  next = PURPLE;
            else if (roll < 110) next = ORANGE;
            /* else if (roll < 128) next = MIXED; */
            return next;
        }
    }

    public static boolean itemClick(Player player, Item item) {
        if (item.getId() == MITHRIL_SEEDS) {
            plantMithrilSeeds(player);
            return true;
        }
        return false;
    }

    /**
     * Plants flowers using mithril seeds.
     * @param player
     */
    public static void plantMithrilSeeds(Player player) {
        if (player.isUnderCombat()) {
            player.getPackets().sendGameMessage("You cant plant a seed while under combat.");
            return;
        } else if (World.getStandartObject(player) != null) {
            player.getPackets().sendGameMessage("You can't plant a flower here.");
            return;
        }

        final Flowers flowers = Flowers.random();

        player.setNextAnimation(new Animation(827));
        final WorldObject object = new WorldObject(flowers.objectId, 10, 0, player.getX(), player.getY(),
                player.getPlane());
        player.getInventory().deleteItem(MITHRIL_SEEDS, 1);
        player.getActionManager().setActionDelay(1200);
        player.sendMessage("You plant the seed and suddenly some flowers appear..");
        player.lock(3);
        player.anim(827);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1))
                    if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1))
                        if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1))
                            if (!player.addWalkSteps(player.getX(), player.getY() - 1, 1))
                                return;
            }
        }, 1);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                World.spawnObjectTemporary(object, /*25000*/60000);
                player.getDialogueManager().startDialogue("FlowerPickD", object);
            }
        }, 2);
    }

}
