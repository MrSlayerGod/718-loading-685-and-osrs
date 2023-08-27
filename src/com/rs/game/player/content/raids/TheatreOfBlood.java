package com.rs.game.player.content.raids;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.MapInstance;
import com.rs.game.map.MapBuilder;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.theatreOfBlood.PestilentBloat;
import com.rs.game.npc.theatreOfBlood.TOBBoss;
import com.rs.game.npc.theatreOfBlood.Xarpus;
import com.rs.game.npc.theatreOfBlood.maiden.Maiden;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasGenerator;
import com.rs.game.npc.theatreOfBlood.nycolas.NycolasPillar;
import com.rs.game.npc.theatreOfBlood.sotetseg.Sotetseg;
import com.rs.game.npc.theatreOfBlood.verzikVitur.VerzikVitur;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.FriendsChat;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.collectionlog.CategoryType;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.content.pet.LuckyPets.LuckyPet;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.TheatreOfBloodController;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author Alex  (Dragonkk)
 *
 */
public class TheatreOfBlood extends MapInstance {

	
	private static final int[][] ENTRANCE_TILES = new int[][] {
		{147, 44, 0},
		{58, 32, 0},
		{160, 123, 0},
		{91, 104, 0},
		{162, 151, 1},
		{96, 143, 0},
		{37, 147, 0},
		
	}; //tp here when entering
	
	private static final int[][] CHEST_TILES = new int[][] {
		{5, 33, 0, 3},
		{78, 69, 0, 3},
		
	};
	
	private static final int[][] JAIL_TILES = new int[][] {
		{94, 17, 0},
		{31, 20, 0},
		{151, 94, 0},
		{70, 90, 0},
		{149, 163, 1},
		{89, 165, 0},
		
	}; //tp here when jailed make exeption for wave7
	private static final int[][] OUT_BARRIER_TILES = new int[][] {
		{114, 30, 0},
		{41, 31, 0},
		{160, 96, 0},
		{79, 82, 0},
		{162, 154, 1},
		{96, 157, 0},
	}; //tped here if win wave and jailed
	
	private static final String[] WAVE_MUSICS = {"The Maiden's Sorrow", "Welcome to my Nightmare", "Dance of the Nylocas", "The Dark Beast Sotetseg", "Predator Xarpus", "It's not over 'til...", "The Curtain Closes"};  
	private static final String[] WAVE_COMBAT_MUSICS = {"The Maiden's Anger", "The Nightmare Continues", "Arachnids of Vampyrium", "Power of the Shadow Realm", "Last King of the Yarasa", "The Fat Lady Sings"};  
	
	private static final String[] WAVE_NAMES = { //0
			"The Maiden of Sugadinti", //1
			"The Pestilent Bloat", //2
			"The Nylocas", //3
			"Sotetseg", //4
			"Xarpus", //5
			"The Final Challenge", //6
			"Verzik Vitur's Vault", //7
	};
	
	//id, min, max
	private static final int[] COMMON_REWARDS = {
			246, 27, 44, //wine of zammy
			1374, 2, 3,//rune bat
			1128, 2, 3, //rune platebody
			1114, 2, 3, //rune chain
			5289, 1, 3, //palm tree seed
			5315, 1, 2, //yew seed
			5316, 1, 6, //magic seed
			1776, 167, 374, //molten glass
			560, 306, 543, //death rune
			565, 414, 567, //blood rune
			1939, 367, 530, //swamp tar
			3139, 42, 54, //potato cactus
			1392, 11, 17, //battlestaff
			450, 94, 140, //adamant ore
			452, 43, 128, //rune ore
			454, 374, 395, //coal
			445, 250, 348, //gold ore
			208, 23, 27, //grimmy rannar
			3050, 28, 39, //grimmy toadflax
			210, 26, 55, //grimmy irit
			212, 19, 35, //grimy avantoe
			218, 19, 48, //grimy dwarf
			3052, 20, 34, //grimmy snapdragon
			216, 31, 49, //grimy canda
			220, 15, 43 //grimy torstol
			
	};

	public static final Integer[] UNIQUE_REWARDS = {52477, 52324, 52323, 52325, 52326, 52327, 52328};
	
	public static final int LEAVE = 0, TELEPORT = 1, LOGOUT = 2;
	
