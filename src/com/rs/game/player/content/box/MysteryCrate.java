package com.rs.game.player.content.box;

import com.rs.discord.Bot;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.controllers.Inferno;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.DropTable;
import com.rs.utils.DropTable.DropCategory;
import com.rs.utils.DropTable.ItemDrop;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Simplex
 * @since Sep 15, 2020
 */
public class MysteryCrate {

    private static DropTable improvedSuperBoxDropTable, improvedGodBoxDropTable, improvedMillionaireBox;

    private static Integer[] SKIPPABLE_REWARDS = {6570, Inferno.INFERNAL_CAPE, FightKiln.TOKHAAR_KAL};

    public static void init() {
        InventoryOptionsHandler.register(ID, 1, ((player, item) -> {

            player.getPackets().sendGameMessage("You open the <col=ffff00><shad=ffffff>Mystery crate</col></shad> ...");
            player.getInventory().deleteItem(item);
            open(player, improvedSuperBoxDropTable);
            open(player, improvedGodBoxDropTable);
            open(player, improvedMillionaireBox);
        }));

        improvedSuperBoxDropTable = new DropTable(
                DropCategory.create("COMMON", 69, false, COMMON),
                DropCategory.create("UNCOMMON", 26, true, UNCOMMON),
                DropCategory.create("RARE", 8, true, RARE),
                DropCategory.create("VERY RARE", 4, true, VERY_RARE),
                DropCategory.create("EXTREMELY RARE", 1, true, EXTREMELY_RARE)
        );

        improvedGodBoxDropTable = new DropTable(
                DropCategory.create("COMMON", 69, false, GWD_COMMON),
                DropCategory.create("UNCOMMON", 26, true, GWD_UNCOMMON),
                DropCategory.create("RARE", 8, true, GWD_ULTI),
                DropCategory.create("VERY RARE", 4, true, NEX_TABLE),
                DropCategory.create("EXTREMELY RARE", 1, true, NEX_Ulti)
        );

        improvedMillionaireBox = new DropTable(
                new DropCategory("COMMON", 1, false,
                        new ItemDrop(995, 30_000_000, 150_000_000, 5), // 85%
                        new ItemDrop(43307, 50_000, 100_000, 1)) // 15%
        );
    }

    private static boolean skip(Player player, Integer rwd) {
        Optional<Integer> skipItem = Arrays.stream(SKIPPABLE_REWARDS).filter(item -> item==rwd).findAny();
        if(skipItem.isPresent())
            return player.containsItem(skipItem.get());
        return false;
    }

    private static void open(Player player, DropTable dropTable) {
        ItemDrop itemDrop = dropTable.roll();

        // skip capes
        while(skip(player, itemDrop.getId()))
            itemDrop = dropTable.roll();

        Item reward = itemDrop.get();

        player.sendMessage(" -  You receive <img=10>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + " <img=10>!");

        if (!player.getInventory().hasFreeSlots(reward)) {
            player.getPackets().sendGameMessage(reward.getName() + " x" + reward.getAmount() + " has been added to your bank.");
            player.getBank().addItem(reward.getId(), reward.getAmount(), false);
        } else
            player.getInventory().addItemMoneyPouch(reward);

        Logger.globalLog(player.getUsername(), player.getSession().getIP(), " got " + reward.getId() + ", " + reward.getAmount() + " from Mystery crate.(" + ID + ")");

        if (Utils.random(20) == 0) {
            player.getPackets().sendGameMessage("You find a mystery box as well!");
            player.getInventory().addItemDrop(6199, 1);
        }

        player.setNextGraphics(new Graphics(itemDrop.isAnnounceDrop() ? 1513 : 1514));
        Bot.sendLog(Bot.BOX_CHANNEL, "[type=MCRATE][name=" + player.getUsername() + "]" + "[item=" + reward.getName() + "(" + reward.getId() + ")" + "x" + reward.getAmount() + "]");

        if(itemDrop.isAnnounceDrop())
            World.sendNews(player, " " + itemDrop.getParent().getName().toUpperCase() + "! " + Utils.formatPlayerNameForDisplay(player.getDisplayName()) +
                    " just received <img=11><col=00ACE6>" + Utils.getFormattedNumber(reward.getAmount()) + " " + reward.getName() + "<col=D80000> <img=11> from a <col=ffff00><shad=ffffff>Mystery crate!</col></shad>", 5);
    }



    public static final int[] NEX_Ulti = {25568, 25569, 25570, 25572, 25573,//Ulti torva
            25562, 25563, 25564, 25566, 25567,//Ulti pernix
            25556, 25557, 25558, 25560, 25561, //Ulti virtus

    };//0.5%

    public static final int[] NEX_TABLE = {24977, 24983, 20135, 20139, 20143,//torva gloves boots
            24974, 24989, 20147, 20151, 20155,//pernix
            24980, 24986, 20159, 20163, 20167, //virtus

    };//2.5%


    public static final int[] GWD_ULTI = {25505, 25506, 25507, 25508, 25509, 25510,
            25511, 25512, 25513, 25514, 25515, 25516,
            25517, 25518, 25519, 25520, 25521, 25522
    };//10%

    private static final int[] GWD_UNCOMMON = {
            41791, //staff of the dead
            25037, //armadyl crossbow
            20171, //zaryte bow
            23679, 23680, 23681, 23682,  //lucky godswords
            11716, 42808, //zspear, saradomin blessed sword instead of ss
            23684, 23685, 23686, //lucky armadyl
            23687, 23688, //lucky bandos
            25420, 25421, //lucky subj


    }; //37%
    private static final int[] GWD_COMMON = {
            25413, 25414, 25415, 11728, //lucky bandos, normal boots
            25416, 25417, 25418, //lucky armadyl
            25419, 25422, 25423, 25424, //lucky subj
            25028, 25031, 25034, //gwd amulets
    }; //50%

    public static final int ID = 27004;
    public static final Item[] EXTREMELY_RARE = {
            new Item(25578),
            new Item(25577),
            new Item(25576),
            new Item(25588),
            new Item(25589),
            new Item(44702),
            new Item(25695),
            new Item(25696),
            new Item(25697),
            new Item(25698),
            new Item(25523),
            new Item(25502),
            new Item(25496),
            new Item(25476),
            new Item(25529),
            new Item(25533),
            new Item(50997),
    }; //1 in 1k
    public static final Item[] VERY_RARE = {
            new Item(25504),
            new Item(25495),
            new Item(52325),
            new Item(24455),
            new Item(24456),
            new Item(24457),
            new Item(51003),
            new Item(995, 250000000),
            new Item(24155, 250),
            new Item(25428),
            new Item(51006),
            new Item(25486),
            new Item(25488),
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
            new Item(24155, 120),
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
            new Item(24155, 60),
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
            new Item(24155, 30),
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
            new Item(52002),
    }; //1 in 10


    public static final Item[] COMMON = {
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
            new Item(24155, 15),
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
}
