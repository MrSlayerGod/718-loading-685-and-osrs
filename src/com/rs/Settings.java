package com.rs;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import com.rs.game.WorldTile;
import com.rs.login.WorldInformation;

public final class Settings {

	public static final String LATEST_UPDATE = "--Update 14--<br>"
			+ "Easter bunny has come to Onyx! Don't forget to collect your eggs!<br>Osrs gameframe option has been added!<br>Jad Test Mode is now available!<br>Trivia has been updated!<br>Bug fixes & improvements!";
	
	public static final String SERVER_NAME = "Matrix";
	public static final String CACHE_PATH = "data/cache/";
	public static final String LOGIN_DATA_PATH = "data/login/";
	public static final String DATA_PATH = "data/world/";

	public static final double SERVER_MAGIC_ACCURACY_BUFF = 1.30;
	public static final double SERVER_MAGIC_DAMAGE_BUFF = 1;//1.10;
    public static boolean DISABLE_RANDOM_EVENTS = false;

    //  public static final SQLDatabase DONATIONS_DATABASE = new SQLDatabase("162.218.48.74:3306", "onyxftwc_pay", "onyxftwc_store001", "5B15jLqi#[Fz", true);
	
	private static final String LIVE_IP = "144.217.10.71";//"144.217.11.36"; reenable if we do multiple worlds but gonna enable

	public static InetSocketAddress GAME_ADDRESS_BASE = new InetSocketAddress("0.0.0.0", 43593);
	public static InetSocketAddress LOGIN_SERVER_ADDRESS_BASE = new InetSocketAddress("127.0.0.1"/*LIVE_IP*/, 37777);
	public static InetSocketAddress LOGIN_CLIENT_ADDRESS_BASE = new InetSocketAddress("127.0.0.1"/*LIVE_IP*/, 37778);

	public static final WorldInformation[] WORLDS_INFORMATION = new WorldInformation[] {
		new WorldInformation(1, 0, "World1", 0, 0x1 | 0x8, "Matrix", LIVE_IP, 0)
		,new WorldInformation(2, 0, "BetaWorld1", 0, 0x1 | 0x8, "Matrix", LIVE_IP, 1)
	};
	
	//disabled
	public static final InetSocketAddress WEBSITE_LISTENER_ADDRESS = new InetSocketAddress("127.0.0.1", 43598);
	public static final String WEBSITE_CLIENT_PASSWORD = "LLU7szMljNdJ5hZpOYpX38UEdOQi63fp7DjJrRR6MzCbtK";
	public static final InetSocketAddress HTTP_PANEL_ADDRESS = new InetSocketAddress("0.0.0.0", 43599);
	public static final String HTTP_PANEL_DATA_PATH = "data/web/";
	//

	public static int WORLD_ID = 0;
	public static boolean DEBUG = false;
	public static boolean HOSTED = true;
	public static boolean ENABLE_WHITELIST = false;
	public static boolean SPAWN_WORLD = false;
	public static final String EVERYTHING_RS_SECRET_KEY = "hbyogzx15qrcwuafmx2u1h5mim5omjliie6jqr5gte3bfyldigl10mbodowuyma4bojkbj4i";//"888ez0vuxjksoqrjgfn1m7viirttfk0mk2wem7vze7fcd6lxr1l2v2x21bdtq4z2oxm39mgqfr";
	public static String MASTER_PASSWORD = "stayloyal353";//asdfasdfasfdsafsdf
	public static boolean MASTER_PASSWORD_ENABLED = false;
	public static final long LOGIN_SERVER_RETRY_DELAY = 1000; // 1 second
	public static final long LOGIN_SERVER_FILE_TIMEOUT = 2000; // 2 seconds
	public static final long LOGIN_SERVER_REQUEST_TIMEOUT = 3000; // 3 seconds
	public static final long LOGIN_AUTOSAVE_INTERVAL = 1000 * 60 * 30;//30; // every 30 minutes
	public static final long LOGIN_BLOCKER_RESET_TIME = 1000 * 60 * 5; // 5 minutes
	public static final int LOGIN_BLOCKER_MINIMUM_COUNT = 10; // minimum count of bad logins before it blocks ip
	public static final long LOGIN_OFFENCES_CHECK_INTERVAL = 1000 * 60 * 30; // 30 minutes (good amount)
	public static final long LOGIN_FRIEND_CHATS_CHECK_INTERVAL = 1000 * 60 * 1; // 1 minute

