/**
 *
 */
package com.rs.game.player.content.box;

import com.rs.discord.Bot;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author dragonkk(Alex)
 * Oct 23, 2017
 */
public class MysteryBox {

    public static void init() {
        MysteryCrate.init();
    }

    public static final int MYSTERY_CRATE_ID = 27004;
    public static final int ID = 6199, PREMIUM_ID = 25436; //429m. before 729
    public static final Item[] EXTREMELY_RARE = {
          //  new Item(25504), llr
         //   new Item(25495), almighty shield
           // new Item(52325), scythe
            new Item(24455),
            new Item(24456),
            new Item(24457),
            new Item(51003),
            //new Item(50997, 1), tbow
            new Item(995, 250000000),
            new Item(25428)
    }; //1 in 1k
    public static final Item[] VERY_RARE = {
            new Item(51006),
        //    new Item(25486), infinityneck
          //  new Item(25488), infinityring
            new Item(25526),
            new Item(25477),
            new Item(52322),
            new Item(43343),
            new Item(52324),
            new Item(54422),
            new Item(54417),
            new Item(54419),
            new Item(54420),
            new Item(54421),
            new Item(52323),
            new Item(52326),
            new Item(52327),
            new Item(52328),
            new Item(1048),
            new Item(41862, 1),
            new Item(995, 80000000),
            new Item(25587, 2500000),
            new Item(18349),
            new Item(18351),
            new Item(18353),
            new Item(18355),
            new Item(18357),
            new Item(18359),
            new Item(18361),
            new Item(18363),
            new Item(42926),
            new Item(25427)
    }; //1 in 100
    public static final Item[] RARE = {
            new Item(49544),
            new Item(49547),
            new Item(49550),
            new Item(49553), //zenyte
            new Item(995, 35000000),
            new Item(25587, 1500000),
            new Item(23697),
            new Item(23698),
            new Item(23699),
            new Item(23700),
            new Item(1046),
            new Item(1044),
            new Item(1038),
            new Item(1042),
            new Item(1040),
            new Item(25037),
            new Item(25426),
            new Item(21777),

            new Item(1053),
            new Item(1055),
            new Item(1057),
            new Item(1050),

            new Item(51012),
            new Item(52978),
            new Item(51295)
    }; //infernal}; //1 in 50
    public static final Item[] UNCOMMON = {
            new Item(995, 15000000),
            new Item(25587, 500000),
            new Item(23695),
            new Item(11716),
            new Item(23681),
            new Item(23679),
            new Item(23687),
            new Item(23688),
            /* new Item(23689),*/
            new Item(23684),
            new Item(23685),
            new Item(23686),
            new Item(25419),
            new Item(25420),
            new Item(25421),
            new Item(42931),
            new Item(43263),
            new Item(25425),
            new Item(25425),
            new Item(43235),
            new Item(43237),
            new Item(43239),
            new Item(21371),
            new Item(51733),
            new Item(25484),
            new Item(25470),
            new Item(51633),
            new Item(52002)
    }; //1 in 10


    public static final Item[] COMMON = {
            new Item(25587, 250000),
            new Item(25424),
            new Item(25423),
            new Item(23674),
            new Item(23680),
            new Item(23682),
            new Item(25436),
            new Item(25413),
            new Item(25414),
            new Item(25415),
            new Item(21787),
            new Item(21790),
            new Item(21793),
            new Item(25416),
            new Item(25417),
            new Item(25418),
            new Item(25422),
            new Item(11283),
            new Item(6914), //master wand
            new Item(49481), //heavy balista
            new Item(51902), //dragon crossbow
            new Item(20667), //vecna skull
            new Item(22358, 3),
            new Item(22362, 3),
            new Item(22366, 3), //dominion gloves
            new Item(19335), //fury or
            new Item(15126), //amulet of ranging
            new Item(42002), //occult neck
            new Item(6585), //fury
            new Item(2581), //robin hat
            new Item(15403), //balmung
            new Item(52981),
            new Item(15606),
            new Item(15608),
            new Item(15610),

            new Item(15220),
            new Item(15020),
            new Item(15018),
            new Item(15019),
            new Item(52975), //
            new Item(6739),
            new Item(15259),
            new Item(995, 10000000),
            new Item(43307, 100000),
            new Item(6199),
            new Item(4151),
            new Item(42006),
            new Item(11730),
            new Item(15486),
            new Item(11235),
            new Item(10551),


            new Item(11846),
            new Item(11848),
            new Item(11850),
            new Item(11852),
            new Item(11854),
            new Item(11856),
            new Item(21768),
            new Item(20072),
            new Item(6570),
            new Item(23659),
            new Item(22298),
            new Item(23876),
            new Item(23854),
            new Item(23874),
            new Item(6920),
            new Item(2577),
            new Item(22494),
            new Item(11728),
            new Item(50714),
            new Item(42785),
            new Item(52545),
            new Item(52550),
            new Item(52555),
            new Item(25503),
            new Item(20072),
            new Item(14529)
    };
    public static final int[] SLOTS = {
            6,
            8,
            10,
            12,
            14
    };
    public static final int BEG_ID = 25492;
    public static double MULTIPlIER = 1;
    private static final int[] BEGINNER_IDS = {
            15441,
            15442,
            15443,
            15444,
            15441,
            15442,
            15443,
            15444 /*1187, 3140, 3204, 4087, 11732, 1305, 8850*/ /*1187, 3140, 3204, 4087, 6739, 11732, 41335, 14480, 4708, 4710, 4712, 4714, 4716, 4718, 4720, 4722, 4724, 4726, 4728, 4730, 4732, 4734, 4736, 4738, 4745, 4747, 4749, 4751, 4753, 4755, 4757, 4759, 15441, 15442, 15443, 15444, 42002, 15126*/
    };

