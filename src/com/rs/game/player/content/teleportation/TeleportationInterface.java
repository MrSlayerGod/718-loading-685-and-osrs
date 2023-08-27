package com.rs.game.player.content.teleportation;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCConfig;
import com.rs.cache.loaders.ObjectConfig;
import com.rs.cache.loaders.StanceConfig;
import com.rs.game.TemporaryAtributtes;
import com.rs.game.WorldTile;
import com.rs.game.npc.skeletalhorror.SkeletalHorror;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * @since Sep 26, 2020
 */
public class TeleportationInterface {
    public static ArrayList<Teleport> teleportList = new ArrayList<>();

    public static final int ID = 3073;

    private static final int TRAINING = 0, SKILLING = 1, MONSTER = 2,  MINI = 3, WILDERNESS = 4, OTHER = 5;

    public static final int NPC = 0, OBJ = 1;

    private static final String[]
            CAT_NAMES = {"Training", "Skilling", "PvM", "Minigames", "Wilderness","Other"};

    private static final int[]
            CAT_NAMES_COMPONENTS = {61, 68, 75, 82, 89, 720},
            CAT_SPRITES = {4236, 4243, 4198, 4268, 4264, 4200},
            CAT_SPRITES_COMPONENTS = {62, 69, 76, 83, 90, 721},
            CAT_BUTTON_COMPONENTS = {56, 63, 70, 77, 84, 715}
    ;

    private static final Teleport NONE = new Teleport("None found", -1, null, false, NPC, 20306, "There were no results.");

