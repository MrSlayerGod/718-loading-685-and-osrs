package com.rs.game.player.content.raids.cox;

import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.dialogues.impl.UnlockPrayerD;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Colour;
import com.rs.utils.DropTable;
import com.rs.utils.ItemSetsKeyGenerator;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.rs.utils.DropTable.ItemDrop;

/**
 * @author Simplex
 * @since Dec 07, 2020
 */
public class ChambersRewards {

    public static final int REWARDS_KEY = ItemSetsKeyGenerator.generateKey();
    public static final int REWARDS_INTERFACE = 3077;
    public static final int REWARDS_CONTAINER = 19;
    public static final int
            REGULAR_LIGHT_BEAM = 130029,
            PURPLE_LIGHT_BEAM = 130030,
            GOLD_LIGHT_BEAM = 150000,
            BLUE_LIGHT_BEAM = 150001;
    public static double liveMultiplier = 1.0;

    public static int DEX = 51034, TORN = 51047, ARCANE = 51079;

    public static DropTable uniqueTable = new DropTable(
            new ItemDrop(51034, 1, 1, 20), // dexterous scroll
            new ItemDrop(51079, 1,1, 20), // arcane scroll
            new ItemDrop(51000, 1,1, 4), // twisted buckler
            new ItemDrop(51012, 1,1, 4), // dragon hunter crossbow
            new ItemDrop(51015, 1,1, 3), // dinh's bulwark
            new ItemDrop(51018, 1,1, 3), // ancestral hat
            new ItemDrop(51021, 1,1, 3), // ancestral top
            new ItemDrop(51024, 1,1, 3), // ancestral bottom
            new ItemDrop(50784, 1,1, 3),  // dragon claws
            new ItemDrop(51003, 1,1, 2),  // elder maul
            new ItemDrop(51043, 1,1, 2), // kodai insignia
            new ItemDrop(50997, 1,1, 2), // twisted bow
            new ItemDrop(85, 1,1, 1) // custom item
    );
    public static DropTable regularTable = new DropTable(
            new ItemDrop(560, 40,40, 1), // death rune
            new ItemDrop(565, 32,32, 1), // blood rune
            new ItemDrop(566, 20,20, 1), // soul rune
            new ItemDrop(892, 14,14, 1), // rune arrow
            new ItemDrop(11212, 140,140, 1), // dragon arrow

            new ItemDrop(3050, 370,370, 1), // grimy toadflax
            new ItemDrop(208, 800,800, 1), // grimy ranarr weed
            new ItemDrop(210, 196,196, 1), // grimy irit
            new ItemDrop(212, 370,370, 1), // grimy avantoe
            new ItemDrop(214, 405,405, 1), // grimy kwuarm
            new ItemDrop(3052, 1000,1000, 1), // grimy snapdragon
            new ItemDrop(216, 400,400, 1), // grimy cadantine
            new ItemDrop(2486, 293,293, 1), // grimy lantadyme
            new ItemDrop(218, 212,212, 1), // grimy dwarf weed
            new ItemDrop(220, 856,856, 1), // grimy torstol

            new ItemDrop(443, 20,20, 1), // silver ore
            new ItemDrop(454, 20,20, 1), // coal
            new ItemDrop(445, 45,45, 1), // gold ore
            new ItemDrop(448, 45,45, 1), // mithril ore
            new ItemDrop(450, 100,180, 1), // adamantite ore
            new ItemDrop(452, 1500,1500, 1), // runite ore

            new ItemDrop(1624, 205,205, 1), // uncut sapphire
            new ItemDrop(1622, 140,140, 1), // uncut emerald
            new ItemDrop(1620, 250,250, 1), // uncut ruby
            new ItemDrop(1618, 520,520, 1), // uncut diamond

//                    new ItemDrop(13391, 25, 1), // lizardman fang
            new ItemDrop(7937, 2,2, 1), // pure essence
//                    new ItemDrop(13422, 24, 1), // saltpetre
            new ItemDrop(8781, 100,100, 1), // teak plank
            new ItemDrop(8783, 240,240, 1), // mahogany plank
//                    new ItemDrop(13574, 55, 1), // dynamite
            new ItemDrop(51047, 1,1, 1), // torn prayer scroll
            new ItemDrop(51027, 1,1, 1) // dark relic

    );
    public static DropTable osrsRegularTable = new DropTable(
            new ItemDrop(560, 10,10, 1), // death rune
            new ItemDrop(9075, 10,10, 1), // Astral rune
            new ItemDrop(4698, 50,50, 1), // Mud runes
            new ItemDrop(565, 13,13, 1), // blood rune
            new ItemDrop(566, 13,13, 1), // soul rune
            new ItemDrop(892, 7,7, 1), // rune arrow
            new ItemDrop(11212, 70,70, 1), // dragon arrow
            new ItemDrop(11230, 70,70, 1), // dragon dart

            new ItemDrop(3050, 250,250, 1), // grimy toadflax
            new ItemDrop(208, 200,200, 1), // grimy ranarr weed
            new ItemDrop(210, 110,110, 1), // grimy irit
            new ItemDrop(212, 105,185, 1), // grimy avantoe
            new ItemDrop(214, 200,200, 1), // grimy kwuarm
            new ItemDrop(3052, 250,250, 1), // grimy snapdragon
            new ItemDrop(216, 250,250, 1), // grimy cadantine
            new ItemDrop(2486, 400,400, 1), // grimy lantadyme
            new ItemDrop(218, 350,350, 1), // grimy dwarf weed
            new ItemDrop(220, 500,500, 1), // grimy torstol

            new ItemDrop(443, 33,33, 1), // silver ore
            new ItemDrop(454, 25,25, 1), // coal
            new ItemDrop(445, 67,67, 1), // gold ore
            new ItemDrop(448, 100,100, 1), // mithril ore
            new ItemDrop(450, 167,167, 1), // adamantite ore
            new ItemDrop(452, 400,400, 1), // runite ore

            new ItemDrop(1624, 111,111, 1), // uncut sapphire
            new ItemDrop(1622, 154,154, 1), // uncut emerald
            new ItemDrop(1620, 200,200, 1), // uncut ruby
            new ItemDrop(1618, 400,400, 1), // uncut diamond

//                    new ItemDrop(13391, 25, 1), // lizardman fang
            new ItemDrop(7937, 2,2, 1), // pure essence
//                    new ItemDrop(13422, 24, 1), // saltpetre
            new ItemDrop(8781, 100,100, 1), // teak plank
            new ItemDrop(8783, 125,125, 1), // mahogany plank
//                    new ItemDrop(13574, 55, 1), // dynamite
            new ItemDrop(51047, 1,1, 1), // torn prayer scroll
            new ItemDrop(51027, 1,1, 1), // dark relic
            new ItemDrop(246, 200,200, 1), // Wine of Zamorak
            new ItemDrop(12539, 83,83, 1), // Grenwall Spikes
            new ItemDrop(21623, 200,200, 1), // Morchella Mushroom


            new ItemDrop(23352, 833,833, 1), // Saradomin Brew Flask (6)
            new ItemDrop(23400, 1250,1250, 1), // Super Restore Flask (6)
            new ItemDrop(23610, 2000,2000, 1), // Prayer Renewal Flask (6)
            new ItemDrop(23531, 5000,5000, 1), // Overload Flask (6)
            new ItemDrop(23192, 400, 400,1), // Potion Flask
            new ItemDrop(25587, 1,1, 1) // Upgrade Fragments

    );
    public static final DropTable megaRare = new DropTable(
            new ItemDrop(25632, 1, 1),
            new ItemDrop(25631, 1, 1),
            new ItemDrop(25627, 1, 1)
    );