    private static Item selectReward(Player player, Item[] rewards, boolean premium) {
        /*if (Utils.random(5/*3*/ //) == 0) { //adk, 1 in 5 cash now, nerfed a bit too from 1.5 to 1.4
        /*for (Item item : rewards)
				if (item.getId() == 995)
					return new Item(995, (int) (1.4 * ((int) (item.getAmount() * (premium ? 0.6 : 0.4) + Utils.random(item.getAmount() *(premium ? 1.1 : 0.7))))));
		}*/
        Item item = null;
        int tries = 0;
        while ((item == null || (!ItemConstants.isTradeable(item) && player.containsItem(item.getId()))) && tries++ < 10)
            item = rewards[Utils.random(rewards.length)];
        return item;
    }

    public static void open(Player player, int id, boolean quickOpen) {
        player.stopAll();
        player.lock(1); //just in case
        player.getInventory().deleteItem(id, 1);
        double MULTIPlIER = MysteryBox.MULTIPlIER;
        boolean premium = id == PREMIUM_ID;
        int rarity = Utils.random(900 / (premium ? 2 : 1)) == 0 ? 4 :
                Utils.random((int)(120 * MULTIPlIER) / (premium ? 2 : 1)) == 0 ? 3 :
                        Utils.random((int)(60 * MULTIPlIER) / (premium ? 2 : 1)) == 0 ? 2 :
                                Utils.random((int)(10 * MULTIPlIER) / (premium ? 2 : 1)) == 0 ? 1 : 0;
        List < Item > rewards = new ArrayList < Item > (SLOTS.length);
        rewards.add(selectReward(player, COMMON, premium));
        rewards.add(selectReward(player, UNCOMMON, premium));
        rewards.add(selectReward(player, RARE, premium));
        rewards.add(selectReward(player, VERY_RARE, premium));
        rewards.add(selectReward(player, EXTREMELY_RARE, premium));
        Item reward = rewards.get(rarity);
        WorldTask runTask = new WorldTask() {


            int currentSlot = 0;
            boolean selected;

            @Override
            public void run() {

                if (selected) {
                    stop();
                    for (int i = 0; i < SLOTS.length; i++)
                        player.getPackets().sendIComponentSprite(893, SLOTS[i], i == currentSlot ? 2206 : 20762);
                    player.getDialogueManager().startDialogue("SimpleItemMessageClose", reward.getId(), "<col=ff0000>You just WON " + reward.getName() + "!<br>Continue to claim your reward.");
                    return;
                }
                int currentIndex = (currentSlot % SLOTS.length);
                Item item = rewards.get(currentIndex);
                player.getPackets().sendIComponentSprite(893, SLOTS[(currentIndex == 0 ? SLOTS.length : currentIndex) - 1], 21120);
                player.getPackets().sendIComponentSprite(893, SLOTS[currentIndex], 21121);
                player.getPackets().sendIComponentText(893, 3, item.getName());
                if (item == reward && Utils.random(2) == 0) {
                    selected = true;
                    return;
                }
                currentSlot = ++currentSlot % SLOTS.length;
            }

        };
        Runnable closeEvent = new Runnable() {

            @Override
            public void run() {
                runTask.stop();
                Dialogue.closeNoContinueDialogue(player);
                player.getPackets().sendGameMessage("You open the " + (premium ? " premium" : "super") + " mystery box and receive <img=10>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + " <img=10>!");
                if (!player.getInventory().hasFreeSlots()) {
                    player.getPackets().sendGameMessage(reward.getName() + " x" + reward.getAmount() + " has been added to your bank.");
                    player.getBank().addItem(reward.getId(), reward.getAmount(), false);
                } else
                    player.getInventory().addItemMoneyPouch(reward);
                Logger.globalLog(player.getUsername(), player.getSession().getIP(), " got " + reward.getId() + ", " + reward.getAmount() + " from mystery box.(" + id + ")");

                if (premium && Utils.random(20) == 0) {
                    player.getPackets().sendGameMessage("You find a mystery box as well!");
                    player.getInventory().addItemDrop(6199, 1);
                }
                player.setNextGraphics(new Graphics(rarity == 4 || rarity == 3 ? 1512 : rarity == 2 || rarity == 1 ? 1513 : 1514));
                Bot.sendLog(Bot.BOX_CHANNEL, "[type=SUPER-MBOX][name=" + player.getUsername() + "]" + "[item=" + reward.getName() + "(" + reward.getId() + ")" + "x" + reward.getAmount() + "]");

                if (rarity == 4)
                    World.sendNews(player, "LEGENDARY! " + Utils.formatPlayerNameForDisplay(player.getDisplayName()) +
                            "just received <img=14><col=ffffff>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + "<col=D80000> <img=14> from a" + (premium ? "<col=ffff00> premium" : "<col=ff9933> super") + " mystery box!", 5);
                else if (rarity == 3)
                    World.sendNews(player, "ULTRA RARE! " + Utils.formatPlayerNameForDisplay(player.getDisplayName()) +
                            "just received <img=13><col=ffff00>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + "<col=D80000> <img=13> from a" + (premium ? "<col=ffff00> premium" : "<col=ff9933> super") + " mystery box!", 5);
                else if (rarity == 2)
                    World.sendNews(player, "VERY RARE! " + Utils.formatPlayerNameForDisplay(player.getDisplayName()) +
                            "just received <img=12><col=ff9933>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + "<col=D80000> <img=12> from a" + (premium ? "<col=ffff00> premium" : "<col=ff9933> super") + " mystery box!", 5);
                else if (rarity == 1)
                    World.sendNews(player, " RARE! " + Utils.formatPlayerNameForDisplay(player.getDisplayName()) +
                            " just received <img=11><col=00ACE6>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + "<col=D80000> <img=11> from a" + (premium ? "<col=ffff00> premium" : "<col=ff9933> super") + " mystery box!", 5);
            }

        };
        if (quickOpen) {
            closeEvent.run();
            return;
        }
        Collections.shuffle(rewards);
        player.getInterfaceManager().sendInterface(893);
        player.getPackets().sendIComponentText(893, 18, "Mystery Box");
        player.getPackets().sendIComponentText(893, 3, "");
        for (int i = 0; i < SLOTS.length; i++) {
            Item item = rewards.get(i);
            player.getPackets().sendIComponentSprite(893, SLOTS[i], 21120);
            player.getPackets().sendItemOnIComponent(893, SLOTS[i] + 1, item.getId(), item.getAmount());
            player.getPackets().sendIComponentSettings(893, SLOTS[i] + 1, -1, 0, 0);
        }
        player.setCloseInterfacesEvent(closeEvent);
        Dialogue.sendItemDialogueNoContinue(player, id, "Rolling....");
        WorldTasksManager.schedule(runTask, 0, 0);
    }

    public static void openBeginner(Player player, boolean quickOpen) {
        player.stopAll();
        player.lock(1); //just in case
        player.getInventory().deleteItem(BEG_ID, 1);

        List < Item > rewards = new ArrayList < Item > (SLOTS.length);
        for (int item: BEGINNER_IDS)
            rewards.add(new Item(item));
        Collections.shuffle(rewards);
        Item reward = rewards.get(Utils.random(SLOTS.length));
        WorldTask runTask = new WorldTask() {


            int currentSlot = 0;
            boolean selected;

            @Override
            public void run() {

                if (selected) {
                    stop();
                    for (int i = 0; i < SLOTS.length; i++)
                        player.getPackets().sendIComponentSprite(893, SLOTS[i], i == currentSlot ? 2206 : 20762);
                    player.getDialogueManager().startDialogue("SimpleItemMessageClose", reward.getId(), "<col=ff0000>You just WON " + reward.getName() + "!<br>Continue to claim your reward.");
                    return;
                }
                int currentIndex = (currentSlot % SLOTS.length);
                Item item = rewards.get(currentIndex);
                player.getPackets().sendIComponentSprite(893, SLOTS[(currentIndex == 0 ? SLOTS.length : currentIndex) - 1], 21120);
                player.getPackets().sendIComponentSprite(893, SLOTS[currentIndex], 21121);
                player.getPackets().sendIComponentText(893, 3, item.getName());
                if (item == reward && Utils.random(2) == 0) {
                    selected = true;
                    return;
                }
                currentSlot = ++currentSlot % SLOTS.length;
            }

        };
        Runnable closeEvent = new Runnable() {

            @Override
            public void run() {
                runTask.stop();
                Dialogue.closeNoContinueDialogue(player);
                player.getPackets().sendGameMessage("You open the mystery box and receive <img=10>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + " <img=10>!");
                if (!player.getInventory().hasFreeSlots()) {
                    player.getPackets().sendGameMessage(reward.getName() + " x" + reward.getAmount() + " has been added to your bank.");
                    player.getBank().addItem(reward.getId(), reward.getAmount(), false);
                } else
                    player.getInventory().addItemMoneyPouch(reward);
                player.setNextGraphics(new Graphics(1516));
                Bot.sendLog(Bot.BOX_CHANNEL, "[type=STARTER-MBOX][name=" + player.getUsername() + "]" + "[item=" + reward.getName() + "(" + reward.getId() + ")" + "x" + reward.getAmount() + "]");
            }

        };
        if (quickOpen) {
            closeEvent.run();
            return;
        }
        player.getInterfaceManager().sendInterface(893);
        player.getPackets().sendIComponentText(893, 18, "Mystery Box");
        player.getPackets().sendIComponentText(893, 3, "");
        for (int i = 0; i < SLOTS.length; i++) {
            Item item = rewards.get(i);
            player.getPackets().sendIComponentSprite(893, SLOTS[i], 21120);
            player.getPackets().sendItemOnIComponent(893, SLOTS[i] + 1, item.getId(), item.getAmount());
            player.getPackets().sendIComponentSettings(893, SLOTS[i] + 1, -1, 0, 0);
        }
        player.setCloseInterfacesEvent(closeEvent);
        Dialogue.sendItemDialogueNoContinue(player, BEG_ID, "Rolling....");
        WorldTasksManager.schedule(runTask, 0, 0);
    }

    public static void previewBeg(Player player) {
        Item[] itemA = new Item[BEGINNER_IDS.length];
        for (int i = 0; i < BEGINNER_IDS.length; i++)
            itemA[i] = new Item(BEGINNER_IDS[i]);
        preview(player, "Mystery Box Rewards", itemA);
    }

    public static void preview(Player player) {
        List < Item > items = new ArrayList < Item > ();
        items.addAll(Arrays.asList(EXTREMELY_RARE));
        items.addAll(Arrays.asList(VERY_RARE));
        items.addAll(Arrays.asList(RARE));
        items.addAll(Arrays.asList(UNCOMMON));
        items.addAll(Arrays.asList(COMMON));
        Item[] itemA = items.toArray(new Item[items.size()]);
        preview(player, "Super Mystery Box Rewards", itemA);
    }

    public static void preview(Player player, String title, Item[] itemA) {
        player.stopAll();
        player.getInterfaceManager().sendInterface(762);
        player.getVarsManager().sendVarBit(8348, 2);
        player.getPackets().sendItems(95, itemA);
        player.getInterfaceManager().sendInterface(762);
        player.getVarsManager().sendVarBit(4893, 1);
        for (int i = 1; i < 9; i++)
            player.getVarsManager().sendVarBit(4885 + i - 1, 0);
        player.getPackets().sendIComponentSettings(762, 95, 0, 1200, 1024);
        player.getPackets().sendIComponentText(762, 47, title);
        player.getPackets().sendHideIComponent(762, 19, true);
        player.getPackets().sendHideIComponent(762, 20, true);
        player.getPackets().sendHideIComponent(762, 33, true);
        player.getPackets().sendHideIComponent(762, 34, true);
        player.getPackets().sendHideIComponent(762, 37, true);
        player.getPackets().sendHideIComponent(762, 38, true);
        player.getPackets().sendHideIComponent(762, 39, true);
        player.getPackets().sendHideIComponent(762, 40, true);
        player.getPackets().sendHideIComponent(762, 35, true);
        player.getPackets().sendHideIComponent(762, 36, true);
        player.getPackets().sendHideIComponent(762, 17, true);
        player.getPackets().sendHideIComponent(762, 18, true);
        player.getPackets().sendHideIComponent(762, 15, true);
        player.getPackets().sendHideIComponent(762, 16, true);
        player.getPackets().sendHideIComponent(762, 119, true);
        player.getPackets().sendHideIComponent(762, 120, true);
        player.getPackets().sendHideIComponent(762, 64, true);
        player.getPackets().sendHideIComponent(762, 65, true);
        player.getPackets().sendHideIComponent(762, 28, true);
        player.getPackets().sendHideIComponent(762, 29, true);
        player.getPackets().sendHideIComponent(762, 30, true);
        player.getPackets().sendHideIComponent(762, 31, true);
        player.getPackets().sendHideIComponent(762, 32, true);
        player.getPackets().sendHideIComponent(762, 46, true);
        player.getPackets().sendHideIComponent(762, 62, true);
        player.getPackets().sendHideIComponent(762, 63, true);
        player.getPackets().sendHideIComponent(762, 124, true);
        player.getPackets().sendHideIComponent(762, 125, true);
    }
}