    public static void main(String[] args) {
        try {
            Cache.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();

        ArrayList<ArrayList<Integer>> o = new ArrayList<>();
        for(int i = 0; i < 20; i++)
            o.add(new ArrayList<>());

        HashMap<Integer, Integer> modSize = new HashMap<>();
        modSize.put(28387, 2); //sotetseg
        modSize.put(27286, 3); //scotizo
        modSize.put(6203, 3); //kril
        modSize.put(6222, 3); //kree
        modSize.put(13447, 1); //nex
        modSize.put(28360, 2); //maiden
        modSize.put(132690, 3); //vitur
        modSize.put(22042, 3); //zulrah
        modSize.put(25886, 3); //sire
        modSize.put(25862, 3); //cerb
        modSize.put(3068, 1); //wyvern
        modSize.put(20499, 1); //wyvern
        modSize.put(14301, 1); //glacor
        modSize.put(28027, 1); //rune dragon
        modSize.put(3340, 1); //mole
        modSize.put(13460, 1); //hati
        modSize.put(8528, 1); //nomad
        modSize.put(50, 3); //kbd
        modSize.put(8349, 1); //td
        modSize.put(13460, 1); //mole
        modSize.put(1158, 1); //kalphite
        modSize.put(29050, 1); //zalcano


        NPCConfig npc = NPCConfig.forID(26644);
        for(int m : npc.models)
            o.get(3).add(m);//graardor size is glitched

        for(Teleport t : teleportList) {
            if(t.modelType == NPC) {
                npc = NPCConfig.forID(t.entityID);
                if(npc.boundSize > 1) {
                    for(int j = 0; j < npc.models.length; j++) {
                        Integer size = modSize.get(npc.id);
                        size = size != null ? size : npc.boundSize;
                        if(size > 3 && modSize.get(npc.id) == null)
                            size = 3;
                        o.get(size).add(npc.models[j]);
                    }
                }
            }

            if(t.modelType == OBJ) {
                ObjectConfig obj = ObjectConfig.forID(t.entityID);
                int size = Math.max(obj.sizeX, obj.sizeY);
                if(size > 3) size = 3;
                if(size > 1) {
                    int[] models = collapseAllIds(obj);
                    for(int j = 0; j < models.length; j++) {
                        o.get(size).add(models[j]);
                    }
                }
            }
        }

        System.out.println("private static final int[][] HC_MODEL_DATA = {");
        o.stream().forEach(integers -> {
            //if(integers.size() > 0) {
                System.out.print("{");
                integers.stream().forEach(integer -> {
                    System.out.print(integer + ",");
                });
                System.out.println("},");
            //}
        });
        System.out.println("}");
    }

    public static void addPreviousTeleport(Player player, Teleport teleport) {
        if(!player.getLastTeleports().contains(teleport)) {
            // add to front
            player.getLastTeleports().add(0, teleport);
        }
        while(player.getLastTeleports().size() > 3)
            player.getLastTeleports().remove(3);
    }

    public static void init() {

        // register previous teleports
        ButtonHandler.register(ID, 43, 1, (Player player, int s, int s2, int action)
                -> previousTeleport(player, 0));
        ButtonHandler.register(ID, 47, 1, (Player player, int s, int s2, int action)
                -> previousTeleport(player, 1));
        ButtonHandler.register(ID, 51, 1, (Player player, int s, int s2, int action)
                -> previousTeleport(player, 2));

        // register search button
        ButtonHandler.register(ID, 708, 1, (Player player, int s, int s2, int action)
                -> {
            player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.SEARCH_TELEPORT, Boolean.TRUE);
            player.getPackets().sendInputLongTextScript("Search teleport:");
        });

        // register teleport 'go' button
        ButtonHandler.register(ID, 36, 1, (Player player, int s, int s2, int action)
                -> {
            if(player.getTeleportExpanded() < 0 ||
                    player.getTeleportExpanded() >= player.getVisibleTeleports().size()) {
                player.sendMessage("You must select a category, then a destination!");
                return;
            }

            Teleport teleport = player.getVisibleTeleports().get(player.getTeleportExpanded());

            if(teleport == null) {
                return;
            }
            addPreviousTeleport(player, teleport);
            teleport(player, teleport);
        });

        // register category button clicks
        for(int i = 0; i < CAT_BUTTON_COMPONENTS.length; i++) {
            final int CAT = i;
            ButtonHandler.register(ID, CAT_BUTTON_COMPONENTS[CAT], 1, (Player player, int s, int s2, int action)
                -> expandCategory(player, CAT));
        }

        // register location button clicks
        for(int c = 0; c < 101; c++) {
            final int C = c;
            ButtonHandler.register(ID, 92 + (C*6), 1, (Player player, int s, int s2, int action)
                    -> expandLocation(player, C));
        }

        teleportList.add(new Teleport("Rock Crabs", TRAINING, new WorldTile(2675, 3712, 0), false, NPC, 1265, "A starter training zone with low HP monsters"));
        teleportList.add(new Teleport("Sand Crabs", TRAINING, new WorldTile(1868, 3551, 0), false, NPC, 25935, "A starter training zone with medium HP monsters"));
        teleportList.add(new Teleport("Ammonite Crabs", TRAINING, new WorldTile(3732, 3845, 0), false, NPC, 27799, "A starter training zone with high HP monsters"));
        teleportList.add(new Teleport("General Graardor", MONSTER, new WorldTile(2859, 5357, 0), false, NPC, 26644, "The antechamber of the Bandosian war general, Graardor, and his bodyguards"));
        teleportList.add(new Teleport("Kree'arra", MONSTER, new WorldTile(2835, 5291, 0), false, NPC, 6222, "The antechamber of the Armadyl war general, Kree, and his bodyguards"));
        teleportList.add(new Teleport("K'ril Tsutsaroth", MONSTER, new WorldTile(2925, 5336, 0), false, NPC, 6203, "The antechamber of the Zamorakian war general, K’ril, and his bodyguards"));
        teleportList.add(new Teleport("Commander Zilyana", MONSTER, new WorldTile(2923, 5262, 0), false, NPC, 6247, "The antechamber of the Saradomin war general, Zilyana, and her bodyguards"));
        teleportList.add(new Teleport("Nex", MONSTER, new WorldTile(2897, 5203, 0), false, NPC, 13447, "The antechamber of the Zarosian war general, Nex, and her four henchmen"));
        teleportList.add(new Teleport("The Nightmare", MONSTER, new WorldTile(3808, 9755, 1), false, NPC, 29446, "A large, otherworldly horror who feeds off the life force of those asleep"));
        teleportList.add(new Teleport("Chambers of Xeric", MONSTER, new WorldTile(1234, 3567, 0), false, NPC, 27519, "Experience the hardest raid ingame"));
        teleportList.add(new Teleport("Theatre Of Blood", MONSTER, new WorldTile(3630, 3219, 0), false, NPC, 28387, "A gauntlet of experiments and beasts which serve Lady Verzik Vitur"));
        teleportList.add(new Teleport("Zalcano", MONSTER, new WorldTile(3034, 6068, 0), false, NPC, 29050, "A stony demon which will put your skills to the test"));
        teleportList.add(new Teleport("Dark Cavern", MONSTER, new WorldTile(2564, 4936, 0), false, NPC, 16026, "Within this dimly lit cavern lurk the beastly wolpertingers"));
        teleportList.add(new Teleport("Kourend Catacombs", MONSTER, new WorldTile(1639, 3673, 0), false, NPC, 27286, "These catacombs are home to Skotizo and many dangerous slayer monsters"));
        teleportList.add(new Teleport("Alchemical Hydra", MONSTER, new WorldTile(1352, 10248, 0), false, NPC, 28615, "Outside the laboratory doors of a twisted, abandoned Karuulm experiment"));
        teleportList.add(new Teleport("Vorkath", MONSTER, new WorldTile(2641, 3697, 0), false, NPC, 28060, "Torfinn will row you to the isle of the undead dragon, Vorkath"));
        teleportList.add(new Teleport("Zulrah", MONSTER, new WorldTile(2204, 3056, 0), false, NPC, 22042, "Home to the the hooded serpent of the poison waste, Zulrah"));
        teleportList.add(new Teleport("Abyssal Nexus", MONSTER, new WorldTile(3038, 4768, 0), false, NPC, 25886, "Home of four Abyssal Sires, which connects to the Runecrafting Abyss"));
        teleportList.add(new Teleport("Cerberus Lair", MONSTER, new WorldTile(2871, 9849, 0), false, NPC, 25862, "The cursed Key Master allows passage to the mighty hellhound, Cerberus"));
        teleportList.add(new Teleport("Kraken Cove", MONSTER, new WorldTile(2279, 3611, 0), false, NPC, 20492, "Passageway to the mythological Kraken, hunted by mages"));
        teleportList.add(new Teleport("Lizardman Settlement", MONSTER, new WorldTile(1309, 3574, 0), false, NPC, 26766, "This settlement in Kebos Lowlands is inhabited by the Lizardmen"));
        teleportList.add(new Teleport("Demonic Gorillas", MONSTER, new WorldTile(2026, 5610, 0), false, NPC, 27144, "These Gorillas found in the caverns near Crash Site use multiple combat styles"));
        teleportList.add(new Teleport("Wyvern Cave", MONSTER, new WorldTile(3746, 3779, 0), false, NPC, 3068, "A cavern system in Fossil Island, home to the Wyverns")); teleportList.add(new Teleport("Deranged archaeol..", MONSTER, new WorldTile(3681, 3719, 0), false, NPC, 27806, "Lead to read... or die by barrages of flames!"));
        teleportList.add(new Teleport("Smoke Devil", MONSTER, new WorldTile(2412, 3054, 0), false, NPC, 20499, "This mutated Dust Devil requires you to wear a mask"));
        teleportList.add(new Teleport("Bork", MONSTER, new WorldTile(3143, 5545, 0), false, NPC, 7133, "The portal to Bork, servant of Zamorak, whose drops help with Summoning"));
        teleportList.add(new Teleport("Skeletal Horror", MONSTER, SkeletalHorror.OUTSIDE, false, NPC, 9173, "A monstrous skeletal horror composed of many different animals' remains.."));
        teleportList.add(new Teleport("Barrelchest", MONSTER, new WorldTile(3803, 2844, 0), false, NPC, 5666, "Zombified rum. Not served on tap."));
        teleportList.add(new Teleport("Phoenix Lair", MONSTER, new WorldTile(2294, 3626, 0), false, NPC, 8548, "Hot Wings… that launch fireballs"));
        teleportList.add(new Teleport("Grotesque Guardians", MONSTER, new WorldTile(3422, 3541, 2), false, NPC, 27852, "The rooftop of the gargoyles, Dusk and Dawn, who serve Bandos"));
        teleportList.add(new Teleport("Dagannoth Kings", MONSTER, new WorldTile(2527, 3739, 0), false, NPC, 1338, "The entrance to Waterbirth Island dungeon, where three Kings reside"));
        teleportList.add(new Teleport("Glacors Cave", MONSTER, new WorldTile(4181, 5723, 0), false, NPC, 14301, "This icy underground cavern is home to Glacors"));
        teleportList.add(new Teleport("Ancient Cavern", MONSTER, new WorldTile(2512, 3512, 0), false, OBJ, 28088, "These caverns are home to brutal green and mithril dragons"));
        teleportList.add(new Teleport("Lithkren lab", MONSTER, new WorldTile(3554, 4000, 0), false, NPC, 28027, "An underground research facility located in the ruins of the castle in Lithkren"));
        teleportList.add(new Teleport("Giant Mole", MONSTER, new WorldTile(2988, 3387, 0), false, NPC, 3340, "One vial of Malignius-Mortifer's-Super-Ultra-Flora-Growth-Potion later..."));
        teleportList.add(new Teleport("Hati", MONSTER, new WorldTile(2741, 3636, 0), false, NPC, 13460, "These legendary wolves are both feared and respected by the Freminnik"));
        teleportList.add(new Teleport("Nomad", MONSTER, new WorldTile(1895, 3177, 0), false, NPC, 8528, "The cavern of Nomad and his twisted minigame, Soul Wars"));
        teleportList.add(new Teleport("King black dragon", MONSTER, new WorldTile(3051, 3519, 0), false, NPC, 50, "Shakorexis is a three-headed black dragon which fires various magical attacks"));
        teleportList.add(new Teleport("Corporeal beast", MONSTER, new WorldTile(2966, 4383, 2), false, NPC, 8133, "The antechamber to the Spirit Beast, who gladly stomps out any challengers"));
        teleportList.add(new Teleport("Tormented demons", MONSTER, new WorldTile(2562, 5739, 0), false, NPC, 8349, "These massive demons are summoned and controlled by the Mahjarrat Lucien"));
        teleportList.add(new Teleport("Stronghold of security", MONSTER, new WorldTile(3080, 3418, 0), false, OBJ, 16047, "A low-level combat training dungeon located in Barbarian Village"));
        teleportList.add(new Teleport("Karamja & Crandor", MONSTER, new WorldTile(2861, 9570, 0), false, NPC, 82, "Your introduction to this island chain was smuggling rum from it"));
        teleportList.add(new Teleport("Brimhaven dungeon", MONSTER, new WorldTile(2745, 3152, 0), false, OBJ, 5083, "A large dungeon located south-west of Brimhaven"));
        teleportList.add(new Teleport("TzHaar", MONSTER, new WorldTile(4673, 5116, 0), false, NPC, 2611, "A lava-filled region beneath Karamja volcano, inhabited by obsidian creatures"));
        teleportList.add(new Teleport("Jungle Strykewyrms", MONSTER, new WorldTile(2450, 2898, 0), false, NPC, 9467, "Who said worms were small and harmless?"));
        teleportList.add(new Teleport("Desert Strykewyrms", MONSTER, new WorldTile(3381, 3162, 0), false, NPC, 9465, "I dread to think of how many of these are in the sand"));
        teleportList.add(new Teleport("Ice Strykewyrms", MONSTER, new WorldTile(3508, 5516, 0), false, NPC, 9463, "Cold worms with a tunnelling attack"));
        teleportList.add(new Teleport("Kalphite hive", MONSTER, new WorldTile(3228, 3106, 0), false, NPC, 1158, "The Kalphite Hive is located north of the Bedabin Camp in the Kharidian Desert"));
        teleportList.add(new Teleport("Kalphite Queen", MONSTER, new WorldTile(3438, 9481, 0), false, NPC, 4234, "Can you defeat both mighty forms of the Kalphite Queen?"));
        teleportList.add(new Teleport("Asgarnia ice dungeon", MONSTER, new WorldTile(3010, 3150, 0), false, NPC, 125, "This cold dungeon is said to host the only deposits of Blurite Ore"));
        teleportList.add(new Teleport("Mos le harmless jungle", MONSTER, new WorldTile(3731, 3039, 0), false, NPC, 4353, "This jungle is mostly harmless, just be careful of the maneating horrors"));
        teleportList.add(new Teleport("Gorak", MONSTER, new WorldTile(3035, 5346, 0), false, NPC, 4418, "Goraks migrate to our plane for battle"));
        teleportList.add(new Teleport("Lumbridge swamp caves", MONSTER, new WorldTile(3169, 3171, 0), false, OBJ, 5947, "Often referred to as a Slayer Caves, various low level monsters reside here"));
        teleportList.add(new Teleport("Grotworm lair (QBD)", MONSTER, new WorldTile(2990, 3237, 0), false, NPC, 15454, "The fabled Queen Black Dragon lays dormant in this lair"));
        teleportList.add(new Teleport("Fremennik Slayer Dungeon", MONSTER, new WorldTile(2794, 3615, 0), false, NPC, 9172, "A slayer dungeon located south-east of Rellekka in which the Fremennik do battle"));
        teleportList.add(new Teleport("Pollnivneach Slayer Dungeon", MONSTER, new WorldTile(3359, 2968, 0), false, NPC, 1870, "A large, smoke-filled dungeon found beneath the desert town of Pollnivneach"));
        teleportList.add(new Teleport("Polypore Dungeon", MONSTER, new WorldTile(3408, 3326, 0), false, NPC, 14696, "Ganodermic monsters lurk deep within the dungeon"));
        teleportList.add(new Teleport("Waterfall Dungeon", MONSTER, new WorldTile(2511, 3466, 0), false, OBJ, 2014, "A set of connected caverns located under Baxtorian Falls"));
        teleportList.add(new Teleport("Jadinko Lair", MONSTER, new WorldTile(2950, 2957, 0), false, OBJ, 12287, "Rumor has is the Jadinko Queen beckons travelers to rid her lair of mutations"));
        teleportList.add(new Teleport("Living Rock Caverns", MONSTER, new WorldTile(3657, 5113, 0), false, NPC, 8834, "Come down for some live rock n’ roll"));
        teleportList.add(new Teleport("Slayer Tower", MONSTER, new WorldTile(3427, 3538, 0), false, NPC, 1648, "You most likely won’t need a hand to clear the first floor"));
        teleportList.add(new Teleport("Taverley Dungeon", MONSTER, new WorldTile(2885, 3395, 0), false, NPC, 181, "Back in my day this was called Member’s Dungeon"));
        teleportList.add(new Teleport("Mourner Tunnels (Dark Beast)", MONSTER, new WorldTile(2550, 3323, 0), false, NPC, 2783, "The fallen Mourners lie between the Temple of light and Dark Beasts"));
        teleportList.add(new Teleport("Sophanem Dungeon", MONSTER, new WorldTile(2799, 5165, 0), false, NPC, 5251, "Giant Scarabs and Locust Riders roam this trap-ridden dungeon"));
        teleportList.add(new Teleport("Lumbridge Catacombs", MONSTER, new WorldTile(3246, 3198, 0), false, OBJ, 48797, "What was once the beginning for many adventures has become the end for some"));
        teleportList.add(new Teleport("Poison Waste Slayer Dungeon", MONSTER, new WorldTile(2321, 3100, 0), false, NPC, 6296, "The Legendary gnome city of Arposandra disposes of their waste here "));
        teleportList.add(new Teleport("Corsair Cove Dungeon", MONSTER, new WorldTile(2395, 2798, 0), false, NPC, 7051, "A large cave system underneath the southern part of Felip Hills"));
        teleportList.add(new Teleport("Abandoned Mine", MONSTER, new WorldTile(3442, 3232, 0), false, OBJ, 637, "A multi-level mine located under Morytania that was abandoned"));
        teleportList.add(new Teleport("Lighthouse Dungeon", MONSTER, new WorldTile(2509, 3641, 0), false, OBJ, 4587, "Go down to find horror’s that lurk deep, go up for a good view"));
        teleportList.add(new Teleport("Braindeath Island", MONSTER, new WorldTile(3680, 3536, 0), false, OBJ, 5282, "Not sure if the residents are undead or had a bit too much rum"));
        teleportList.add(new Teleport("World Boss", MONSTER, new WorldTile(2401, 5082, 0), false, NPC, 15184, "Fight the All-Powerful World Boss and his pets alongside the players of Matrix"));
        teleportList.add(new Teleport("Karuulm Slayer Dungeon", MONSTER, new WorldTile(1311, 3807, 0), false, NPC, 28609, "This slayer dungeon located beneath Mount Karuulm houses creatures never seen"));
        teleportList.add(new Teleport("Stronghold of Player Safety", MONSTER, new WorldTile(3159, 4279, 3), false, NPC, 7160, "Safety First!"));
        teleportList.add(new Teleport("Kuradal's Dungeon", MONSTER, new WorldTile(1735, 5312, 1), false, NPC, 9084, "A high level Slayer Dungeon ran by Kuradal, not for the faint of heart"));
        teleportList.add(new Teleport("Myths' Guild", MONSTER, new WorldTile(2329, 2799, 0), false, NPC, 28037, "Those capable of slaying powerful dragons are worthy of passage here"));
        teleportList.add(new Teleport("Heroes' Guild", MONSTER, new WorldTile(2919, 3515, 0), false, OBJ, 36695, "I need a HERO, I'm holdin' out for a hero 'til the end of the night"));
        teleportList.add(new Teleport("Legends' Guild", MONSTER, new WorldTile(2729, 3347, 0), false, OBJ, 2938, "Legen, wait for it"));
        teleportList.add(new Teleport("Iorwerth Dungeon", MONSTER, new WorldTile(2203, 3295, 0), false, OBJ, 136492, "This dungeon located in Prifddinas' Iorwerth District houses Irowerth Warriors"));
        teleportList.add(new Teleport("Jormungand's Prison", MONSTER, new WorldTile(2465, 4010, 0), false, NPC, 20417, "This dungeon located beneath the Island of Stone Houses Basilisk Knight's"));
        teleportList.add(new Teleport("Woodcutting", SKILLING, new WorldTile(1626, 3504, 0), false, OBJ, 1306, "A skilling teleport to begin training woodcutting"));
        teleportList.add(new Teleport("Ivy", SKILLING, new WorldTile(2938, 3429, 0), false, OBJ, 110323, "A skilling teleport which yields high amounts of woodcutting exp"));
        teleportList.add(new Teleport("Hunter", SKILLING, new WorldTile(2608, 2927, 0), false, NPC, 5112, "A skilling teleport to begin training hunter"));
        teleportList.add(new Teleport("Falconry", SKILLING, new WorldTile(2373, 3625, 0), false, NPC, 5092, "Use the aid of your falcon to hunt down various kebbits"));
        teleportList.add(new Teleport("Red Chins", SKILLING, new WorldTile(2558, 2914, 0), false, NPC, 5080, "A skilling teleport for hunting Red Carnivorous Chinchompas"));
        teleportList.add(new Teleport("Grenwalls", SKILLING, new WorldTile(2207, 3230, 0), false, NPC, 7010, "A skilling teleport for hunting Grenwalls"));
        teleportList.add(new Teleport("Fishing", SKILLING, new WorldTile(3092, 3226, 0), false, NPC, 219, "A skilling teleport to begin training Fishing"));
        teleportList.add(new Teleport("Barbarian Fishing", SKILLING, new WorldTile(2498, 3509, 0), false, NPC, 2877, "A skilling teleport using Barbarian methods"));
        teleportList.add(new Teleport("Catherby", SKILLING, new WorldTile(2846, 3433, 0), false, NPC, 576, "There are various fish that swim in the coasts of Catherby"));
        teleportList.add(new Teleport("Fishing Guild", SKILLING, new WorldTile(2596, 3405, 0), false, NPC, 592, "This guild is visited by some of the best fishermen in Gielinor "));
        teleportList.add(new Teleport("Cooking", SKILLING, new WorldTile(1679, 3620, 0), false, NPC, 278, "A skilling teleport to begin training Cooking"));
        teleportList.add(new Teleport("Smithing", SKILLING, new WorldTile(1507, 3768, 0), false, NPC, 2782, "A skilling teleport to begin training Smithing"));
        teleportList.add(new Teleport("Crafting", SKILLING, new WorldTile(2743, 3444, 0), false, OBJ, 2646, "A skilling teleport to begin training Crafting"));
        teleportList.add(new Teleport("Shilo Gems", SKILLING, new WorldTile(2827, 2998, 0), false, OBJ, 11179, "This village holds a large mineral deposit of Gem Rocks"));
        teleportList.add(new Teleport("Thieving", SKILLING, new WorldTile(3095, 3507, 0), false, OBJ, 78328, "A skilling teleport to begin training Thieving and earn some good early game GP"));
        teleportList.add(new Teleport("Draynor Market", SKILLING, new WorldTile(3080, 3250, 0), false, NPC, 970, "Come by to Draynor Market if you wish to purchase seeds and please NO THIEVING"));
        teleportList.add(new Teleport("Ardy Market", SKILLING, new WorldTile(2661, 3306, 0), false, NPC, 23, "Our wares include pure silver, fresh pastries, fine silk, and more!"));
        teleportList.add(new Teleport("Agility", SKILLING, new WorldTile(2474, 3437, 0), false, NPC, 66, "A skilling teleport to begin training agility"));
        teleportList.add(new Teleport("Barbarian Agility", SKILLING, new WorldTile(2552, 3558, 0), false, NPC, 384, "So you trained with the gnomes and think you have what it takes to run with us?"));
        teleportList.add(new Teleport("Runecrafting", SKILLING, new WorldTile(3108, 3161, 1), false, NPC, 13632, "A skilling teleport to the Runespan Portal to begin training Runecrafting"));
        teleportList.add(new Teleport("Abyss RC", SKILLING, new WorldTile(3039, 4835, 0), false, OBJ, 7171, "The Dark Mage welcomes all runecrafters into the abyss"));
        teleportList.add(new Teleport("Mining Guild", SKILLING, new WorldTile(3046, 9753, 0), false, NPC, 604, "You will receive an invisible +7 Mining level boost for mining here!"));
        teleportList.add(new Teleport("Herblore", SKILLING, new WorldTile(2923, 3488, 0), false, NPC, 14854, "A skilling teleport to begin training Herblore"));
        teleportList.add(new Teleport("Dung", SKILLING, new WorldTile(3448, 3719, 0), false, NPC, 10705, "A skilling teleport to venture into the dungeons of Daemonheim"));
        teleportList.add(new Teleport("Farming Patch", SKILLING, new WorldTile(2665, 3373, 0), false, NPC, 3021, "Farming patches located south of the Rangers Guild"));
        teleportList.add(new Teleport("Farming Patch 2", SKILLING, new WorldTile(2812, 3465, 0), false, NPC, 2324, "Farming patches located in Catherby"));
        teleportList.add(new Teleport("Farming Patch 3", SKILLING, new WorldTile(3053, 3305, 0), false, NPC, 2323, "Farming patches located north of Port Sarim"));
        teleportList.add(new Teleport("Farming Guild", SKILLING, new WorldTile(1249, 3719, 0), false, NPC, 28629, "The farmers here have created the largest greenhouse in Gielinor"));
        teleportList.add(new Teleport("The Horde", MINI, new WorldTile(1714, 5600, 0), false, NPC, 14256, "A challenging boss horde style minigame that will stretch your PvM capabilities"));
        teleportList.add(new Teleport("Inferno", MINI, new WorldTile(4571, 5255, 0), false, NPC, 27706, "Step forth into the flames of the Inferno if you wish to challenge Tzkal-Zuk"));
        teleportList.add(new Teleport("Duel arena", MINI, new WorldTile(3370, 3270, 0), false, NPC, 961, "An arena designed for gamblers who prefer trial by combat"));
        teleportList.add(new Teleport("Dominion tower", MINI, new WorldTile(3361, 3082, 0), false, OBJ, 62677, "Fight various bosses within these combat trials for a chance at powerful gloves"));
        teleportList.add(new Teleport("God Wars", MINI, new WorldTile(2908, 3707, 0), false, NPC, 6255, "A dungeon in which the armies of various Gods wage war"));
        teleportList.add(new Teleport("Barrows", MINI, new WorldTile(3565, 3306, 0), false, NPC, 2024, "After the third-age these brothers we're entombed here to never be disturbed"));
        teleportList.add(new Teleport("Fight pits", MINI, new WorldTile(4602, 5062, 0), false, OBJ, 68222, "A safe minigame in which players fight each other in a free-for-all arena"));
        teleportList.add(new Teleport("Fight caves", MINI, new WorldTile(4615, 5129, 0), false, NPC, 2745, "A fierce wave of TokHaar creatures ruled by the almighty TzTok-Jad"));
        teleportList.add(new Teleport("Kiln", MINI, new WorldTile(4743, 5170, 0), false, NPC, 15211, "Battle through 37 challenging waves in hopes to defeat Kal-Haar-Xil"));
        teleportList.add(new Teleport("Puro-puro", MINI, new WorldTile(2428, 4441, 0), false, NPC, 7903, "Many different implings like to gather at this location"));
        teleportList.add(new Teleport("Clan wars", MINI, new WorldTile(2961, 9675, 0), false, OBJ, 54017, "Create your own clan here with customizable capes and banners"));
        teleportList.add(new Teleport("Last man standing", MINI, new WorldTile(3088, 3478, 0), false, OBJ, 668, "Do you have the PVP prowess to be the Last Man Standing?"));
        teleportList.add(new Teleport("Stealing creation", MINI, new WorldTile(2961, 9675, 0), false, OBJ, 49907, "A skilling minigame which rewards tools that grant bonus exp"));
        teleportList.add(new Teleport("High & Low runespan", MINI, new WorldTile(3106, 3160, 0), false, NPC, 13, "A skilling teleport to the Runespan Portal to begin training Runecrafting"));
        teleportList.add(new Teleport("Sorceror's garden", MINI, new WorldTile(3323, 3139, 0), false, NPC, 5531, "Sneak through the sorcerer’s garden in search of some forbidden fruit"));
        teleportList.add(new Teleport("Pest Control", MINI, new WorldTile(2662, 2649, 0), false, NPC, 25513, "Help the void knights repel these pests and earn a place in their ranks"));
        teleportList.add(new Teleport("Warrior Guild", MINI, new WorldTile(2879, 3542, 0), false, NPC, 4285, "Only the mightiest of warriors are welcome to our finest defenders"));
        teleportList.add(new Teleport("Sawmill", MINI, new WorldTile(3309, 3491, 0), false, NPC, 8904, "A woodcutting minigame designed for those who wish to become a lumberjack"));
        teleportList.add(new Teleport("Castle Wars", MINI, new WorldTile(2442, 3090, 0), false, NPC, 1526, "A team based PvP event where you capture the enemies flag in honor of your God"));
        teleportList.add(new Teleport("Falconry", MINI, new WorldTile(2373, 3625, 0), false, NPC, 5092, "Use the aid of your falcon to hunt down various kebbits"));
        teleportList.add(new Teleport("Aerial Fishing", MINI, new WorldTile(1408, 3612, 0), false, NPC, 28521, "A great way to train fishing and hunter at once, beware the Golden Trench"));
        teleportList.add(new Teleport("Lava Flow Mine", MINI, new WorldTile(2939, 10198, 0), false, NPC, 14, "Help control the flow of lava where the gauge reads 50% in this mining minigame"));
        teleportList.add(new Teleport("Party Room", MINI, new WorldTile(3052, 3378, 0), false, NPC, 659, "Party Pete is always down for a drop party"));
        teleportList.add(new Teleport("Mages Bank (Safe)", WILDERNESS, new WorldTile(2538, 4715, 0), true, NPC, 905, "Purchase multiple runes here and obtain a God Cape if your magic level allows"));
        teleportList.add(new Teleport("Revenant Caves", WILDERNESS, new WorldTile(3070, 3649, 0), true, NPC, 13480, "Deep within these caves located in the wilderness many Revenants lurk"));
        teleportList.add(new Teleport("God Wars Dungeon", WILDERNESS, new WorldTile(3009, 3735, 0), true, NPC, 6258, "A dungeon in which the armies of various Gods wage war"));
        teleportList.add(new Teleport("Chaos Altar", WILDERNESS, new WorldTile(2960, 3821, 0), true, OBJ, 411, "This Chaos Altar has a chance to preserve offered bones, beware of pkers"));
        teleportList.add(new Teleport("Multi pvp(Chaos Temple)", WILDERNESS, new WorldTile(3240, 3611, 0), true, NPC, 26607, "A dark gathering of Eastern wilderness Chaos Druids"));
        teleportList.add(new Teleport("Wests", WILDERNESS, new WorldTile(2984, 3596, 0), true, NPC, 4679, "A collection of green dragons in the Western wilderness"));
        teleportList.add(new Teleport("Easts", WILDERNESS, new WorldTile(3360, 3658, 0), true, NPC, 4678, "A collection of green dragons in the Eastern wilderness"));
        teleportList.add(new Teleport("Wilderness Resource Area", WILDERNESS, new WorldTile(3173, 3936, 0), true, OBJ, 14921, "Wilderness skilling area offering +5% xp… to those brave enough to partake"));
        teleportList.add(new Teleport("Fountain of Rune", WILDERNESS, new WorldTile(3372, 3891, 0), true, OBJ, 131941, "A magical rock in deep wilderness that can charge jewelry"));
        teleportList.add(new Teleport("Chaos elemental", WILDERNESS, new WorldTile(3263, 3922, 0), true, NPC, 3200, "A tentacled purple cloud floating around the deepest wilderness"));
        teleportList.add(new Teleport("Vet'ion", WILDERNESS, new WorldTile(3210, 3797, 0), true, NPC, 26611, "An undead skeleton champion placed in Eastern Wilderness by Zamorak"));
        teleportList.add(new Teleport("Callisto", WILDERNESS, new WorldTile(3282, 3851, 0), true, NPC, 26609, "A colossal bear empowered by the Demonic Ruins of the Eastern wilderness"));
        teleportList.add(new Teleport("Venenatis", WILDERNESS, new WorldTile(3314, 3741, 0), true, NPC, 26610, "A large poisonous spider in Eastern wilderness"));
        teleportList.add(new Teleport("Dragon Slayer", OTHER, new WorldTile(2842, 9640, 0), false, NPC, 742, "Home of Crandor’s nostalgic green dragon, Elvarg"));
        teleportList.add(new Teleport("The Hunt for Surok", OTHER, new WorldTile(3143, 5545, 0), false, NPC, 7133, "The portal to Bork, servant of Zamorak, whose drops help with Summoning"));
        teleportList.add(new Teleport("The Great Brain Robbery", OTHER, new WorldTile(3803, 2844, 0), false, NPC, 5666, "Zombified rum. Not served on tap."));
        teleportList.add(new Teleport("In Pyre Need", OTHER, new WorldTile(2294, 3626, 0), false, NPC, 8548, "Hot Wings… that launch fireballs"));
        teleportList.add(new Teleport("Heroes' Quest", OTHER, new WorldTile(2868, 9939, 0), false, NPC, 795, "The Icy Throne of Misthalin’s coldest mistress"));
        teleportList.add(new Teleport("Nomad's Requiem", OTHER, new WorldTile(1890, 3177, 0), false, NPC, 8528, "The cavern of Nomad and his twisted minigame, Soul Wars"));
        teleportList.add(new Teleport("Home", OTHER, new WorldTile(3087, 3491, 0), false, NPC, 25975, "Home of PvP Tournaments, Invaluable Shops, and the infamous Upgrade Chest"));
        teleportList.add(new Teleport("Chests", OTHER, new WorldTile(2758, 3493, 1), false, OBJ, 133114, "Luxurious vault of all the chests on Matrix"));
        teleportList.add(new Teleport("Prifddinas", OTHER, new WorldTile(2239, 3323, 0), false, OBJ, 136609, "Capital of elven Tirannwn. An incredible mine, imps, and agility course"));
        teleportList.add(new Teleport("Mount Karuulm", OTHER, new WorldTile(1309, 3794, 0), false, NPC, 28623, "A volcano in northern Kebos. Slayer monsters are plentiful beneath it"));
        teleportList.add(new Teleport("Lovakengj House", OTHER, new WorldTile(1504, 3806, 0), false, NPC, 56, "One of five Kourend lordships. Famous for Mining"));
        teleportList.add(new Teleport("Arceuus House", OTHER, new WorldTile(1686, 3741, 0), false, NPC, 27053, "One of five Kourend lordships. Famous for Runecrafting"));
        teleportList.add(new Teleport("Piscarilius House", OTHER, new WorldTile(1803, 3748, 0), false, NPC, 26964, "One of five Kourend lordships. Famous for Fishing"));
        teleportList.add(new Teleport("Hosidius House", OTHER, new WorldTile(1761, 3597, 0), false, NPC, 26945, "One of five Kourend lordships. Famous for Cooking"));
        teleportList.add(new Teleport("Shayzien House", OTHER, new WorldTile(1544, 3597, 0), false, NPC, 23361, "One of five Kourend lordships. Famous for Combat"));
        teleportList.add(new Teleport("Wintertodt Camp", OTHER, new WorldTile(1629, 3944, 0), false, NPC, 27374, "Grab your warm coat; the winds are fierce in northern Kourend."));
        teleportList.add(new Teleport("Mount Quidamortem", OTHER, new WorldTile(1257, 3564, 0), false, NPC, 27595, "The evil mage Xeric and his henchmen lie beneath"));
        teleportList.add(new Teleport("Land's End", OTHER, new WorldTile(1504, 3411, 0), false, NPC, 27471, "Just the tip… of Great Kourend"));
        teleportList.add(new Teleport("Lumbridge", OTHER, new WorldTile(3222, 3219, 0), false, OBJ, 36924, "The destination of noobs instructed to ‘sit’"));
        teleportList.add(new Teleport("Varrock", OTHER, new WorldTile(3212, 3422, 0), false, OBJ, 24260, "Capital city of Misthalin: Beware the Dark Wizards"));
        teleportList.add(new Teleport("Edgeville", OTHER, new WorldTile(3094, 3502, 0), false, NPC, 747, "Home of PvP Tournaments, Invaluable Shops, and the infamous Upgrade Chest"));
        teleportList.add(new Teleport("Falador", OTHER, new WorldTile(2965, 3386, 0), false, OBJ, 865, "Home of the Mining Guild and White Knights"));
        teleportList.add(new Teleport("Seer's village", OTHER, new WorldTile(2725, 3491, 0), false, NPC, 388, "City of maple trees and honey bees"));
        teleportList.add(new Teleport("Ardougne", OTHER, new WorldTile(2662, 3305, 0), false, OBJ, 34352, "A city fit for aspiring thieves to make a name for themselves"));
        teleportList.add(new Teleport("Yanille", OTHER, new WorldTile(2605, 3093, 0), false, OBJ, 869, "A town to the South of Ardougne containing the Wizards’ guild"));
        teleportList.add(new Teleport("Keldagrim", OTHER, new WorldTile(2845, 10210, 0), false, NPC, 2228, "A Dwarven city where players train Thieving, Mining, and buy Construction goods"));
        teleportList.add(new Teleport("Dorgesh-Kaan", OTHER, new WorldTile(2720, 5351, 0), false, NPC, 5771, "The capital city of the Dorgeshuun cave goblin tribe"));
        teleportList.add(new Teleport("Lletya", OTHER, new WorldTile(2341, 3171, 0), false, NPC, 2359, "An elvish settlement located in the southern reaches of the Tirannwn forest"));
        teleportList.add(new Teleport("Etceteria", OTHER, new WorldTile(2614, 3894, 0), false, OBJ, 4692, "A capable lumberjack could gather construction supplies here"));
        teleportList.add(new Teleport("Daemonheim", OTHER, new WorldTile(3450, 3718, 0), false, NPC, 10705, "Home to the Dungeoneering skill: 60 floors beneath an icy Mahjarrat fortress"));
        teleportList.add(new Teleport("Canifis", OTHER, new WorldTile(3496, 3489, 0), false, NPC, 1665, "A village of shape-shifting werewolves, located south of the Slayer Tower"));
        teleportList.add(new Teleport("Tzhaar area", OTHER, new WorldTile(4651, 5151, 0), false, NPC, 2591, "The main shopping plaza of the Tzhaar City, located under the Karamja Volcano"));
        teleportList.add(new Teleport("Burthrope(should be Burthorpe)", OTHER, new WorldTile(2889, 3528, 0), false, NPC, 14850, "A military encampment, known for training great Warriors and Heroes"));
        teleportList.add(new Teleport("Al-Kharid", OTHER, new WorldTile(3275, 3166, 0), false, NPC, 2809, "The Great Bazaar city located North of the Kharidian Desert"));
        teleportList.add(new Teleport("Draynor village", OTHER, new WorldTile(3079, 3250, 0), false, NPC, 970, "Master Farmers and wise men reside in this seaside fishing village"));
        teleportList.add(new Teleport("Zanaris", OTHER, new WorldTile(2386, 4458, 0), false, NPC, 3303, "The Fairy City, located inside a shack in the Lumbridge swamp"));
        teleportList.add(new Teleport("Darkmeyer", OTHER, new WorldTile(3613, 3371, 0), false, NPC, 3630, "The capital city of Morytania, infested with Vyrewatch from North to South"));
        teleportList.add(new Teleport("Zul-andra", OTHER, new WorldTile(2199, 3056, 0), false, NPC, 22042, "Home to the the hooded serpent of the poison waste, Zulrah"));
        teleportList.add(new Teleport("Shilo", OTHER, new WorldTile(2849, 2963, 0), false, NPC, 8275, "A large skilling village on the southern end of Karamja"));
        teleportList.add(new Teleport("Piscatoris Fishing Colony)", OTHER, new WorldTile(2335, 3689, 0), false, NPC, 3824, "A place where monks fish, or was it a place to fish monks?"));
        teleportList.add(new Teleport("Neitiznot", OTHER, new WorldTile(2336, 3806, 0), false, NPC, 3642, "Pros: Great location for skilling, Cons: Smells like yaks"));
        teleportList.add(new Teleport("Tyras Camp", OTHER, new WorldTile(2188, 3147, 0), false, NPC, 1200, "Home to the finest quality halberd shop in all of Matrix"));
    }

