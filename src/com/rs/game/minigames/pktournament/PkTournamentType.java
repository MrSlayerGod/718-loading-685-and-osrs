package com.rs.game.minigames.pktournament;

import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RunePouch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Jun 07, 2020
 */
public enum PkTournamentType {

    Hybrid_NH(new int[]{42931, 51793, 6585, 51932, 1000, 15486, 4712, 4714, 13738, 7462, 43235, 15017}, new int[]{25037, 4736, 42006, 42695,
            52109, 4738, 52322, 2444,
            6685, 6685, 3024, 43441,
            6685, 6685, 3024, 43441,
            6685, 6685, 3024, 43441,
            11694, 43441, 43441, 43441,
            565, 560, 555, 566}, 1, "Hybrid-NH",
            false,
            false, 99, true),
    Dharoks(new int[]{4716, 51295, 49553, 42006, 4720, 4722, 52322, 52981, 43239, 15220}, new int[]{4718, 42695, 6685, 6685,
            3144, 3144, 3024, 3024,
            3144, 3144, 43441, 43441,
            43441, 43441, 43441, 43441,
            43441, 43441, 43441, 43441,
            11694, 43441, 43441, 43441,
            4153, 9075, 560, 557}, 2, "Dharok's",
            false,
            true, 70, true),

    Max_Strength(new int[]{42931, 51295, 49553, 52324, 13887, 13893, 52322, 52981, 43239, 15220}, new int[]{51003, 42695, 6685, 6685,
            3144, 3144, 3024, 3024,
            3144, 3144, 43441, 43441,
            43441, 43441, 43441, 43441,
            43441, 43441, 43441, 43441,
            43441, 43441, 43441, 43441,
            11694, 9075, 560, 557}, 2, "Maxed Strength",
            false,
            true, 99, true),
    Hybrid_Main_Mystics(new int[]{10828, 51791, 6585, 15486, 4091, 4093, 13736, 7462, 11732, 15220}, new int[]{4151, 4720, 4736, 43441,
            20072, 4759, 6570, 43441,
            6685, 6685, 6685, 43441,
            3024, 3024, 42695, 43441,
            43441, 43441, 43441, 43441,
            5698, 43441, 43441, 43441,
            565, 560, 555, 566}, 1, "Hybrid-Main Mystics",
            false,
            false, 99, true),
    Pure(new int[]{2581, 6570, 6585, 5627, 42788, 1129, 2497, 7458, 3105, 15220}, new int[]{10887, 42695, 2444, 6685,
            43441, 43441, 3024, 3024,
            43441, 43441, 3144, 3144,
            43441, 43441, 3144, 3144,
            43441, 43441, 43441, 43441,
            5698, 43441, 43441, 43441,
            4153, 43441, 43441, 43441}, 1, "Pure",
            true,
            false, 1, true),
    Pure_NH(new int[]{6109, 51795, 51932, 6585, 15486, 6107, 6108, 3842, 7458, 3105, 15220}, new int[]{51902, 10499, 6685, 6685,
            4587, 2497, 6685, 6685,
            42695, 2444, 3024, 3024,
            3144, 3144, 43441, 43441,
            43441, 43441, 43441, 43441,
            43441, 43441, 43441, 43441,
            5698, 565, 555, 560}, 1, "Pure NH",
            true,
            false, 1, true),
    Void_Pure(new int[]{11664, 52109, 6585, 49484, 52804, 19785, 51000, 19786, 8842, 11732, 15220}, new int[]{49481, 11665, 6685, 6685,
            50849, 51295, 3024, 3024,
            3144, 3144, 42695, 2444,
            3144, 3144, 43441, 43441,
            43441, 43441, 43441, 43441,
            43441, 43441, 43441, 43441,
            14484, 9075, 560, 557}, 2, "Void Pure",
            true,
            false, 75, true),
    cox1(
            new int[]{25578, 25486, 25528, 25577, 25576, //gear
                    24454, 25568, 25488, 11212, 25554, // gear
                    13902, 52322}, //gear
            new int[]{25567, 25565, 44702, 25695, //inv
                    25562, 25564, 25698, 25696, //inv
                    25502, 25533, 25496, 25495,//inv
                    25589, 25476, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23351, 23399, 23399, 10588,//inv
                    23609, 4694, 23531, 42791}, //inv
            1, //spellbook
            "cox1",
            false,
            false,
            99, false),

    cox2(
            new int[]{25507, 6585, 6570, 25505, 25506, //gear
                    22358, 25509, 15220, 11212, 25554, // gear
                    43576, 20072}, //gear
            new int[]{52109, 25512, 51795, 25518, //inv
                    52109, 25513, 25698, 25519, //inv
                    18357, 25516, 22494, 50714,//inv
                    42926, 18349, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23351, 23399, 23399, 10588,//inv
                    23609, 4694, 23531, 42791}, //inv
            1, //spellbook
            "cox2",
            false,
            false,
            99, false),

    cox3(
            new int[]{11665, 6585, 6570, 19785, 19786, //gear
                    8842, 11732, 15220, 51944, 22298, // gear
                    18349, 20072}, //gear
            new int[]{11664, 20068, 11663, 2414, //inv
                    18357, 52284, 18355, 50714, //inv
                    42926, 11696, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23399, 23399, 23399, 10588,//inv
                    23609, 4694, 23531, 42791}, //inv
            1, //spellbook
            "cox3",
            false,
            false,
            99, false),