	private static final Map<Object, TheatreOfBlood> cachedRaid = Collections.synchronizedMap(new HashMap<Object, TheatreOfBlood>());
	private static final AtomicLong keyMaker = new AtomicLong(Utils.currentTimeMillis());
	public static final WorldTile OUTSIDE = new WorldTile(3675, 3219, 0);
	
	private String fcOwner;
	private int size;
	private List<Player> team;
	private String key;
	private boolean[] waveComplete;
	private List<String> enteredBoss;
	private List<String> jailed;
	private Map<String, ItemsContainer<Item>> lootChest;
	private int deathCount;
	private Map<String, Integer> deaths;
	private Map<String, Integer> damageDealt;
	
	private DestroyTimer destroyTimer;
    private NycolasGenerator nycolasGenerator;
    private Sotetseg sotetseg;
    private long fightStart;
    private long waveStart;
    
	public static TheatreOfBlood getRaid(String key) {
		return cachedRaid.get(key);
	}
	
	public static void enter(Player player) {
		if (player.getPrayer().isAncientCurses()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "Curse prayer's are restricted in this Theatre! Switch your prayer book to face the challenge!");
			return;
		}
		if (player.getFamiliar() != null || Summoning.hasPouch(player)) {
			player.getDialogueManager().startDialogue("SimpleMessage","You don't want your friends to be eaten. You are not allowed to take familiars onto raids.");
			return;
		}
		FriendsChat chat = player.getCurrentFriendsChat();
		if (chat == null) {
			player.getDialogueManager().startDialogue("SimpleMessage","You need to be part of a friend chat in order to enter.");
			player.getInterfaceManager().openGameTab(10);
			return;
		}
		if (!chat.getChannel().equalsIgnoreCase(player.getUsername())) {
			Player owner = null;
			for (Player p2 : chat.getLocalMembers()) {
				if (chat.getChannel().equalsIgnoreCase(p2.getUsername())) {
					owner = p2;
					break;
				}
			}
			if (owner == null) {
				player.getDialogueManager().startDialogue("SimpleMessage","Could not find leader.");
				return;
			}
			Controller controller = owner.getControlerManager().getControler();
			if (!(controller instanceof TheatreOfBloodController)) {
				player.getDialogueManager().startDialogue("SimpleMessage","Your leader, "+owner.getName()+", must go first.");
				return;
			}
			TheatreOfBlood raid = ((TheatreOfBloodController)controller).getRaid();
			if (raid.hasStarted()) {
				player.getDialogueManager().startDialogue("SimpleMessage","This raid has already started.");
				return;
			}
			if (raid.team.size() >= 5) {
				player.getDialogueManager().startDialogue("SimpleMessage","This raid is full.");
				return;
			}
			raid.add(player, false);
		} else 
			new TheatreOfBlood(player, chat);
	}
	public TheatreOfBlood(Player leader, FriendsChat chat) {
		super(0, 0, 3, 3);
		leader.lock();
		leader.stopAll();
		fcOwner = leader.getUsername();
		team = new CopyOnWriteArrayList<Player>();
		key = fcOwner + "_" + keyMaker.getAndIncrement();
		waveComplete = new boolean[6];
		enteredBoss = new LinkedList<String>();
		jailed = new LinkedList<String>();
		lootChest = new HashMap<String, ItemsContainer<Item>>();
		deaths = new HashMap<String, Integer>();
		damageDealt = new HashMap<String, Integer>();
		cachedRaid.put(key, this);
		load(() -> {
			for (int[] chest : CHEST_TILES)
				World.spawnObject(new WorldObject(133016, 10, chest[3], getTile(chest[0], chest[1], chest[2])));
			World.spawnObject(new WorldObject(132992, 10, 3, getTile(26, 165, 0)));
			add(leader, false);
			for (Player player : chat.getLocalMembers()) 
				player.getPackets().sendGameMessage("<col=ff66cc>"+(player == leader ? "Inviting party" : (leader.getName()+" has entered the Theatre of Blood. Step inside to join him....")));
		});
	}
	
	public void add(Player player, boolean login) {
		player.lock(2);
		endDestroyTimer();
		if (!login) {
			player.getControlerManager().startControler("TheatreOfBloodController", key, getTile(147, 44, 0));
			enterRoom(player, 0, false); //0
		}
		team.add(player);
		player.setForceMultiArea(true);
		player.setLargeSceneView(true);
		if (login) 
			jail(player, true);
	}
	
	//0 - leave, 1 - teleport, 2 - logout
	public void remove(Player player, int type) {
		team.remove(player);
		if (type == LOGOUT)
			player.setLocation(OUTSIDE);
		else {
			if (type == LEAVE)
				player.useStairs(-1, OUTSIDE, 0, 2);
			player.getInterfaceManager().removeOverlay(true);
			player.setForceMultiArea(false);
			player.setLargeSceneView(false);
			player.getPackets().sendStopCameraShake();
			player.getControlerManager().removeControlerWithoutCheck();
		}
		if (team.isEmpty()) {
			if (type == LOGOUT && destroyTimer == null && hasStarted() && enteredBoss.isEmpty()) 
				setDestroyTimer();
			else
				destroy();
		} else if (type == LOGOUT) {
			yell("<col=FF0040>"+player.getName()+"</col> has disconnected.");
			checkJail();
		}
		player.getInventory().deleteItem(52516, Integer.MAX_VALUE);
		if (player.getEquipment().getWeaponId() == 52516) {
			player.getEquipment().getItems().set(Equipment.SLOT_WEAPON, null);
			player.getEquipment().refresh(Equipment.SLOT_WEAPON);
			player.getAppearence().generateAppearenceData();
		}
	}
	
	private void destroy() {
		endNycolasGenerator();
		endDestroyTimer();
		cachedRaid.remove(key, this);
		destroy(null);
	}
	
	public int getWave(Player player) {
		/*int mapID = player.getRegionId();
		for (int i = 0; i < WAVE_MAP_IDS.length; i++)
			if (WAVE_MAP_IDS[i] == mapID)
				return i;*/
		int roomX = (player.getChunkX() - getInstancePos()[0]) / 8;
		int roomY = (player.getChunkY() - getInstancePos()[1]) / 8;
		if (roomX == 2 && roomY == 0 || (roomX == 1 && roomY == 0))
			return 0;
		if (roomX == 0 && roomY == 0)
			return 1;
		if (roomX == 2 && roomY == 1)
			return 2;
		if ((roomX == 1 && roomY == 1) || (roomX == 0 && roomY == 1))
			return 3;
		if (roomX == 2 && roomY == 2)
			return 4;
		if (roomX == 1 && roomY == 2)
			return 5;
		if (roomX == 0 && roomY == 2)
			return 6;
		return 0;
	}
	
	
	@Override
	protected void buildMap() {
		//lobby
		MapBuilder.copyAllPlanesMap(744, 1184, getInstancePos()[0]+16, getInstancePos()[1], 8);
		//1st boss
		MapBuilder.copyAllPlanesMap(392, 552, getInstancePos()[0]+8, getInstancePos()[1], 8);
		//2nd boss
		MapBuilder.copyAllPlanesMap(408, 552, getInstancePos()[0], getInstancePos()[1], 8);
		//3rd boss
		MapBuilder.copyAllPlanesMap(408, 528, getInstancePos()[0]+16, getInstancePos()[1]+8, 8);
		//4th boss
		MapBuilder.copyAllPlanesMap(744, 1192, getInstancePos()[0]+8, getInstancePos()[1]+8, 8);
		//hidden dimension
		MapBuilder.copyAllPlanesMap(416, 536, getInstancePos()[0], getInstancePos()[1]+8, 8);
		//5th boss
		MapBuilder.copyAllPlanesMap(392, 544, getInstancePos()[0]+16, getInstancePos()[1]+16, 8);
		//6th boss
		MapBuilder.copyAllPlanesMap(744, 1200, getInstancePos()[0]+8, getInstancePos()[1]+16, 8);
		//treasure room
		MapBuilder.copyAllPlanesMap(744, 1208, getInstancePos()[0], getInstancePos()[1]+16, 8);
	}
	
	public void start() {
		size = team.size();
		yell("<col=ff66cc>The raid has begun!");
		//set bosses here
		new Maiden(this);
		new PestilentBloat(this); //bloat
		setNycolasGenerator(new NycolasPillar(this, getTile(164, 93)), new NycolasPillar(this, getTile(153, 93)), new NycolasPillar(this, getTile(164, 82)), new NycolasPillar(this, getTile(153, 82))); 
		sotetseg = new Sotetseg(this); //sotetseg
		new Xarpus(this);  //xarpus
		new VerzikVitur(this);
	}
	
	private void yell(String message) {
		for (Player player : team)
			player.getPackets().sendGameMessage(message);
	}
	
	public boolean hasStarted() {
		return size > 0;
	}
	
	public String getFCOwner() {
		return fcOwner;
	}
	
	public void clearWave(Player killer) {
		clearWave(getWave(killer));
	}
	
	public void clearWave(int wave) {
		waveComplete[wave] = true;
		enteredBoss.clear();
		for (Player player : team) {
			String text;
			if (waveStart > 0) {
				long time = Utils.currentTimeMillis() - waveStart;
				text = String.format("%d:%02d",
		                TimeUnit.MILLISECONDS.toMinutes(time),
		                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
			} else
				text = "N/A";
			player.getPackets().sendGameMessage("Wave "+WAVE_NAMES[wave]+"' complete! Duration: <col=FF0040>"+text);
			player.heal(player.getMaxHitpoints());
			player.getPrayer().reset();
			player.getPoison().reset();
			player.getCombatDefinitions().resetSpecialAttack();
			playMusic(player);
			if (wave == 5 && getDamage(player) >= 10000) {
				NPCKillLog.addKill(player, "Theatre of Blood", fightStart == 0 ? 0 : (Utils.currentTimeMillis() - fightStart), false); //exception
				if (player.getSlayerManager().getBossTask() != null && player.getSlayerManager().getBossTask().equalsIgnoreCase("Theatre of Blood"))
					player.getSlayerManager().addBossKill(getDamage(player));
			}
		}
		if (wave == 1 || wave == 3) 
			for (Player player : team) 
				player.getPackets().sendGameMessage("The Vampyres are impressed by your prowess. <col=FF0040>Check the chest</col> to see what they've sent you.");
		for (Player player : team) {
			if (jailed.contains(player.getUsername())) {
				player.getPackets().sendGameMessage("The wave has ended. So you're released.");
				player.useStairs(-1, getTile(OUT_BARRIER_TILES[wave][0], OUT_BARRIER_TILES[wave][1], OUT_BARRIER_TILES[wave][2]), 0, 2);
			}
		}
		jailed.clear();
		if (wave == 5) {
			World.removeObject(World.getObjectWithId(getTile(94, 164), 132736));
			World.spawnObject(new WorldObject(132738, 10, 0, getTile(95, 164)));
		}
	}
	
	public boolean canPass(Player player, int wave) {
		if (!waveComplete[wave]) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 28324, "You must stay and fight!");
			player.getPackets().sendGameMessage("You must stay and fight!");
			return false;
		}
		return true;
	}
	
	public void enterRoom(Player player, boolean quickEnter) {
		enterRoom(player, getWave(player)+1, quickEnter);
	}
	
	public void enterRoom(Player player, int wave, boolean quickEnter) {
		if (wave > 0 && !canPass(player, wave-1)) 
			return;
		player.lock();
		if (quickEnter)
			enterRoomEnd(player, wave);
		else {
			FadingScreen.fade(player, 0, new Runnable() {
				@Override
				public void run() {
					enterRoomEnd(player, wave);
				}
			});
		}
	}
	
	private void enterRoomEnd(Player player, int wave) {
		if (wave < 6) {
			for (Player p2 : team) {
				int p2Wave = getWave(p2);
				if (p2Wave < wave && p2 != player)
					p2.getPackets().sendGameMessage(player.getName()+" has advanced to Wave "+(wave+1)+". Join him...");
			}
		}
		if (wave == 0)
			player.getPackets().sendGameMessage("<col=FF0040>The Theatre awaits...");
		player.getPackets().sendGameMessage("<col=FF0040>"+WAVE_NAMES[wave]);
		player.useStairs(-1, getTile(ENTRANCE_TILES[wave][0], ENTRANCE_TILES[wave][1], ENTRANCE_TILES[wave][2]), 0, 2);
		if (wave == 5 && !waveComplete[wave] && !enteredBoss.isEmpty()) //boss room goes directly inside with fight already started
			enteredBoss.add(player.getUsername());
		playMusic(player, wave);
	}
	
	public void startLastBoss() {
		for (Player player : team) {
			if (getWave(player) == 5) {
				enteredBoss.add(player.getUsername());
				playMusic(player, 5);
			}
		}
	}
	
	public void passBarrier(Player player, WorldObject barrier) {
		int wave = getWave(player);
		if (enteredBoss.contains(player.getUsername()) && !canPass(player, wave))
			return;
		if (!waveComplete[wave]) {
			if (enteredBoss.isEmpty()) {
				waveStart = Utils.currentTimeMillis();
				if (fightStart == 0)
					fightStart = waveStart;
			}
			enteredBoss.add(player.getUsername());
		}
		player.lock(2);
		if (barrier.getRotation() == 1 || barrier.getRotation() == 3) 
			player.addWalkSteps(barrier.getX() + (barrier.getX() > player.getX() ? 2 : -1), barrier.getY(), 2, false);
		if (barrier.getRotation() == 0 || barrier.getRotation() == 2) 
			player.addWalkSteps(barrier.getX(), barrier.getY() + (barrier.getY() > player.getY() ? 2 : -1), 2, false);
		playMusic(player);
	}
	
	public void jail(Player player, boolean login) { //called on death and login
		/*if (jailed.contains(player.getUsername())) //already in jail
			return;*/
		int wave = getWave(player);
		if (wave >=  waveComplete.length) //died after complete or treasure, just dont tp or anything
			return;
		if (waveComplete[wave] || enteredBoss.isEmpty()) {
			if (login)
				player.useStairs(-1, getTile(OUT_BARRIER_TILES[wave][0], OUT_BARRIER_TILES[wave][1], OUT_BARRIER_TILES[wave][2]), 0, 2);
			return;
		}
		if (!login) {
			yell("<col=FF0040>"+player.getName()+"</col> has died. Death count: <col=FF0040> "+(++deathCount));
			addDeath(player);
		}
		player.getPackets().sendGameMessage("<col=FF0040>If your party survives the wave, you will respawn.");
		player.useStairs(-1, getTile(JAIL_TILES[wave][0], JAIL_TILES[wave][1], JAIL_TILES[wave][2]), 0, 2);
		if (!jailed.contains(player.getUsername()))
			jailed.add(player.getUsername());
		checkJail();
	}
	
	private void addDeath(Player player) {
		Integer deathCount = deaths.get(player.getUsername());
		deaths.put(player.getUsername(), deathCount == null ? 1 : (deathCount+1));
	}
	
	public void addDamage(Player player, int damage) {
		Integer dmg = damageDealt.get(player.getUsername());
		damageDealt.put(player.getUsername(), dmg == null ? damage : (dmg+damage));
	}
	
	public int getDamage(Player player) {
		Integer dmg = damageDealt.get(player.getUsername());
		return dmg == null ? 0 : dmg;
	}
	
	public int getDeaths(Player player) {
		Integer deathCount = deaths.get(player.getUsername());
		return deathCount == null ? 0 : deathCount;
	}
	
	public void checkJail() { //called on jail and logout
		if (allJailed()) {
			for (Player player : team) {
				player.getPackets().sendGameMessage("<col=FF0040>Your party has failed.");
				if (player.isHCIronman())
					player.revokeHC();
				remove(player, LEAVE); //autodestroys since all leave
			}
		}
	}
	
	public boolean allJailed() {
		for (Player player : team)
			if (!jailed.contains(player.getUsername()))
				return false;
		return true;
	}
	
	public boolean isJailed(Player player) {
		return jailed.contains(player.getUsername());
	}
	
	public void lootChest(Player player, boolean normal) {
		int wave = getWave(player);
		if (normal && (wave >= waveComplete.length || !waveComplete[wave])) {
			player.getPackets().sendGameMessage("The chest is empty.");
			return;
		}
		if (!normal && lootChest.get(player.getUsername()+"%"+wave) == null) {
			player.getDialogueManager().startDialogue("TOBReward", this);
			return;
		}
		openChest(player);
	}
	
	public void openChest(Player player) {
		player.getInterfaceManager().sendInterface(1284);
		player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 100, 8, 3, "Take", "Bank", "Discard", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 10, 0, 1, 2, 3);
		sendChestItems(player);
	}
	
	public static ItemsContainer<Item> getRewardCalc(Player player) {
		ItemsContainer<Item> items = new ItemsContainer<Item>(10, true);
		//unique chance 1 in 50
		int size = 1;
		int deaths = 0;
		double chance = 1d - (0.1d * deaths) + (((5d-size) * 0.2d));
		
		if (size == 1)
			chance *= 1.3;//2;
		else if (size == 2)
			chance *= 1.3;
		else if (size == 3)
			chance *= 1.15;

		int damage = 1000000;
		int roll = (int)(59d / player.getDropRateMultiplier() / Drops.NERF_DROP_RATE / chance);

		//55 before
		if (damage >= 10000 && chance > 0 && Utils.random(roll) == 0 ) {
			Item item = new Item(UNIQUE_REWARDS[Utils.random(UNIQUE_REWARDS.length)]);
			if (item.getId() != 52325 || Utils.random(3) == 0) {
				items.add(item);
				player.getCollectionLog().add(CategoryType.BOSSES, "Theatre of Blood", item);
				World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + item.getName() + "<col=ff8c38> drop!", 1);
			}

		}
		if (items.getFreeSlots() == items.getSize()) {
			for (int i = 0; i < (damage >= 10000 ? 3 : damage >= 5000 ? 2 : 1); i++) {
				int index = Utils.random(COMMON_REWARDS.length / 3);
				items.add(new Item(COMMON_REWARDS[index * 3], COMMON_REWARDS[index * 3 + 1] + Utils.random(1 + COMMON_REWARDS[index * 3 + 2] - COMMON_REWARDS[index * 3 + 1])));
			}
		}
		if (!player.getTreasureTrailsManager().hasClueScrollItem() && chance >= 0.8) {//elite clue scroll
			player.getTreasureTrailsManager().resetCurrentClue();
			items.add(new Item(19043));
		}
		if (damage >= 10000)
			LuckyPets.checkPet(player, LuckyPet.LILZIK, "Theatre of Blood");
		
		for (Item item : items.getItems()) {
			if (item != null)
				player.getInventory().addItem(item.getId(), item.getAmount());
		}
		
		return items;
		
		
	}
	public ItemsContainer<Item> getRewards(Player player) {
		int wave = getWave(player);
		String key = player.getUsername()+"%"+wave;
		ItemsContainer<Item> items = lootChest.get(key);
		if (items == null) {
			items = new ItemsContainer<Item>(10, true);
			if (wave == 6) {
				//unique chance 1 in 50
				double chance = 1d - (0.1d * getDeaths(player)) + (((5d-size) * 0.2d));
				
				if (size == 1)
					chance *= 1.3;//2;
				else if (size == 2)
					chance *= 1.3;
				else if (size == 3)
					chance *= 1.15;

				int damage = getDamage(player);

				//55 before
				int roll = (int)(59d / player.getDropRateMultiplier() / Drops.NERF_DROP_RATE / chance);
				if (damage >= 10000 && chance > 0 && Utils.random(roll) == 0 ) {
					Item item = new Item(UNIQUE_REWARDS[Utils.random(UNIQUE_REWARDS.length)]);
					if (item.getId() != 52325 || Utils.random(3) == 0) {
						items.add(item);
						Bot.sendLog(Bot.RAID_REWARDS, "[type=TOB_UNIQUE][name="+player.getUsername()+", display="+player.getDisplayName() + "][item="+ ItemConfig.forID(item.getId()).getName() + "][chance=1 in " + roll + "]");
						player.getCollectionLog().add(CategoryType.BOSSES, "Theatre of Blood", item);
						World.sendNews(player, player.getDisplayName() + " has received <col=ffff00>" + item.getName() + "<col=ff8c38> drop!", 1);
					}
				}

				if (items.getFreeSlots() == items.getSize()) {
					for (int i = 0; i < (damage >= 10000 ? 3 : damage >= 5000 ? 2 : 1); i++) {
						int index = Utils.random(COMMON_REWARDS.length / 3);
						items.add(new Item(COMMON_REWARDS[index * 3], COMMON_REWARDS[index * 3 + 1] + Utils.random(1 + COMMON_REWARDS[index * 3 + 2] - COMMON_REWARDS[index * 3 + 1])));
					}
				}
				if (!player.getTreasureTrailsManager().hasClueScrollItem() && chance >= 0.8) {//elite clue scroll
					player.getTreasureTrailsManager().resetCurrentClue();
					items.add(new Item(19043));
				}
				if (damage >= 10000)
					LuckyPets.checkPet(player, LuckyPet.LILZIK, "Theatre of Blood");
			} else {
				items.add(new Item(391, 10));
				items.add(new Item(6685, 2));
				items.add(new Item(3024, 1));
			}
			lootChest.put(key, items);
		}
		return lootChest.get(key);
	}
	
	public void sendChestItems(Player player) {
		player.getPackets().sendItems(100, getRewards(player));
	}
	
	public void playMusic(Player player) {
		playMusic(player, getWave(player));
	}
	
	public void playMusic(Player player, int wave) {
		player.getMusicsManager().playOSRSMusic(enteredBoss.contains(player.getUsername()) ? WAVE_COMBAT_MUSICS[wave] : WAVE_MUSICS[wave]);
	}
	
	private class DestroyTimer extends TimerTask {
		private long timeLeft;

		public DestroyTimer() {
			timeLeft = 600; //10min
		}

		@Override
		public void run() {
			try {
				if (timeLeft > 0) {
					timeLeft -= 5;
					return;
				}
				destroy();
			} catch (Throwable e) {
				Logger.handle(e);
			}
		}
	}
	
	public void setDestroyTimer() {
		//cant be already instanced before anyway, afterall only isntances hwen party is 0 and remvoes if party not 0
		GameExecutorManager.fastExecutor.schedule(destroyTimer = new DestroyTimer(), 1000, 5000);
	}

	public void endDestroyTimer() {
		if (destroyTimer != null) {
			destroyTimer.cancel();
			destroyTimer = null;
		}
	}
	/*
	 * ignores obstacles & distance
	 */
	public List<Player> getTargets(int wave) {
		List<Player> targets = new LinkedList<Player>();
		for (Player player : team) {
			//remove locked if gets abused
			if (!player.hasFinished() && !player.isDead() && !player.isLocked() && getWave(player) == wave && enteredBoss.contains(player.getUsername()) && !jailed.contains(player.getUsername()))
				targets.add(player);
		}
		return targets;
	}
	
	public List<Player> getTeam() {
		return team;
	}
	
	public List<Player> getTargets(NPC npc) {
		int mapID = npc.getRegionId();
		List<Player> targets = new LinkedList<Player>();
		for (Player player : team) {
			if (!player.hasFinished() && !player.isDead() && !player.isLocked() && player.getRegionId() == mapID && enteredBoss.contains(player.getUsername()) && !jailed.contains(player.getUsername()))
				targets.add(player);
		}
		return targets;
	}
	
	
	public void setHPBar(TOBBoss boss) {
		int mapID = boss.getRegionId();
		if (!enteredBoss.isEmpty()) {
			for (Player player : team) {
				if (player.getRegionId() == mapID) {
					Controller controller = player.getControlerManager().getControler();
					if (controller instanceof TheatreOfBloodController)
						((TheatreOfBloodController)controller).setHPBar(boss);
				}
			}
		}
	}
	
	public void setHPBar(int hp, int maxHP, int wave) {
		if (!enteredBoss.isEmpty()) {
			for (Player player : team) {
				if (getWave(player) == wave) {
					Controller controller = player.getControlerManager().getControler();
					if (controller instanceof TheatreOfBloodController)
						((TheatreOfBloodController)controller).setHPBar(hp, maxHP);
				}
			}
		}
	}

	public int getTeamSize() {
		return size;
	}
	
    public void setNycolasGenerator(NycolasPillar ne, NycolasPillar nw, NycolasPillar se, NycolasPillar sw) {
       // Tasks.scheduleWorldTask(nycolasGenerator = new NycolasGenerator(this, ne, nw, se, sw), 2, 1);
    	GameExecutorManager.fastExecutor.schedule(nycolasGenerator = new NycolasGenerator(this, ne, nw, se, sw), 1200, 600);
    }
    
    public void endNycolasGenerator() {
        if (nycolasGenerator != null) {
            nycolasGenerator.cancel();
            nycolasGenerator = null;
        }
    }
	
    public NycolasGenerator getNycolasGenerator() {
        return nycolasGenerator;
    }
    
    public Sotetseg getSotetseg() {
    	return sotetseg;
    }
    
  
}