    private static void previousTeleport(Player player, int i) {
        Teleport t = player.getLastTeleports().size() > i ? player.getLastTeleports().get(i) : null;
        if(t == null) {
            player.sendMessage("You can't teleport nowhere! Use a teleport first.");
        } else {
            teleport(player, t);
        }
    }

    private static void teleport(Player player, Teleport teleport) {
        if(teleport.wild) {
            player.getDialogueManager().startDialogue("DeepWildD", teleport);
        } else {
            Magic.sendCommandTeleportSpell(player, teleport.tile);
            player.stopAll();
        }
    }

    private static void expandCategory(Player player, int cat) {
        writeCategories(player, cat);
        filterTeleports(player, (teleport -> teleport.category == cat));
        writeLocations(player, player.getVisibleTeleports());
        writeLocationData(player, player.getVisibleTeleports().get(0));
    }

    private static void writeCategories(Player player, int cat) {
        for(int i = 0; i < CAT_NAMES.length; i++) {
            player.getPackets().sendHideIComponent(ID, CAT_NAMES_COMPONENTS[i] - 2, cat != i);
            player.getPackets().sendIComponentText(ID, CAT_NAMES_COMPONENTS[i], CAT_NAMES[i]);
            player.getPackets().sendIComponentSprite(ID, CAT_SPRITES_COMPONENTS[i], CAT_SPRITES[i]);
        }
    }