    public static String forcedLootName = null;
    public static int forcedLootId = -1;

    public static void init() {
        InventoryOptionsHandler.register(new int[] {DEX, ARCANE, TORN}, 1, ((player, item) -> {
            player.getDialogueManager().startDialogue("UnlockPrayerD", item.getId());
        }));

        /*ObjectHandler.register(130028, 1, (player, obj) -> { // reward chest
            openRewards(player, obj);
        });*/
    }

    public static void openRewards(Player player, WorldObject chest) {
        if (player.getRaidRewards().isEmpty()) {
            player.sendMessage("The chest is empty.");
            return;
        }
        player.getPackets().sendUnlockIComponentOptionSlots(REWARDS_INTERFACE, REWARDS_CONTAINER, 0, 6, 0);
        player.getPackets().sendInterSetItemsOptionsScript(REWARDS_INTERFACE, REWARDS_CONTAINER, REWARDS_KEY, 3, 2, "Examine");
        player.getPackets().sendItems(REWARDS_KEY, player.getRaidRewards());
        player.getInterfaceManager().sendInterface(REWARDS_INTERFACE);

        player.setCloseInterfacesEvent(() -> {
            for (Item item : player.getRaidRewards().getItems()) {
                if (item != null) {
                    checkAnnounce(player, item);
                    player.getInventory().addItemDrop(item.getId(), item.getAmount());
                    if (chest != null && ChambersOfXeric.getRaid(player) != null) {
                        WorldObject beam = World.getObjectWithType(chest, 22);
                        if (beam != null)
                            beam.remove();
                        if(item.getId() == 52386) { // meta dust
                            World.sendNews( "<col=" + Colour.CYAN.hex + "><shad=0000ff>" + player.getName() + " has found a Metamorphic dust in Chambers of Xeric!", 0);
                        }
                    }
                }
            }

            player.getRaidRewards().clear();
        });
    }