    cox4(
            new int[]{54271, 49553, 51284, 11724, 11726, //gear
                    7462, 43239, 15220, 11212, // gear
                    43576, 52322}, //gear
            new int[]{11718, 11720, 51018, 51021, //inv
                    49547, 11722, 42002, 51024, //inv
                    42926, 50997, 52323, 13738,//inv
                    52324, 52325, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23351, 23399, 23399, 10588,//inv
                    23609, 4694, 23531, 42791}, //inv
            1, //spellbook
            "cox4",
            false,
            false,
            99, false),

    cox5(
            new int[]{54271, 6585, 6570, 11724, 11726, //gear
                    7462, 43239, 15220, 51944, // gear
                    43576, 20072}, //gear
            new int[]{11718, 11720, 4708, 4712, //inv
                    52109, 11722, 51795, 4714, //inv
                    51012, 51000, 42899, 50714,//inv
                    42926, 52978, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23351, 23399, 23399, 10588,//inv
                    23609, 4694, 23531, 42791}, //inv
            1, //spellbook
            "cox5",
            false,
            false,
            99, false),

    cox6(
            new int[]{11665, 6585, 6570, 19785, 19786, //gear
                    8842, 11732, 15220, 51944,  // gear
                    4151, 20072}, //gear
            new int[]{11664, 20068, 11663, 2414, //inv
                    9185, 52284, 42899, 50714, //inv
                    42926, 11696, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23351, 23351, 23351, 23351,//inv
                    23399, 23399, 23399, 10588,//inv
                    23609, 4694, 23531, 42791}, //inv
            1, //spellbook
            "cox6",
            false,
            false,
            99, false),
    LMS(new int[]{10828, 51793, 1704, 9243, 4151, 2503, 20072, 1079, 7462, 3105, 6737}, // EQUIPMENT
            new int[]{4091, 4093, 13734, 4710, // INV line 1
                    9185, 1215, 6685, 3024, // INV line 2
                    42625, 2444, 42695, // INV line 3
                    3144, 3144, // INV line 4
                    385, 385, 385, 385, // INV line 5
                    385, 385, 385, 385, // INV line 6
                    385, 385, 385, 42791 }, // INV line 7
            1, "LMS", // don't touch
            false, // don't touch
            false, 99, false); // don't touch

    private static final int LUNAR = 2, ANCIENT = 1;

    public String formattedName;

    public int[] inventory, equipment;
    public int spellbook, defenceLevel;
    public boolean pure, disablePrayers;

    PkTournamentType(int[] equipment, int[] inventory, int spellbook, String formattedName, boolean pure, boolean disablePrayers, int defenceLevel, boolean enabledForTourney) {
        this.formattedName = formattedName;
        this.inventory = inventory;
        this.equipment = equipment;
        this.spellbook = spellbook;
        this.pure = pure;
        this.disablePrayers = disablePrayers;
        this.defenceLevel = defenceLevel;
    }

    final static Item[] BARRAGE_RUNES = new Item[] {new Item(555, 2500), new Item(565, 2500),new Item(560, 2500)};

    public void setup(Player player) {
        Arrays.stream(inventory).forEach(i -> {
                    Item item = new Item(i);
                    if (item.getDefinitions().isStackable())
                        item.setAmount(2000);
                    player.getInventory().addItem(item);
                }
        );

        Arrays.stream(equipment).forEach(i -> {
                    Item item = new Item(i);
                    if (item.getDefinitions().isStackable())
                        item.setAmount(2000);
                    player.getEquipment().getItems().set(Equipment.getItemSlot(i), item);
                }
        );

        for(int i = 0; i < 3; i++) {
            Item rune = player.getRunePouch().get(i);
            if(rune != null) {
                player.getBank().addItem(rune.getId(), rune.getAmount(), true);
            }
            player.getRunePouch().set(i, BARRAGE_RUNES[i]);
        }
        RunePouch.refreshRunePouch(player);

        player.getPackets().sendItems(100, player.getRunePouch().getItems());

        player.getPrayer().setPrayerBook(false);

        double[] xpArray = new double[player.getSkills().getXp().length];
        Arrays.fill(xpArray, Skills.getXPForLevel(99));
        xpArray[Skills.DEFENCE] = Skills.getXPForLevel(defenceLevel);
        xpArray[Skills.SUMMONING] = 0;

        player.getSkills().setTemporaryXP(xpArray);

        player.reset(); //reset all back to default

        player.getCombatDefinitions().setSpellBook(spellbook);
        player.getPrayer().setPrayerBook(false);
        player.getEquipment().init();
        player.getAppearence().generateAppearenceData();
    }


    public int getLevelForSkill(int skill) {
        return pure ? (skill == Skills.DEFENCE ? 1 : skill == Skills.PRAYER ? 52 : 99) : 99;
    }

    public static void removeSetup(Player player) {
        player.getInventory().getItems().clear();
        player.getEquipment().getItems().clear();
        player.getEquipment().init();
        player.getInventory().init();
        player.getAppearence().generateAppearenceData();
        player.getCombatDefinitions().setSpellBook(0);
        player.getSkills().setTemporaryXP(null);
        player.reset();
        for(int i = 0; i < 3; i++) {
            player.getRunePouch().set(i, null);
        }
        RunePouch.refreshRunePouch(player);
    }

    public String getFormattedName() {
        return formattedName;
    }

    public boolean prayerDisabled() {
        return disablePrayers;
    }
}