    private static void expandLocation(Player player, int c) {
        if(c >= player.getVisibleTeleports().size()) {
            c = 0;
        }
        player.setTeleportExpanded(c);
        writeLocations(player, player.getVisibleTeleports());
        writeLocationData(player, player.getVisibleTeleports().get(player.getTeleportExpanded()));
    }

    public static void openInterface(Player player) {
        // write previous teles
        for(int c = 46, i = 0; i < 3; c += 4, i++) {
            Teleport t = player.getLastTeleports().size() > i ? player.getLastTeleports().get(i) : null;
            String s = t == null ? "None" : t.name.length() > 18 ? t.name.substring(0, 18) + ".." : t.name;
            player.getPackets().sendIComponentText(ID, c, s);
        }

        // write category names / selection
        writeCategories(player, 0);

        // get list of teleports
        filterTeleports(player, (teleport -> teleport.category == 0));

        // write location list panel
        writeLocations(player, player.getVisibleTeleports());

        // write location data panel
        writeLocationData(player, player.getVisibleTeleports().get(0));

        player.getInterfaceManager().sendInterface(ID);
    }

    private static void writeLocationData(Player player, Teleport selectedLocation) {
        player.getPackets().sendIComponentText(ID, 23, selectedLocation.name);
        player.getPackets().sendIComponentText(ID, 24,  Utils.splitString(selectedLocation.description, 40));
        //player.getPackets().sendIComponentText(ID, 28, selectedLocation.wild ? "<col=ff0000>Dangerous teleport" : "<col=00ff00>Safe Teleport");

        player.getPackets().sendHideIComponent(ID, 30, !selectedLocation.wild); // wild
        player.getPackets().sendHideIComponent(ID, 34, selectedLocation.wild); // safe
        // sprite component = 25
        // extra models = 698 - 707
        player.getPackets().sendHideIComponent(ID, 25, true);
        for(int i = 698; i < 708; i++)
            player.getPackets().sendHideIComponent(ID, i, true);
        int[] models;
        NPCConfig npc = null;
        if(selectedLocation.modelType == NPC) {
            npc = NPCConfig.forID(selectedLocation.entityID);
            models = npc.models;
        } else {
            ObjectConfig obj = ObjectConfig.forID(selectedLocation.entityID);
            models = collapseAllIds(obj);
        }
        for(int i = 0; i < models.length; i++) {
            player.getPackets().sendIComponentModel(ID, 698 + i, models[i]);
            if(selectedLocation.modelType == NPC && npc.models.length == 1) {

                int anim = npc.walkAnimation > 0 ? npc.standAnimation : npc.renderEmote > 0 ? StanceConfig.forID(npc.renderEmote).standAnimation : -1;
                //NPCCombatDefinitions def = NPCCombatDefinitionsL.getNPCCombatDefinitions(selectedLocation.entityID);
                //if(def != null && def.getAttackEmote() > 0)
                    //anim = def.getAttackEmote();
                if(selectedLocation.entityID == 28615)
                    anim = 28233; // hydra
                player.getPackets().sendIComponentAnimation(anim, ID, 698 + i);
            } else {
                player.getPackets().sendIComponentAnimation(-1, ID, 698 + i);
            }
            //if(npc != null)
            //    System.out.println(Arrays.toString(npc.models) + " npc " + npc.id + " - " + npc.boundSize);
           player.getPackets().sendHideIComponent(ID, 698 + i, false);
        }
    }