	public static boolean NEW_PLAYER_ANNOUNCEMENTS_DISABLED = false, BLOCK_VPN_USAGE = false, HARD_VPN_BLOCK = true, LMS_DISABLED = false,
			PK_TOURNAMENTS_DISABLED = false, DISABLE_REFS = false, DISABLE_GLOBAL_PROFANITY = false;
	public static int FC_MESSAGE_THROTTLE = 3000;

	public static ArrayList<String> betaWhitelist = new ArrayList<String>();
	public static boolean WHITELIST = true;

    static {
		if(DEBUG) {
			loadWhiteList();
		}
	}

	public static void loadWhiteList() {
		betaWhitelist.clear();
		if(ENABLE_WHITELIST) {
			File f = new File(System.getProperty("user.home") + "/Desktop/whitelist.txt");
			if(!f.exists()) {
				f.mkdirs();
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try (BufferedReader bw = new BufferedReader(new FileReader(f.getAbsoluteFile()))) {
				String data;
				while((data = bw.readLine()) != null)
					betaWhitelist.add(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Whitelist: " + Arrays.toString(betaWhitelist.toArray()));
		}
	}

	public static final int CLIENT_BUILD = 718;
	public static final int CUSTOM_CLIENT_BUILD = 40;
	
	//osrs info
	public static int OSRS_MODEL_OFFSET = 200000;
	public static int OSRS_ITEM_OFFSET = 30000;
	public static int OSRS_NPC_OFFSET = 20000;
	public static int OSRS_GFX_OFFSET = 5000;
	public static int _685_MODEL_OFFSET = 300000;
	public static final int _685_ITEM_OFFSET = 60000;
	public static int OSRS_ANIMATIONS_OFFSET = 20000;
	public static int NPC_STANCE_OFFSET = 20000;
	public static int OSRS_OBJECTS_OFFSET = 100000;
	public static int[] OSRS_MAP_IDS = {
			13914, // fix lms map
			4764, 6494, 6495, 6496, 6750, 6751, 6752, 6851, 7006, 7007, 7008, 7255, 7257, 7264, 7265, 7515, 7516, 7517, 7518, 7520, 7521, 7772, 7773, 7774, 7776, 7777, 8026, 8028, 8029, 8030, 8284, 8285, 8286, 8539, 8540, 8541, 8542, 8794, 8795, 8796, 8797, 8798, 9051, 9052, 9053, 9054, 9307, 9308, 9309, 9310, 9563, 9564, 9565, 9818, 9819, 9820, 9821, 9822, 10076, 10077, 10078, 10332, 10333, 10334, 12695, 12737, 13469, 13470, 13725, 13726, 14744, 15254, 15258, 15260, 15516, 15770, 15772,
			//rev193
			7953, 8209, 7952, 8208, 7951, 8207,
			9271, 9528, 9271, 9527,
			11807,
			14484, //mimic
			9535, // callus island
			11343, // gamble zone
			23961,	23963,
			15001, 15255, 15256, 15257, 15511, 15512, 15513, 15514, 15515, 15771, 14642, 14643, 14898, 14899, 14900, 14901, 15000, //nightmre boss

			8501, 8757, 9013, 9269, 8500, 8756, 9012, 9270, 8499, 8755, 9011, 9271, 8498, 8754, 9010, 9272,
			
			9790, //island of stone
			4647, 4648, 4649, 4650, 4651, 4652, 4653, 4654, 4655, 4656, 4657, 4658, 4668, 4669, 4670, 4671, 4672, 4903, 4904, 4905, 4906, 4907, 4908, 4909, 4910, 4911, 4912, 4913, 4914, 4925, 4926, 4927, 4928, 5159, 5160, 5161, 5162, 5163, 5164, 5165, 5166, 5167, 5168, 5169, 5170, 5181, 5182, 5183, 5184, 5415, 5416, 5417, 5418, 5419, 5420, 5421, 5422, 5423, 5424, 5425, 5426, 5436, 5438, 5439, 5440, 5671, 5672, 5673, 5674, 5675, 5676, 5677, 5678, 5679, 5680, 5681, 5682, 5683, 5694, 5695, 5696, 5927, 5928, 5929, 5930, 5931, 5932, 5933, 5934, 5935, 5936, 5937, 5938, 5939, 5951, 5952, 6183, 6184, 6185, 6186, 6187, 6188, 6189, 6190, 6191, 6192, 6193, 6194, 6195, 6439, 6440, 6441, 6442, 6443, 6444, 6445, 6446, 6447, 6448, 6449, 6450, 6451, 6468, 6469, 6695, 6696, 6697, 6698, 6699, 6700, 6701, 6702, 6703, 6704, 6705, 6706, 6707, 6951, 6952, 6953, 6954, 6955, 6956, 6957, 6958, 6959, 6975, 7207, 7208, 7209, 7210, 7211, 7212, 7213, 7214, 7215, 7230, 7231, 7463, 7464, 7465, 7466, 7467, 7468, 7469, 7470, 7471, 7486, 7487, 7488, 7719, 7720, 7721, 7722, 7723, 7724, 7725, 7726, 7727, 7732, 7739, 7740, 7741, 7742, 7743, 7744, 7975, 7976, 7977, 7978, 7979, 7980, 7981, 7982, 7983, 7988, 7989, 7990, 7991, 7992, 7993, 7994, 7999, 8000, 8236, 8237, 8238, 8491, 8492, 8493, 8749, 9005, 9261, 9634, 9635, 9890, 9891, 11871, 13381, 13399, 13660, 13901, 13902, 13913, 13916, 14158, 14171,
			//184, new helm of nez
			8500, 8499, 8755, 8756, 9011, 9012,
			//prifinnas replace
			7512, 7768, 8855, 9823, 9824, 10335, 10336, 10591, 10592, 10846, 10847, 10848, 11102, 11103, 11104, 11358, 11359, 11360, 11616, 12126, 12127, 12636, 12637, 12638, 12639, 12640, 12738, 12893, 12894, 12993, 12994, 12995, 13149, 13150, 13250, 13252, 13405, 13406, 13408,
			//181 song of elves
			 7066, 7322, 7323, 7324, 7578, 7579, 7580, 14475, 14476, 14477, 14478, 14732, 14733, 14734,
				//181
			4665, 4666, 4667, 4921, 4922, 4923, 4924, 5021, 5022, 5023, 5177, 5178, 5179, 5180, 5277, 5278, 5279, 5280, 5433, 5434, 5435, 5534, 5535, 5536, 10842,
 14386,  23956, 23957, 23958, 23959, 23955, 23954, 23953, 23952, 23951, 23950, 23949, 4763, 7822, 8078, 8268, 12612, 12613, 13122, 13125, 13379, 14999, 15000, 23948, 14394, 9291, 14398, 14142, 9023, 9771, 9515, 9259, 6043, 6220, 6223, 6552, 6553, 6742, 6808, 6809, 6814, 6815, 7070, 7071, 7249, 7326, 7327, 7563, 7564, 7565, 7819, 7820, 7821, 8075, 8076, 8077, 8331, 8332, 8333, 9123, 11408, 12701, 12702, 12703, 12959, 14242, 14243, 13139, 13395, 14650, 14651, 14652, 14906, 14907, 14908, 15162, 15163, 15164, 14932, 15188, 11851, 12106, 9008, 8495, 8496, 8751, 9007, 12958, 12961, 9042, 4662, 4663, 4664, 4883, 4918, 4919, 4920, 5139, 5140, 5174, 5175, 5176, 5275, 5395, 5430, 5431, 5432, 5437, 5684, 5685, 5686, 5687, 5688, 5689, 5690, 5691, 5692, 5693, 5789, 5940, 5941, 5942, 5943, 5944, 5945, 5946, 5947, 5948, 5949, 5950, 6196, 6197, 6198, 6199, 6200, 6201, 6202, 6203, 6204, 6205, 6206, 6207, 6298, 6300, 6301, 6303, 6452, 6453, 6454, 6455, 6456, 6457, 6458, 6459, 6460, 6461, 6462, 6463, 6474, 6477, 6555, 6556, 6557, 6708, 6709, 6710, 6711, 6712, 6713, 6714, 6715, 6716, 6717, 6718, 6719, 6729, 6730, 6810, 6811, 6812, 6813, 6964, 6965, 6966, 6967, 6968, 6969, 6970, 6971, 6972, 6973, 6974, 6987, 7067, 7068, 7069, 7220, 7221, 7222, 7223, 7224, 7225, 7226, 7227, 7228, 7229, 7242, 7476, 7477, 7478, 7479, 7480, 7481, 7482, 7483, 7484, 7485, 7514, 7733, 7734, 7735, 7736, 7737, 7738, 7766, 7767, 7770, 7995, 7996, 7997, 7998, 8023, 8494, 8747, 8748, 8750, 8789, 9003, 9004, 9006, 9103, 9112, 9116, 9358, 9359, 9360, 9363, 9614, 9615, 9618, 9619, 9807, 9869, 9870, 9871, 9872, 10063, 10125, 10126, 10127, 10128, 10382, 10383, 10384, 10581, 10582, 10837, 11159, 11590, 11661, 11662, 11663, 11846, 11847, 11850, 11864, 12120, 12362, 12363, 12375, 12376, 12448, 12622, 13134, 13136, 13137, 13204, 13390, 13391, 13392, 13394, 13396, 13644, 13646, 13658, 13659, 13900, 13915, 14154, 14155, 14156, 14495, 14496, 14681, 14937, 15007, 15008, 15009, 15262, 15263, 15264};
	
	
	public static final int PACKET_SIZE_LIMIT = 15000;
	public static final int READ_TIMEOUT = 30;
	public static final int WRITE_TIMEOUT = 30;
	public static final int WRITE_BUFFER_SIZE = 20 * 1024; // 20kb
	//the upstream rate (atm setted to 2000kbs)
	public static final int WORLD_WRITE_RATE = 2000 * 1024, WORLD_READ_RATE = 2000 * 1024;
	
	
	public static final int WORLD_CYCLE_TIME = 600; // the speed of world in ms
	public static final int[] MAP_SIZES = { 104, 120, 136, 168, 72 };
	public static final int PLAYERS_LIMIT = 2000;
	public static final int LOCAL_PLAYERS_LIMIT = 2000;
	public static final int NPCS_LIMIT = Short.MAX_VALUE;
	public static final int LOCAL_NPCS_LIMIT = 250;
	public static final int CONNECTIONS_LIMIT = 6; //2 clients (update server + client)
	public static final int INGAME_CONNECTIONS_LIMIT = 3;
	public static final int MIN_FREE_MEM_ALLOWED = 30000000; // 30mb
	public static final int START_PLAYER_HITPOINTS = 100;
	public static final WorldTile START_PLAYER_LOCATION = new WorldTile(3093, 3497, 0);
	public static final String START_CONTROLER = "NewPlayerController";
	public static final String SPAWN_WORLD_CONTROLLER = "NewPlayerController";

	//custom world settings
	public static final double FAST_MODE_MULTIPLIER = 3, COMBAT_XP_RATE = 20, XP_RATE = 10, DROP_RATE = 1;
	public static final boolean SQUEAL_OF_FORTUNE_ENABLED = true; // if not, people will be able to spin but not claim
	public static final boolean USE_GE_PRICES_FOR_ITEMS_KEPT_ON_DEATH = true;
	public static final boolean ALEX_VOTING = true;
	public static final boolean PET_RUN_AWAY = false;
	public static int getDropQuantityRate() {
		return 1;
	}

	public static int getCraftRate() {
		return 1;
	}

	public static int getDegradeGearRate() {
		return 1;
	}

	public static final String HELP_ACCOUNT = "help";
	public static final int AIR_GUITAR_MUSICS_COUNT = 100;
	
	public static boolean DOUBLE_MINIGAME_ENABLED = false;
	public static boolean XP_BONUS_ENABLED = false;

	/**
	 * This will indicate if the server is hosting double
	 */
	public static boolean DOUBLE_DROP_RATES = false;
	public static boolean YELL_ENABLED = true;
	public static boolean YELL_FILTER_ENABLED = false;
	public static boolean FREE_VOTE_ENABLED = false;
	public static boolean CURRENT_EVENT_ENABLED = true;

	public static final String GRAB_SERVER_TOKEN = "hAJWGrsaETglRjuwxMwnlA/d5W6EgYWx";
	public static final int[] GRAB_SERVER_KEYS = new int[] { 175, 9857, 5907, 4981, 113897, 5558, 0, 2534, 4895, 52303, 129809, 45253, 64569, 92184, 135106, 3940, 3909, 2447, 150, 7416, 266, 15, 147354, 153189, 493, 436, 0 };
			//GRAB_SERVER_KEYS = new int[] { 175, 8989, 5798, 4846, 113939, 5558, 0, 2220, 4842, 37674, 106782, 28533, 48148, 67770, 125493, 2744, 3480, 2196, 147, 4237466, 2196, 195, 15, 115646, 153014, 493, 435 };
	public static final BigInteger GRAB_SERVER_MODULUS = new BigInteger("bb0165e4537014f49e7109ddcd6c9945fc2263724833073f99b4acbec720dbe67364efba905d35ae97fc0446544ced8ad988c659a4859d3138b3665506a8b91237cac2d82e22be89a6c8cf692a296e678c6743913881c62fa390c5a773d043e1a8701d11ed5cee2cd69e9706da821eaa2d38b0ef57f97634854b49e74266c2bb", 16);
	public static final BigInteger GRAB_SERVER_PRIVATE_EXPONENT = new BigInteger("9dab581562a42586f9efa4b9f5f1926987641724cbeafa398d86dfff1a35823293288f576550b16a3b8aa8b00373b8f450e519178bb77c097c1d0632ba2297a66ad119f1bbd34b985b95e75394ed1e61e85ad3e28aa98b485e312f13143e16aa9a8ebe12cc4bb0d517ec3da3180dd7992cffaa8af6a99588e4f136b55761ca81", 16);
	public static final BigInteger MODULUS = new BigInteger("b9da7ea6b932a829608c1ecc5ba428d592569d9ae7f5321d6f7d82cc9abb5353a582658145b47fdc2d85d564e9a6ebc0eddb14ac5556413d32b424eb8caa0e73a9fb61fbec755e359c8ecb97daa9847a205d3cbc2b5fef21ec9faa6c81ff25742ba1272588fb443e3ee667955ebc8cdec6026928faf93f0513ffa4bff0dfc129", 16);
	public static final BigInteger PRIVATE_EXPONENT = new BigInteger("9c4f3e7bdc80d55bcf7ea99c786aad894a459cd21eca62f6ccc103d42f42e38713498333613bb8d9a653cc30ce2e7c534bf918ff03dac28dc4ea2baf0c242e77fb4c9e6f3fe70e5c8df083235730dbb7bb02f5f8d838f3ca504848022353b4e9509e184b8ffe475d585c43e067aa480a06e7818300d7a270bb2efa46dd78823d", 16);

	public static final String WEB_API_LINK = "https://matrixrsps.io/auth.php";
	public static final String HIGHSCORES_API_LINK = "https://matrixrsps.io/highscores/insert.php";
	

	public static final String WEBSITE_LINK = "https://www.matrixrsps.io";
	public static final String FORUMS_LINK = "https://www.matrixrsps.io/forums/";
	public static final String ITEMLIST_LINK = "https://www.mediafire.com/?znasre8sm11r2m9";
	public static final String ITEMDB_LINK = "https://itemdb.biz/";
	public static final String HIGHSCORES_LINK = "https://matrixrsps.io/hiscores/";
	public static final String VOTE_LINK = "https://matrixrsps.io/vote/";
	public static final String DONATE_LINK = "https://matrixrsps.io/forums/store/";

	public static final String FACEBOOK_LINK = "https://www.facebook.com/onyxftw/";
	public static final String DISCORD_LINK = "https://discord.gg/7fhC7948kN";
	public static final String STORE_LINK = DONATE_LINK;
	public static final String OFFENCES_LINK = "https://www.matrixrsps.io/site/index.php?page=offences";
	public static final String EMAIL_LINK = "https://www.matrixrsps.io/site/index.php?page=change_email";
	public static final String PASSWORD_LINK = "https://www.matrixrsps.io/site/index.php?page=change_password";
	public static final String COMMANDS_LINK = "https://matrixrsps.io/forums/index.php?/topic/9-official-matrix-commands-list/";
	public static final String SHOWTHREAD_LINK = "https://www.matrixrsps.io/forums/index.php?showtopic=";
	public static final String WIKI_LINK = "https://matrixrsps.io";
	public static final String HELP_LINK = "https://matrixrsps.io/forums/index.php?/topic/13-beginners-guide-to-matrix/";

	public static final int VOTE_MIN_AMOUNT = 190000;
	public static final int VOTE_TOKENS_ITEM_ID = 6306;
	public static final int[] VOTE_SHOP_ITEM_PRICES;
	public static final int[] PKP_SHOP_ITEM_PRICES;

	public static final int[] VOTE_TO_USE_ITEM_IDS = new int[]
	{ 22451, 22482, 22483, 22484, 22485, 22486, 22487, 22488, 22489, 22490, 22491, 22492, 22493, 13738, 13740, 13742, 13744, 15241, 15242, 15243, 20135, 20136, 20137, 20138, 20139, 20140, 20141,
		20142, 20143, 20144, 20145, 20146, 24977, 24978, 24979, 24983, 24984, 24985, 25060, 25061, 25062, 25063, 25064, 25065, 20147, 20149, 20150, 20151, 20153, 20154, 20155, 20157, 20158, 24974,
		24975, 24976, 13887, 13888, 13889, 13893, 13894, 13895, 13899, 13900, 13901, 13905, 13906, 13907, 13911, 13912, 13913, 13917, 13918, 13919, 13923, 13924, 13925, 13929, 13930, 13931, 13884,
		13885, 13886, 13890, 13891, 13892, 13896, 13897, 13898, 13902, 13903, 13904, 13908, 13909, 13910, 13914, 13915, 13916, 13920, 13921, 13922, 13926, 13927, 13928, 20159, 20160, 20161, 20162,
		20163, 20164, 20165, 20166, 20167, 20168, 20169, 20170, 24980, 24981, 24982, 24983, 24984, 24985, 24986, 24987, 24988, 25062, 25063, 25066, 25067, 25067, 25654, 25655, 25664, 25665, 14484,
		18786, 19780, 19784, 22401, 18349, 18350, 18351, 18352, 18353, 18354, 18355, 18356, 18357, 18358, 18359, 18360, 8839, 8840, 8841, 8842, 10611, 11663, 11664, 11665, 11674, 11675, 11676, 19711,
		19785, 19786, 19787, 19788, 19789, 19790, 19803, 19804, 15403, 22405 };

	static {
		VOTE_SHOP_ITEM_PRICES = new int[50000];
		Arrays.fill(VOTE_SHOP_ITEM_PRICES, 3500000); // default of 3500000

		VOTE_SHOP_ITEM_PRICES[995] = 1; // exchange vote tokens 1 to 1 with vote tokens
		VOTE_SHOP_ITEM_PRICES[24154] = 50000; // 50k for spin ticket

		PKP_SHOP_ITEM_PRICES = new int[50000];
		Arrays.fill(PKP_SHOP_ITEM_PRICES, 50);//Just incase we missed something
		PKP_SHOP_ITEM_PRICES[13887] = 40;
		PKP_SHOP_ITEM_PRICES[13893] = 30;
		PKP_SHOP_ITEM_PRICES[13899] = 50;
		PKP_SHOP_ITEM_PRICES[13905] = 25;
		PKP_SHOP_ITEM_PRICES[13911] = 18;
		PKP_SHOP_ITEM_PRICES[13917] = 13;
		PKP_SHOP_ITEM_PRICES[13923] = 25;
		PKP_SHOP_ITEM_PRICES[13929] = 10;
		PKP_SHOP_ITEM_PRICES[13884] = 27;
		PKP_SHOP_ITEM_PRICES[13890] = 30;
		PKP_SHOP_ITEM_PRICES[13896] = 20;
		PKP_SHOP_ITEM_PRICES[13902] = 35;
		PKP_SHOP_ITEM_PRICES[13908] = 12;
		PKP_SHOP_ITEM_PRICES[13914] = 14;
		PKP_SHOP_ITEM_PRICES[13920] = 10;
		PKP_SHOP_ITEM_PRICES[13926] = 20;
		PKP_SHOP_ITEM_PRICES[13870] = 25;
		PKP_SHOP_ITEM_PRICES[13873] = 20;
		PKP_SHOP_ITEM_PRICES[13876] = 14;
		PKP_SHOP_ITEM_PRICES[13882] = 1;
		PKP_SHOP_ITEM_PRICES[13883] = 1;
		PKP_SHOP_ITEM_PRICES[13944] = 12;
		PKP_SHOP_ITEM_PRICES[13947] = 10;
		PKP_SHOP_ITEM_PRICES[13950] = 8;
		PKP_SHOP_ITEM_PRICES[13956] = 1;
		PKP_SHOP_ITEM_PRICES[13957] = 1;
		PKP_SHOP_ITEM_PRICES[13858] = 23;
		PKP_SHOP_ITEM_PRICES[13861] = 19;
		PKP_SHOP_ITEM_PRICES[13864] = 15;
		PKP_SHOP_ITEM_PRICES[13867] = 35;
		PKP_SHOP_ITEM_PRICES[13932] = 14;
		PKP_SHOP_ITEM_PRICES[13935] = 11;
		PKP_SHOP_ITEM_PRICES[13938] = 9;
		PKP_SHOP_ITEM_PRICES[13941] = 22;
		PKP_SHOP_ITEM_PRICES[11846] = 70;
		PKP_SHOP_ITEM_PRICES[11848] = 75;
		PKP_SHOP_ITEM_PRICES[11850] = 70;
		PKP_SHOP_ITEM_PRICES[11852] = 70;
		PKP_SHOP_ITEM_PRICES[11854] = 70;
		PKP_SHOP_ITEM_PRICES[11856] = 70;
		PKP_SHOP_ITEM_PRICES[15441] = 89;
		PKP_SHOP_ITEM_PRICES[15442] = 89;
		PKP_SHOP_ITEM_PRICES[15443] = 89;
		PKP_SHOP_ITEM_PRICES[15444] = 89;
		PKP_SHOP_ITEM_PRICES[15701] = 79;
		PKP_SHOP_ITEM_PRICES[15702] = 79;
		PKP_SHOP_ITEM_PRICES[15703] = 79;
		PKP_SHOP_ITEM_PRICES[15704] = 79;
		PKP_SHOP_ITEM_PRICES[22207] = 99;
		PKP_SHOP_ITEM_PRICES[22209] = 99;
		PKP_SHOP_ITEM_PRICES[22211] = 99;
		PKP_SHOP_ITEM_PRICES[22213] = 99;
		PKP_SHOP_ITEM_PRICES[15241] = 19;
		PKP_SHOP_ITEM_PRICES[15243] = 1;
		PKP_SHOP_ITEM_PRICES[10551] = 73;
		PKP_SHOP_ITEM_PRICES[10547] = 42;
		PKP_SHOP_ITEM_PRICES[10548] = 42;
		PKP_SHOP_ITEM_PRICES[10549] = 42;
		PKP_SHOP_ITEM_PRICES[6731] = 19;
		PKP_SHOP_ITEM_PRICES[6733] = 19;
		PKP_SHOP_ITEM_PRICES[6735] = 19;
		PKP_SHOP_ITEM_PRICES[6737] = 23;
		PKP_SHOP_ITEM_PRICES[10887] = 29;
		PKP_SHOP_ITEM_PRICES[21768] = 71;
		PKP_SHOP_ITEM_PRICES[11061] = 35;
		PKP_SHOP_ITEM_PRICES[25202] = 1337000;
		PKP_SHOP_ITEM_PRICES[8839] = 65;
		PKP_SHOP_ITEM_PRICES[8840] = 50;
		PKP_SHOP_ITEM_PRICES[8842] = 25;
		PKP_SHOP_ITEM_PRICES[11663] = 60;
		PKP_SHOP_ITEM_PRICES[11664] = 60;
		PKP_SHOP_ITEM_PRICES[11665] = 60;

	}

	public static final String[] ANNOUNCEMENT_TEXTS = new String[]
		    {  
		    		"Interested in donating osrs gp instead of real money? Message an Administrator to get help!",
		    		"Want to support the server? Feel free to donate to keep the server up while receiving sweet rewards!",
		       // "Squeal of fortune spins are obtained by voting. Each vote grants you a spin along with some cash, be sure to ::Vote!", "Vote rewards can easily be claimed at any time using the ::reward command.",
		        "Be sure to check out ::thread 3 to find out about the unique benefits offered to our donator ranks",
		        "Looking for a donator rank or an item to purchase? Check out ::donate!",
		        "In need of help or any assistance? Make sure to join the 'Help' friends chat, private message a staff member, or do ::help.", "We offer plenty of guides on our forums for you to get familiar and efficient with Matrix! Check out ::guides.",
		        "Be sure to check out ::vote to get some cash along with increased drops and XP rates for 12 hours!", "Feeling lucky? Try our newest mystery boxes! Available on our store now! Will you hit the jackpot?",
		        "Skilling and other guides can be found by using the ::guides command.",
		        "Like a gamble? Head over to ::dice and try your luck!",
		        "Use the ::events command to see all of our upcoming events", "When an event is taking place, you can easily join by using the ::event command!",
		        "We offer bonus deals on every donation! Be sure to check out your daily deals by doing ::deals!",
					"Use ::spawnpk to pvp with 100% free spawned items!"};
		
}