    private static void checkAnnounce(Player player, Item item) {
        if (megaRare.getAllDropsOnTable().stream().anyMatch(itemDrop -> itemDrop.getId() == item.getId())) {
            World.sendNews(player, player.getDisplayName() + " has received " + Colour.GOLD.wrap(item.getName()) + " from " + Colour.RAID_PURPLE.wrap("Chambers of Xeric") + "!", 1);
        }

        if (uniqueTable.getAllDropsOnTable().stream().anyMatch(itemDrop -> itemDrop.getId() == item.getId())) {
            World.sendNews(player, player.getDisplayName() + " has received " + Colour.RAID_PURPLE.wrap(item.getName()) + " from " + Colour.RAID_PURPLE.wrap("Chambers of Xeric") + "!", 1);
        }
    }

    private static int u = 0;

    public static void giveRewards(ChambersOfXeric raid, WorldObject chest) {
        raid.getTeam().forEach(p -> {
            p.resetRaidRewards();
        });
        // clear previous loot (if any)
        //uniques
        int uniqueBudget = raid.getPartyPoints();
        int uniques = 0;

        for (int i = 0; i < 3; i++) { // up to 3 uniques
            if (uniqueBudget <= 0)
                break;
            int pointsToUse = Math.min(570000, uniqueBudget); // max of 570k points per unique attempt
            uniqueBudget -= pointsToUse;
            double chance = pointsToUse / 8675d / 100.0; // 1% chance per 8,675 points;
            chance *= liveMultiplier;

            if (forcedLootName != null || Utils.random(1.0) < chance) {
                uniques++;
                Player lucker = getPlayerToReceiveUnique(raid);

                // rig drops, used for events
                if(forcedLootName != null && raid.getTeam().stream().anyMatch(player -> player.getUsername() == forcedLootName)) {
                    lucker = World.getPlayer(forcedLootName);
                    if(lucker == null)
                        getPlayerToReceiveUnique(raid);
                }

                double probablity = Utils.round(chance * 100, 2);
                if (Settings.DEBUG)
                    lucker.sendMessage("Unique rolled with " + probablity + "% chance of purple (1% per 8,675 party points)");
                Item item = rollUnique();

                // don't allow mega-rare on onyx mode
                while(!raid.isOsrsMode() && item.getId() == 85)
                    item = rollUnique();

                boolean forcedLoot = false;

                // reset rig
                if(forcedLootName != null) {
                    for(Player player : raid.getTeam()) {
                        if(player != null && player.getUsername().equalsIgnoreCase(forcedLootName)) {
                            lucker = player;
                            item.setId(forcedLootId);
                            item.setAmount(1);
                            forcedLootName = null;
                            forcedLootId = -1;
                            forcedLoot = true;
                        }
                    }
                }

                // set lightbeam over reward chest
                WorldObject lightBeam = new WorldObject(item.getId() == 85 ? GOLD_LIGHT_BEAM : PURPLE_LIGHT_BEAM, 22, chest.getRotation(), chest.clone());
                lucker.getPackets().sendAddObject(lightBeam);

                // don't allow pet if drop was rigged
                if(!forcedLoot) {
                    LuckyPets.checkPet(lucker, LuckyPets.LuckyPet.OLMLET);
                }

                // mega rare drop accessor item id
                if (item.getId() == 85) {
                    item.setId(megaRare.roll().getId());
                }

                // roll custom unique

                lucker.getRaidRewards().add(item);

                // log drop
                Bot.sendLog(Bot.RAID_REWARDS, "[type=COX_UNIQUE][name="+lucker.getUsername()+", display="+lucker.getDisplayName() + "][item="+ ItemConfig.forID(item.getId()).getName() + "][points=" + raid.getPoints(lucker) + "][probablity=" + probablity + "%]");

                if (uniques == 1) {
                    raid.yell(Colour.RAID_PURPLE.wrap("Special loot:"));
                }
                raid.yell(Colour.RAID_PURPLE.wrap(lucker.getName() + " - ") + Colour.RED.wrap(item.getDefinitions().name));
            }
        }

        //regular drops
        List<Player> remaining = raid.getTeam().stream().filter(p -> p.getRaidRewards().isEmpty()).collect(Collectors.toList());
        boolean dustDropped = false;

        for (Player p : remaining) {
            WorldObject lightBeam = new WorldObject(REGULAR_LIGHT_BEAM, 22, chest.getRotation(), chest.clone());
            if (raid.isOsrsRaid()) {
                // osrs raid has a chance at meta dust
                if (!dustDropped && Utils.random(0, 400) == 1) {
                    dustDropped = true;
                    p.getRaidRewards().add(new Item(52386, 1));
                    lightBeam.setId(BLUE_LIGHT_BEAM);
                }
            }
            p.getPackets().sendAddObject(lightBeam);
            int playerPoints = Math.max(131071, raid.getPoints(p));
            if (playerPoints == 0)
                return;
            boolean rollTrash = true;
            for (int i = 0; i < 2; i++) {
                ItemDrop def = raid.isOsrsRaid() ? rollOsrsRegular(rollTrash) : rollRegular(rollTrash);

                double pointsPerItem = def.getMax();
                int amount = (int) Math.ceil((double) playerPoints / pointsPerItem);

                if (def.getId() == 51047 || def.getId() == 51027) {
                    // torn scroll / dark relic
                    rollTrash = false;
                    amount = 1;
                }

                amount = (int) Utils.random((double) amount * 0.3, (double) amount * 0.6);
                if (amount == 0)
                    amount = 1;
                p.getRaidRewards().add(new Item(def.getId(), amount));
            }
        }
    }

    private static ItemDrop rollOsrsRegular(boolean rollTrash) {
        ItemDrop item = osrsRegularTable.roll();
        // if a trash item has already been rolled
        if (!rollTrash && (item.getId() == 51047 || item.getId() == 51027)) // dark relic / torn scroll
            // re-roll
            return rollOsrsRegular(rollTrash);
        return item;
    }

    private static ItemDrop rollRegular(boolean rollTrash) {
        ItemDrop item = regularTable.roll();
        // if a trash item has already been rolled
        if (!rollTrash && (item.getId() == 51047 || item.getId() == 51027)) // dark relic / torn scroll
            // re-roll
            return rollRegular(rollTrash);
        return item;
    }

    private static Item rollUnique() {
        return uniqueTable.roll().get();
    }

    private static Player getPlayerToReceiveUnique(ChambersOfXeric raid) {
        int roll = Utils.get(raid.getPartyPoints());
        List<Player> plrs = new ArrayList<>();
        plrs.addAll(raid.getTeam());
        Collections.shuffle(plrs);

        for (Player player : plrs) {
            roll -= raid.getPoints(player);
            if (roll <= 0) {
                return player;
            }
        }
        return Utils.get(raid.getTeam()); // shouldnt happen, but just in case
    }
}