    public static int[] collapseAllIds(ObjectConfig obj) {
        List<Integer> ids = new ArrayList<>();
        Arrays.stream(obj.modelIDs).filter(Objects::nonNull).forEach(
                ints -> Arrays.stream(ints).filter(i->i>0).forEach(ids::add)
        );
        int[] idsArr = new int[ids.size()];
        for(int i = 0; i < ids.size(); i++)
            idsArr[i] = ids.get(i);
        return idsArr;
    }

    public static void filterTeleports(Player player, Predicate<Teleport> filter) {
        player.setTeleportExpanded(0);
        player.getVisibleTeleports().clear();
        player.getVisibleTeleports().addAll(
                teleportList.stream().filter(filter::test).collect(Collectors.toList())
        );

        if(player.getVisibleTeleports().size() == 0) {
            player.getVisibleTeleports().add(NONE);
        }
    }

    public static void writeLocations(Player player, List<Teleport> teles) {

        for(int i = 0; i < CAT_NAMES.length; i++) {
            player.getPackets().sendIComponentText(ID, CAT_NAMES_COMPONENTS[i], CAT_NAMES[i]);
            player.getPackets().sendIComponentSprite(ID, CAT_SPRITES_COMPONENTS[i], CAT_SPRITES[i]);
        }

        int idx = 0;

        for(int c = 92; c < 692; c+=6) {
            Teleport tele = idx < teles.size() ? teles.get(idx) : null;

            //hide comp if no teleport to populate
            player.getPackets().sendHideIComponent(ID, c, tele == null);

            // reset red background for 'active' component unless this is selected
            int expandedComp = 92 + (player.getTeleportExpanded()*6);
            player.getPackets().sendHideIComponent(ID, c+3, c != expandedComp);
            if(tele != null) {
                player.getPackets().sendIComponentText(ID, c+5, tele.name);
            }
            idx++;
        }

    }

    public static void search(Player player, String value) {
        filterTeleports(player, (teleport -> teleport.name.toLowerCase().contains(value)));
        writeLocations(player, player.getVisibleTeleports());
        writeLocationData(player, player.getVisibleTeleports().get(0));
    }
}
