
package com.rs.game.player.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.rs.Settings;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.game.TemporaryAtributtes;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Appearence;
import com.rs.game.player.Player;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.content.surpriseevents.SurpriseEvent;
import com.rs.game.player.content.teleportation.Teleport;
import com.rs.game.player.content.teleportation.TeleportationInterface;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.NomadsRequiem;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.LoginProtocol;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.utils.ShopsHandler;
import com.rs.utils.Utils;

public class EconomyManager extends Dialogue {


	public static void init() {

		InventoryOptionsHandler.register(25659, 3, (player1, item) -> {
			player1.getInventory().deleteItem(25659, 1);
			player1.getInventory().add(new Item(25670, 1));
			player1.sendMessage("The boots transform!");
		});
		InventoryOptionsHandler.register(25670, 3, (player1, item) -> {
			player1.getInventory().deleteItem(25670, 1);
			player1.getInventory().add(new Item(25671, 1));
			player1.sendMessage("The boots transform!");
		});
		InventoryOptionsHandler.register(25671, 3, (player1, item) -> {
			player1.getInventory().deleteItem(25671, 1);
			player1.getInventory().add(new Item(25659, 1));
			player1.sendMessage("The boots transform!");
		});
		InventoryOptionsHandler.register(25662, 3, (player1, item) -> {
			player1.getInventory().deleteItem(25662, 1);
			player1.getInventory().add(new Item(25533, 1));
			player1.getInventory().add(new Item(25664, 1));
			player1.sendMessage("You revert the Twisted bow Mk. II back to its original form.");
		});
		InventoryOptionsHandler.register(25663, 3, (player1, item) -> {
			player1.getInventory().deleteItem(25663, 1);
			player1.getInventory().add(new Item(25529, 1));
			player1.getInventory().add(new Item(25664, 1));
			player1.sendMessage("You revert the Legendary lightning rapier (u) back to its original form.");
		});
		NPCHandler.register(2253, 1, (player,npc) -> {
			if (!player.getBank().hasVerified(11))
				return;
			player.getDialogueManager().startDialogue(new EconomyManager(), false);
		});
		ObjectHandler.register(126761, 1, ((player, obj) -> Magic.pushLeverTeleport(player, new WorldTile(2539, 4712, 0))));
		NPCHandler.register(1918, 2, (player,npc) -> ShopsHandler.openShop(player, 916));
		NPCHandler.register(2253, 2, (player,npc) -> ShopsHandler.openShop(player, 900));
		ObjectHandler.register(129422, 1, ((player, obj) -> player.getDialogueManager().startDialogue("SpiritTreeD", 3636)));
		ObjectHandler.register(129422, 2, ((player, obj) -> SpiritTree.openInterface(player, true)));
		ObjectHandler.register(129422, 3, ((player, obj) -> FairyRings.openRingInterface(player, obj, false)));
		ObjectHandler.register(133408, 2,
				((player, obj) -> {
					TeleportationInterface.openInterface(player);
					player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.SEARCH_TELEPORT, Boolean.TRUE);
					player.getPackets().sendInputLongTextScript("Search teleport:");
				}));
		ObjectHandler.register(129153 , 1, ((player, obj) -> player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/topic/2657-guide-directory/")));
	}
	
	public static final WorldTile[] SKILL_TELES = new WorldTile[] {
			
			new WorldTile(1868,3550,0), //attack
			new WorldTile(2673,3711,0), //defence
			new WorldTile(3730,3843,0), //strength
			new WorldTile(1868,3550,0), //hitpoints
			new WorldTile(2673,3711,0), //range
			new WorldTile(3080, 3485,0), //prayer -- NOTE: THIS WILL CHANGE AS WE MOVING HOMES.
			new WorldTile(2673,3711,0), //magic
			new WorldTile(1679, 3619, 0), //cooking
			new WorldTile(1647,3504,0), //woodcutting
			new WorldTile(1647,3504,0), //fletching
			new WorldTile(3087, 3229, 0), //fishing
			new WorldTile(1647,3504,0), //firemaking
			new WorldTile(2933,3285,0), //crafting
			new WorldTile(3109, 3501, 0), //smithing
			new WorldTile(3285,3366,0), //mining
			new WorldTile(2923,3489,0), //herblore
			new WorldTile(2474,3438,0), //agility
			new WorldTile(3096, 3509, 0), //thieving -- NOTE: THIS WILL CHANGE AS WE MOVING HOMES.
			new WorldTile(3094, 3478, 0), //slayer -- NOTE: THIS WILL CHANGE AS WE MOVING HOMES.
			new WorldTile(2812,3464,0), //farming
			new WorldTile(3040,4834,0), //runecrafting
			new WorldTile(2607,2925,0), //hunter
			new WorldTile(2215,3323,0), //construction
			new WorldTile(2928,3449,0), //summoning
			new WorldTile(3449,3716,0), //dungeoneering
	};
	private static int[] ROOT_COMPONENTS = new int[] { 5, 6, 7, 8, 9, 10, 11, 12, 13 };
	private static int[] TEXT_COMPONENTS = new int[] { 38, 46, 54, 62, 70, 78, 86, 94, 102 };
	private static int[] CLICK_COMPONENTS = new int[] { 35, 43, 51, 59, 67, 75, 83, 91, 99 };

	private static String[] SHOPS_NAMES = new String[] { "General store", "Vote shop", "PKP shop 1", "PKP shop 2",
			"Weapons 1", "Weapons 2", "Melee armor", "Ranged armor", "Magic armor", "Food & Potions", "Runes", "Ammo",
			"Summoning items", "Capes", "Jewelry", "Quest items", "Skilling stuff 1", "Skilling stuff 2",
			"Heblore Secondaries 1", "Heblore Secondaries 2", "Mastery Capes", "Back" };

	private static int[] SHOPS_IDS = new int[] { 1200, 1201, 500, 501, 1202, 1203, 1205, 1207, 1208, 1209, 1210, 1211,
			1212, 1213, 1214, 1215, 1216, 1217, 1218, 1219, 1220, -1 };

	public static int[] MANAGER_NPC_IDS = new int[] { 946 };
	
	public static String[] MANAGER_NPC_TEXTS = new String[] { "Onyx is life!", "Make sure to keep your password safe!", "::vote every day for bonuses!",
			"::donate to support the server and get benefits!", "Trade me for goodies!", "Come to me, traveler!", "Check the blue portal at middle for teleports.", "Check our ::commands list!" };

	public static String[] NEWBIE_LOC_NAMES = new String[] { "Rock Crabs", "Sand Crabs", "Ammonite Crabs",
			"Back" };
	public static WorldTile[] NEWBIE_LOCATIONS = new WorldTile[] {new WorldTile(2675, 3712, 0),
			new WorldTile(1868, 3551, 0), new WorldTile(3732, 3845, 0), null };


	public static String[] QUEST_NAMES = {"Dragon Slayer", "The Hunt for Surok", "The Great Brain Robbery", "In Pyre Need", "Heroes' Quest", "Nomad's Requiem", "Back"};
	public static WorldTile[] QUEST_LOCATIONS = {new WorldTile(2842, 9640, 0), new WorldTile(3143, 5545, 0), new WorldTile(3803, 2844, 0), new WorldTile(2294, 3626, 0)
			, new WorldTile(2868, 9939, 0)//hero quest
			, new WorldTile(1890, 3177, 0), null};

	public static String[] SKILLING_NAMES = {"Woodcutting", "Ivy", "Etcetaria", "Hunter", "Falconry", "Red Chins", "Grenwalls", "Fishing", "Barbarian Fishing", "Catherby", "Pisc Colony", "Fishing Guild", "Cooking", "Smithing", "Crafting", "Shilo Gems", "Thieving", "Draynor Market", "Ardy Market", "Agility", "Barbarian Agility", "Runecrafting", "Abyss RC", "Herblore", "Dung", "Farming Patch", "Farming Patch 2", "Farming Patch 3",  "Farming Guild", "Back"};
	public static WorldTile[] SKILLING_LOCATIONS = {new WorldTile(1626,3504,0), new WorldTile(2938,3429,0), new WorldTile(2611,3893,0),
			
			new WorldTile(2608,2927,0), new WorldTile(2365,3622,0), new WorldTile(2558,2914,0), new WorldTile(2207,3230,0),
			
			new WorldTile(3092,3226,0), //fishing
			new WorldTile(2498, 3509, 0), //barb fishing
			new WorldTile(2846,3433,0), new WorldTile(2338,3698,0), new WorldTile(2596,3405,0), new WorldTile(1679, 3620, 0), new WorldTile(1507,3768,0), new WorldTile(2743,3444,0), new WorldTile(2827,2998,0), new WorldTile(3097,3512,0), new WorldTile(3080,3250,0), new WorldTile(2661,3306,0), new WorldTile(2474,3437,0), new WorldTile(2552,3558,0), new WorldTile(3108,3161,1), new WorldTile(3039,4835,0), new WorldTile(2923,3488,0), new WorldTile(3448,3719,0), new WorldTile(2665,3373,0), new WorldTile(2812,3465,0), new WorldTile(3053,3305,0),
			
			new WorldTile(1249, 3719, 0),//farming guild
			null};

	public static String[] CITIES_NAMES = new String[] {
			"Home",
			"Chests",
			"Prifddinas",
			"Mount Karuulm",
			"Lovakengj House"
			, "Arceuus House"
			, "Piscarilius House"
			, "Hosidius House"
			, "Shayzian House"
			, "Wintertodt Camp"
			, "Mount Quidamortem"
			, "Land's End"
			,"Lumbridge", "Varrock", "Edgeville", "Falador",
			"Seer's village", "Ardougne", "Yanille", "Keldagrim", "Dorgesh-Kan", "Lletya", "Etceteria", "Daemonheim",
			"Canifis", "Tzhaar area", "Burthrope", "Al-Kharid", "Draynor village", "Zanaris", "Shilo village",
			"Darkmeyer", "Zul-andra", "Shillo", "Piscatoris Fishing Colony", "Neitiznot", "Tyras Camp", "Museum Camp", "Back" };
	public static WorldTile[] CITIES_LOCATIONS = new WorldTile[] {
			HomeTeleport.HOME_LODE_STONE,
			new WorldTile(3122, 3488, 1),
			new WorldTile(2239, 3323, 0),  //Prifddinas
			new WorldTile(1309, 3794, 0), //mount ka
			new WorldTile(1504, 3806, 0), //Lovakengj House
			new WorldTile(1686, 3741, 0), //Arceuus House
			new WorldTile(1803, 3748, 0), //"Piscariius House"
			new WorldTile(1761, 3597, 0), //hosidius house
			new WorldTile(1544, 3597, 0), //hayzian house
			new WorldTile(1629, 3944, 0),//Winterdoth camp
			new WorldTile(1257, 3564, 0),//Mount Quidamortem
			new WorldTile(1504, 3411, 0),//Land's End
			new WorldTile(3222, 3219, 0),
			new WorldTile(3212, 3422, 0), new WorldTile(3094, 3502, 0), new WorldTile(2965, 3386, 0),
			new WorldTile(2725, 3491, 0), new WorldTile(2662, 3305, 0), new WorldTile(2605, 3093, 0),
			new WorldTile(2845, 10210, 0), new WorldTile(2720, 5351, 0), new WorldTile(2341, 3171, 0),
			new WorldTile(2614, 3894, 0), new WorldTile(3450, 3718, 0), new WorldTile(3496, 3489, 0),
			new WorldTile(4651, 5151, 0), new WorldTile(2889, 3528, 0), new WorldTile(3275, 3166, 0),
			new WorldTile(3079, 3250, 0), new WorldTile(2386, 4458, 0), new WorldTile(2849, 2958, 0),
			new WorldTile(3613, 3371, 0), new WorldTile(2199, 3056, 0), new WorldTile(2849, 2963, 0), // shilo
			new WorldTile(2335, 3689, 0), // piscatoris
			new WorldTile(2336, 3806, 0), // Neitiznot
			new WorldTile(2188, 3147, 0), // Tyras Camp
			new WorldTile(3747, 3813, 0), // Museum camp
			null };

	public static String[] DUNGEON_NAMES = new String[] {
			
			"General Graardor",
			"Kree'arra",
			"K'ril Tsutsaroth",
			"Commander Zilyana",
			"Nex",
			"The Nightmare",
			"Theatre Of Blood",
			"Zalcano",
			"Dark Cavern", "Kourend Catacombs",/* "Chambers of Xeric",*/  "Alchemical Hydra", "Vorkath", "Zulrah", "Abyssal Nexus", "Cerberus Lair", "Kraken Cove",
			"Lizardman Settlement", "Demonic Gorillas", "Wyvern Cave", "Deranged archaeol..", "Smoke Devil", "Bork", "Barrelchest", "Phoenix Lair", "God Wars", "Grotesque Guardians", "Dagannoth Kings", "Glacors Cave",
			"Ancient Cavern", "Lithkren lab", "Giant Mole","Hati", "Nomad", "King black dragon", "Corporeal beast",
			"Tormented demons", "Stronghold of security", "Karamja & Crandor", "Brimhaven dungeon", "TzHaar",
			"Jungle Strykewyrms", "Desert Strykewyrms", "Ice Strykewyrms", "Kalphite hive", "Asgarnia ice dungeon",
			"Mos le harmless jungle", "Gorak", "Lumbridge swamp caves", "Grotworm lair (QBD)",
			"Fremennik Slayer dungeon", "Pollnivneach Slayer Dungeon", "Polypore Dungeon", "Waterfall Dungeon",
			"Jadinko Lair", "Living Rock Caverns", "Slayer Tower", "Taverley Dungeon", "Mourner Tunnels (Dark Beast)",
			"Sophanem Dungeon", "Lumbridge Catacombs", "Poison Waste Slayer Dungeon", "Corsair Cove Dungeon",
			"Polypore Dungeon",
			"Abandoned Mine",
			"Lighthouse Dungeon",
			"Braindeath Island",
			"World Boss",
			"Karuulm Slayer Dungeon",
			"Stronghold of Player Safety",
			"Kuradal's Dungeon",
			"Myths' Guild",
			"Heroes' Guild",
			"Legends' Guild",
			"Iorwerth Dungeon",
			"Jormungand's Prison",
			"Back" };

	public static WorldTile[] DUNGEON_LOCATIONS = new WorldTile[] {
			new WorldTile(2859, 5357, 0),
			new WorldTile(2835, 5291, 0),
			new WorldTile(2925, 5336, 0),
			new WorldTile(2923, 5262, 0),
			new WorldTile(2897, 5203, 0),
			
			new WorldTile(3808, 9755, 1),//nightmare
			new WorldTile(3630, 3219, 0),//TheatreOfBlood
			new WorldTile(3034, 6068, 0),//zalcano
			
			new WorldTile(2564, 4936, 0),
			new WorldTile(1639, 3673, 0)
		//	,new WorldTile(1233, 3568, 0) //chambers
			, new WorldTile(1352, 10248, 0) //hydra
			, new WorldTile(2641, 3697, 0), new WorldTile(2204, 3056, 0),
			new WorldTile(3038, 4768, 0), new WorldTile(2871, 9849, 0), new WorldTile(2279, 3611, 0),
			new WorldTile(1309, 3574, 0), //lizardman stellment
			new WorldTile(2026, 5610, 0), //crash site
			new WorldTile(3746, 3779, 0), //wyvern cave
			new WorldTile(3681, 3719, 0), //deranged archo
			new WorldTile(2412, 3054, 0), //smoke devil
			new WorldTile(3143, 5545, 0),//bork
			new WorldTile(3803, 2844, 0),//barrelchest
			new WorldTile(2294, 3626, 0),//phoenix lair
			new WorldTile(2908, 3707, 0), //godwars
			new WorldTile(3422, 3541, 2), //grotesque
			new WorldTile(2527, 3739, 0), // kings
			new WorldTile(4181, 5723, 0), // glacor cave
			new WorldTile(2512, 3512, 0), // mit dragons
			new WorldTile(3554, 4000, 0), // adamant + rune dragons
			new WorldTile(2988, 3387, 0), // giant mole
			new WorldTile(2741, 3636, 0), // hati
			NomadsRequiem.OUTSIDE, // nomad
			new WorldTile(3051, 3519, 0), new WorldTile(2966, 4383, 2), new WorldTile(2562, 5739, 0),
			new WorldTile(3080, 3418, 0), new WorldTile(2861, 9570, 0), new WorldTile(2745, 3152, 0),
			new WorldTile(4673, 5116, 0), new WorldTile(2450, 2898, 0), new WorldTile(3381, 3162, 0),
			new WorldTile(3508, 5516, 0), new WorldTile(3228, 3106, 0), new WorldTile(3010, 3150, 0),
			new WorldTile(3731, 3039, 0), new WorldTile(3035, 5346, 0), new WorldTile(3169, 3171, 0),
			new WorldTile(2990, 3237, 0), new WorldTile(2794, 3615, 0), // fremik
			new WorldTile(3359, 2968, 0), // poli slayer cave
			new WorldTile(3408, 3326, 0), // polypore
			new WorldTile(2511, 3466, 0), // waterfall
			new WorldTile(2950, 2957, 0), // jakindo lair
			new WorldTile(3657, 5113, 0), // living rock caverns
			new WorldTile(3427, 3538, 0), // slayer tower
			new WorldTile(2885, 3395, 0), // taverley dungeon
			new WorldTile(2550, 3323, 0), // mourner tunnel
			new WorldTile(2799, 5165, 0), //Sophanem Dungeon
			new WorldTile(3246, 3198, 0), //lumbridge catacombs
			new WorldTile(2321, 3100, 0), //poison waste dung
			new WorldTile(2395, 2798, 0), //Corsair Cove Dungeon
			new WorldTile(3408, 3326, 0), //polypore
			new WorldTile(3442, 3232, 0), //abandoned mine
			new WorldTile(2509, 3641, 0), //light house dungeon
			new WorldTile(3680, 3536, 0), //braindead island
			new WorldTile(2401, 5082, 0), //world boss
			new WorldTile(1311, 3807, 0), //ka slayer dung
			new WorldTile(3159, 4279, 3), //stronghold
			new WorldTile(1735, 5312, 1), //kuradal
			
			new WorldTile(2329, 2799, 0),//"Myths' Guild",
			new WorldTile(2919, 3515, 0),//"Heroes' Guild",
			new WorldTile(2729, 3347, 0),//"Legends' Guild",
			new WorldTile(2203, 3295, 0),//Iorwerth Dungeon
			new WorldTile(2465, 4010, 0),//Jormungand's Prison
			null };

	public static String[] MINIGAMES_NAMES = new String[] { "The Horde", "Inferno", "Duel arena", "Dominion tower", "Bork", "God Wars",
			"Barrows", "Fight pits", "Fight caves", "Kiln", "Puro-puro", "Clan wars", "Stealing creation",
			"High & Low runespan", "Sorceror's garden", "Crucible", "Pest Control", "Warrior Guild", "Sawmill",
			"Castle Wars", "Falconry", "Aerial Fishing", "Lava Flow Mine", "Party Room", "Back" };
	public static WorldTile[] MINIGAMES_LOCATIONS = new WorldTile[] {  new WorldTile(1714, 5600, 0), new WorldTile(4571, 5255, 0), new WorldTile(3370, 3270, 0),
			new WorldTile(3361, 3082, 0), new WorldTile(3143, 5545, 0), new WorldTile(2908, 3707, 0),
			new WorldTile(3565, 3306, 0), new WorldTile(4602, 5062, 0), new WorldTile(4615, 5129, 0),
			new WorldTile(4743, 5170, 0), new WorldTile(2428, 4441, 0), new WorldTile(2961, 9675, 0),
			new WorldTile(2961, 9675, 0),
			new WorldTile(3106, 3160, 0), new WorldTile(3323, 3139, 0), new WorldTile(3120, 3519, 0),
			new WorldTile(2662, 2649, 0), //pest control
			
			new WorldTile(2879, 3542, 0), new WorldTile(3309, 3491, 0),
			new WorldTile(2442, 3090, 0), new WorldTile(2371, 3623, 0),
			new WorldTile(1408, 3612, 0),
			new WorldTile(2939, 10198, 0), //lava flow mine
			new WorldTile(3052, 3378, 0), //party room
			null };

	// wilderness resource area and rune

	public static String[] WILD_NAMES = new String[] { "Slayer (safe)", "Mages Bank (Safe)", "Revenant Caves", "God Wars Dungeon", "Chaos Temple", "Multi pvp", "Wests",
			"Easts", "Wilderness Resource Area", "Fountain of Rune", "Chaos elemental", "Vet'ion", "Callisto", "Venenatis", "Back" };
	public static WorldTile[] WILD_LOCATIONS = new WorldTile[] { new WorldTile(3095, 3503, 0), new WorldTile(2538, 4715, 0), new WorldTile(3070, 3649, 0), new WorldTile(3009, 3735, 0), new WorldTile(2960, 3821, 0),
			
			new WorldTile(3240, 3611, 0), new WorldTile(2984, 3596, 0), new WorldTile(3360, 3658, 0),
			new WorldTile(3173, 3936, 0), new WorldTile(3372, 3891, 0), 
			new WorldTile(3263, 3922, 0), //Chaos elemental"
			new WorldTile(3210, 3797, 0),//Vet'ion
			new WorldTile(3282, 3851, 0),//callisto
			new WorldTile(3314, 3741, 0),//Venenatis
			null };

	/**
	 * Whether task was submitted.
	 */
	private static boolean eventTaskSubmitted;
	/**
	 * Current surprise event.
	 */
	public static SurpriseEvent surpriseEvent;
	/**
	 * Whether event is happening.
	 */
	public static boolean tileEventHappening;
	/**
	 * The location of event.
	 */
	public static WorldTile eventTile;
	/**
	 * The invite text of event.
	 */
	private static String eventText;

	public static synchronized void startEvent(String text, WorldTile tile, SurpriseEvent event) {
		if (!eventTaskSubmitted) {
			eventTaskSubmitted = true;
			/*GameExecutorManager.fastExecutor.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if (tileEventHappening) {
							for (NPC npc : World.getNPCs()) {
								if (npc == null || npc.isDead() || npc.getNextForceTalk() != null)
									continue;
								int deltaX = npc.getX() - eventTile.getX();
								int deltaY = npc.getY() - eventTile.getY();
								if (npc.getPlane() == eventTile.getPlane()
										&& !(deltaX < -25 || deltaX > 25 || deltaY < -25 || deltaY > 25))
									continue;
								if (Utils.random(10) != 0)
									continue;
								String message = "An event: " + eventText
										+ " is currently happening! Type ::event to get there!";
								if (isEconomyManagerNpc(npc.getId()))
									message = message.replace("Onyx", "me");
								npc.setNextForceTalk(new ForceTalk(message));
							}
						} else if (surpriseEvent != null) {
							for (NPC npc : World.getNPCs()) {
								if (npc == null || npc.isDead() || npc.getNextForceTalk() != null)
									continue;
								if (Utils.random(10) != 0)
									continue;

								String message = "An event: " + eventText
										+ " is currently happening! Type ::event to get there!";
								if (isEconomyManagerNpc(npc.getId()))
									message = message.replace("Onyx guide", "me");
								npc.setNextForceTalk(new ForceTalk(message));
							}
						}
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
			}, 0, 6000);*/
		}

		eventText = text;
		if (tile != null) {
			tileEventHappening = true;
			eventTile = tile;
		} else {
			surpriseEvent = event;
			event.start();
		}
	}
	
	public static void login(Player player) {
		if (tileEventHappening && eventText != null)
			player.getInterfaceManager().sendNotification("EVENT", "An event: " + eventText+ " is currently happening! Type ::event to get there!");
	}

	public static synchronized void stopEvent() {
		tileEventHappening = false;
		surpriseEvent = null;
	}

	public static boolean isEconomyManagerNpc(int id) {
		for (int i = 0; i < MANAGER_NPC_IDS.length; i++)
			if (MANAGER_NPC_IDS[i] == id)
				return true;
		return false;
	}

	private static void sendOptionsInterface(Player player) {
		player.getInterfaceManager().sendInterface(1312, true);
		player.getPackets().sendHideIComponent(1312, 2, true);
		player.getPackets().sendHideIComponent(1312, 25, true);
		player.getPackets().sendHideIComponent(1312, 26, true);
		player.getPackets().sendHideIComponent(1312, 29, true);
	}

	public static void setupInterface(Player player, String[] options) {
		for (int i = 0; i < ROOT_COMPONENTS.length; i++) {
			if (options[i] == null) {
				player.getPackets().sendHideIComponent(1312, ROOT_COMPONENTS[i], true);
			} else {
				player.getPackets().sendHideIComponent(1312, ROOT_COMPONENTS[i], false);
				player.getPackets().sendIComponentText(1312, TEXT_COMPONENTS[i], "<col=ffb84d>"+options[i]);
			}
		}
	}

	public static boolean openTPS(Player player) {
		Controller c = player.getControlerManager().getControler();
		if (c != null && c instanceof DungeonController) { // becaus dung can tp
			player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
			return false;
		}
		
		if (player.isUnderCombat() && player.getControlerManager().getControler() instanceof Wilderness) { //or could be abused
			player.getPackets().sendGameMessage("You can't home teleport shortly after the end of combat.");
			return false;
		}
		/*if (player.isIronman()) {
			player.getPackets().sendGameMessage("You can't use this feature as an ironman.");
			return false;
		}*/
		if (player.isDungeoneer()) {
			player.getPackets().sendGameMessage("You can't use this feature as a dungeoneer.");
			return false;
		}
		open(player, true);
		return true;
	}

	public static void open(final Player player, boolean tpOnly) {
		processManagerNpcClick(player, 947, tpOnly);
	}

	public static void processManagerNpcClick(final Player player, final int npcId, boolean teleportsOnly) {
		if (!player.getBank().hasVerified(11))
			return;
		player.getDialogueManager().startDialogue(new EconomyManager(), teleportsOnly);
	}
	

	public static final void processStorePurchase(final Player player, String item) {
		if (item.equals("Random nex set")) {
			int[][] sets = new int[][] { new int[] { 20159, 20163, 20167 }, new int[] { 20147, 20151, 20155 },
					new int[] { 20135, 20139, 20143 } };
			int[] set = sets[Utils.random(sets.length)];
			for (int itemid : set)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Random chaotic item")) {
			int[] items = new int[] { 18349, 18351, 18353, 18355, 18357, 18359, };
			int itemid = items[Utils.random(items.length)];
			player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Random spirit shield")) {
			int[] items = new int[] { 13738, 13740, 13742, 13744 };
			int itemid = items[Utils.random(items.length)];
			player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Random godsword")) {
			int[] items = new int[] { 11694, 11696, 11698, 11700 };
			int itemid = items[Utils.random(items.length)];
			player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Random partyhat")) {
			int[] items = new int[] { 1038, 1040, 1042, 1044, 1046, 1048 };
			int itemid = items[Utils.random(items.length)];
			player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Random haloween mask")) {
			int[] items = new int[] { 1053, 1055, 1057, };
			int itemid = items[Utils.random(items.length)];
			player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Experience (Random skill)")) {
			int skill = Utils.random(Skills.SKILL_NAME.length);
			player.getSkills().addXpStore(skill, 3000000.0D);
		} else if (item.equals("All barrows sets")) {
			int[] items = new int[] { 11846, 11848, 11850, 11852, 11854, 11856 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Bandos set (With godsword)")) {
			int[] items = new int[] { 11696, 11724, 11726, 11728 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Armadyl set (With godsword)")) {
			int[] items = new int[] { 11694, 11718, 11720, 11722 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Divine spirit shield")) {
			int[] items = new int[] { 13740 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Dragon claws")) {
			int[] items = new int[] { 14484 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Abyssal whip")) {
			int[] items = new int[] { 4151 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Coins")) {
			int[] items = new int[] { 995 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 100000000);
		} else if (item.equals("Vote tokens")) {
			int[] items = new int[] { Settings.VOTE_TOKENS_ITEM_ID };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 10000000);
		} else if (item.equals("Fire cape")) {
			player.setCompletedFightCaves();
			int[] items = new int[] { 6570 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.equals("Kiln cape")) {
			player.setCompletedFightKiln();
			int[] items = new int[] { 23659 };
			for (int itemid : items)
				player.getInventory().addItemDrop(itemid, 1);
		} else if (item.startsWith("vote_tokens:")) {
			if (!player.hasVotedInLast24Hours())
				player.setVoteCount(0);
			int votes = player.getVoteCount();
			if (votes >= 3) {
				player.getPackets()
						.sendGameMessage("You may only claim a vote three times a day. This auth has been terminated.");
				player.getPackets().sendGameMessage("For more news please refer to ::thread 75672.");
				return;
			}
			player.setVoteCount(player.getVoteCount() + 1);
			int amount = Integer.parseInt(item.substring(12));
			Item tokens = new Item(Settings.VOTE_TOKENS_ITEM_ID, amount);
			if (player.getBank().addItems(new Item[] { tokens }, true) == 0)
				player.getInventory().addItemDrop(tokens.getId(), tokens.getAmount());
			if (amount >= Settings.VOTE_MIN_AMOUNT)
				player.refreshLastVote();
			World.sendNews(player, Utils.formatPlayerNameForDisplay(player.getDisplayName())
					+ " has just voted and received " + amount + " vote tokens! (::vote)", 0);
		} else if (item.startsWith("auto_purchase:")) {
			int autotype = Integer.parseInt(item.substring(14, item.indexOf('-', 0)));
			if (autotype < 1 || autotype > 2) {
				player.getPackets().sendGameMessage("Unknown purchase:" + item);
				return;
			}

			String[] randomsets = item.substring(item.indexOf('-', 0) + 1).split("\\/");
			String[] set = randomsets[Utils.random(randomsets.length)].split("\\;");
			for (int i = 0; i < set.length; i++) {
				String[] data = set[i].split("\\,");
				int id = Integer.parseInt(data[0]);
				int amt = Integer.parseInt(data[1]);

				if (id < 0 || amt <= 0 || (autotype == 2 && id >= Skills.SKILL_NAME.length))
					continue;

				if (autotype == 1)
					player.getInventory().addItemDrop(id, amt);
				else if (autotype == 2)
					player.getSkills().addXpStore(id, (double) amt);
			}
		} else {
			player.getPackets().sendGameMessage("Unknown purchase:" + item);
		}
	}
	
		private int pageId = 0;
		private String[] currentOptions;
		private int currentOptionsOffset;

		@Override
		public void start() {
			sendOptionsInterface(player);
			if ((boolean) parameters[0])
				setTeleportsTitlePage();
			else
				setTitlePage();
		}

		@Override
		public void run(int interfaceId, int componentId) {
			int buttonId = -1;
			for (int i = 0; i < CLICK_COMPONENTS.length; i++) {
				if (componentId == CLICK_COMPONENTS[i]) {
					buttonId = i;
					break;
				}
			}

			if (currentOptions == null || buttonId == -1)
				return;

			int length = currentOptions.length - currentOptionsOffset;
			if (currentOptionsOffset != 0 || length > 9) {
				if (buttonId >= 0 && buttonId <= 7) {
					if ((buttonId + currentOptionsOffset) >= currentOptions.length
							|| currentOptions[buttonId + currentOptionsOffset] == null)
						return;
					handlePage(currentOptionsOffset + buttonId);
				} else {
					// more button
					if ((currentOptionsOffset + 8) >= currentOptions.length) {
						currentOptionsOffset = 0;
					} else {
						currentOptionsOffset += 8;
					}
					updateCurrentPage();
				}
			} else {
				if ((buttonId + currentOptionsOffset) >= currentOptions.length
						|| currentOptions[buttonId + currentOptionsOffset] == null)
					return;
				handlePage(currentOptionsOffset + buttonId);
			}
		}

		private void setPage(int page, String tip, String... options) {
			pageId = page;
			currentOptions = options;
			if (options[options.length-1].contains("Previous") && player.getGetPreviousTPName() != null)
				options[options.length-1] += "</col> ("+player.getGetPreviousTPName()+")";
			currentOptionsOffset = 0;
		//	sendEntityDialogueNoContinue(player, Dialogue.IS_NPC, "Onyx guide", npcId, 9810, tip);
			updateCurrentPage();
		}
		
		private WorldTile[] searchTeles;
		private String[] searchTelesNames;
		
		public void searchTeleport(String name) {
			name = name.toLowerCase();
			Map<String, WorldTile> teleports = new HashMap<String, WorldTile>();
			find(name, teleports, NEWBIE_LOC_NAMES, NEWBIE_LOCATIONS);
			find(name, teleports, QUEST_NAMES, QUEST_LOCATIONS);
			find(name, teleports, SKILLING_NAMES, SKILLING_LOCATIONS);
			find(name, teleports, CITIES_NAMES, CITIES_LOCATIONS);
			find(name, teleports, DUNGEON_NAMES, DUNGEON_LOCATIONS);
			find(name, teleports, MINIGAMES_NAMES, MINIGAMES_LOCATIONS);
			find(name, teleports, WILD_NAMES, WILD_LOCATIONS);
			String[] names = teleports.keySet().toArray(new String[teleports.size()+1]);
			names[names.length-1] = "Search";
			names[names.length-1] = "Back";
			searchTelesNames = names;
			searchTeles = teleports.values().toArray(new WorldTile[teleports.size()+1]);
			this.setPage(17, "", names);
			player.getTemporaryAttributtes().put(Key.SEARCH_TELEPORT, Boolean.TRUE);
			player.getPackets().sendInputLongTextScript("Search teleport:");
		}
		
		private void find(String name, Map<String, WorldTile> map, String[] teleNames, WorldTile[] teleLocs) {
			for (int i = 0; i < teleNames.length; i++) {
				if (!teleNames[i].equals("Back") && teleNames[i].toLowerCase().contains(name)) 
					map.put(teleNames[i], teleLocs[i]);
			}
		} 

		private void updateCurrentPage() {
			String[] buffer = new String[9];
			int length = currentOptions.length - currentOptionsOffset;
			if (currentOptionsOffset != 0 || length > 9) {
				System.arraycopy(currentOptions, currentOptionsOffset, buffer, 0, Math.min(length, 8));
				buffer[8] = "More"; // copy up to 8 options + more button
			} else {
				System.arraycopy(currentOptions, currentOptionsOffset, buffer, 0, length);
			}

			setupInterface(player, buffer);
		}

		private void handlePage(int optionId) {
			if (pageId == 0) { // title page
				if (optionId == 0) // information & links
					setPage(1,
							"This section contains links to our websites and wiki<br>If you are beginner, it is strongly advisted to read our beginners guide.",
							"Website & Forums", "Wiki", "Beginners guide", "Back");
				else if (optionId == 1) // Account & character management.
					setManagementPage();
				else if (optionId == 2) {// Shop
					end();
					ShopsHandler.openShop(player, 900);
				} else if (optionId == 3) // Vote
					player.getPackets().sendOpenURL(Settings.VOTE_LINK);
				else if (optionId == 4) // Donate
					player.getPackets().sendOpenURL(Settings.DONATE_LINK);
				else if (optionId == 5) // discord
					player.getPackets().sendOpenURL(Settings.DISCORD_LINK);
				else if (optionId == 6) { // Ticket
					if (player.isMuted()) {
						player.getPackets().sendGameMessage("You can't submit ticket when you are muted.");
						return;
					}
					end();
					player.getDialogueManager().startDialogue("TicketDialouge");
				} else if (optionId == 7) { // Ticket
					setTeleportsTitlePage();
				} else if (optionId == 8) // nevermind
					end();
			} else if (pageId == 1) { // information & links
				if (optionId == 0)
					player.getPackets().sendOpenURL(Settings.WEBSITE_LINK);
				else if (optionId == 1)
					player.getPackets().sendOpenURL(Settings.WIKI_LINK);
				else if (optionId == 2)
					player.getPackets().sendOpenURL(Settings.HELP_LINK);
				else if (optionId == 3)
					setTitlePage();
			} else if (pageId == 2) { // character management
				if (optionId == 0) {
					end();
					player.getBank().openPinSettings(true);
				} else if (optionId == 1) { // change your password
					/*player.getTemporaryAttributtes().put(Key.CHANGE_PASSWORD, Boolean.TRUE);
					player.getPackets().sendInputLongTextScript("Please enter your new password:");*/
					player.getPackets().sendOpenURL("https://matrixrsps.io/forums/index.php?/settings/password/");
				} else if (optionId == 2) { // display name
					setPage(10, "Here you can set your display name or remove it.", "Set display name",
							"Remove display name", "Back");
				} else if (optionId == 3) { // title select
					String[] page = getTitlesPage();
					setPage(11,
							"Here you can set your title, which will be displayed before or after your characters name.",
							page);
				} else if (optionId == 4) { // lock xp
					player.setXpLocked(!player.isXpLocked());
					setManagementPage();
				} else if (optionId == 5) { // toogle yellf
					player.setYellOff(!player.isYellOff());
					setManagementPage();
				} else if (optionId == 6) { // set yell color
					if (!player.isDonator()) {
						player.getPackets().sendGameMessage("This feature is only available to donators!");
						return;
					}
					player.getTemporaryAttributtes().put("yellcolor", Boolean.TRUE);
					player.getPackets().sendInputLongTextScript("Please enter the yell color in HEX format.");
				}
				/*
				 * } else if (optionId == 8) { // set baby troll name if
				 * (!player.isExtremeDonator()) { player.getPackets().
				 * sendGameMessage("This feature is only available to extreme donators!");
				 * return; } player.getTemporaryAttributtes().put("change_troll_name", true);
				 * player.getPackets().
				 * sendInputLongTextScript("Enter your baby troll name (type none for default):"
				 * ); }
				 */else if (optionId == 7) { // redesign character
					/*if (!player.isDonator()) {
						player.getPackets().sendGameMessage("This feature is only available to donators!");
						return;
					}*/
					end();
					PlayerLook.openCharacterCustomizing(player);
				} else if (optionId == 8) { // back
					setTitlePage();
				}
			} else if (pageId == 3) { // teleports
				if (optionId == 1000) { // current event, disabeld
					if (tileEventHappening) {
						Magic.sendNormalTeleportSpell(player, 0, 0, eventTile);
					} else if (surpriseEvent != null) {
						end();
						surpriseEvent.tryJoin(player);
					} else {
						player.getPackets().sendGameMessage("No official event is currently happening.");
					}
				/*} else if (optionId == 0) { // current starter town
					Magic.sendNormalTeleportSpell(player, 0, 0, Settings.START_PLAYER_LOCATION);
				*/} else if (optionId == 1000) { // disabled
					end();
					WorldTile tile = new WorldTile(2815, 5511, 0);
					if (!player.getControlerManager().processMagicTeleport(tile))
						return;
					player.setNextWorldTile(tile);
					player.getControlerManager().magicTeleported(Magic.MAGIC_TELEPORT);
					player.getControlerManager().startControler("clan_wars_ffa", false);
				} else if (optionId == 0) { // Combat training spots
					setPage(12, "This section contains various teleports to locations recommended for beginners.",
							NEWBIE_LOC_NAMES);
				} else if (optionId == 2) {
					setPage(18, "", QUEST_NAMES);
				} else if (optionId == 1) {
					setPage(19, "", SKILLING_NAMES);
				} else if (optionId == 3) { // cities & towns
					setPage(13, "This section contains teleports to various cities & towns.", CITIES_NAMES);
				} else if (optionId == 4) { // dungeons & pvm
					setPage(14, "This section contains teleports to various pvm locations.", DUNGEON_NAMES);
				} else if (optionId == 5) { // minigames
					setPage(15, "This section contains teleports to various minigames locations.", MINIGAMES_NAMES);
				} else if (optionId == 6) { // others
					setPage(16, "This section contains various miscellaneous teleports.", WILD_NAMES);
				} else if (optionId == 7) { // back
					/*if (teleportsOnly)
						end();
					else
						setTitlePage();*/
					player.getTemporaryAttributtes().put(Key.SEARCH_TELEPORT, Boolean.TRUE);
					player.getPackets().sendInputLongTextScript("Search teleport:");
				} else if (optionId == 8) {
					if (player.getGetPreviousTPTile() == null) {
						player.getPackets().sendGameMessage("You have no previous teleport.");
						return;
					}
					Magic.sendLunarTeleportSpell(player, 0, 0, player.getGetPreviousTPTile());
				}
			} else if (pageId == 4) { // shops
				int shopId = SHOPS_IDS[optionId];
				if (shopId < 0) { // back
					setTitlePage();
				} else {
					end();
					ShopsHandler.openShop(player, shopId);
				}
			} else if (pageId == 10) { // display name management
				if (optionId == 0) { // set display name
					if (!player.isDonator() && !player.getDisplayName().startsWith("#")) {
						player.getPackets().sendGameMessage(
								"This feature is only available to donators or users with taken usernames!");
						return;
					}
					player.getTemporaryAttributtes().put("setdisplay", Boolean.TRUE);
					player.getPackets().sendInputLongTextScript("Enter display name you want to be set:");
				} else if (optionId == 1) { // remove display name
					LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder
							.encodeAccountVarUpdate(player.getUsername(), LoginProtocol.VAR_TYPE_DISPLAY_NAME,
									Utils.formatPlayerNameForDisplay(player.getUsername()))
							.trim());
				} else if (optionId == 2) { // back
					setManagementPage();
				}
			} else if (pageId == 11) { // titles page
				int[] ids = getTitlesIds();
				if (currentOptions.length != ids.length) {
					// error
					setManagementPage();
					return;
				}

				int titleId = ids[optionId];
				if (titleId == -2) { // back button
					setManagementPage();
				} else if (titleId == -1) { // no title
					player.getAppearence().setTitle(0);
					setManagementPage();
				} else if (titleId > 0) {
					player.getAppearence().setTitle(titleId);
					setManagementPage();
				} else {
					setManagementPage();
				}
			} else if (pageId == 12) { // newbie teles
				if (NEWBIE_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else {
					Magic.sendLunarTeleportSpell(player, 0, 0, NEWBIE_LOCATIONS[optionId]);
					player.setGetPreviousTPName(NEWBIE_LOC_NAMES[optionId]);
					player.setGetPreviousTPTile(NEWBIE_LOCATIONS[optionId]);
				}
			} else if (pageId == 13) { // teleports cities & towns
				if (CITIES_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else {
					Magic.sendLunarTeleportSpell(player, 0, 0, CITIES_LOCATIONS[optionId]);
					player.setGetPreviousTPName(CITIES_NAMES[optionId]);
					player.setGetPreviousTPTile(CITIES_LOCATIONS[optionId]);
				}
			} else if (pageId == 14) { // dungeons
				if (DUNGEON_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else {
					if (DUNGEON_NAMES[optionId].contains("(GWD)")) {
						player.setNextWorldTile(DUNGEON_LOCATIONS[optionId]);
						player.stopAll();
						player.getControlerManager().startControler("GodWars");
					} else {
						Magic.sendLunarTeleportSpell(player, 0, 0, DUNGEON_LOCATIONS[optionId]);
						player.setGetPreviousTPName(DUNGEON_NAMES[optionId]);
						player.setGetPreviousTPTile(DUNGEON_LOCATIONS[optionId]);
					}
				}
			} else if (pageId == 15) { // minigames
				if (MINIGAMES_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else {
					Magic.sendLunarTeleportSpell(player, 0, 0, MINIGAMES_LOCATIONS[optionId]);
					player.setGetPreviousTPName(MINIGAMES_NAMES[optionId]);
					player.setGetPreviousTPTile(MINIGAMES_LOCATIONS[optionId]);
				}
			} else if (pageId == 18) { // quest
				if (QUEST_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else {
					Magic.sendLunarTeleportSpell(player, 0, 0, QUEST_LOCATIONS[optionId]);
					player.setGetPreviousTPName(QUEST_NAMES[optionId]);
					player.setGetPreviousTPTile(QUEST_LOCATIONS[optionId]);
					
					if (QUEST_NAMES[optionId].toLowerCase().contains("nomad")) {
						if (player.getQuestManager().getQuestStage(Quests.NOMADS_REQUIEM) == -2) // for
							player.getQuestManager().setQuestStageAndRefresh(Quests.NOMADS_REQUIEM, 0);
					}
				}
			} else if (pageId == 19) { // skilling
				if (SKILLING_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else  {
					Magic.sendLunarTeleportSpell(player, 0, 0, SKILLING_LOCATIONS[optionId]);
					player.setGetPreviousTPName(SKILLING_NAMES[optionId]);
					player.setGetPreviousTPTile(SKILLING_LOCATIONS[optionId]);
				}
			} else if (pageId == 16) { // others
				if (WILD_LOCATIONS[optionId] == null) { // back
					setTeleportsTitlePage();
				} else {
					Magic.sendLunarTeleportSpell(player, 0, 0, WILD_LOCATIONS[optionId]);
					player.setGetPreviousTPName(WILD_NAMES[optionId]);
					player.setGetPreviousTPTile(WILD_LOCATIONS[optionId]);
					/*if (OTHER_NAMES[optionId].contains("(Wilderness")) {
						player.getControlerManager().startControler("Wilderness");
					}*/
				}
			} else if (pageId == 17) { //search teleports1
				if (optionId >= searchTeles.length || searchTeles[optionId] == null)
					setTeleportsTitlePage();
				else {
					Magic.sendLunarTeleportSpell(player, 0, 0, searchTeles[optionId]);
					player.setGetPreviousTPName(searchTelesNames[optionId]);
					player.setGetPreviousTPTile(searchTeles[optionId]);
				}
			} else if (pageId == 99) { // temp page
				setTeleportsTitlePage();
			}
		}

		private void setTitlePage() {
			setPage(0,
					"Welcome to " + Settings.SERVER_NAME
							+ "!<br>I provide various services to make your life here easier.",
					"Information & Links", "Account management", "Black Market Store (Shop)", "Vote", "Donate",
					"Discord", "Submit a ticket", "Teleports", "Close");
		}

		private void setManagementPage() {
			setPage(2, "This section contains features, which will help you to manage your account easier.",
					"Bank Pin Settings", "Change password", "Display name management", "Set your title",
					player.isXpLocked() ? "Unlock XP" : "Lock XP",
					player.isYellOff() ? "Toggle yell on" : "Toggle yell off", "Set yell color",
					"Redesign character", "Back");
		}

		private void setTeleportsTitlePage() {
			/*if (player.isIronman()) {
				end();
				player.getPackets().sendGameMessage("You can't use this feature as an ironman.");
				return;
			}*/
			/*setPage(3, "This section contains teleports to various different locations.", "Combat Training",
					"Skilling", "Quests", "Cities & Towns", "Dungeons & Bosses", "Minigames",
					/*"Others"*/ /*"<col=ff0000>Wilderness", "<col=ffff00>Search", "<col=ffff00>Previous");*/
			TeleportationInterface.openInterface(player);
			player.getTemporaryAttributtes().put(TemporaryAtributtes.Key.SEARCH_TELEPORT, Boolean.TRUE);
			player.getPackets().sendInputLongTextScript("Search teleport:");
		}

		private String[] getTitlesPage() {
			String[] buffer = new String[102];
			int count = 0;

			buffer[count++] = "No title";

			if (player.getSkills().getTotalLevel() >= 2467)
				buffer[count++] = (String) Appearence.getTitle(player.getAppearence().isMale(), player.isUltimateIronman() ? 3026 : player.isIronman() ? 3006 : player.isFast() ? 3004 : 3005);
			if (player.isIronman())
				buffer[count++] = (String) Appearence.getTitle(player.getAppearence().isMale(), 3002);
			if (player.containsOneItem(15098))
				buffer[count++] = (String) Appearence.getTitle(player.getAppearence().isMale(), 3007);
			ClientScriptMap map = player.getAppearence().isMale() ? ClientScriptMap.getMap(1093)
					: ClientScriptMap.getMap(3872);
			for (Object value : map.getValues().values()) {
				if (value instanceof String && ((String) value).length() > 0) {
					buffer[count++] = (String) value;
				}

				if (count >= (buffer.length - 2))
					break;
			}

			buffer[count++] = "Back";

			if (count != buffer.length) {
				String[] rebuff = new String[count];
				System.arraycopy(buffer, 0, rebuff, 0, rebuff.length);
				return rebuff;
			} else {
				return buffer;
			}
		}

		private int[] getTitlesIds() {
			int[] buffer = new int[200];
			int count = 0;

			buffer[count++] = -1;

			if (player.getSkills().getTotalLevel() >= 2467)
				buffer[count++] = player.isUltimateIronman() ? 3026 : player.isIronman() ? 3006 : player.isFast() ? 3004 : 3005;
			if (player.isIronman())
				buffer[count++] = 3002;
			if (player.containsOneItem(15098))
				buffer[count++] = 3007;

			ClientScriptMap map = player.getAppearence().isMale() ? ClientScriptMap.getMap(1093)
					: ClientScriptMap.getMap(3872);
			for (Object value : map.getValues().values()) {
				if (value instanceof String && ((String) value).length() > 0) {
					buffer[count++] = (int) map.getKeyForValue(value);
				}

				if (count >= (buffer.length - 2))
					break;
			}

			buffer[count++] = -2;

			if (count != buffer.length) {
				int[] rebuff = new int[count];
				System.arraycopy(buffer, 0, rebuff, 0, rebuff.length);
				return rebuff;
			} else {
				return buffer;
			}
		}

		@Override
		public void finish() {
			closeNoContinueDialogue(player);
			player.getInterfaceManager().removeScreenInterface();

		}

		
		public static boolean teleport(Player player, String command) {
			String name = command.toLowerCase();
			if (name.equals("prev") || name.equals("previous")) {
				if (player.getGetPreviousTPTile() == null) {
					player.getPackets().sendGameMessage("You have no previous teleport.");
					return true;
				}
				player.getPackets().sendGameMessage("Found teleport.");
				Magic.sendLunarTeleportSpell(player, 0, 0, player.getGetPreviousTPTile());
				return true;
			}
			name = name.replace("corp", "corporealbeast");
			name = name.replace("tob", "Theatre Of Blood");
			name = name.replace("cox", "chambersofxeric");
			name = name.replace("tds", "tormenteddemons");
			name = name.replace("hydra", "alchemicalhydra");
			name = name.replace("cerb", "cerberuslair");
			name = name.replace("kraken", "krakencove");
			name = name.replace("dks", "dagannothlair(kings)");
			name = name.replace("qbd", "Grotworm lair (QBD)");//
			name = name.replace("kbd", "king black dragon");
			name = name.replace("kq", "kalphitequeen");
			name = name.replace("mole", "giantmole");
			if (name.equalsIgnoreCase("party"))
				name = "partyroom";
			else if (name.equalsIgnoreCase("nightmare"))
				name = "thenightmare";

			final String n = name.toLowerCase();
			Map<String, WorldTile> teleports = new HashMap<String, WorldTile>();
			Optional<Teleport> findTele = TeleportationInterface.teleportList.stream().filter(tele-> {
				return tele.name.toLowerCase().equals(n) || tele.name.replaceAll(" ", "").toLowerCase().equals(n);
			}).findFirst();

			/*findExact(name, teleports, NEWBIE_LOC_NAMES, NEWBIE_LOCATIONS);
			findExact(name, teleports, QUEST_NAMES, QUEST_LOCATIONS);
			findExact(name, teleports, SKILLING_NAMES, SKILLING_LOCATIONS);
			findExact(name, teleports, CITIES_NAMES, CITIES_LOCATIONS);
			findExact(name, teleports, DUNGEON_NAMES, DUNGEON_LOCATIONS);
			findExact(name, teleports, MINIGAMES_NAMES, MINIGAMES_LOCATIONS);
			findExact(name, teleports, WILD_NAMES, WILD_LOCATIONS);*/
			if (findTele.isPresent()/*!teleports.isEmpty()*/) {
				String teleportName = findTele.get().name;//(String) teleports.keySet().toArray()[0];
				if (teleportName == null)
					return false;
				WorldTile teleportTile = findTele.get().tile.clone();//teleports.get(teleportName);
				if (teleportTile == null)
					return false;
				Controller c = player.getControlerManager().getControler();
				if (c != null && c instanceof DungeonController) { // becaus dung can tp
					player.getPackets().sendGameMessage("A magical force prevents you from leaving this area.");
					return false;
				}
				
				if (player.isUnderCombat() && player.getControlerManager().getControler() instanceof Wilderness) { //or could be abused
					player.getPackets().sendGameMessage("You can't home teleport shortly after the end of combat.");
					return false;
				}
				player.getPackets().sendGameMessage("Found teleport.");
				if (findTele.get().wild)
					player.getDialogueManager().startDialogue("DeepWildD", findTele.get());
				else
					Magic.sendCommandTeleportSpell(player, teleportTile);
				//player.setGetPreviousTPName(teleportName);
				//player.setGetPreviousTPTile(teleportTile);
				TeleportationInterface.addPreviousTeleport(player, findTele.get());
				if (teleportName.toLowerCase().contains("nomad")) {
					if (player.getQuestManager().getQuestStage(Quests.NOMADS_REQUIEM) == -2) // for
						player.getQuestManager().setQuestStageAndRefresh(Quests.NOMADS_REQUIEM, 0);
				}
				return true;
			}
			return false;
		}
		
		
		private static void findExact(String name, Map<String, WorldTile> map, String[] teleNames, WorldTile[] teleLocs) {
			for (int i = 0; i < teleNames.length; i++) {
				if (!teleNames[i].equals("Back") && (teleNames[i].toLowerCase().equals(name) ||
						teleNames[i].toLowerCase().replace(" ", "").equals(name))) 
					map.put(teleNames[i], teleLocs[i]);
			}
		} 
}
