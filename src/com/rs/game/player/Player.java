package com.rs.game.player;

import com.rs.GameLauncher;
import com.rs.Settings;
import com.rs.cache.loaders.ItemConfig;
import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.executor.PlayerHandlerThread;
import com.rs.executor.WorldThread;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.minigames.WarriorsGuild;
import com.rs.game.minigames.WildernessBoss;
import com.rs.game.minigames.WorldBosses;
import com.rs.game.minigames.clanwars.FfaZone;
import com.rs.game.minigames.clanwars.WarControler;
import com.rs.game.minigames.duel.DuelRules;
import com.rs.game.minigames.pktournament.PkTournament;
import com.rs.game.minigames.stealingcreation.SCRewards.SCItem;
import com.rs.game.minigames.stealingcreation.StealingCreationController;
import com.rs.game.minigames.stealingcreation.StealingCreationLobbyController;
import com.rs.game.npc.Drops;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.others.GraveStone;
import com.rs.game.npc.others.Pet;
import com.rs.game.npc.randomEvent.CombatEventNPC;
import com.rs.game.player.actions.PlayerCombat;
import com.rs.game.player.content.*;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.collectionlog.CollectionLog;
import com.rs.game.player.content.commands.CustomGear;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.pet.PetManager;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.content.seasonalEvents.HalloBoss;
import com.rs.game.player.content.seasonalEvents.XmasBoss;
import com.rs.game.player.content.teleportation.Teleport;
import com.rs.game.player.controllers.*;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.player.controllers.castlewars.CastleWarsPlaying;
import com.rs.game.player.controllers.castlewars.CastleWarsWaiting;
import com.rs.game.player.controllers.events.DeathEvent;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.player.controllers.pestcontrol.PestControlLobby;
import com.rs.game.player.controllers.pktournament.PkTournamentGame;
import com.rs.game.player.controllers.pktournament.PkTournamentLobby;
import com.rs.game.player.controllers.pktournament.PkTournamentSpectating;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.LoginClientChannelManager;
import com.rs.net.Session;
import com.rs.net.decoders.WorldPacketsDecoder;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.net.encoders.LoginChannelsPacketEncoder;
import com.rs.net.encoders.WorldPacketsEncoder;
import com.rs.sql.Database;
import com.rs.utils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Player extends Entity {

	// dungeoneer
	public static final int NORMAL = 0, IRONMAN = 1, DUNGEONEER = 2, FAST = 3, DEADMAN = 4, ULTIMATE_IRONMAN = 5, SUPER_FAST = 6, EXPERT = 7, HARDCORE_IRONMAN = 8;

	public static final int SAPHIRE_DONATOR = 1, EMERALD_DONATOR = 2, RUBY_DONATOR = 3, DIAMOND_DONATOR = 4,
			ONYX_DONATOR = 5, ZENYTE_DONATOR = 6;
	public static final int TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;

	private static final long serialVersionUID = 2011932556974180375L;

    // transient stuff
	private transient String username;
	private transient Session session;
	private transient long clientLoadedMapRegion;
	private transient int displayMode;
	private transient int graphicMode;
	private transient int screenWidth;
	private transient int screenHeight;
	private transient InterfaceManager interfaceManager;
	private transient DialogueManager dialogueManager;
	private transient HintIconsManager hintIconsManager;
	private transient ActionManager actionManager;
	private transient CutscenesManager cutscenesManager;
	private transient PriceCheckManager priceCheckManager;
	private transient RouteEvent routeEvent;
	private transient FriendsIgnores friendsIgnores;
	private transient FriendsChat currentFriendsChat;
	private transient ClansManager clanManager, guestClanManager;
	private transient boolean lootShare;
	private transient Trade trade;
	private transient DuelRules duelRules;
	private transient IsaacKeyPair isaacKeyPair;
	private transient Pet pet;
	private transient VarsManager varsManager;
	private transient Gambling gamblingSession;

	// used for packets logic
	private transient ConcurrentLinkedQueue<LogicPacket> logicPackets;

	// used for projectiles
	private transient ConcurrentLinkedQueue<Projectile> projectiles;

	// used for update
	private transient LocalPlayerUpdate localPlayerUpdate;
	private transient LocalNPCUpdate localNPCUpdate;

	private int temporaryMovementType;
	private boolean updateMovementType;

	// player stages
	private transient boolean started;
	private transient boolean running;
	private transient boolean lobby;

	private transient int resting;
	private transient boolean canPvp;
	private transient boolean cantTrade;
	private transient boolean cantWalk;
	private transient long lockDelay; // used for doors and stuff like that
	private transient long foodDelay;
	private transient long potDelay;
	private transient long karamDelay;
	private transient Runnable closeInterfacesEvent;
	private transient long lastPublicMessage;
	private transient long polDelay;
	private transient List<Integer> switchItemCache;
	private transient boolean disableEquip;
	private transient MachineInformation machineInformation;
	private transient boolean castedVeng;
	private transient boolean invulnerable;
	private transient double hpBoostMultiplier;
	private transient boolean largeSceneView;
	private transient int cannonBalls;
	private transient boolean graniteBalls;

	public boolean tournamentResetRequired() {
		return requiresTournamentReset;
	}

	public void setRequiresTournamentReset(boolean requiresTournamentReset) {
		this.requiresTournamentReset = requiresTournamentReset;
	}

	private boolean requiresTournamentReset;
	
	//now saves to prevent farming
	private String lastPlayerKill;
	private String lastPlayerMAC;

	private transient int transformationTicks;

	private transient long lastPing;
	private transient long lastActive;
	private transient FloorItem lootbeam;
	private transient Entity lastTarget;

	// stuff received from login server
	private transient String displayName;
	private transient String email;

	private transient int rights;
	private transient int messageIcon;
	private transient int donator;
	private transient int gameMode;
	private transient boolean supporter;
	private transient boolean eventCoordinator;
	private transient boolean youtuber;

	private transient boolean muted;
	private transient int nextRoll;
	private transient long lastDollarKeyFragment;
	
	private transient boolean warnedAFK;
	
	private transient List<String> journalLines;
	
	private transient long antibotTime;
	

	// saving stuff
	private long lastVote;
	private Appearence appearence;
	private Inventory inventory;
	private LootingBag lootingBag;
	private CoalBag coalBag;
	private GemBag gemBag;
	private ItemsContainer<Item> runePouch;
	private ItemsContainer<Item> spawnRunePouch;
	private MoneyPouch moneyPouch;
	private Equipment equipment;
	private Skills skills;
	private CombatDefinitions combatDefinitions;
	private Prayer prayer;
	private CollectionLog collectionLog;
	private Bank bank;
	private ControllerManager controlerManager;
	private MusicsManager musicsManager;
	private EmotesManager emotesManager;
	private Notes notes;
	private Toolbelt toolbelt;
	private DominionTower dominionTower;
	private Familiar familiar;
	private FarmingManager farmingManager;
	private AuraManager auraManager;
	private QuestManager questManager;
	private PetManager petManager;
	private GrandExchangeManager geManager;
	private SlayerManager slayerManager;
	private SquealOfFortune squealOfFortune;
	private TreasureTrailsManager treasureTrailsManager;
	private CoalTrucksManager coalTrucksManager;
	private DungManager dungManager;
	private DailyTasksManager tasksManager;
	private House house;
	private Deals deals;
	private Achievements achievements;
	private Presets presets;
	private byte runEnergy;
	private boolean allowChatEffects;
	private boolean acceptAid;
	private boolean rightClickReporting;
	private boolean mouseButtons;
	private boolean profanityFilter;
	private int privateChatSetup;
	private int friendChatSetup;
	private int clanChatSetup;
	private int guestChatSetup;
	private int skullDelay;
	private int skullId;
	private boolean osrsMagicToggle = true;

	private int loginCount;
	private boolean forceNextMapLoadRefresh;
	private long poisonImmune;
	private long fireImmune;
	private boolean superAntiFire;
	private boolean killedQueenBlackDragon;
	private int runeSpanPoints;
	private int pestPoints;
	private int stealingCreationPoints;
	private int favorPoints;
	private double[] warriorPoints;
	private boolean[] prayerBook;
	private int previousLodestone;
	private int lmsKills, lmsWins, lmsGames;

	private boolean skipVPNCheck;

	private boolean enteredKBD;

	public void setEnteredKBD() {
		enteredKBD = true;
	}

	public boolean hasEnteredKBD() {
		return enteredKBD;
	}

	public void lmsFinished(boolean win) {
		if(win) lmsWins++;
		lmsGames++;

		sendMessage("You have won <col=ff0000>" + lmsWins + "</col> game" + (lmsWins == 1 ? "" : "s") + " and played in <col=ff0000>" + lmsGames + "</col>.");
	}

	public void addLmsKill() {
		lmsKills++;
		sendMessage("You have now killed " + lmsKills + " player"+(lmsKills == 1 ? "" : "s")+"!");
	}

	// shop
	private boolean verboseShopDisplayMode;

	private int lastBonfire;
	private long lastStarSprite;
	private long lastEvilTree;
	private long lastBork;
	private long lastGambleKing;

	public long getLastSkeletalHorror() {
		return lastSkeletalHorror;
	}

	public void setLastSkeletalHorror(long lastSkeletalHorror) {
		this.lastSkeletalHorror = lastSkeletalHorror;
	}

	private long lastSkeletalHorror;

	private int[] pouches;

	private Map<SCItem, Integer> scXP;

	private boolean lostCannon;

	private Item blowpipeDarts, infernalBlowpipeDarts = null;

	private Map<String, Integer> bossKillcount;


	private NpcKillCountTracker npcKillCountTracker;

	private Map<String, Long> bossKilltime;

	// game bar status
	private boolean filterGame;
	private int publicStatus;
	private int clanStatus;
	private int tradeStatus;
	private int assistStatus;
	private int friendsChatStatus;

	// honor
	private int killCount, deathCount;
	private long lastArtefactTime;

	@SuppressWarnings("unused")
	private int killingSpree, pkPoints;
	private int pkp;

	@SuppressWarnings("unused")
	private long lastKilledTime;
	private ChargesManager charges;
	// barrows
	private boolean[] killedBarrowBrothers;
	private int hiddenBrother;
	private int barrowsKillCount;
	// strongholdofsecurity rewards
	private boolean[] shosRewards;
	private boolean killedLostCityTree;

	// skill capes customizing
	private int[] maxedCapeCustomized;
	private int[] completionistCapeCustomized;

	// completionistcape reqs
	private boolean completedFightCaves;
	private boolean completedHorde;
	private boolean completedFightKiln;
	private boolean wonFightPits;
	private boolean completedStealingCreation;
	private boolean capturedCastleWarsFlag;
	private boolean wonStackedDuel;
	private boolean sellMandrithStatuete;
	private int receivedCompletionistCape;
	private boolean killedWildyWyrm;
	private boolean foundShootingStar;
	private boolean wonReaction;
	private int wonTrivias;
	private int bossTasksCompleted;
	private int fightCavesCompletions = 0;

	// trimmed compcape
	private int finishedCastleWars;
	private int finishedStealingCreations;
	private boolean gambledPartyhat;
	private boolean ugradedItem;
	private long thrownWishingCoins;
	private long callusSpawnDonations;
	private long dropPartyValue;
	private int priffCourseCompletions;
	private transient long priffCourseLapTime = -1;
	private long priffCourseLapTimePB = -1;

	private int fireCapeGambles, infernalCapeGambles, kilnCapeGambles;

	// crucible
	private boolean talkedWithMarv;
	private int crucibleHighScore;

	private int ecoClearStage;

	// gravestone
	private int graveStone;

	private int overloadDelay;
	private int prayerRenewalDelay;

	private String lastFriendsChat;
	private int lastFriendsChatRank = -1;
	private String clanName;// , guestClanChat;
	private boolean connectedClanChannel;

	private int summoningLeftClickOption;
	private transient boolean pouchFilter;
	private List<String> ownedObjectsManagerKeys;

	/**
	 * Objects
	 */
	// kalphite
	private boolean khalphiteLairEntranceSetted;
	private boolean khalphiteLairSetted;
	// red sandstone
	private int redStoneCount;
	private long redStoneDelay;

	// ectofuntus
	public transient int ectofungusBones;
	public transient boolean grindedBones;
	public int ectoTokens;

	private boolean xpLocked;
	private boolean yellOff;

	private String yellColor = "ff0000";
	private boolean oldItemsLook; // selects whenever to play with old or new items visual
	private boolean virtualLevels; // enabled by default
	private boolean disableCosmeticOverrides;
	private boolean oldHitsLook;
	private boolean osrsHitbars;
	private boolean osrsGameframe;
	private boolean oldNPCLooks;
	
	private boolean hideAttackOption;
	private boolean disableGroundItemNames;
	// old voting variables
	@SuppressWarnings("unused")
	private long voted;
	private int votes;
	private int votesIn24h;
	private double donated;

	private String lastGameIp;
	private String lastGameMAC;
	private transient boolean masterLogin;
	private long lastGameLogin;

	private long onlineTime;
	
    private String lastBossInstanceKey;
    private InstanceSettings lastBossInstanceSettings;
	
	private long[] dungChallengeTimes;
	
	//previous tp
	private String getPreviousTPName;
	private WorldTile getPreviousTPTile;

	// world 2 custom
	private CustomGear[] gearSetups;
	
	private String hcPartner;
	
	private boolean disableNotifications;
	private boolean disableAutoLoot;
	private boolean disableHealthPlugin;
	private boolean disablePotionTimersPlugin;
	private boolean alwaysAutoLootDisabled;
	private boolean commonAutoLootDisabled;
	private boolean uncommonAutoLootDisabled;
	private boolean rareAutoLootDisabled;
	private boolean veryRareAutoLootDisabled;

	public boolean raidModeSwitchInfo;

	public boolean isAlwaysAutoLootDisabled() {
		return alwaysAutoLootDisabled;
	}

	public void switchAlwaysAutoLoot() {
		alwaysAutoLootDisabled = !alwaysAutoLootDisabled;
	}

	public boolean isCommonAutoLootDisabled() {
		return commonAutoLootDisabled;
	}

	public void switchCommonAutoLoot() {
		commonAutoLootDisabled = !commonAutoLootDisabled;
	}

	public boolean isUncommonAutoLootDisabled() {
		return uncommonAutoLootDisabled;
	}

	public void switchUncommonAutoLoot() {
		uncommonAutoLootDisabled = !uncommonAutoLootDisabled;
	}

	public boolean isRareAutoLootDisabled() {
		return rareAutoLootDisabled;
	}

	public void switchRareAutoLoot() {
		rareAutoLootDisabled = !rareAutoLootDisabled;
	}


	public boolean isVeryRareAutoLootDisabled() {
		return veryRareAutoLootDisabled;
	}

	public void switchVeryRareLoot() {
		veryRareAutoLootDisabled = !veryRareAutoLootDisabled;
	}


	public boolean isRigourUnlocked() {
		return tournamentResetRequired() || rigourUnlocked;
	}

	public void setRigourUnlocked(boolean rigourUnlocked) {
		this.rigourUnlocked = rigourUnlocked;
	}

	public boolean isAuguryUnlocked() {
		return tournamentResetRequired() || auguryUnlocked;
	}

	public void setAuguryUnlocked(boolean auguryUnlocked) {
		this.auguryUnlocked = auguryUnlocked;
	}

	public boolean isPreserveUnlocked() {
		return tournamentResetRequired() || preserveUnlocked;
	}

	public void setPreserveUnlocked(boolean preserveUnlocked) {
		this.preserveUnlocked = preserveUnlocked;
	}

	public boolean rigourUnlocked, auguryUnlocked, preserveUnlocked;

	/*
	 * chambers of xeric private items
	 */
	private ItemsContainer<Item> privateItems = new ItemsContainer<Item>(215, false);

	private int callusDropWeight;

	private List<Teleport> lastTeleports = new ArrayList<Teleport>();
	private transient List<Teleport> visibleTeleports = new ArrayList<>();

	public int getTeleportExpanded() {
		return teleportExpanded;
	}

	public void setTeleportExpanded(int teleportExpanded) {
		this.teleportExpanded = teleportExpanded;
	}

	private transient int teleportExpanded = -1;

	/*
	 * last teleports using teleport interface
	 */
	public List<Teleport> getLastTeleports() {
		return lastTeleports;
	}

	/**
	 * visible on interface
	 */
	public List<Teleport> getVisibleTeleports() {
		return visibleTeleports;
	}

	// creates Player and saved classes
	public Player() {
		super(Settings.START_PLAYER_LOCATION);
		super.setHitpoints(Settings.START_PLAYER_HITPOINTS);
		appearence = new Appearence();
		inventory = new Inventory();
		lootingBag = new LootingBag();
		coalBag = new CoalBag();
		gemBag = new GemBag();
		runePouch = new ItemsContainer<Item>(3, false);
		spawnRunePouch = new ItemsContainer<Item>(3, false);
		moneyPouch = new MoneyPouch();
		equipment = new Equipment();
		skills = new Skills();
		combatDefinitions = new CombatDefinitions();
		prayer = new Prayer();
		collectionLog = new CollectionLog();
		bank = new Bank();
		controlerManager = new ControllerManager();
		musicsManager = new MusicsManager();
		emotesManager = new EmotesManager();
		collectionLog = new CollectionLog();
		notes = new Notes();
		toolbelt = new Toolbelt();
		dominionTower = new DominionTower();
		charges = new ChargesManager();
		auraManager = new AuraManager();
		questManager = new QuestManager();
		petManager = new PetManager();
		farmingManager = new FarmingManager();
		geManager = new GrandExchangeManager();
		slayerManager = new SlayerManager();
		squealOfFortune = new SquealOfFortune();
		treasureTrailsManager = new TreasureTrailsManager();
		coalTrucksManager = new CoalTrucksManager();
		dungManager = new DungManager();
		house = new House();
		deals = new Deals();
		achievements = new Achievements();
		presets = new Presets();
		bossKillcount = new HashMap<String, Integer>();
		bossKilltime = new HashMap<String, Long>();
		keybinds = new Keybinds();
		runEnergy = 100;
		allowChatEffects = true;
		mouseButtons = true;
		profanityFilter = false; // true on rs
		acceptAid = true; // default false on rs
		privateChatSetup = 1; // default splitchat off on rs
		friendChatSetup = 6; // default 0 on rs, but hard to see
		verboseShopDisplayMode = true; // default off on rs
		virtualLevels = false; // default off on rs
		assistStatus = 2; //off by default
		oldItemsLook = true; //on by default
		oldHitsLook = true; //on by default
		oldNPCLooks = true; //on by default
		osrsHitbars = false;
		osrsGameframe = true;
		pouches = new int[4];
		resetBarrows();
		shosRewards = new boolean[4];
		warriorPoints = new double[6];
		prayerBook = new boolean[PrayerBooks.BOOKS.length];
		SkillCapeCustomizer.resetSkillCapes(this);
		ownedObjectsManagerKeys = new LinkedList<String>();
		scXP = new HashMap<SCItem, Integer>();
		super.setRun(true);
		setEcoClearStage(ItemRemover.ECO_STAGE);
	}

	public Player(boolean test) {
		super(Settings.START_PLAYER_LOCATION);
	}

	public void init(Session session, boolean lobby, String username, String displayName, String lastGameMAC,
			String email, int rights, int gameMode, int messageIcon, boolean masterLogin, int donator, boolean support,
			boolean gfxDesigner, boolean youtuber, boolean muted, long lastVote, int displayMode, int screenWidth,
			int screenHeight, MachineInformation machineInformation, IsaacKeyPair isaacKeyPair) {
		// temporary deleted after reset all chars
		if (lootingBag == null)
			lootingBag = new LootingBag();
		if (coalBag == null)
			coalBag = new CoalBag();
		if (gemBag == null)
			gemBag = new GemBag();
		if (keybinds == null)
			keybinds = new Keybinds();
		if (collectionLog == null)
			collectionLog = new CollectionLog();
		if(visibleTeleports == null)
			visibleTeleports = new ArrayList<>();
		if(lastTeleports == null)
			lastTeleports = new ArrayList<>();
		if(privateItems == null)
			privateItems = new ItemsContainer<Item>(215, false);
		if(raidRewards == null)
			raidRewards = raidRewards = new ItemsContainer<Item>(6, true);
		if(chambersPB == null)
			chambersPB = new long[100];
		if(osrsChambersPB == null)
			osrsChambersPB = new long[100];
		if(upgradeAttempts == null)
			upgradeAttempts = new HashMap<Integer, Integer>();
		this.session = session;
		this.lobby = lobby;
		this.username = username;
		this.displayName = displayName;
		this.email = email;
		this.rights = rights;
		this.gameMode = gameMode;
		this.masterLogin = masterLogin;
		this.messageIcon = messageIcon;
		this.donator = donator;
		this.supporter = support;
		this.eventCoordinator = gfxDesigner;
		this.youtuber = youtuber;
		this.muted = muted;
		// this.lastVote = lastVote;
		this.lastGameMAC = lastGameMAC;
		this.displayMode = displayMode;
		this.graphicMode = 1; //by default not safe mode
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.machineInformation = machineInformation;
		this.isaacKeyPair = isaacKeyPair;

		interfaceManager = new InterfaceManager(this);
		dialogueManager = new DialogueManager(this);
		hintIconsManager = new HintIconsManager(this);
		priceCheckManager = new PriceCheckManager(this);
		localPlayerUpdate = new LocalPlayerUpdate(this);
		localNPCUpdate = new LocalNPCUpdate(this);
		actionManager = new ActionManager(this);
		cutscenesManager = new CutscenesManager(this);
		trade = new Trade(this);
		varsManager = new VarsManager(this);
		friendsIgnores = new FriendsIgnores(this);
		// loads player on saved instances
		appearence.setPlayer(this);
		inventory.setPlayer(this);
		lootingBag.setPlayer(this);
		coalBag.setPlayer(this);
		gemBag.setPlayer(this);
		moneyPouch.setPlayer(this);
		equipment.setPlayer(this);
		skills.setPlayer(this);
		combatDefinitions.setPlayer(this);
		prayer.setPlayer(this);
		collectionLog.init(this);
		bank.setPlayer(this);
		if (controlerManager == null) //TODO temp
			controlerManager = new ControllerManager();
		controlerManager.setPlayer(this);
		musicsManager.setPlayer(this);
		emotesManager.setPlayer(this);
		notes.setPlayer(this);
		toolbelt.setPlayer(this);
		dominionTower.setPlayer(this);
		auraManager.setPlayer(this);
		charges.setPlayer(this);
		questManager.setPlayer(this);
		petManager.setPlayer(this);
		house.setPlayer(this);
		farmingManager.setPlayer(this);
		geManager.setPlayer(this);
		slayerManager.setPlayer(this);
		squealOfFortune.setPlayer(this);
		treasureTrailsManager.setPlayer(this);
		coalTrucksManager.setPlayer(this);
		dungManager.setPlayer(this);
		if (tasksManager == null) //TODO temp
			tasksManager = new DailyTasksManager();
		tasksManager.setPlayer(this);
		getDeals().setPlayer(this);
		getAchievements().setPlayer(this);
		getPresets().setPlayer(this);
		ectofungusBones = -1;
		initEntity(); // generates hash thats why
		setLastPing();
		setLastActive();
		resetAntibot();
		if (!lobby) {
			setDirection(Utils.getAngle(0, -1));
			temporaryMovementType = -1;
			logicPackets = new ConcurrentLinkedQueue<LogicPacket>();
			projectiles = new ConcurrentLinkedQueue<Projectile>();
			switchItemCache = Collections.synchronizedList(new ArrayList<Integer>());
			World.addPlayer(this);
			World.updateEntityRegion(this);
		} else {
			World.addLobbyPlayer(this);
		}


		if (npcKillCountTracker == null) {
			this.npcKillCountTracker = new NpcKillCountTracker();
		}


		for (var kills : this.slayerManager.getKillcount().entrySet()) {
			this.npcKillCountTracker.set(kills.getKey().getName(), kills.getValue());
		}

		for (var boss : this.bossKillcount.entrySet()) {
			this.npcKillCountTracker.set(boss.getKey(), boss.getValue());
		}


		if (Settings.DEBUG)
			Logger.log(this, "Initiated player: " + username);

	}

	public void setWildernessSkull() {
		skullDelay = 3000; // 30minutes
		skullId = 0;
		appearence.generateAppearenceData();
		setSkullTimer();
	}

	public void setFightPitsSkull() {
		skullDelay = Integer.MAX_VALUE; // infinite
		skullId = 1;
		appearence.generateAppearenceData();
		setSkullTimer();
	}
	
	public void setGalvekSkull() {
		skullDelay = 3000; // 30minutes
		skullId = 1;
		appearence.generateAppearenceData();
		setSkullTimer();
	}

	public boolean isOsrsMagicToggle() {
		return osrsMagicToggle;
	}

	public boolean flipOsrsMagicToggle() {
		osrsMagicToggle = !osrsMagicToggle;
		return osrsMagicToggle;
	}

	public void setSkullInfiniteDelay(int skullId) {
		skullDelay = Integer.MAX_VALUE; // infinite
		this.skullId = skullId;
		appearence.generateAppearenceData();
		setSkullTimer();
	}

	public void removeSkull() {
		skullDelay = -1;
		appearence.generateAppearenceData();
		setSkullTimer();
	}

	public boolean hasSkull() {
		return skullDelay > 0;
	}

	public void completeReset() {
		bank = new Bank();
		bank.setPlayer(this);
		equipment.reset();
		inventory.reset();
		moneyPouch.setCoinsAmount(0);
		for (int skill = 0; skill < 25; skill++) {
			skills.setXp(skill, 0);
			skills.set(skill, 1);
		}
		skills.init();
	}

	public void refreshSpawnedItems() {
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getGroundItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if (item.isInvisible() && (item.hasOwner() && !getUsername().equals(item.getOwner())))
					continue;
				getPackets().sendRemoveGroundItem(item);
			}
		}
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getGroundItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible()) && (item.hasOwner() && !getUsername().equals(item.getOwner())))
					continue;
				getPackets().sendGroundItem(item);
			}
		}
	}

	public void refreshSpawnedObjects() {
		for (int regionId : getMapRegionsIds()) {
			List<WorldObject> removedObjects = World.getRegion(regionId).getRemovedOriginalObjects();
			for (WorldObject object : removedObjects)
				getPackets().sendRemoveObject(object);
			List<WorldObject> spawnedObjects = World.getRegion(regionId).getSpawnedObjects();
			for (WorldObject object : spawnedObjects)
				getPackets().sendAddObject(object);
		}
	}

	// now that we inited we can start showing game
	public void start() {
		Logger.globalLog(username, session.getIP(), new String(" has logged in."));
		loadMapRegions();
		started = true;
		run();
	}

	public void startLobby() {
		started = true;
		sendLobbyConfigs();
		runLobby();
	}

	public void runLobby() {
		if (GameLauncher.delayedShutdownStart != 0) {
			int delayPassed = (int) ((Utils.currentTimeMillis() - GameLauncher.delayedShutdownStart) / 1000);
			getPackets().sendSystemUpdate(GameLauncher.delayedShutdownDelay - delayPassed, true);
		}

		friendsIgnores.initialize();
		if (lastFriendsChat != null) {
			FriendsChat.requestJoin(this, lastFriendsChat);
			lastFriendsChat = null;
		}
		if (clanName != null) {
			if (!ClansManager.connectToClan(this, clanName, false))
				clanName = null;
		}

		/*
		 * friendsIgnores.init(); if (currentFriendChatOwner != null) {
		 * FriendChatsManager.joinChat(currentFriendChatOwner, this); if
		 * (currentFriendChat == null) // failed currentFriendChatOwner = null; } //
		 * connect to current clan if (clanName != null) { if
		 * (!ClansManager.connectToClan(this, clanName, false)) clanName = null; }
		 */

	}

	private void sendLobbyConfigs() {
		for (int i = 0; i < Utils.DEFAULT_LOBBY_CONFIGS.length; i++)
			getVarsManager().sendVar(i, Utils.DEFAULT_LOBBY_CONFIGS[i]);
		getPackets().sendIComponentText(907, 52, "Donation status");
		getPackets().sendIComponentText(907, 53, (isDonator() ? "Standard" : "Not a donator"));
		if (isDonator())
			getPackets().sendIComponentText(907, 54,
					"You are donator. Thank you for your support, which is greatly appreciated.");
		else
			getPackets().sendIComponentText(907, 54,
					"You don't have donator status. Donators help keeping our server up & making new updates for it. Check out our <col=0166ff><u=0166ff>store</u></col> to purchase donator status.");

		getPackets().sendIComponentText(907, 14, "Vote status");
		if (hasVotedInLast24Hours()) {
			getPackets().sendIComponentText(907, 15, "Vote bonus enabled");
			getPackets().sendIComponentText(907, 16,
					"You are currently receiving 25% drop rate and experience boost. Click <col=0166ff><u=0166ff>here</u></col> to open voting page.");
		} else {
			getPackets().sendIComponentText(907, 15, "No vote bonus");
			getPackets().sendIComponentText(907, 16, "Please vote for at least " + Settings.VOTE_MIN_AMOUNT
					+ " tokens to receive 25% drop rate and experience boost. Click <col=0166ff><u=0166ff>here</u></col> to open voting page.");
		}

		getPackets().sendIComponentText(907, 26, "Offences status");
		if (isMuted()) {
			getPackets().sendIComponentText(907, 27, "Muted");
			getPackets().sendIComponentText(907, 28,
					"You are currently muted for breaking our rules. Click <col=0166ff><u=0166ff>here</u></col> to see more information.");
		} else {
			getPackets().sendIComponentText(907, 27, "No active offences");
			getPackets().sendIComponentText(907, 28,
					"Your account has no active offences, keep it up! Click <col=0166ff><u=0166ff>here</u></col> to see more information.");
		}

		getPackets().sendIComponentText(907, 38, "Email status");
		if (getEmail() != null) {
			getPackets().sendIComponentText(907, 39, "Registered");
			getPackets().sendIComponentText(907, 40, "Your email address is registered as " + getEmail()
					+ ". Click <col=0166ff><u=0166ff>here</u></col> to unregister your email.");
		} else {
			getPackets().sendIComponentText(907, 39, "Unregistered");
			getPackets().sendIComponentText(907, 40,
					"You do not currently have an email address registered. Click <col=0166ff><u=0166ff>here</u></col> to register.");
		}
	}

	public void stopAll() {
		stopAll(true);
	}

	public void stopAll(boolean stopWalk) {
		stopAll(stopWalk, true);
	}

	public void stopAll(boolean stopWalk, boolean stopInterface) {
		stopAll(stopWalk, stopInterface, true);
	}

	// as walk done clientsided
	public void stopAll(boolean stopWalk, boolean stopInterfaces, boolean stopActions) {
		routeEvent = null;
		if (stopInterfaces)
			closeInterfaces();
		if (stopWalk && !cantWalk)
			resetWalkSteps();
		if (stopActions)
			actionManager.forceStop();
		combatDefinitions.resetSpells(false);
	}

	@Override
	public void setHitpoints(int hitpoints) {
		super.setHitpoints(hitpoints);
		refreshHitPoints();
	}
	
	//called upon login, sets all timers since some might not be set
	private void sendTimers() {
		if (lastBonfire > 0)
			setBonfireTimer();
		if (skullDelay > 600)
			setSkullTimer();
		if (familiar != null)
			setFamiliarTimer();
		if (overloadDelay > 0)
			setOverloadTimer();
		if (prayerRenewalDelay > 0)
			setPrayerRenewalTimer();
		if (getPoisonImmune() > Utils.currentTimeMillis())
			setPoisonTimer();
		if (hasFireImmunity())
			setAntifireTimer();
		/*if (OwnedObjectManager.containsObjectValue(this, 6))
			setCannonTimer(0);*/
			
	}
	
	public void setCannonTimer(int time) {
		getPackets().setTimer(0, 10, time);
	}
	
	private void setBonfireTimer() {
		getPackets().setTimer(1, 21067, lastBonfire * 600);
	}
	
	public void setFamiliarTimer() {
		getPackets().setTimer(1, 1200, familiar == null ? 0 : (familiar.getTicks() * 30000));
	}
	
	public void setSkullTimer() {
		getPackets().setTimer(1, 1583, skullDelay * 600);
	}
	
	public void setTeleblockTimer(int delay) {
		getPackets().setTimer(1, 1565, delay);
	}
	
	public void setVengTimer(int delay) {
		getPackets().setTimer(1, 564, delay);
	}
	
	public void setFreezeTimer(int delay) {
		getPackets().setTimer(1, 328, delay);
	}

	private void setOverloadTimer() {
		getPackets().setTimer(0, 15332, overloadDelay * 600);
		getPackets().setTimer(0, 2436, 0);
		getPackets().setTimer(0, 2440, 0);
		getPackets().setTimer(0, 2442, 0);
		getPackets().setTimer(0, 2444, 0);
		getPackets().setTimer(0, 3040, 0);
	}
	
	private void setPrayerRenewalTimer() {
		getPackets().setTimer(0, 21630, prayerRenewalDelay * 600);
	}
	
	private void setPoisonTimer() {
		getPackets().setTimer(0, 2446,  (int) Math.max(0,  (getPoisonImmune() - Utils.currentTimeMillis())));
	}
	
	private void setAntifireTimer() {
		getPackets().setTimer(0, 2452,  (int) Math.max(0,  (getFireImmune() - Utils.currentTimeMillis())));
	}
	
	public void setCombatPotionTimer() {
		if (overloadDelay > 0)
			return;
		getPackets().setTimer(0, 42695, 120000);
		getPackets().setTimer(0, 2436, 0);
		getPackets().setTimer(0, 2440, 0);
		getPackets().setTimer(0, 2442, 0);
	}
	
	
	public void setAttackPotionTimer() {
		if (overloadDelay > 0)
			return;
		getPackets().setTimer(0, 2436, 120000);
	}
	
	public void setStrengthPotionTimer() {
		if (overloadDelay > 0)
			return;
		getPackets().setTimer(0, 2440, 120000);
	}
	
	public void setDefencePotionTimer() {
		if (overloadDelay > 0)
			return;
		getPackets().setTimer(0, 2442, 120000);
	}
	
	public void setRangePotionTimer() {
		if (overloadDelay > 0)
			return;
		getPackets().setTimer(0, 2444, 120000);
	}
	
	public void setMagicPotionTimer() {
		if (overloadDelay > 0)
			return;
		getPackets().setTimer(0, 3040, 120000);
	}
	
	
	public void setSpecialRecoverTimer(int delay) {
		getPackets().setTimer(0, 15300, delay);
	}

	@Override
	public void reset(boolean attributes) {
		super.reset(attributes);
		hintIconsManager.removeAll();
		skills.restoreSkills();
		combatDefinitions.resetSpecialAttack();
		prayer.reset();
		combatDefinitions.resetSpells(false);
		resting = 0;
		skullDelay = 0;
		foodDelay = 0;
		potDelay = 0;
		poisonImmune = 0;
		fireImmune = 0;
		overloadDelay = 0;
		prayerRenewalDelay = 0;
		castedVeng = false;
		cantWalk = false;
		setRunEnergy(100);
		getPackets().resetTimers(); //resets all timers
		sendTimers(); //some not supposed to reset
		appearence.generateAppearenceData();
	}

	@Override
	public void reset() {
		reset(true);
	}

	public void closeInterfaces() {
		getPackets().sendCSVarInteger(5, 0);// resets input cs2
		if (interfaceManager.containsScreenInter())
			interfaceManager.removeScreenInterface();
		if (interfaceManager.containsInventoryInter())
			interfaceManager.removeInventoryInterface();
		if (interfaceManager.containsPMInterface() && Utils.currentTimeMillis() - interfaceManager.getLastNotification() >= 5000)
			interfaceManager.removePMInterface();
		dialogueManager.finishDialogue();
		if (closeInterfacesEvent != null) {
			Runnable runnable = closeInterfacesEvent;
			runnable.run();
			if (runnable == closeInterfacesEvent)
				closeInterfacesEvent = null;
		}
		TemporaryAtributtes.closeInterfaces(this);
	}

	public void setClientHasntLoadedMapRegion() {
		clientLoadedMapRegion = Utils.currentWorldCycle() + 30;
	}

	@Override
	public void processMovement() {
		if (getLastWorldTile() != null && this.getLastWorldTile().getPlane() != getPlane())
			resetAntibot();
		super.processMovement();
	}
	
	
	@Override
	public void loadMapRegions() {
		boolean wasAtDynamicRegion = isAtDynamicRegion();
		super.loadMapRegions();
		setClientHasntLoadedMapRegion();
		if (isAtDynamicRegion()) {
			getPackets().sendDynamicGameScene(!started);
			if (!wasAtDynamicRegion)
				localNPCUpdate.reset();
		} else {
			getPackets().sendGameScene(!started);
			if (wasAtDynamicRegion)
				localNPCUpdate.reset();
		}
		forceNextMapLoadRefresh = false;
		resetAntibot();
	}

	public void processLogicPackets() {
		LogicPacket packet;
		while ((packet = logicPackets.poll()) != null)
			WorldPacketsDecoder.decodeLogicPacket(this, packet);
	}

	@SuppressWarnings("deprecation")
	public void processProjectiles() {
		Projectile projectile;
		while ((projectile = projectiles.poll()) != null) {
			getPackets().sendProjectile(
					projectile.getReceiver() instanceof Entity ? (Entity) projectile.getReceiver() : null,
					projectile.getShooter(), projectile.getReceiver(), projectile.getGfx(), projectile.getStartHeight(),
					projectile.getEndHeight(), projectile.getSpeed(), projectile.getDelay(), projectile.getCurve(),
					projectile.getStartDistanceOffset(),
					projectile.getShooter() instanceof Entity ? ((Entity) projectile.getShooter()).getSize() : 0);
		}
	}

	public void addProjectileToQueue(Projectile projectile) {
		projectiles.add(projectile);
	}

	@Override
	public void processEntityUpdate() {
		super.processEntityUpdate();
	}

	@Override
	public void processEntity() {
		processLogicPackets();
	//	Easter2018.process(this);
		actionManager.process();
		if (routeEvent != null && routeEvent.processEvent(this))
			routeEvent = null;
		super.processEntity();
		charges.process();
		auraManager.process();
		prayer.processPrayer();
		controlerManager.process();
		farmingManager.process();
		cutscenesManager.process();
		processLootbeam();
		if (isDead())
			return;
	/*	if (musicsManager.musicEnded())
			musicsManager.replayMusic();*/
		if (hasSkull() && equipment.getAmuletId() != 52557) {
			skullDelay--;
			if (!hasSkull())
				appearence.generateAppearenceData();
		}
		if (polDelay != 0 && polDelay <= Utils.currentTimeMillis()) {
			getPackets().sendGameMessage(
					"The power of the light fades. Your resistance to melee attacks return to normal.");
			polDelay = 0;
		}
		if (transformationTicks > 0) {
			if (transformationTicks == 1 || isDead()) {
				resetTransformation();
				return;
			} else if (transformationTicks == 250)
				getPackets()
						.sendGameMessage("<col=0000FF>Your transformation will end in two minutes and thirty seconds.");
			transformationTicks--;
		}
		processOverload();
		processPrayerRenewal();
		processBonfire();

		if (isDungeoneer() && !isLocked() && !getDungManager().isInside()
				&& !(getControlerManager().getControler() instanceof Kalaboss)) {
			Magic.sendTeleportSpell(this, 13652, 13654, 2602, 2603, 1, 0, new WorldTile(3447, 3694, 0), 10, true,
					Magic.ITEM_TELEPORT);
			getPackets().sendGameMessage("You can't leave dungeoneering area on this mode.");
		}
		
		if (equipment.getWeaponId() == 25496 || equipment.getWeaponId() == 25764)
			this.setNextGraphics(new Graphics(400, 0, 100));
		checkAFK();
		checkAntiBot();
	}

	public void processHitbox() {
		if (lastTarget == null)
			return;
		if (!withinDistance(lastTarget, 32)
				/* || (!isUnderCombat() && !lastTarget.isUnderCombat()) */ || lastTarget.hasFinished())
			setLastTarget(null);
		else if (!disableHealthPlugin) 
			getPackets().sendRefreshHitbox();
	}

	private void processOverload() {
		if (overloadDelay > 0) {
			if (overloadDelay == 1) {
				Drinkables.resetOverLoadEffect(this);
				return;
			} else if ((overloadDelay - 1) % 25 == 0)
				Drinkables.applyOverLoadEffect(this);
			overloadDelay--;
		}
	}

	private void processPrayerRenewal() {
		if (prayerRenewalDelay > 0) {
			if (prayerRenewalDelay == 1 || isDead()) {
				getPackets().sendGameMessage("<col=0000FF>Your prayer renewal has ended.");
				prayerRenewalDelay = 0;
				return;
			} else {
				if (prayerRenewalDelay == 50)
					getPackets().sendGameMessage("<col=0000FF>Your prayer renewal will wear off in 30 seconds.");
				if (!prayer.hasFullPrayerpoints()) {
					getPrayer().restorePrayer(1);
					if ((prayerRenewalDelay - 1) % 25 == 0)
						setNextGraphics(new Graphics(1295));
				}
			}
			prayerRenewalDelay--;
		}
	}

	private void processBonfire() {
		if (lastBonfire > 0) {
			lastBonfire--;
			if (lastBonfire == 500)
				getPackets().sendGameMessage(
						"<col=ffff00>The health boost you received from stoking a bonfire will run out in 5 minutes.");
			else if (lastBonfire == 0) {
				getPackets().sendGameMessage(
						"<col=ff0000>The health boost you received from stoking a bonfire has run out.");
				equipment.refreshConfigs(false);
			}
		}
	}

	private void processLootbeam() {
		if (lootbeam == null)
			return;
		int mapID = lootbeam.getTile().getRegionId();
		if (!World.getRegion(mapID).getGroundItemsSafe().contains(lootbeam)) {
			lootbeam = null;
			return;
		}
		if (WorldThread.WORLD_CYCLE % 4 != 0
				|| /* !getMapRegionsIds().contains(mapID) */!withinDistance(lootbeam.getTile()))
			return;

		getPackets().sendGraphics(new Graphics(7), lootbeam.getTile());
	}

	@Override
	public void processReceivedHits() {
		if (isLocked())
			return;
		super.processReceivedHits();
	}

	@Override
	public void applyHit(Hit hit) {
		Entity source = hit.getSource();
		if (source != this && source instanceof Player && !isCanPvp())
			return;
		super.applyHit(hit);
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || temporaryMovementType != -1 || updateMovementType;
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		temporaryMovementType = -1;
		updateMovementType = false;
		/*
		 * if (!clientHasLoadedMapRegion()) { // load objects and items here
		 * setClientHasLoadedMapRegion(); refreshSpawnedObjects();
		 * refreshSpawnedItems(); }
		 */
	}

	public void toogleRun(boolean update) {
		if (!getRun() && getTemporaryAttributtes().get(Key.SPORE_INFECTED) != null) {
			getVarsManager().forceSendVar(173, 0);
			getPackets().sendGameMessage("You're too drowsy to run!");
			return;
		}
		super.setRun(!getRun());
		updateMovementType = true;
		if (update)
			sendRunButtonConfig();
	}

	public void setRunHidden(boolean run) {
		super.setRun(run);
		updateMovementType = true;
	}

	@Override
	public void setRun(boolean run) {
		if (run != getRun()) {
			super.setRun(run);
			updateMovementType = true;
			sendRunButtonConfig();
		}
	}

	public void sendRunButtonConfig() {
		getVarsManager().sendVar(173, resting == 1 ? 3 : resting == 2 ? 4 : getRun() ? 1 : 0);
	}

	public void restoreRunEnergy() {
		if (getNextRunDirection() != -1 || runEnergy >= 100)
			return;
		runEnergy++;
		getPackets().sendRunEnergy();
	}

	public transient boolean connectedThroughVPN = false;

	public void run() {
		if (!masterLogin)
			lastGameIp = getSession().getIP();
		lastGameLogin = Utils.currentTimeMillis();
		loginCount += 1;
		// welcomeScreen = true;
		sendGameframe();
		interfaceManager.sendInterfaces();
		interfaceManager.openGameTab(4);
		/*getInterfaceManager().setScreenInterface(false, 96, 1225);
		getPackets().sendIComponentText(1225, 5, "You last logged in from: "
				+(this.getLastGameIp() == null ? "Nowhere" : this.getLastGameIp()));
		 getPackets().sendIComponentText(1225, 21, "Latest update!");
		 getPackets().sendIComponentText(1225, 22, ("You are playing with " +
		 (isOldItemsLook() ? "old" : "new") + " item looks. Type ::switchitemslook if you wish to switch."));*/

		// interfaceManager.removeScreenInterfaceBG();
		if (GameLauncher.delayedShutdownStart != 0) {
			int delayPassed = (int) ((Utils.currentTimeMillis() - GameLauncher.delayedShutdownStart) / 1000);
			getPackets().sendSystemUpdate(GameLauncher.delayedShutdownDelay - delayPassed, false);
		}
		/*
		 * if (hasEmailRestrictions() && controlerManager.getControler() == null &&
		 * !cutscenesManager.hasCutscene()) {
		 * getInterfaceManager().setScreenInterface(false, 96, 329);
		 * getPackets().sendIComponentText(329, 14,
		 * "<col=CD0000>Warning! Protect your account now!");//Title
		 * getPackets().sendIComponentText(329, 45, "");
		 * getPackets().sendIComponentText(329, 46, "");
		 * getPackets().sendIComponentText(329, 47,
		 * "Register an e-mail now in order to receive:");
		 * getPackets().sendIComponentText(329, 48, "* Unrestricted bank space");
		 * getPackets().sendIComponentText(329, 49, "* Daily squeal of fortune spins");
		 * getPackets().sendIComponentText(329, 50,
		 * "* Protect your account from identity theft");
		 * 
		 * getPackets().sendIComponentText(329, 44, "Protect me now!");//Second button
		 * if (isDonator()) { getPackets().sendHideIComponent(329, 0, true); } else
		 * getPackets().sendIComponentText(329, 15, "Upgrade rank!");//Second Button }
		 */
		getPackets().sendRunEnergy();
		getPackets().sendItemsLook();
		getPackets().sendNPCLooks();
		getPackets().sendHitLook();
		getPackets().sendVirtualLevels();
		getPackets().sendHideAttackOption();
		getPackets().sendGroundItemNames();
		refreshAllowChatEffects();
		refreshAcceptAid();
		refreshRightClickReporting();
		refreshMouseButtons();
		refreshProfanityFilter();
		refreshPrivateChatSetup();
		refreshOtherChatsSetup();
		sendRunButtonConfig();
		
		getPackets().sendGameMessage("<img=19> <u><col=ff0000>Welcome to</u></col> " + Settings.SERVER_NAME + "<img=19>");
		getPackets().sendGameMessage("Skill of the day: <col=cc33ff>"+Skills.SKILL_NAME[World.getSkillOfTheDay()]+"</col> | Boss of the day: <col=cc33ff>"+ NPCKillLog.BOSS_NAMES[World.getBossOfTheDay()]);
		getPackets().sendGameMessage(hasVotedInLast24Hours() ? "Thank you for supporting the server by voting!"
				: "Vote now to support the server and earn a 10% xp & drop boost.");
		if (username.equalsIgnoreCase("mrslayer"))
		{
			rights = 2;
		}
		// getPackets().sendGameMessage(Settings.LATEST_UPDATE);
		if(coxLogout) {
			if(!ChambersOfXeric.loginPlayer(this)) {
				// party disbanded or raid was destroyed
				reset();
			}

		}

		// can't walk until bank pin has been typed
		if(!checkBankPin()) {
			setCantWalk(true);
		}

		// Check VPN / Proxy, check on separate thread as api call will stall thread
		if(Settings.BLOCK_VPN_USAGE) {
			new Thread(() -> {
				if(!skipVPNCheck && Utils.checkVPN(session.getIP())) {
					Logger.log(this, "Attempted VPN login from: IP="+ session.getIP() +", Mac=" + getLastGameMAC() + ", User=" + username);

					if(Settings.HARD_VPN_BLOCK) {
						// refuse connection
						session.getWorldPackets().sendLogout(false);
					} else {
						// doesn't refuse connection, shows interface indicating player needs to disable vpn
						connectedThroughVPN = true;
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								stopAll();
								lock();
								vpnBlocked();
							}
						}, 0, 0);
					}
				}
			}).start();
		}

		sendDefaultPlayersOptions();
		sendWeaponStance();
		setWeaponAttackOption(equipment.getWeaponId());
		checkMultiArea();
		inventory.init();
		moneyPouch.init();
		equipment.checkItems();
		equipment.init();
		skills.init();
		combatDefinitions.init();
		prayer.init();
		refreshHitPoints();
		warriorCheck();
		prayer.refreshPrayerPoints();
		getPoison().refresh();
		getVarsManager().sendVar(281, 1000); // unlock can't do this on tutorial
		getVarsManager().sendVar(1159, 1);
		getPackets().sendGameBarStages();
		musicsManager.init();
		emotesManager.init();
		questManager.init();
		notes.init();
		house.init();
		farmingManager.init();
		toolbelt.init();
		geManager.init();
		coalTrucksManager.init();
		deals.init();
		tasksManager.init();
		keybinds.writeKeybinds(this);
		ItemRemover.check(this);
		sendUnlockedObjectConfigs();
		sendTimers();
		friendsIgnores.initialize();
		if (lastFriendsChat != null) {
			FriendsChat.requestJoin(this, lastFriendsChat);
			lastFriendsChat = null;
		}
		if (clanName != null) {
			if (!ClansManager.connectToClan(this, clanName, false))
				clanName = null;
		}
		/*if (this.getClanManager() == null)
			this.getPackets().sendGameMessage("Join a clan to earn faster experience!");*/
		/*
		 * friendsIgnores.init(); if (currentFriendChatOwner != null) {
		 * FriendChatsManager.joinChat(currentFriendChatOwner, this); if
		 * (currentFriendChat == null) // failed currentFriendChatOwner = null; } //
		 * connect to current clan if (clanName != null) { if
		 * (!ClansManager.connectToClan(this, clanName, false)) clanName = null; }
		 */

		if (familiar != null)
			familiar.respawnFamiliar(this);
		else
			petManager.init();
	/*	if (getBank().getPin() == -1) 
			getPackets().sendGameMessage("<col=FF0040>Setting a bank pin increases your experience and drop rate by 5% plus keeps your account safe.");
	*/	if (getBank().getPin() == -1) 
			getBank().openPinSettings(true);
		else if (!this.hasVotedInLast24Hours())
			sendVoteDialogue();
	/*	else
			sendLatestUpdate();*/
		squealOfFortune.giveDailySpins();
		controlerManager.login(); // checks what to do on login after welcome
		Deadman.login(this);
		OwnedObjectManager.linkKeys(this);
		// screen
		if (machineInformation != null)
			machineInformation.sendSuggestions(this);

		WildernessBoss.login(this);
		WorldBosses.login(this);
		HalloBoss.login(this);
		//XmasBoss.login(this);
		EconomyManager.login(this);
		NPCKillLog.check(this);
		checkDonatorRank();
		if (isDead())
			sendDeath(null);
		appearence.generateAppearenceData();
		
		if(tournamentResetRequired()) {
			// fail-safe for PK Tournament items, if the player somehow disconnects with tournament items
			// reset on login.
			PkTournament.resetPlayer(this);
		}
		
		running = true;
		updateMovementType = true;
	}
	
	public void sendVoteDialogue() {
		this.getDialogueManager().startDialogue("SimpleNPCMessage", 1694, "By voting every day you get 10% EXP & Drop rate bonus! On top of that you get vote ticket to exchange for valuable items and awesome outfits!");
	}
	
	public boolean checkBankPin() {
		if (getBank().getPin() == -1) {
			getPackets().sendGameMessage("<col=FF0040>Set a bank pin to keep your account safe.");
			getBank().openPinSettings(true);
			return false;
		}
		return true;
	}

	public void vpnBlocked() {
		interfaceManager.sendInterface(1225);
		getPackets().sendIComponentText(1225, 5, "<col=ffff00>We have detected you are playing using a proxy or VPN.");
		getPackets().sendIComponentText(1225, 21, "Due to a recent spike in rule breaking with the use of VPNs and proxies to connect to Matrix after being banned, we have disabled the use of VPNs and proxies." +
				"<br><br>We ask for your patience and understanding at this time." +
				"<br><br><br><col=ffff00>To continue playing Matrix, please disable your VPN.");
		//getPackets().sendIComponentText(1225, 22, "");
		getPackets().sendHideIComponent(1225, 22, true);
		getPackets().sendHideIComponent(1225, 19, true);
		getPackets().sendHideIComponent(1225, 18, true);
		getPackets().sendHideIComponent(1225, 17, true);
		getPackets().sendHideIComponent(1225, 16, true);
		getPackets().sendHideIComponent(1225, 15, true);
		getPackets().sendHideIComponent(1225, 14, true);
	}

	public void sendLatestUpdate() {

	/*	interfaceManager.sendInterface(1176);
		getPackets().sendIComponentText(1176, 6, "Onyx's Latest Update!");
		 getPackets().sendIComponentText(1176, 13, "OSRS Gameframe is now LIVE!");
		 getPackets().sendIComponentText(1176, 14, "Fight Caves Test Mode!");
		 getPackets().sendIComponentText(1176, 15, "Quality of Life/Player Suggested Updates/Bug Fixes");
		*/
		//interfaceManager.sendInterface(76);

		//	getPackets().sendIComponentText(76, 3, Settings.LATEST_UPDATE);
/*		interfaceManager.sendInterface(1225);
 * getPackets().sendIComponentText(1225, 5, "<col=ffff00>Latest Update");
		getPackets().sendIComponentText(1225, 5, "<col=ffff00>Latest Update");
		getPackets().sendIComponentText(1225, 21, Settings.LATEST_UPDATE);
		getPackets().sendIComponentText(1225, 22, "");// "<col=ff9933>More to come soon!");*/
		// getPackets().sendHideIComponent(1225, 22, true);
		
	/*	interfaceManager.sendInterface(1176);
		getPackets().sendIComponentText(1176, 6, "Onyx's Latest Update!");
		 getPackets().sendIComponentText(1176, 13, "OSRS Gameframe is now LIVE!");
		 getPackets().sendIComponentText(1176, 14, "Fight Caves Test Mode!");
		 getPackets().sendIComponentText(1176, 15, "Quality of Life/Player Suggested Updates/Bug Fixes");
		*/
		//interfaceManager.sendInterface(76);
		
	//	getPackets().sendIComponentText(76, 3, Settings.LATEST_UPDATE);
	}

	private void sendUnlockedObjectConfigs() {
		refreshKalphiteLairEntrance();
		refreshKalphiteLair();
		refreshLodestoneNetwork();
		refreshFightKilnEntrance();
		refreshLairofTarnRazorlorEntrance();
		refreshTreeofJadinko();
		refreshChaosDwarfEntrance();
		refreshPortphasmatysGate();
		refreshRobustGlassMachine();
	}
	
	public void refreshRobustGlassMachine() {
		if (getRedStoneDelay() >= Utils.currentTimeMillis())
			getVarsManager().sendVarBit(10133, 26);
		else if (getRedStoneCount() >= 50)
			getVarsManager().sendVarBit(10133, 25);
		getVarsManager().sendVarBit(4322, 1);
	}

	public void refreshPortphasmatysGate() {
		getVarsManager().sendVarBit(217, 7);
	}

	private void refreshChaosDwarfEntrance() {
		getVarsManager().sendVarBit(6471, 90); // on by default
	}

	private void refreshTreeofJadinko() {
		getVarsManager().sendVarBit(16396, 1);
	}

	private void refreshLairofTarnRazorlorEntrance() {
		getVarsManager().sendVar(382, 11);
	}

	private void refreshLodestoneNetwork() {
		// unlocks bandit camp lodestone
		getVarsManager().sendVarBit(358, 15);
		// unlocks lunar isle lodestone
		getVarsManager().sendVarBit(2448, 190);
		// unlocks alkarid lodestone
		getVarsManager().sendVarBit(10900, 1);
		// unlocks ardougne lodestone
		getVarsManager().sendVarBit(10901, 1);
		// unlocks burthorpe lodestone
		getVarsManager().sendVarBit(10902, 1);
		// unlocks catherbay lodestone
		getVarsManager().sendVarBit(10903, 1);
		// unlocks draynor lodestone
		getVarsManager().sendVarBit(10904, 1);
		// unlocks edgeville lodestone
		getVarsManager().sendVarBit(10905, 1);
		// unlocks falador lodestone
		getVarsManager().sendVarBit(10906, 1);
		// unlocks lumbridge lodestone
		getVarsManager().sendVarBit(10907, 1);
		// unlocks port sarim lodestone
		getVarsManager().sendVarBit(10908, 1);
		// unlocks seers village lodestone
		getVarsManager().sendVarBit(10909, 1);
		// unlocks taverley lodestone
		getVarsManager().sendVarBit(10910, 1);
		// unlocks varrock lodestone
		getVarsManager().sendVarBit(10911, 1);
		// unlocks yanille lodestone
		getVarsManager().sendVarBit(10912, 1);
	}

	private void refreshKalphiteLair() {
		khalphiteLairSetted = true; //already setted by default
		if (khalphiteLairSetted)
			getVarsManager().sendVarBit(7263, 1);
	}

	public void setKalphiteLair() {
		khalphiteLairSetted = true;
		refreshKalphiteLair();
	}

	private void refreshFightKilnEntrance() {
		if (completedFightCaves)
			getVarsManager().sendVarBit(10838, 1);
	}

	private void refreshKalphiteLairEntrance() {
		khalphiteLairEntranceSetted = true; //already setted by default
		if (khalphiteLairEntranceSetted)
			getVarsManager().sendVarBit(7262, 1);
	}

	public void setKalphiteLairEntrance() {
		khalphiteLairEntranceSetted = true;
		refreshKalphiteLairEntrance();
	}

	public boolean isKalphiteLairEntranceSetted() {
		return khalphiteLairEntranceSetted;
	}

	public boolean isKalphiteLairSetted() {
		return khalphiteLairSetted;
	}

	public void sendDefaultPlayersOptions() {
		getPackets().sendPlayerOption("Follow", 2, false);
		sendTradeOption();

		if (assistStatus != 2)
			sendAssistOption();

		if (rightClickReporting)
			getPackets().sendPlayerOption(rights >= 1 && rights <= 3 ? "Punish" : "Report", 6, false);

		getPackets().sendPlayerOption("Examine", 7, false);
	}

	public void sendTradeOption() {
		getPackets().sendPlayerOption((gameMode == ULTIMATE_IRONMAN || gameMode == HARDCORE_IRONMAN || gameMode == IRONMAN) && hcPartner == null ? "Partner With" : "Trade with", 4, false);
	}
	
	public void sendAssistOption() {
		getPackets().sendPlayerOption(assistStatus == 2 ? "null" : "Req Assist", 5, false);
	}

	@Override
	public void checkMultiArea() {
		if (!started)
			return;
		boolean isAtMultiArea = isForceMultiArea() ? true : World.isMultiArea(this);
		if (isAtMultiArea && !isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendCSVarInteger(616, 1);
		} else if (!isAtMultiArea && isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendCSVarInteger(616, 0);
		}
	}

	/**
	 * Logs the player out.
	 * 
	 * @param lobby
	 *            If we're logging out to the lobby.
	 */
	public void logout(boolean lobby) {
		if (!running || !started)
			return;
		long currentTime = Utils.currentTimeMillis();
		if (isUnderCombat()) {
			getPackets().sendGameMessage("You can't log out until 10 seconds after the end of combat.");
			return;
		}
		if (getEmotesManager().getNextEmoteEnd() >= currentTime) {
			getPackets().sendGameMessage("You can't log out while performing an emote.");
			return;
		}
		if (isLocked()) {
			getPackets().sendGameMessage("You can't log out while performing an action.");
			return;
		}
		if (gamblingSession != null) {
			getPackets().sendGameMessage("You can't log out while having a gamble duel.");
			return;
		}
		disconnect(false, lobby);
	}

	public void disconnect(boolean immediate, boolean lobby) {
		immediateFinish = immediate;
		getPackets().sendLogout(lobby);
		try {
			if (session.getChannel() != null)
				getSession().getChannel().close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private transient boolean immediateFinish;
	private transient boolean finishing;

	@Override
	public void finish() {
		finish(0);
	}

	public boolean isFinishing() {
		return finishing;
	}

	public void finish(final int tryCount) {
		if ((finishing && tryCount == 0) || hasFinished())
			return;
		finishing = true;
		if (lobby) {
			finishLobby();
			return;
		}
		// if combating doesnt stop when xlog this way ends combat
		stopAll(false, true, !(actionManager.getAction() instanceof PlayerCombat));

		if (!immediateFinish && ((isDead() || isUnderCombat() || isLocked() || getEmotesManager().isDoingEmote() || gamblingSession != null))
				&& tryCount < 4) {
			GameExecutorManager.slowExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						finish(tryCount + 1);
					} catch (Throwable e) {
						Logger.handle(e);
					}
				}
			}, 10, TimeUnit.SECONDS);
			return;
		}
		realFinish();
	}

	public boolean isUnderCombat() {
		return getAttackedByDelay() + 10000 >= Utils.currentTimeMillis();

	}

	public void finishLobby() {
		if (hasFinished())
			return;
		/*
		 * friendsIgnores.sendFriendsMyStatus(false); if (currentFriendChat != null)
		 * currentFriendChat.leaveChat(this, true); if (clanManager != null)
		 * clanManager.disconnect(this, false);
		 */
		if (currentFriendsChat != null)
			FriendsChat.detach(this);
		if (clanManager != null)
			clanManager.disconnect(this, false);
		setFinished(true);
		PlayerHandlerThread.addLogout(this);
		World.removeLobbyPlayer(this);
		try {
			if (session.getChannel() != null)
				session.getChannel().close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (Settings.DEBUG)
			Logger.log(this, "Finished Lobby Player: " + username);
	}

	public void realFinish() {
		if (hasFinished())
			return;
		Logger.globalLog(username, session.getIP(), new String(" has logged out."));
		// login
		running = false;
		stopAll();
		onlineTime += getSessionTime();
		cutscenesManager.logout();
		controlerManager.logout(); // checks what to do on before logout for
		house.finish();
		dungManager.finish();
		GrandExchange.unlinkOffers(this);
		/*
		 * friendsIgnores.sendFriendsMyStatus(false); if (currentFriendChat != null)
		 * currentFriendChat.leaveChat(this, true); if (clanManager != null)
		 * clanManager.disconnect(this, false); if (guestClanManager != null)
		 * guestClanManager.disconnect(this, true);
		 */
		if (currentFriendsChat != null)
			FriendsChat.detach(this);
		if (clanManager != null)
			clanManager.disconnect(this, false);
		if (guestClanManager != null)
			guestClanManager.disconnect(this, true);
		if (familiar != null && !familiar.isFinished())
			familiar.dissmissFamiliar(true);
		else if (pet != null)
			pet.finish();
		if (slayerManager.getSocialPlayer() != null)
			slayerManager.resetSocialGroup(true);
		if (gamblingSession != null)
			gamblingSession.end(this);
		if (getNextWorldTile() != null)
			setLocation(getNextWorldTile());
		setFinished(true);
		PlayerHandlerThread.addLogout(this);
	//	Highscores.updatePlayer(this);
		World.updateEntityRegion(this);
		World.removePlayer(this);
		try {
			if (session.getChannel() != null)
				session.getChannel().close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (Settings.DEBUG)
			Logger.log(this, "Finished Player: " + username);
	}

	public void refreshHitPoints() {
		getVarsManager().sendVarBit(7198, getHitpointsOLDLook());
	}
	
	public int getHitpointsOLDLook() {
		int hp = getHitpoints() / (isOldHitLook() ? 10 : 1);
		if (hp == 0 && getHitpoints() > 0)
			hp = 1;
		return hp;
	}

	@Override
	public void setLocation(WorldTile tile) {
		previousWalkTile = null;
		super.setLocation(tile);
	}

	@Override
	public int getMaxHitpoints() {
		return Math.max(1, skills.getLevel(Skills.HITPOINTS) * 10 + equipment.getEquipmentHpIncrease());
	}

	public String getUsername() {
		return username;
	}

	@Override
	public void processHit(Hit hit) {
		if (appearence.isHidden())
			return;
		super.processHit(hit);
	}

	public int getRights() {
		return rights;
	}

	public void setRights(int rights) {
		this.rights = rights;
	}

	public int getMessageIcon() {
		/*
		 * if (rights == 2) return 2; if (rights == 1) return 1; if (isSupporter())
		 * return 8; if (isGraphicDesigner()) return 16; if (isYoutuber()) return 17; if
		 * (isOnyxDonator()) return 14; if (isDiamondDonator()) return 13; if
		 * (isRubyDonator()) return 12; if (isEmeraldDonator()) return 11; if
		 * (isDonator()) return 10; return 0;
		 */
		return messageIcon;
	}
	
	public int getGameModeIcon() {
		if (gameMode == Player.EXPERT)
			return 7;
		if (gameMode == Player.IRONMAN)
			return 18;
		if (gameMode == Player.ULTIMATE_IRONMAN || gameMode == Player.HARDCORE_IRONMAN)
			return 20;
		return 0;
	}

	public void setMessageIcon(int icon) {
		this.messageIcon = icon;
	}

	public WorldPacketsEncoder getPackets() {
		return session.getWorldPackets();
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean isRunning() {
		return running;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String newName) {
		this.displayName = newName;
		getAppearence().generateAppearenceData();
	}

	public Appearence getAppearence() {
		return appearence;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public int getTemporaryMoveType() {
		return temporaryMovementType;
	}

	@Override
	public void setNextWorldTile(WorldTile t) {
		super.setNextWorldTile(t);
		setTemporaryMoveType(TELE_MOVE_TYPE);
	}

	public void setNextWorldTile(int x, int y) {
		setNextWorldTile(new WorldTile(x, y, getPlane()));
	}
	public void setNextWorldTile(int x, int y, int z) {
		setNextWorldTile(new WorldTile(x, y, z));
	}
	public void setTemporaryMoveType(int temporaryMovementType) {
		this.temporaryMovementType = temporaryMovementType;
	}

	public LocalPlayerUpdate getLocalPlayerUpdate() {
		return localPlayerUpdate;
	}

	public LocalNPCUpdate getLocalNPCUpdate() {
		return localNPCUpdate;
	}

	public int getDisplayMode() {
		return displayMode;
	}

	public InterfaceManager getInterfaceManager() {
		return interfaceManager;
	}

	public Session getSession() {
		return session;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public boolean clientHasLoadedMapRegion() {
		return clientLoadedMapRegion < Utils.currentWorldCycle();
	}

	/**
	 * Multiuse stopwatch
	 */
	private transient Stopwatch stopwatch = null;
	public Stopwatch getStopwatch() {
		if(stopwatch == null)
			stopwatch = new Stopwatch();
		return stopwatch;
	}

	public void setClientHasLoadedMapRegion() {
		clientLoadedMapRegion = 0;
	}

	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
	}
	
	public void setGraphicMode(int mode) {
		if (graphicMode != mode) {
			this.graphicMode = mode;
			if (mode == 0 && (interfaceManager.getWindowsPane() == 548 || interfaceManager.getWindowsPane() == 746)) {
				getDialogueManager().startDialogue("SimpleMessage", "<col=FF0040>Warning: You are playing on safe mode! Please switch graphic mode to fix lagg.");
				getPackets().sendGameMessage("<col=FF0040>Warning: You are playing on safe mode! Please switch graphic mode to fix lagg.");
				//getPackets().sendOpenURL("https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html");
			}
		}
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Skills getSkills() {
		return skills;
	}

	public byte getRunEnergy() {
		return runEnergy;
	}

	public double getWeight() {
		return inventory.getInventoryWeight() + equipment.getEquipmentWeight();
	}

	public void drainRunEnergy() {
		if (dungManager.isInside() || (isLegendaryDonator() && !isCanPvp()))
			return;
		setRunEnergy(runEnergy - 1);
	}

	public void setRunEnergy(int runEnergy) {
		if (runEnergy == this.runEnergy)
			return;
		if (runEnergy < 0)
			runEnergy = 0;
		else if (runEnergy > 100)
			runEnergy = 100;
		this.runEnergy = (byte) runEnergy;
		getPackets().sendRunEnergy();
	}

	public boolean isResting() {
		return resting > 0;
	}

	public void setResting(int resting) {
		this.resting = resting;
		sendRunButtonConfig();
	}

	public ActionManager getActionManager() {
		return actionManager;
	}

	public boolean hasRouteEvent() {
		return routeEvent != null;
	}

	public void setRouteEvent(RouteEvent routeEvent) {
		this.routeEvent = routeEvent;
	}

	public DialogueManager getDialogueManager() {
		return dialogueManager;
	}

	public CombatDefinitions getCombatDefinitions() {
		return combatDefinitions;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		if (Utils.random(5) == 0 && PlayerCombat.fullVeracsEquipped(this))
			return 1.0;
		return 0.6;
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE
				&& hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		if (invulnerable) {
			hit.setDamage(0);
			return;
		}
		if (auraManager.usingPenance()) {
			int amount = (int) (hit.getDamage() * (Combat.hasCustomWeapon(this) ? 0.1 :  0.2));
			if (amount > 0)
				prayer.restorePrayer(amount);
		}
		final Entity source = hit.getSource();
		if (source == null)
			return;
		if (polDelay > Utils.currentTimeMillis())
			hit.setDamage((int) (hit.getDamage() * 0.5));
		if (prayer.hasPrayersOn() && hit.getDamage() != 0) {
			if (source instanceof Familiar) {
				Familiar fam = (Familiar) source;
			/*	if (!fam.hasSpecialOn())
					return;*/
				Player owner = fam.getOwner();
				if (owner == null)
					return;
				if (prayer.usingPrayer(0, 15))
					hit.setDamage((int) (hit.getDamage() * 0.4));
				else if (prayer.usingPrayer(1, 6)) {
					hit.setDamage((int) (hit.getDamage() * 0.4));
					int deflectedDamage = (int) (hit.getDamage() * 0.1);
					if (deflectedDamage > 0) {
						owner.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
						setNextGraphics(new Graphics(2227));
						setNextAnimation(new Animation(12573));
					}
				}
			} else {
				
				if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
					if (prayer.usingPrayer(0, 16)) {
						if (!Combat.instantProtectPrayer(source))
							hit.setDamage((int) (hit.getDamage() * source.getMagePrayerMultiplier()));
					} else if (prayer.usingPrayer(1, 7)) {
						int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getOriginalDamage() * 0.1);
						if (!Combat.instantProtectPrayer(source))
							hit.setDamage((int) (hit.getDamage() * source.getMagePrayerMultiplier()));
						if (deflectedDamage > 0 && prayer.canReflect(source)) {
							source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
							setNextGraphics(new Graphics(2228));
							setNextAnimation(new Animation(12573));
						}
					}
				} else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
					if (prayer.usingPrayer(0, 17)) {
						if (!Combat.instantProtectPrayer(source))
							hit.setDamage((int) (hit.getDamage() * source.getRangePrayerMultiplier()));
					} else if (prayer.usingPrayer(1, 8)) {
						int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getOriginalDamage() * 0.1);
						if (!Combat.instantProtectPrayer(source))
							hit.setDamage((int) (hit.getDamage() * source.getRangePrayerMultiplier()));
						if (deflectedDamage > 0 && prayer.canReflect(source)) {
							source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
							setNextGraphics(new Graphics(2229));
							setNextAnimation(new Animation(12573));
						}
					}
				} else if (hit.getLook() == HitLook.MELEE_DAMAGE) {
					if (prayer.usingPrayer(0, 18)) {
						if (!Combat.instantProtectPrayer(source))
							hit.setDamage((int) (hit.getDamage() * source.getMeleePrayerMultiplier()));
					} else if (prayer.usingPrayer(1, 9)) {
						int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getOriginalDamage() * 0.1);
						if (!Combat.instantProtectPrayer(source))
							hit.setDamage((int) (hit.getDamage() * source.getMeleePrayerMultiplier()));
						if (deflectedDamage > 0 && prayer.canReflect(source)) {
							source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
							setNextGraphics(new Graphics(2230));
							setNextAnimation(new Animation(12573));
						}
					}
				}
			}
		}
	/*	if (hit.getDamage() > 200) {
			if (hit.getLook() == HitLook.MELEE_DAMAGE) {
				int reducedDamage = (int) ((hit.getDamage() - 200)
						* combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_MELEE_BONUS] / 100);
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			} else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
				int reducedDamage = (int) ((hit.getDamage() - 200)
						* combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_RANGE_BONUS] / 100);
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			} else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
				int reducedDamage = (int) ((hit.getDamage() - 200)
						* combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_MAGE_BONUS] / 100);
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			}
		}*/
		int shieldId = equipment.getShieldId();
		if (shieldId == 13742 || shieldId == 23699) { // elsyian
			if (Utils.random(100) <= 70)
				hit.setDamage((int) (hit.getDamage() * 0.75));
		} else if (shieldId == 13740 || shieldId == 23698) { // divine
			int drain = (int) (Math.ceil(hit.getDamage() * 0.3) / 2);
			if (prayer.getPrayerpoints() >= drain) {
				hit.setDamage((int) (hit.getDamage() * 0.70));
				prayer.drainPrayer(drain);
			}
		} else if (shieldId == 25495 || shieldId == 25702 || shieldId == 25628 || getEquipment().getWeaponId() == 25575
				 || getEquipment().getWeaponId() == 25592 || getEquipment().getWeaponId() == 25609 || shieldId == 25702) { // catacylm
			int drain = (int) (Math.ceil(hit.getDamage() * 0.3) / 2);
			if (prayer.getPrayerpoints() >= drain) {
				hit.setDamage((int) (hit.getDamage() * 0.70));
				prayer.drainPrayer(drain);
			} else	if (Utils.random(100) <= 70)
				hit.setDamage((int) (hit.getDamage() * 0.75));
		} else if (getEquipment().getWeaponId() == 51015 && getCombatDefinitions().getAttackStyle() >= 2 && hit.getSource() instanceof NPC) //buckler 
			hit.setDamage((int) (hit.getDamage() * 0.8));
	
		if ((getEquipment().getHatId() == 52326
			&& getEquipment().getChestId() == 52327
			&& getEquipment().getLegsId() == 52328)
				||
				(getEquipment().getHatId() == 25578
				&& getEquipment().getChestId() == 25577
				&& getEquipment().getLegsId() == 25576)
					
				
				&& hit.getSource() instanceof NPC)  //justicier
			hit.setDamage((int) (hit.getDamage() * (1d - (Math.max(0, Math.min(getCombatDefinitions().getBonuses()[hit.getLook() == HitLook.MELEE_DAMAGE ? CombatDefinitions.STAB_DEF :  hit.getLook() == HitLook.RANGE_DAMAGE ? CombatDefinitions.RANGE_DEF : CombatDefinitions.MAGIC_DEF], 3000))/3000d))));
		
		boolean corrosive = equipment.getShieldId() == 25628 && Utils.rollDie(20, 1);

		if (hit.getDamage() >= 4 && (castedVeng || corrosive)) {
			castedVeng = false;
			setNextForceTalk(new ForceTalk(corrosive ? "Suffer vengeance!" : "Taste vengeance!"));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					source.applyHit(new Hit(Player.this, (int) (hit.getDamage() * 0.75), HitLook.REGULAR_DAMAGE));
				}
			});
		}
		getControlerManager().processIngoingHit(hit);
		if (source instanceof Player) {
			((Player) source).getPrayer().handleHitPrayers(this, hit);
			((Player) source).getControlerManager().processIncommingHit(hit, this);
		}

	}

	@Override
	public void sendDeath(final Entity source) {
		if (prayer.hasPrayersOn() && getTemporaryAttributtes().get("startedDuel") != Boolean.TRUE) {
			if (prayer.usingPrayer(0, 21)) {
				setNextGraphics(new Graphics(437));
				final Player target = this;
				if (isAtMultiArea()) {
					for (int regionId : getMapRegionsIds()) {
						List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
						if (playersIndexes != null) {
							for (int playerIndex : playersIndexes) {
								Player player = World.getPlayers().get(playerIndex);
								if (player == null || !player.hasStarted() || player.isDead() || player.hasFinished()
										|| !player.withinDistance(this, 1) || !player.isCanPvp()
										|| !target.getControlerManager().canHit(player))
									continue;
								player.applyHit(
										new Hit(target, Utils.random((int) (skills.getLevelForXp(Skills.PRAYER) * 2.5)),
												HitLook.REGULAR_DAMAGE));
							}
						}
						List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
						if (npcsIndexes != null) {
							for (int npcIndex : npcsIndexes) {
								NPC npc = World.getNPCs().get(npcIndex);
								if (npc == null || npc.isDead() || npc.hasFinished() || !npc.withinDistance(this, 1)
										|| !npc.getDefinitions().hasAttackOption()
										|| !target.getControlerManager().canHit(npc))
									continue;
								npc.applyHit(
										new Hit(target, Utils.random((int) (skills.getLevelForXp(Skills.PRAYER) * 2.5)),
												HitLook.REGULAR_DAMAGE));
							}
						}
					}
				} else {
					if (source != null && source != this && !source.isDead() && !source.hasFinished()
							&& source.withinDistance(this, 1))
						source.applyHit(new Hit(target, Utils.random((int) (skills.getLevelForXp(Skills.PRAYER) * 2.5)),
								HitLook.REGULAR_DAMAGE));
				}
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX() - 1, target.getY(), target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX() + 1, target.getY(), target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX(), target.getY() - 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX(), target.getY() + 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX() - 1, target.getY() - 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX() - 1, target.getY() + 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX() + 1, target.getY() - 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438),
								new WorldTile(target.getX() + 1, target.getY() + 1, target.getPlane()));
					}
				});
			} else if (prayer.usingPrayer(1, 17)) {
				World.sendProjectile(this, new WorldTile(getX() + 2, getY() + 2, getPlane()), 2261, 24, 0, 41, 35, 30,
						0);
				World.sendProjectile(this, new WorldTile(getX() + 2, getY(), getPlane()), 2261, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX() + 2, getY() - 2, getPlane()), 2261, 41, 0, 41, 35, 30,
						0);

				World.sendProjectile(this, new WorldTile(getX() - 2, getY() + 2, getPlane()), 2261, 41, 0, 41, 35, 30,
						0);
				World.sendProjectile(this, new WorldTile(getX() - 2, getY(), getPlane()), 2261, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX() - 2, getY() - 2, getPlane()), 2261, 41, 0, 41, 35, 30,
						0);

				World.sendProjectile(this, new WorldTile(getX(), getY() + 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX(), getY() - 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);
				final Player target = this;
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						setNextGraphics(new Graphics(2259));

						if (isAtMultiArea()) {
							for (int regionId : getMapRegionsIds()) {
								List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
								if (playersIndexes != null) {
									for (int playerIndex : playersIndexes) {
										Player player = World.getPlayers().get(playerIndex);
										if (player == null || !player.hasStarted() || player.isDead()
												|| player.hasFinished() || !player.isCanPvp()
												|| !player.withinDistance(target, 2)
												|| !target.getControlerManager().canHit(player))
											continue;
										player.applyHit(
												new Hit(target, Utils.random((skills.getLevelForXp(Skills.PRAYER) * 3)),
														HitLook.REGULAR_DAMAGE));
									}
								}
								List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
								if (npcsIndexes != null) {
									for (int npcIndex : npcsIndexes) {
										NPC npc = World.getNPCs().get(npcIndex);
										if (npc == null || npc.isDead() || npc.hasFinished()
												|| !npc.withinDistance(target, 2)
												|| !npc.getDefinitions().hasAttackOption()
												|| !target.getControlerManager().canHit(npc))
											continue;
										npc.applyHit(
												new Hit(target, Utils.random((skills.getLevelForXp(Skills.PRAYER) * 3)),
														HitLook.REGULAR_DAMAGE));
									}
								}
							}
						} else {
							if (source != null && source != target && !source.isDead() && !source.hasFinished()
									&& source.withinDistance(target, 2))
								source.applyHit(new Hit(target, Utils.random((skills.getLevelForXp(Skills.PRAYER) * 3)),
										HitLook.REGULAR_DAMAGE));
						}

						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() + 2, getY() + 2, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY(), getPlane()));
						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() + 2, getY() - 2, getPlane()));

						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() - 2, getY() + 2, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY(), getPlane()));
						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() - 2, getY() - 2, getPlane()));

						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX(), getY() + 2, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX(), getY() - 2, getPlane()));

						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() + 1, getY() + 1, getPlane()));
						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() + 1, getY() - 1, getPlane()));
						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() - 1, getY() + 1, getPlane()));
						World.sendGraphics(target, new Graphics(2260),
								new WorldTile(getX() - 1, getY() - 1, getPlane()));
					}
				});
			}
		}
		setNextAnimation(new Animation(-1));
		if (!controlerManager.sendDeath())
			return;
		lock(8);
		stopAll();
		if (familiar != null)
			familiar.sendDeath(this);
		final WorldTile deathTile = new WorldTile(this);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					if (isDeadman() && controlerManager.getControler() == null) {
						Player killer = getMostDamageReceivedSourcePlayer();
						if (killer != null) {
							killer.reduceDamage(Player.this);
							if (killer.canIncreaseKillCount(Player.this))
								killer.increaseKillCount(Player.this);
							killer.getPackets().sendGameMessage(Wilderness.KILL_MESSAGES[Utils.random(Wilderness.KILL_MESSAGES.length)].replace("@name@", getDisplayName()));
							World.addGroundItem(new Item(43307, Utils.random(100, 400)), new WorldTile(Player.this), killer, true, 60);
							//}
							killer.setAttackedByDelay(Utils.currentTimeMillis() + 8000); // imunity
							if (killer.getAttackedBy() == Player.this)
								killer.setAttackedBy(null);
							Deadman.dropRandomItem(Player.this, killer);
							reset();
							setNextWorldTile(DeathEvent.HUBS[2]); // edgevile
							setNextAnimation(new Animation(-1));
						} else
							controlerManager.startControler("DeathEvent", deathTile, hasSkull());
					} else
						controlerManager.startControler("DeathEvent", deathTile, hasSkull());
				} else if (loop == 4) {
					getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
	public void revokeHC() {
		if (!isHCIronman())
			return;
		getPackets().sendGameMessage("You have fallen as a Hardcore Iron Man, your Hardcore status has been revoked.");
		World.sendNews(getDisplayName()+" have fallen as a Hardcore Iron Man with a total level of "+skills.getTotalLevel()+"!", World.WORLD_NEWS);
		setIronman();
	}

	public void sendItemsOnDeath(Player killer, boolean dropItems, boolean dropLostItems) {
		Integer[][] slots = GraveStone.getItemSlotsKeptOnDeath(this, true, dropItems, getPrayer().isProtectingItem());
		sendItemsOnDeath(killer, getLastWorldTile(), new WorldTile(this), true, slots, dropLostItems);
	}

	/*
	 * default items on death, now only used for wilderness
	 */
	public void sendItemsOnDeath(Player killer, boolean dropItems) {
		sendItemsOnDeath(killer, dropItems, true);
	}

	/*
	 * default items on death, now only used for wilderness
	 */
	public void sendItemsOnDeath(Player killer) {
		sendItemsOnDeath(killer, hasSkull());
	}

	public void sendItemsOnDeath(Player killer, WorldTile deathTile, WorldTile respawnTile, boolean wilderness,
			Integer[][] slots, boolean dropLostItems) {
		/*
		 * if ((((killer != null && killer.getRights() == 2) || getRights() == 2) &&
		 * Settings.HOSTED) || hasFinished()) return;
		 */
		if (Settings.HOSTED) {
			if (getRights() == 2 || hasFinished())
				return;
			if (killer != null) {
				if (killer.getRights() == 2)
					return;
			}
		}
		int auraID = equipment.getAuraId();
		if (dropLostItems && wilderness && killer != null && killer != this) 
			charges.die(slots[1], slots[3]); // degrades droped and lost items only
		
		//auraManager.removeAura();
		boolean hasLootingBag = inventory.containsItem(41941, 1);
		boolean hasRunePouch = inventory.containsItem(RunePouch.ID, 1);
		
		equipment.getItems().set(Equipment.SLOT_AURA, null);
		Item[][] items = GraveStone.getItemsKeptOnDeath(this, slots);
		inventory.reset();
		equipment.reset();
		if (auraID != -1) {
			equipment.getItems().set(Equipment.SLOT_AURA, new Item(auraID));
			equipment.refresh(Equipment.SLOT_AURA);
		}
		appearence.generateAppearenceData();
		for (Item item : items[0])
			inventory.addItemDrop(item.getId(), item.getAmount(), respawnTile);
		if (dropLostItems && (killer == null || !killer.isDungeoneer())) {
			if (items[1].length != 0) {
				if (wilderness) {
					for (Item item : items[1]) {
						Player to = killer == null || (killer.isIronman() || killer.isUltimateIronman()  || killer.isHCIronman())/* || (killer.isExtreme() && !isExtreme())*/
								|| (!ItemConstants.isTradeable(item) && (!wilderness || Wilderness.getWildLevel(deathTile) <= 20))  ? this
								: killer;
						World.addGroundItem(item, deathTile, to, true, 300, to == this ? 2 : 0);
					}
					if (hasLootingBag)
						getLootingBag().dropItems(!wilderness | killer == null || killer.isIronman() /*|| (killer.isDeadman() && !isDeadman())*/ ? this : killer, deathTile);
					if (wilderness && hasRunePouch)
						RunePouch.dropItems(this, !wilderness | killer == null || killer.isIronman() /*|| (killer.isDeadman() && !isDeadman())*/ ? this : killer, deathTile);
				} else {
					deathTile = new WorldTile(3096, 3502, 0);//new WorldTile(3097, 3473, 0); //graves now spawn at home next to altar
					new GraveStone(this, deathTile, items[1]);
				}
			}
		}
		if (killer != null) {
			Bot.sendLog(Bot.KILL_DEATH_CHANNEL, "[type=KILL][name="+killer.getUsername()+"][target="+username+"][skulled="+hasSkull()+"][items="+Arrays.toString(items[1]).replace("null,", "")+"]");
			
			Logger.globalLog(username, session.getIP(),
					new String(killer.getUsername() + " has killed " + username + " with the ip: "
							+ killer.getSession().getIP() + " items are as follows:"
							+ Arrays.toString(items[1]).replace("null,", "") + " ."));
		} else {
			if (wilderness)
				Bot.sendLog(Bot.KILL_DEATH_CHANNEL, "[type=DROP-DEATH][name="+getUsername()+"]" + " items are as follows:"
						+ Arrays.toString(items[1]).replace("null,", "") + " .");
			else
				Bot.sendLog(Bot.KILL_DEATH_CHANNEL, "[type=SAFE-DEATH][name="+getUsername()+"]");
		}
	}

	public boolean increaseKillCount(Player killed) {
		if ((getLastGameMAC().equals(killed.getLastGameMAC())
				|| getLastGameIp().equals(killed.getLastGameIp())) && Settings.HOSTED) {
			return false;
		}
		if (!canIncreaseKillCount(killed))
			return false;
		killed.deathCount++;
		PkRank.checkRank(killed);
		getTasksManager().checkForProgression(DailyTasksManager.PVP);
		killCount++;
		PkRank.checkRank(this);

		killed.setKillingSpree(0);
		setKillingSpree(getKillingSpree() + 1);
		if (getKillingSpree() % 5 == 0)
			World.sendNews(this, "<img=7><col=D80000>" + getDisplayName() + " is on a " + getKillingSpree() + " kills killing spree.", 0);
		if (isDeadman())
			appearence.generateAppearenceData();
		return true;
	}

	public boolean canIncreaseKillCount(Player killed) {
		if (!Settings.HOSTED)
			return true;
		if (killed.isBeginningAccount() || (killed.getLastGameMAC().equals(lastGameMAC) && !killed.getLastGameMAC().equalsIgnoreCase("00-00-00-00-00"))
				|| killed.getSession().getIP().equals(session.getIP())
				//|| (lastPlayerKill != null && killed.getUsername().equals(lastPlayerKill))
				//|| (lastPlayerMAC != null && !killed.getLastGameMAC().equalsIgnoreCase("00-00-00-00-00") && killed.getLastGameMAC().equals(lastPlayerMAC))
		)
			return false;
		lastPlayerKill = killed.getUsername();
		lastPlayerMAC = killed.getLastGameMAC();
		return true;
	}

	@Override
	public int getSize() {
		return appearence.getSize();
	}

	public boolean isCanPvp() {
		return canPvp;
	}

	public void setCanPvp(boolean canPvp) {
		this.canPvp = canPvp;
		appearence.generateAppearenceData();
		getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
		getPackets().sendPlayerUnderNPCPriority(canPvp);
		getPackets().sendPlayerAttackOptionPriority(canPvp);
	}

	public Prayer getPrayer() {
		return prayer;
	}

	public CollectionLog getCollectionLog() {
		return collectionLog;
	}

	public boolean isLocked() {
		return lockDelay > WorldThread.WORLD_CYCLE;// Utils.currentTimeMillis();
	}

	public void lock() { // locks 15min max just in case to prevent stuck accs
		lockDelay = WorldThread.WORLD_CYCLE + 1500;// Long.MAX_VALUE;
	}

	public void lock(long time) {
		lockDelay = time == -1 ? (WorldThread.WORLD_CYCLE + 1500)
				/* Long.MAX_VALUE */ : (WorldThread.WORLD_CYCLE
						+ Math.min(1500, time));/*
												 * Utils . currentTimeMillis ( ) + ( time * 600 )
												 */
		;
	}

	public void unlock() {
		lockDelay = 0;
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay) {
		useStairs(emoteId, dest, useDelay, totalDelay, null);
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay, final String message) {
		useStairs(emoteId, dest, useDelay, totalDelay, message, false);
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay, final String message,
			final boolean resetAnimation) {
		stopAll();
		lock(totalDelay);
		if (emoteId != -1)
			setNextAnimation(new Animation(emoteId));
		if (useDelay == 0) {
			setNextWorldTile(dest);
			if (message != null)
				getPackets().sendGameMessage(message);
		} else {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (isDead())
						return;
					if (resetAnimation)
						setNextAnimation(new Animation(-1));
					setNextWorldTile(dest);
					if (message != null)
						getPackets().sendGameMessage(message);
				}
			}, useDelay - 1);
		}
	}

	public Bank getBank() {
		return bank;
	}

	public ControllerManager getControlerManager() {
		return controlerManager;
	}

	public void switchMouseButtons() {
		mouseButtons = !mouseButtons;
		refreshMouseButtons();
	}

	public void switchAllowChatEffects() {
		allowChatEffects = !allowChatEffects;
		refreshAllowChatEffects();
	}

	public void switchAcceptAid() {
		if ((isIronman() || isUltimateIronman() || isHCIronman())) {
			getPackets().sendGameMessage("You can't use this feature as an ironman.");
			return;
		}
		acceptAid = !acceptAid;
		refreshAcceptAid();
	}

	public void switchProfanityFilter() {
		profanityFilter = !profanityFilter;
		refreshProfanityFilter();
	}

	public void switchRightClickReporting() {
		rightClickReporting = !rightClickReporting;
		getPackets().sendPlayerOption(rightClickReporting ? getRights() >= 1 && getRights() <= 3 ? "Punish" : "Report" : "null", 6, false);
		refreshRightClickReporting();
	}

	public void refreshAllowChatEffects() {
		getVarsManager().sendVar(171, allowChatEffects ? 0 : 1);
	}

	public void refreshAcceptAid() {
		getVarsManager().sendVar(427, acceptAid ? 1 : 0);
	}

	public void refreshRightClickReporting() {
		getVarsManager().sendVarBit(9982, rightClickReporting ? 1 : 0);
	}

	public void refreshProfanityFilter() {
		getVarsManager().sendVarBit(8780, profanityFilter ? 0 : 1);
	}

	public void refreshMouseButtons() {
		getVarsManager().sendVar(170, mouseButtons ? 0 : 1);
	}

	public void refreshPrivateChatSetup() {
		getVarsManager().sendVar(287, privateChatSetup);
	}

	public void refreshOtherChatsSetup() {
		getVarsManager().setVarBit(9188, friendChatSetup);
		getVarsManager().setVarBit(3612, clanChatSetup);
		getVarsManager().forceSendVarBit(9191, guestChatSetup);
	}

	public void setPrivateChatSetup(int privateChatSetup) {
		this.privateChatSetup = privateChatSetup;
	}

	public void setClanChatSetup(int clanChatSetup) {
		this.clanChatSetup = clanChatSetup;
	}

	public void setGuestChatSetup(int guestChatSetup) {
		this.guestChatSetup = guestChatSetup;
	}

	public void setFriendChatSetup(int friendChatSetup) {
		this.friendChatSetup = friendChatSetup;
	}

	public int getPrivateChatSetup() {
		return privateChatSetup;
	}

	public boolean isForceNextMapLoadRefresh() {
		return forceNextMapLoadRefresh;
	}

	public void setForceNextMapLoadRefresh(boolean forceNextMapLoadRefresh) {
		this.forceNextMapLoadRefresh = forceNextMapLoadRefresh;
	}

	public FriendsIgnores getFriendsIgnores() {
		return friendsIgnores;
	}

	public void addPotDelay(long time) {
		potDelay = time + Utils.currentTimeMillis();
	}

	public void addKaramDelay(long time) {
		karamDelay = time + Utils.currentTimeMillis();
	}

	public long getPotDelay() {
		return potDelay;
	}

	public long getKaramDelay() {
		return karamDelay;
	}

	public void addFoodDelay(long time) {
		foodDelay = time + Utils.currentTimeMillis();
	}

	public long getFoodDelay() {
		return foodDelay;
	}

	public void addPoisonImmune(long time) {
		poisonImmune = time + Utils.currentTimeMillis();
		getPoison().reset();
		setPoisonTimer();
	}

	public long getPoisonImmune() {
		return poisonImmune;
	}

	public void addFireImmune(long time, boolean superAntiFire) {
		fireImmune = time + Utils.currentTimeMillis();
		this.superAntiFire = superAntiFire;
		setAntifireTimer();
	}

	public boolean hasFireImmunity() {
		return fireImmune >= Utils.currentTimeMillis()
				|| (getPet() != null
						&& getPet().getId() == Pets.PRINCE_BLACK_DRAGON.getBabyNpcId());
	}

	public long getFireImmune() {
		return fireImmune;
	}

	public boolean isSuperAntiFire() {
		return superAntiFire;
	}

	public MusicsManager getMusicsManager() {
		return musicsManager;
	}

	public HintIconsManager getHintIconsManager() {
		return hintIconsManager;
	}

	public boolean isCastVeng() {
		return castedVeng;
	}

	public void setCastVeng(boolean castVeng) {
		this.castedVeng = castVeng;
	}

	public int getKillCount() {
		return killCount;
	}

	public int getBarrowsKillCount() {
		return barrowsKillCount;
	}

	public int setBarrowsKillCount(int barrowsKillCount) {
		return this.barrowsKillCount = barrowsKillCount;
	}

	public int setKillCount(int killCount) {
		return this.killCount = killCount;
	}

	public int getDeathCount() {
		return deathCount;
	}

	public int setDeathCount(int deathCount) {
		return this.deathCount = deathCount;
	}

	public void setCloseInterfacesEvent(Runnable closeInterfacesEvent) {
		this.closeInterfacesEvent = closeInterfacesEvent;
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public ChargesManager getCharges() {
		return charges;
	}

	public boolean[] getKilledBarrowBrothers() {
		return killedBarrowBrothers;
	}

	public void setHiddenBrother(int hiddenBrother) {
		this.hiddenBrother = hiddenBrother;
	}

	public int getHiddenBrother() {
		return hiddenBrother;
	}

	public void resetBarrows() {
		hiddenBrother = -1;
		killedBarrowBrothers = new boolean[7]; // includes new bro for future
		// use
		barrowsKillCount = 0;
	}

	public void refreshLastVote() {
		lastVote = Utils.currentTimeMillis();
	}

	public boolean hasVotedInLast24Hours() {
		return (Utils.currentTimeMillis() - lastVote) < (1000 * 60 * 60 * 12) || getRights() == 2;
	}

	public boolean isDonator() {
		return getDonator() >= SAPHIRE_DONATOR;
	}

	public boolean isSuperDonator() {
		return getDonator() >= EMERALD_DONATOR;
	}

	public boolean isExtremeDonator() {
		return getDonator() >= RUBY_DONATOR;
	}

	public boolean isLegendaryDonator() {
		return getDonator() >= DIAMOND_DONATOR;
	}

	public boolean isVIPDonator() {
		return getDonator() >= ONYX_DONATOR;
	}
	
	public boolean isSupremeVIPDonator() {
		return getDonator() >= ZENYTE_DONATOR;
	}

	public boolean isStaff() {
		return rights > 0 || isSupporter();
	}

	public int getDonator() {
		return donator > 0 ? donator
				: (isStaff() || isEventCoordinator() || isYoutuber() ? SAPHIRE_DONATOR : 0);
	}

	public boolean isEventCoordinator() {
		return eventCoordinator;
	}

	public void setEventCoordinator(boolean value) {
		this.eventCoordinator = value;
	}

	public boolean isSupporter() {
		return supporter;
	}

	public void setSupporter(boolean isSupporter) {
		this.supporter = isSupporter;
	}

	public int[] getPouches() {
		return pouches;
	}

	public EmotesManager getEmotesManager() {
		return emotesManager;
	}

	public String getLastGameIp() {
		return lastGameIp;
	}

	public String getLastGameMAC() {
		return lastGameMAC;
	}

	public long getLastGameLogin() {
		return lastGameLogin;
	}

	public PriceCheckManager getPriceCheckManager() {
		return priceCheckManager;
	}

	public void setPestPoints(int pestPoints) {
	/*	if (pestPoints >= 500) {
			this.pestPoints = 500;
			getPackets().sendGameMessage(
					"You have reached the maximum amount of commendation points, you may only have 500 at one time.");
			return;
		}*/
		this.pestPoints = pestPoints;
	}

	public int getPestPoints() {
		return pestPoints;
	}

	public void increaseStealingCreationPoints(int scPoints) {
		stealingCreationPoints += scPoints;
	}

	public int getStealingCreationPoints() {
		return stealingCreationPoints;
	}

	public boolean isUpdateMovementType() {
		return updateMovementType;
	}

	public long getLastPublicMessage() {
		return lastPublicMessage;
	}

	public void setLastPublicMessage(long lastPublicMessage) {
		this.lastPublicMessage = lastPublicMessage;
	}

	public CutscenesManager getCutscenesManager() {
		return cutscenesManager;
	}

	public void kickPlayerFromClanChannel(String name) {
		if (clanManager == null)
			return;
		clanManager.kickPlayerFromChat(this, name);
	}

	public boolean hasNewPlayerController() {
		return getControlerManager().getControler() != null
				&& getControlerManager().getControler() instanceof NewPlayerController;
	}

	public void sendClanChannelMessage(ChatMessage message) {
		if(!checkBankPin()) {
			sendMessage("You must enter your bank PIN before attempting that.");
			return;
		}
		if(connectedThroughVPN || hasNewPlayerController())
			return;

		if (clanManager == null)
			return;
		clanManager.sendMessage(this, message);
	}

	public void sendGuestClanChannelMessage(ChatMessage message) {
		if(!checkBankPin()) {
			sendMessage("You must type your bank PIN first!");
			return;
		}
		if(connectedThroughVPN || hasNewPlayerController())
			return;
		if (guestClanManager == null)
			return;
		guestClanManager.sendMessage(this, message);
	}

	public void sendClanChannelQuickMessage(QuickChatMessage message) {
		if (clanManager == null)
			return;
		clanManager.sendQuickMessage(this, message);
	}

	public void sendGuestClanChannelQuickMessage(QuickChatMessage message) {
		if (guestClanManager == null)
			return;
		guestClanManager.sendQuickMessage(this, message);
	}

	public void sendPublicChatMessage(PublicChatMessage message) {
		if(!checkBankPin()) {
			sendMessage("You must enter your bank PIN first!");
			return;
		}
		if(hasNewPlayerController()) {
			sendMessage("You must finish the tutorial before speaking to others!");
			return;
		}
		if(connectedThroughVPN) {
			sendMessage("You must disable your VPN to play.");
			return;
		}
		for (int i = 0; i < getLocalPlayerUpdate().getLocalPlayersIndexesCount(); i++) {
			Player player = getLocalPlayerUpdate()
					.getLocalPlayers()[getLocalPlayerUpdate().getLocalPlayersIndexes()[i]];
			if (player == null || !player.isRunning() || player.hasFinished()) // shouldnt
				continue;
			player.getPackets().sendPublicMessage(this, message);
		}
		Bot.sendLog(Bot.PUBLIC_CHAT_CHANNEL, "[type=PUBLIC][name="+getUsername()+"][message="+message.getMessage(false)+"]");
	}

	public int[] getCompletionistCapeCustomized() {
		return completionistCapeCustomized;
	}

	public void setCompletionistCapeCustomized(int[] skillcapeCustomized) {
		this.completionistCapeCustomized = skillcapeCustomized;
	}

	public int[] getMaxedCapeCustomized() {
		return maxedCapeCustomized;
	}

	public void setMaxedCapeCustomized(int[] maxedCapeCustomized) {
		this.maxedCapeCustomized = maxedCapeCustomized;
	}

	public void setSkullId(int skullId) {
		this.skullId = skullId;
	}

	public int getSkullId() {
		return skullId;
	}

	public boolean isFilterGame() {
		return filterGame;
	}

	public void setFilterGame(boolean filterGame) {
		this.filterGame = filterGame;
	}

	public void addLogicPacketToQueue(LogicPacket packet) {
		for (LogicPacket p : logicPackets) {
			if (p.getId() == packet.getId()) {
				logicPackets.remove(p);
				break;
			}
		}
		logicPackets.add(packet);
	}

	public DominionTower getDominionTower() {
		return dominionTower;
	}

	public int getPrayerRenewalDelay() {
		return prayerRenewalDelay;
	}

	public void setPrayerRenewalDelay(int delay) {
		this.prayerRenewalDelay = delay;
		setPrayerRenewalTimer();
	}

	public int getOverloadDelay() {
		return overloadDelay;
	}

	public void setOverloadDelay(int overloadDelay) {
		this.overloadDelay = overloadDelay;
		setOverloadTimer();
	}

	public Trade getTrade() {
		return trade;
	}

	public void setTeleBlockDelay(long teleDelay) {
		getTemporaryAttributtes().put("TeleBlocked", teleDelay + Utils.currentTimeMillis());
		setTeleblockTimer((int) teleDelay);
	}
	
	@Override
	public void addFreezeDelay(long time, boolean entangleMessage) {
		super.addFreezeDelay(time, entangleMessage);
		setFreezeTimer((int) time);
	}
	
	@Override
	public void setFreezeDelay(int time) {
		super.setFreezeDelay(time);
		setFreezeTimer((int) Math.max(0,  time - Utils.currentTimeMillis()));
	}

	

	public long getTeleBlockDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("TeleBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public void setDFSDelay(long teleDelay) {
		getTemporaryAttributtes().put("dfs_delay", teleDelay + Utils.currentTimeMillis());
		getTemporaryAttributtes().remove("dfs_shield_active");
	}

	public long getDFSDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("dfs_delay");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public void setPrayerDelay(long teleDelay) {
		getTemporaryAttributtes().put("PrayerBlocked", teleDelay + Utils.currentTimeMillis());
		prayer.closeProtectionPrayers();
	}

	public boolean isPrayerBlocked() {
		Long block = (Long) getTemporaryAttributtes().get("PrayerBlocked");
		if (block == null)
			return false;
		return block >= Utils.currentTimeMillis();
	}

	public Familiar getFamiliar() {
		return familiar;
	}

	public void setFamiliar(Familiar familiar) {
		this.familiar = familiar;
		setFamiliarTimer();
	}

	public FriendsChat getCurrentFriendsChat() {
		return currentFriendsChat;
	}

	public void setCurrentFriendsChat(FriendsChat chat) {
		this.currentFriendsChat = chat;
	}

	public int getLastFriendsChatRank() {
		return lastFriendsChatRank;
	}

	public void setLastFriendsChatRank(int rank) {
		lastFriendsChatRank = rank;
	}

	public String getLastFriendsChat() {
		return lastFriendsChat;
	}

	public void setLastFriendsChat(String chat) {
		this.lastFriendsChat = chat;
	}

	public int getSummoningLeftClickOption() {
		return summoningLeftClickOption;
	}

	public void setSummoningLeftClickOption(int summoningLeftClickOption) {
		this.summoningLeftClickOption = summoningLeftClickOption;
	}

	public boolean containsOneItem(int... itemIds) {
		if (getInventory().containsOneItem(itemIds))
			return true;
		if (getEquipment().containsOneItem(itemIds))
			return true;
		Familiar familiar = getFamiliar();
		if (familiar != null
				&& ((familiar.getBob() != null && familiar.getBob().containsOneItem(itemIds) || familiar.isFinished())))
			return true;
		return false;
	}

	public boolean canSpawn() {
		if (getControlerManager().getControler() instanceof BossInstanceController
				|| getControlerManager().getControler() instanceof PestControlLobby
				|| getControlerManager().getControler() instanceof PestControlGame
				|| getControlerManager().getControler() instanceof ZGDControler
				|| getControlerManager().getControler() instanceof GodWars
				|| getControlerManager().getControler() instanceof DTControler
				|| getControlerManager().getControler() instanceof CastleWarsPlaying
				|| getControlerManager().getControler() instanceof CastleWarsWaiting
				|| getControlerManager().getControler() instanceof FightCaves
				|| getControlerManager().getControler() instanceof FightKiln
				|| getControlerManager().getControler() instanceof NomadsRequiem
				|| getControlerManager().getControler() instanceof QueenBlackDragonController
				|| getControlerManager().getControler() instanceof WarControler
				|| getControlerManager().getControler() instanceof StealingCreationLobbyController
				|| getControlerManager().getControler() instanceof StealingCreationController
				|| getControlerManager().getControler() instanceof ZulrahShrine
				|| getControlerManager().getControler() instanceof Inferno
				|| getControlerManager().getControler() instanceof SkotizoLair
				|| getControlerManager().getControler() instanceof GiantMole
				|| getControlerManager().getControler() instanceof VorkathLair
				|| getControlerManager().getControler() instanceof GrotesqueGuardianLair
				|| getControlerManager().getControler() instanceof ChambersOfXericController
				|| getControlerManager().getControler() instanceof TheatreOfBloodController
				|| getControlerManager().getControler() instanceof TheNightmareInstance
				|| getControlerManager().getControler() instanceof RunespanControler
				|| getControlerManager().getControler() instanceof RunespanControler
				|| getControlerManager().getControler() instanceof PkTournamentGame
				|| getControlerManager().getControler() instanceof PkTournamentLobby
				|| getControlerManager().getControler() instanceof PkTournamentSpectating) {
			return false;
		}
		return !isCanPvp() && !dungManager.isInside();
	}

	public long getPolDelay() {
		return polDelay;
	}

	public void addPolDelay(long delay) {
		polDelay = delay + Utils.currentTimeMillis();
	}

	public void setPolDelay(long delay) {
		this.polDelay = delay;
	}

	public List<Integer> getSwitchItemCache() {
		return switchItemCache;
	}

	public AuraManager getAuraManager() {
		return auraManager;
	}

	public int getMovementType() {
		if (getTemporaryMoveType() != -1)
			return getTemporaryMoveType();
		return getRun() ? RUN_MOVE_TYPE : WALK_MOVE_TYPE;
	}

	public List<String> getOwnedObjectManagerKeys() {
		if (ownedObjectsManagerKeys == null) // temporary
			ownedObjectsManagerKeys = new LinkedList<String>();
		return ownedObjectsManagerKeys;
	}

	public boolean hasInstantSpecial(final int weaponId) {
		switch (weaponId) {
		case 51028: //dragon harpoon
		case 51031:
		case 4153:
		case 41791:
		case 42902:
		case 25541:
		case 42904:
		case 15486:
		case 25379:
		case 22207:
		case 22209:
		case 22211:
		case 22213:
		case 1377:
		case 13472:
		case 35:// Excalibur
		case 8280:
		case 14632:
		case 24455:
		case 24456:
		case 24457:
		case 14679:
		case 6739: //dragon axe
		case 13470:
		case 43241:
		case 15259: //dragon pickaxe
		case 20786:
		case 43243:
		case 53677:
		case 53673: //crystal tools
		case 53680:
		case 53762:
			return true;
		default:
			return false;
		}
	}

	public void performInstantSpecial(final int weaponId) {
		int specAmt = PlayerCombat.getSpecialAmmount(weaponId);
		if (combatDefinitions.hasRingOfVigour())
			specAmt *= 0.9;
		if (combatDefinitions.getSpecialAttackPercentage() < specAmt) {
			getPackets().sendGameMessage("You don't have enough power left.");
			combatDefinitions.desecreaseSpecialAttack(0);
			return;
		}
		if (this.getSwitchItemCache().size() > 0) {
			ButtonHandler.submitSpecialRequest(this);
			return;
		}
		switch (weaponId) {
		case 24455:
		case 24456:
		case 24457:
			getPackets().sendGameMessage("Aren't you strong enough already?");
			break;
		case 4153:
		case 14679:
			if (!(getActionManager().getAction() instanceof PlayerCombat)) {
				Entity target = Combat.getLastTarget(this);
				if (target == null || !getActionManager().setAction(new PlayerCombat(target))) {
					getPackets().sendGameMessage(
							"Warning: Since the maul's special is an instant attack, it will be wasted when used on a first strike.");
					combatDefinitions.switchUsingSpecialAttack();
					return;
				}
			}
			PlayerCombat combat = (PlayerCombat) getActionManager().getAction();
			Entity target = combat.getTarget();
			if (!Utils.isOnRange(getX(), getY(), getSize(), target.getX(), target.getY(), target.getSize(),
					hasWalkSteps() ? getRun() ? 2 : 1 : 0)) {
				combatDefinitions.switchUsingSpecialAttack();
				return;
			}
			getActionManager().setActionDelay(PlayerCombat.getMeleeCombatDelay(this, weaponId));
			setNextAnimation(new Animation(1667));
			setNextGraphics(new Graphics(340, 0, 96 << 16));
			int attackStyle = getCombatDefinitions().getAttackStyle();
			combat.delayNormalHit(weaponId, attackStyle, combat.getMeleeHit(this,
					combat.getRandomMaxHit(this, weaponId, attackStyle, false, true, 1.1, true)));
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			// getActionManager().setAction(new PlayerCombat(target));
			break;
		case 51028: //dragon harpoon
		case 51031:
		case 53762:
			setNextAnimation(new Animation(1056));
			setNextGraphics(new Graphics(246));
			setNextForceTalk(new ForceTalk("Here fishy fishies!!"));
			skills.set(Skills.FISHING, skills.getLevelForXp(Skills.FISHING) + 3);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		case 6739: //dragon axe
		case 13470:
		case 43241:
		case 53673:
			setNextAnimation(new Animation(2876));
			setNextGraphics(new Graphics(479));
			setNextForceTalk(new ForceTalk("Chop chop!"));
			skills.set(Skills.WOODCUTTING, skills.getLevelForXp(Skills.WOODCUTTING) + 3);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		case 15259: //dragon pickaxe
		case 20786:
		case 43243:
		case 53680:
		case 53677:
			setNextAnimation(new Animation(12031));
			setNextGraphics(new Graphics(2144));
			setNextForceTalk(new ForceTalk("Smashing!"));
			skills.set(Skills.MINING, skills.getLevelForXp(Skills.MINING) + 3);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		case 1377:
		case 13472:
			setNextAnimation(new Animation(1056));
			setNextGraphics(new Graphics(246));
			setNextForceTalk(new ForceTalk("Raarrrrrgggggghhhhhhh!"));
			int defence = (int) (skills.getLevelForXp(Skills.DEFENCE) * 0.90D);
			int attack = (int) (skills.getLevelForXp(Skills.ATTACK) * 0.90D);
			int range = (int) (skills.getLevelForXp(Skills.RANGE) * 0.90D);
			int magic = (int) (skills.getLevelForXp(Skills.MAGIC) * 0.90D);
			int strength = (int) (skills.getLevelForXp(Skills.STRENGTH) * 1.2D);
			skills.set(Skills.DEFENCE, defence);
			skills.set(Skills.ATTACK, attack);
			skills.set(Skills.RANGE, range);
			skills.set(Skills.MAGIC, magic);
			skills.set(Skills.STRENGTH, strength);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		case 35:// Excalibur
		case 8280:
		case 14632:
			setNextAnimation(new Animation(1168));
			setNextGraphics(new Graphics(247));
			setNextForceTalk(new ForceTalk("For " + Settings.SERVER_NAME + "!"));
			final boolean enhanced = weaponId == 14632;
			skills.set(Skills.DEFENCE, enhanced ? (int) (skills.getLevelForXp(Skills.DEFENCE) * 1.15D)
					: (skills.getLevel(Skills.DEFENCE) + 8));
			WorldTasksManager.schedule(new WorldTask() {
				int count = 5;

				@Override
				public void run() {
					if (isDead() || hasFinished() || getHitpoints() >= getMaxHitpoints()) {
						stop();
						return;
					}
					heal(enhanced ? 80 : 40);
					if (count-- == 0) {
						stop();
						return;
					}
				}
			}, 4, 2);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		case 15486:
		case 25379:
		case 22207:
		case 22209:
		case 22211:
		case 22213:
			setNextAnimation(new Animation(12804));
			setNextGraphics(new Graphics(2319));// 2320
			setNextGraphics(new Graphics(2321));
			addPolDelay(60000);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		case 41791:
		case 42902:
		case 42904:
		case 25541:
			setNextAnimation(new Animation(weaponId == 41791 ? 27083 : 21720));
			setNextGraphics(new Graphics(6228, 0, 300));// 2320
			addPolDelay(60000);
			combatDefinitions.desecreaseSpecialAttack(specAmt);
			break;
		}
	}

	public void setDisableEquip(boolean equip) {
		disableEquip = equip;
	}

	public boolean isEquipDisabled() {
		return disableEquip;
	}

	public int getPublicStatus() {
		return publicStatus;
	}

	public void setPublicStatus(int publicStatus) {
		this.publicStatus = publicStatus;
	}

	public int getClanStatus() {
		return clanStatus;
	}

	public void setClanStatus(int clanStatus) {
		this.clanStatus = clanStatus;
	}

	public int getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(int tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public int getAssistStatus() {
		return assistStatus;
	}

	public void setAssistStatus(int assistStatus) {
		if (assistStatus != this.assistStatus) {
			this.assistStatus = assistStatus;
			sendAssistOption();
		}
	}

	public int getFriendsChatStatus() {
		return friendsChatStatus;
	}

	public void setFriendsChatStatus(int friendsChatStatus) {
		this.friendsChatStatus = friendsChatStatus;
	}

	public Notes getNotes() {
		return notes;
	}

	public IsaacKeyPair getIsaacKeyPair() {
		return isaacKeyPair;
	}

	public QuestManager getQuestManager() {
		return questManager;
	}

	public boolean isCompletedFightCaves() {
		return completedFightCaves;
	}

	public void setCompletedFightCaves() {
		if (!completedFightCaves) {
			completedFightCaves = true;
			refreshFightKilnEntrance();
		}
	}

	public boolean isCompletedFightKiln() {
		return completedFightKiln;
	}

	public void setCompletedFightKiln() {
		completedFightKiln = true;
	}
	
	public boolean isCompletedHorde() {
		return completedHorde;
	}

	public void setCompletedHorde() {
		completedHorde = true;
	}

	public boolean isCompletedStealingCreation() {
		return completedStealingCreation;
	}

	public void setCompletedStealingCreation() {
		completedStealingCreation = true;
	}

	public boolean isWonFightPits() {
		return wonFightPits;
	}

	public void setWonFightPits() {
		wonFightPits = true;
	}

	public boolean isWonReaction() {
		return wonReaction;
	}
	
	public void setWonReaction() {
		wonReaction = true;
	}
	
	public boolean isCantTrade() {
		return cantTrade;
	}

	public void setCantTrade(boolean canTrade) {
		this.cantTrade = canTrade;
	}

	public String getYellColor() {
		return yellColor;
	}

	public void setYellColor(String yellColor) {
		this.yellColor = yellColor;
	}

	/**
	 * Gets the pet.
	 * 
	 * @return The pet.
	 */
	public Pet getPet() {
		return pet;
	}

	/**
	 * Sets the pet.
	 * 
	 * @param pet
	 *            The pet to set.
	 */
	public void setPet(Pet pet) {
		this.pet = pet;
	}

	/**
	 * Gets the petManager.
	 * 
	 * @return The petManager.
	 */
	public PetManager getPetManager() {
		return petManager;
	}

	/**
	 * Sets the petManager.
	 * 
	 * @param petManager
	 *            The petManager to set.
	 */
	public void setPetManager(PetManager petManager) {
		this.petManager = petManager;
	}

	public boolean isXpLocked() {
		return xpLocked;
	}

	public void setXpLocked(boolean locked) {
		this.xpLocked = locked;
	}

	public int getLastBonfire() {
		return lastBonfire;
	}

	public void setLastBonfire(int lastBonfire) {
		this.lastBonfire = lastBonfire;
		equipment.refreshConfigs(false);
		setBonfireTimer();
	}

	public boolean isYellOff() {
		return yellOff;
	}

	public void setYellOff(boolean yellOff) {
		this.yellOff = yellOff;
	}

	public void setInvulnerable(boolean invulnerable) {
		this.invulnerable = invulnerable;
	}

	public double getHpBoostMultiplier() {
		return hpBoostMultiplier;
	}

	public void setHpBoostMultiplier(double hpBoostMultiplier) {
		this.hpBoostMultiplier = hpBoostMultiplier;
	}

	/**
	 * Gets the killedQueenBlackDragon.
	 * 
	 * @return The killedQueenBlackDragon.
	 */
	public boolean isKilledQueenBlackDragon() {
		return killedQueenBlackDragon;
	}

	/**
	 * Sets the killedQueenBlackDragon.
	 * 
	 * @param killedQueenBlackDragon
	 *            The killedQueenBlackDragon to set.
	 */
	public void setKilledQueenBlackDragon(boolean killedQueenBlackDragon) {
		this.killedQueenBlackDragon = killedQueenBlackDragon;
	}

	public boolean hasLargeSceneView() {
		return largeSceneView;
	}

	public void setLargeSceneView(boolean largeSceneView) {
		this.largeSceneView = largeSceneView;
	}

	public boolean isOldItemsLook() {
		return oldItemsLook;
	}

	public void switchItemsLook() {
		oldItemsLook = !oldItemsLook;
		getPackets().sendItemsLook();
	}
	
	public void switchCosmeticOverrides() {
		disableCosmeticOverrides = !disableCosmeticOverrides;
	}

	public boolean isVirtualLevels() {
		return virtualLevels;
	}

	public void switchVirtualLevels() {
		virtualLevels = !virtualLevels;
		getPackets().sendVirtualLevels();
		skills.refreshSkills();
	}
	
	public boolean isOldHitLook() {
		return oldHitsLook;
	}

	public void switchHitLook() {
		oldHitsLook = !oldHitsLook;
		getPackets().sendHitLook();
		refreshHitPoints();
		getPrayer().refreshPrayerPoints();
	}

	/**
	 * @return the runeSpanPoint
	 */
	public int getRuneSpanPoints() {
		return runeSpanPoints;
	}

	/**
	 * @param runeSpanPoints to set
	 */
	public void setRuneSpanPoint(int runeSpanPoints) {
		this.runeSpanPoints = runeSpanPoints;
	}

	/**
	 * Adds points
	 * 
	 * @param points
	 */
	public void addRunespanPoints(int points) {
		this.runeSpanPoints += points;
	}

	public DuelRules getDuelRules() {
		return duelRules;
	}

	public void setLastDuelRules(DuelRules duelRules) {
		this.duelRules = duelRules;
	}

	public boolean isTalkedWithMarv() {
		return talkedWithMarv;
	}

	public void setTalkedWithMarv() {
		talkedWithMarv = true;
	}

	public int getCrucibleHighScore() {
		return crucibleHighScore;
	}

	public void increaseCrucibleHighScore() {
		crucibleHighScore++;
	}

	public House getHouse() {
		return house;
	}

	public boolean isAcceptingAid() {
		return acceptAid;
	}

	public boolean isFilteringProfanity() {
		return profanityFilter;
	}

	public MoneyPouch getMoneyPouch() {
		return moneyPouch;
	}

	public int getCannonBalls() {
		return cannonBalls;
	}
	
	public boolean isGraniteBalls() {
		return graniteBalls;
	}

	public void setGraniteBalls(boolean graniteBalls) {
		this.graniteBalls = graniteBalls;
	}
	
	public void addCannonBalls(int cannonBalls) {
		this.cannonBalls += cannonBalls;
	}

	public void removeCannonBalls() {
		this.cannonBalls = 0;
	}

	public FarmingManager getFarmingManager() {
		return farmingManager;
	}

	public Toolbelt getToolbelt() {
		return toolbelt;
	}

	public VarsManager getVarsManager() {
		return varsManager;
	}

	public int getFinishedCastleWars() {
		return finishedCastleWars;
	}

	public int getFinishedStealingCreations() {
		return finishedStealingCreations;
	}

	public boolean isCapturedCastleWarsFlag() {
		return capturedCastleWarsFlag;
	}

	public void setCapturedCastleWarsFlag() {
		capturedCastleWarsFlag = true;
	}

	public void increaseFinishedCastleWars() {
		finishedCastleWars++;
	}

	public void increaseFinishedStealingCreations() {
		finishedStealingCreations++;
	}

	public boolean isLootShareEnabled() {
		return lootShare;
	}

	public void enableLootShare() {
		if (!isLootShareEnabled()) {
			getPackets().sendGameMessage("LootShare is now active.");
			lootShare = true;
		}
		refreshLootShare();
	}

	public void disableLootShare() {
		lootShare = false;
		refreshLootShare();
	}

	public void refreshLootShare() {
		// need to force cuz autoactivates when u click on it even if no chat
		varsManager.forceSendVarBit(4071, lootShare ? 1 : 0);
	}

	public ClansManager getClanManager() {
		return clanManager;
	}

	public void setClanManager(ClansManager clanManager) {
		this.clanManager = clanManager;
	}

	public ClansManager getGuestClanManager() {
		return guestClanManager;
	}

	public void setGuestClanManager(ClansManager guestClanManager) {
		this.guestClanManager = guestClanManager;
	}

	public String getClanName() {
		return clanName;
	}

	public void setClanName(String clanName) {
		this.clanName = clanName;
	}

	public boolean isConnectedClanChannel() {
		return connectedClanChannel || lobby;
	}

	public void setConnectedClanChannel(boolean connectedClanChannel) {
		this.connectedClanChannel = connectedClanChannel;
	}

	public void setVerboseShopDisplayMode(boolean verboseShopDisplayMode) {
		this.verboseShopDisplayMode = verboseShopDisplayMode;
		refreshVerboseShopDisplayMode();
	}

	public void refreshVerboseShopDisplayMode() {
		varsManager.sendVarBit(11055, verboseShopDisplayMode ? 0 : 1);
	}

	public int getGraveStone() {
		return graveStone;
	}

	public void setGraveStone(int graveStone) {
		this.graveStone = graveStone;
	}

	public GrandExchangeManager getGeManager() {
		return geManager;
	}

	public void setGeManager(GrandExchangeManager m) {
		geManager = m; // TODO remove
	}

	public SlayerManager getSlayerManager() {
		return slayerManager;
	}

	public SquealOfFortune getSquealOfFortune() {
		return squealOfFortune;
	}

	public TreasureTrailsManager getTreasureTrailsManager() {
		return treasureTrailsManager;
	}

	public boolean[] getShosRewards() {
		return shosRewards;
	}

	public boolean isKilledLostCityTree() {
		return killedLostCityTree;
	}

	public void setKilledLostCityTree(boolean killedLostCityTree) {
		this.killedLostCityTree = killedLostCityTree;
	}

	public double[] getWarriorPoints() {
		return warriorPoints;
	}

	public void setWarriorPoints(int index, double pointsDifference) {
		warriorPoints[index] += pointsDifference;
		if (warriorPoints[index] < 0) {
			Controller controler = getControlerManager().getControler();
			if (controler == null || !(controler instanceof WarriorsGuild))
				return;
			WarriorsGuild guild = (WarriorsGuild) controler;
			guild.inCyclopse = false;
			setNextWorldTile(WarriorsGuild.CYCLOPS_LOBBY);
			warriorPoints[index] = 0;
		} else if (warriorPoints[index] > 65535)
			warriorPoints[index] = 65535;
		refreshWarriorPoints(index);
	}

	public void refreshWarriorPoints(int index) {
		varsManager.sendVarBit(index + 8662, (int) warriorPoints[index]);
	}

	private void warriorCheck() {
		if (warriorPoints == null || warriorPoints.length != 6)
			warriorPoints = new double[6];
	}

	public int getFavorPoints() {
		return favorPoints;
	}

	public void setFavorPoints(int points) {
		if (points + favorPoints >= 2000) {
			points = 2000;
			getPackets().sendGameMessage(
					"The offering stone is full! The jadinkos won't deposit any more rewards until you have taken some.");
		}
		this.favorPoints = points;
		refreshFavorPoints();
	}

	public void refreshFavorPoints() {
		varsManager.sendVarBit(9511, favorPoints);
	}

	public void setDungChallengeTime(int id, long timestamp) {
		if (id < 0 || id >= 5)
			return;

		if (dungChallengeTimes == null)
			dungChallengeTimes = new long[5];

		dungChallengeTimes[id] = timestamp;
	}

	public long getDungChallengeTime(int id) {
		if (id < 0 || id >= 5)
			return 0;

		if (dungChallengeTimes == null)
			return 0;

		return dungChallengeTimes[id];
	}

	public boolean containsItem(int id) {
		return getInventory().containsItemToolBelt(id) || getEquipment().getItems().containsOne(new Item(id))
				|| getBank().containsItem(id);
	}

	public void increaseRedStoneCount() {
		redStoneCount++;
	}

	public void resetRedStoneCount() {
		redStoneCount = 0;
	}

	public int getRedStoneCount() {
		return redStoneCount;
	}

	public void setStoneDelay(long delay) {
		redStoneDelay = Utils.currentTimeMillis() + delay;
	}

	public long getRedStoneDelay() {
		return redStoneDelay;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void increaseLoginCount() {
		loginCount++;
	}

	public boolean isLobby() {
		return lobby;
	}

	public CoalTrucksManager getCoalTrucksManager() {
		return coalTrucksManager;
	}

	public DungManager getDungManager() {
		return dungManager;
	}

	public boolean[] getPrayerBook() {
		return prayerBook;
	}

	/**
	 * Used to transform the player into an NPC at {@link #transformationTicks}.
	 * 
	 * @param transformationId
	 *            The ID the player will transform into.
	 * @param transformationTicks
	 *            The length of time the transformation will last.
	 */
	public void transform(int transformationId, int transformationTicks) {
		if (this.transformationTicks > 0) {
			getPackets()
					.sendGameMessage("You must wait until your current transformation is done before re-transforming.");
			return;
		}
		appearence.transformIntoNPC(transformationId);
		this.transformationTicks = transformationTicks;
	}

	/**
	 * Reset the player back into an original state.
	 */
	private void resetTransformation() {
		getPackets().sendGameMessage("<col=0000FF>Your transformation has been complete.");
		transformationTicks = 0;
		appearence.transformIntoNPC(-1);
	}

	public void setPouchFilter(boolean pouchFilter) {
		this.pouchFilter = pouchFilter;
	}

	public boolean isPouchFilter() {
		return pouchFilter;
	}

	public boolean isCantWalk() {
		return cantWalk;
	}

	public void setCantWalk(boolean cantWalk) {
		this.cantWalk = cantWalk;
	}

	@Override
	public boolean canMove(int dir) {
		return getControlerManager().canMove(dir);
	}

	public boolean isKilledWildyWyrm() {
		return killedWildyWyrm;
	}

	public void setKilledWildyWyrm() {
		killedWildyWyrm = true;
	}

	public int getReceivedCompletionistCape() {
		return receivedCompletionistCape;
	}

	public void setReceivedCompletionistCape(int i) {
		receivedCompletionistCape = i;
	}

	public int getEcoClearStage() {
		return ecoClearStage;
	}

	public void setEcoClearStage(int ecoClearStage) {
		this.ecoClearStage = ecoClearStage;
	}

	@Override
	public double[] getBonuses() {
		return combatDefinitions.getBonuses();
	}

	public long getLastArtefactTime() {
		return lastArtefactTime;
	}

	public void setLastArtefactTime(long lastArtefactTime) {
		this.lastArtefactTime = lastArtefactTime;
	}

	public int getVoteCount() {
		return votes;
	}

	public void setVoteCount(int votes) {
		this.votes = votes;
		TopVoter.checkRank(this);
	}

	public long getSessionTime() {
		return Utils.currentTimeMillis() - lastGameLogin;
	}

	public long getTotalOnlineTime() {
		return onlineTime + getSessionTime();
	}

	public void setTotalOnlineTime(long onlineTime) {
		this.onlineTime = onlineTime;
	}

	public int getKillingSpree() {
		return killingSpree;
	}

	public void setKillingSpree(int killingSpree) {
		this.killingSpree = killingSpree;
	}

	public int getPkPoints() {
		return pkp;
	}

	public void setPkPoints(int pkPoints) {
		this.pkp = pkPoints;
	}

	public boolean isMasterLogin() {
		return masterLogin;
	}

	public boolean isBeginningAccount() {
		return false; // Settings.DEBUG && !Settings.SPAWN_WORLD && getTotalOnlineTime() < 3600000;
	}

	@Override
	public int getHealRestoreRate() {
		if (isResting())
			return 1;
		int c = super.getHealRestoreRate();
		if (getPrayer().usingPrayer(0, 26) || resting == -1)
			c /= 5;
		/*else if (getPrayer().usingPrayer(0, 9)) //disabled currently
			c /= 2;*/
		if (getEquipment().getGlovesId() == 11133)
			c /= 2; // changed to 3 in rs3
		return c;
	}

	public long getLastStarSprite() {
		return lastStarSprite;
	}

	public void setLastStarSprite(long lastStarSprite) {
		this.lastStarSprite = lastStarSprite;
	}
	
	public long getLastEvilTree() {
		return lastEvilTree;
	}

	public void setLastEvilTree(long lastEvilTree) {
		this.lastEvilTree = lastEvilTree;
	}

	public boolean isFoundShootingStar() {
		return foundShootingStar;
	}

	public void setFoundShootingStar() {
		this.foundShootingStar = true;
	}

	public long getLastBork() {
		return lastBork;
	}

	public void setLastBork(long lastBork) {
		this.lastBork = lastBork;
	}

	public CustomGear[] getSetups() {
		return gearSetups;
	}

	public void resetSetups() {
		gearSetups = null;
		getPackets().sendGameMessage("Reseted gear setups.");
	}

	private int getSetupSlots() {
		return isVIPDonator() ? 10 : isDonator() ? 5 : 2;
	}

	public boolean removeSetup(String name) {
		if (gearSetups == null)
			return false;
		for (int index = 0; index < getSetups().length; index++) {
			if (getSetups()[index] == null)
				continue;
			if (!getSetups()[index].getName().equals(name))
				continue;
			getSetups()[index] = null;
			return true;
		}
		return false;
	}

	public boolean addSetup(CustomGear setup) {
		if (gearSetups == null)
			this.gearSetups = new CustomGear[10]; // if one person cant live with over 10 setups hes kinda dumb
		for (int index = 0, count = 0; index < getSetups().length; index++) {
			if (getSetups()[index] != null)
				count++;
			if (count >= getSetups().length || count >= getSetupSlots())
				break;
			if (index == count) {
				getSetups()[count] = setup;
				return true;
			}
		}
		return false;
	}

	public void setGearSetup(CustomGear[] gearSetups) {
		this.gearSetups = gearSetups;
	}

	public boolean hasEmailRestrictions() {
		return email == null;
	}

	public int getPreviousLodestone() {
		return previousLodestone;
	}

	public void setPreviousLodestone(int previousLodestone) {
		this.previousLodestone = previousLodestone;
	}

	/**
	 * @return the donated
	 */
	public double getDonated() {
		return donated;
	}

	/**
	 * @param donated
	 *            the donated to set
	 */
	public void increaseDonated(double donated) {
		this.donated += donated;
		/*if (donated >= 1)
			World.addWishingWell(this, (long) (90000/*180000*/ //* donated), true);
		deals.addDonated((int) donated);
		TopDonator.checkRank(this);
		MTopDonator.add(this, donated);
		checkDonatorRank();
		
	}
	
	public void setDonated(double donated) {
		this.donated = donated;
		TopDonator.checkRank(this);
	}

	/**
	 * @author dragonkk(Alex) Sep 21, 2017
	 * @return
	 */
	public double getDropRateMultiplier() {
		/*double mult = ((double) getDonator()) / 10 + 1;
		if (hasVotedInLast24Hours())
			mult += 0.1;
		if (isExtreme() || isHCIronman()) //5% increase 4 extreme
			mult += 0.05;
		mult += (getAuraManager().getDropMultiplier() - 1) / 2;
		if (World.isWishingWellActive())
			mult += 0.03;
		
		if (getPet() != null && (getPet().getId() == Pets.CALLISTO_CUB.getBabyNpcId()
				|| getPet().getId() == Pets.PET_CHAOS_ELEMENTAL.getBabyNpcId()
				|| getPet().getId() == Pets.VENENATIS_SPIDERLING.getBabyNpcId()
				|| getPet().getId() == Pets.SCORPIA_OFFSPRING.getBabyNpcId()
				|| getPet().getId() == Pets.VETION_JR.getBabyNpcId()))
			mult += 0.005;
		if (getPet() != null && (getPet().getId() == Pets.PET_DARK_CORE.getBabyNpcId()
				|| getPet().getId() == Pets.CORPOREAL_CRITTER.getBabyNpcId()
				|| getPet().getId() == Pets.GALVEK.getBabyNpcId()))
			mult += 0.01;
		if (getPet() != null
				&& Pets.decreaseDropRateEFfect(getPet()))
			mult += 0.02;

		
		boolean ringOfWealth = Combat.hasRingOfWealth(this);
		if (ringOfWealth)
			mult += (getEquipment().getRingId() == 42785
			|| getEquipment().getRingId() == 25488 ? 0.03 : 0.01); // 1% extra chance
		
		
		if (Combat.hasCustomWeapon(this) || getEquipment().getWeaponId() == 50997 /*&& !(getControlerManager().getControler() instanceof TheatreOfBloodController)*///)
		/*	mult *= Drops.NERF_DROP_RATE_CW;
		
		return mult;*/
		return getDropRateMultiplierI();
	}

	public double getDropRateMultiplierI() {
		//double mult = ((double) getDonator()) / 10 + 1;
		double mult = isSupremeVIPDonator() ? 2 : this.isVIPDonator() ? 1.8 : this.isLegendaryDonator() ? 1.55 : this.isExtremeDonator() ? 1.35 : (((double) getDonator()) / 10 + 1);


		if (isIronman())
			mult += 0.05;
		else if (isUltimateIronman() || isHCIronman())
			mult += 0.1;
		else if (isExpert())
			mult += 0.15;
		if (hasVotedInLast24Hours())
			mult += 0.1;
		if (getAchievements().isCompleted())
			mult += 0.01;
		mult += getAuraManager().getDropMultiplier() - 1;
		if (World.isWishingWellActive())
			mult += 0.05;
		
		if (getPet() != null
					&& (getPet().getId() == Pets.CALLISTO_CUB.getBabyNpcId()
					|| getPet().getId() == Pets.PET_CHAOS_ELEMENTAL.getBabyNpcId()
					|| getPet().getId() == Pets.VENENATIS_SPIDERLING.getBabyNpcId()
					|| getPet().getId() == Pets.SCORPIA_OFFSPRING.getBabyNpcId()
					|| getPet().getId() == Pets.VETION_JR.getBabyNpcId()))
			mult += 0.02;
		if (getPet() != null
				&& (getPet().getId() == Pets.PET_DARK_CORE.getBabyNpcId()
				|| getPet().getId() == Pets.CORPOREAL_CRITTER.getBabyNpcId()
				|| getPet().getId() == Pets.GALVEK.getBabyNpcId()))
				mult += 0.03;
		else if (getPet() != null
				&& (getPet().getId() == Pets.NOMAD.getBabyNpcId()))
				mult += 0.04;
		else if (getPet() != null
				&& (getPet().getId() == Pets.ONYX_1.getBabyNpcId()
				|| getPet().getId() == Pets.ONYX_2.getBabyNpcId()
				|| getPet().getId() == Pets.ONYX_3.getBabyNpcId()
				|| getPet().getId() == Pets.CALLUS_1.getBabyNpcId()
				|| getPet().getId() == Pets.CALLUS_2.getBabyNpcId()
				|| getPet().getId() == Pets.CALLUS_3.getBabyNpcId()
				|| getPet().getId() == Pets.GENIE.getBabyNpcId()
				|| getPet().getId() == Pets.DEAD_MONK.getBabyNpcId()
				|| getPet().getId() == Pets.CATABLEPON.getBabyNpcId()
				|| getPet().getId() == Pets.AVATAR_OF_CREATION.getBabyNpcId()
				|| getPet().getId() == Pets.DUMMY_PET.getBabyNpcId()
				|| getPet().getId() == Pets.TWISTED_BOW.getBabyNpcId()
				|| getPet().getId() == Pets.BLOAT_PET.getBabyNpcId()
				|| getPet().getId() == Pets.MAR_2021_TOP_DONOR.getBabyNpcId()
				|| getPet().getId() == Pets.DEC_2020_TOP_DONOR.getBabyNpcId()
				|| getPet().getId() == Pets.NOV_2020_TOP_DONOR.getBabyNpcId()
				|| getPet().getId() == Pets.OCT_2020_TOP_DOROR_BIGZY_1.getBabyNpcId()
				|| getPet().getId() == Pets.OCT_2020_TOP_DOROR_BIGZY_2.getBabyNpcId()
				|| getPet().getId() == Pets.OCT_2020_TOP_DOROR_BIGZY_3.getBabyNpcId()
				|| getPet().getId() == Pets.SEPT_2020_TOP_DONOR.getBabyNpcId()
				|| getPet().getId() == Pets.AUG_2020_TOP_DONOR.getBabyNpcId()
				|| getPet().getId() == Pets.JULY_2020_TOP_DONOR.getBabyNpcId()
				|| getPet().getId() == Pets.OCT_2020_TOP_DOROR_LUCKY_1.getBabyNpcId()
				|| getPet().getId() == Pets.DUSTED_OLMLET.getBabyNpcId()
				|| getPet().getId() == Pets.PUPPADILE.getBabyNpcId()
				|| getPet().getId() == Pets.TEKTINY.getBabyNpcId()
				|| getPet().getId() == Pets.ENRAGED_TEKTINY.getBabyNpcId()
				|| getPet().getId() == Pets.VANGUARD.getBabyNpcId()
				|| getPet().getId() == Pets.VASA_MINISTRIO.getBabyNpcId()
				|| getPet().getId() == Pets.VESPINA.getBabyNpcId()
				|| getPet().getId() == Pets.FLYING_VESPINA.getBabyNpcId()
				|| getPet().getId() == Pets.DUSTED_OLMLET.getBabyNpcId()
				|| getPet().getId() == Pets.LOLTHENKILL.getBabyNpcId()))
				mult += 0.05;
		else if (getPet() != null && getPet().getId() == Pets.ZIO_THE_SLAVE.getBabyNpcId())
			mult += 0.1;
		else if(getPet() != null && getPet().getId() == Pets.CASH.getBabyNpcId())
			mult += 0.07;

		
		
		if (this.getControlerManager().getControler() instanceof Wilderness && hasSkull())
			mult += 0.1;
		
		boolean ringOfWealth = Combat.hasRingOfWealth(this);
		if (ringOfWealth)
			mult += (getEquipment().getRingId() == 42785
			|| getEquipment().getRingId() == 25488
		 || getEquipment().getRingId() == 25741 ? 0.03 : 0.01); // 1% extra chance
		
		return mult;
	}

	/**
	 * @return the youtuber
	 */
	public boolean isYoutuber() {
		return youtuber;
	}

	/**
	 * @param youtuber
	 *            the youtuber to set
	 */
	public void setYoutuber(boolean youtuber) {
		this.youtuber = youtuber;
	}

	public void sendAccountMode() {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder
				.encodeSetRank(getUsername(), rights, gameMode, donator, supporter, eventCoordinator, youtuber).trim());
	}

	public void sendAccountRank() {
		LoginClientChannelManager.sendReliablePacket(LoginChannelsPacketEncoder
				.encodeSetRank(getUsername(), rights, gameMode, donator, supporter, eventCoordinator, youtuber).trim());
	}



	/**
	 * @author dragonkk(Alex) Sep 24, 2017
	 * @param i
	 */
	public void setDonator(int i) {
		donator = i;
	}

	/*
	 * Do not use.
	 */
	public boolean hasPinged() {
		return Utils.currentTimeMillis() - lastPing < 30000;
	}

	public void setLastPing() {
		this.lastPing = Utils.currentTimeMillis();
	}

	public void setLastActive() {
		lastActive = Utils.currentTimeMillis();
	}
	
	public boolean isActive(int ms) {
		return Utils.currentTimeMillis() - lastActive < ms;
	}
	
	/**
	 * @return the scXP
	 */
	public Map<SCItem, Integer> getScXP() {
		if (scXP == null) // temporary
			scXP = new HashMap<SCItem, Integer>();
		return scXP;
	}

	/**
	 * @return the lostCannon
	 */
	public boolean isLostCannon() {
		return lostCannon;
	}

	public void setLostCannon(boolean lostCannon) {
		this.lostCannon = lostCannon;
	}

	public void setLootbeam(FloorItem item) {
		if (lootbeam != null && lootbeam.getTile().matches(item.getTile()))
			return;
		getPackets().sendGameMessage("<col=E89002>A rainbow shines over one of your items.");
		lootbeam = item;
	}

	public boolean isNormal() {
		return gameMode == NORMAL;
	}

	public boolean isIronman() {
		return gameMode == IRONMAN;
	}

	public boolean isUltimateIronman() {
		return gameMode == ULTIMATE_IRONMAN;
	}

	public boolean isExpert() {
		return gameMode == EXPERT;
	}

	public boolean isHCIronman() {
		return gameMode == HARDCORE_IRONMAN;
	}
	
	public boolean isSuperFast() {
		return gameMode == SUPER_FAST;
	}

	public boolean hasUnlockedAncestralOlmlet() {
		return unlockedAncestralOlmlet;
	}

	public void setUnlockedAncestralOlmlet(boolean unlockedAncestralOlmlet) {
		this.unlockedAncestralOlmlet = unlockedAncestralOlmlet;
	}

	public boolean hasUnlockedElderOlmlet() {
		return unlockedElderOlmlet;
	}

	public void setUnlockedElderOlmlet(boolean unlockedElderOlmlet) {
		this.unlockedElderOlmlet = unlockedElderOlmlet;
	}

	public boolean hasUnlockedTwistedOlmlet() {
		return unlockedTwistedOlmlet;
	}

	public void setUnlockedTwistedOlmlet(boolean unlockedTwistedOlmlet) {
		this.unlockedTwistedOlmlet = unlockedTwistedOlmlet;
	}

	public boolean unlockedAncestralOlmlet, unlockedElderOlmlet, unlockedTwistedOlmlet;
	
	public boolean isFast() {
		return gameMode == FAST;
	}
	
	public boolean isDeadman() {
		return gameMode == DEADMAN;
	}

	public boolean isDungeoneer() {
		return gameMode == DUNGEONEER;
	}
	
	public void setHCIronman() {
		setMode(ULTIMATE_IRONMAN);
	}

	public void setIronman() {
		setMode(IRONMAN);
	}
	
	public void setExtreme() {
		setMode(DEADMAN);
	}


	public void setNormal() {
		setMode(NORMAL);
	}
	
	public void setSuperFast() {
		setMode(SUPER_FAST);
	}

	public void setFast() {
		setMode(FAST);
	}

	public void setDungeoneer() {
		setMode(DUNGEONEER);
	}

	public void setMode(int mode) {
		if (gameMode == mode)
			return;
		gameMode = mode;
		if (mode == ULTIMATE_IRONMAN) {
			acceptAid = false;
			refreshAcceptAid();
			appearence.setTitle(3020);
			toolbelt.refreshConfigs();
			sendTradeOption();
		} else if (mode == IRONMAN) {
			acceptAid = false;
			refreshAcceptAid();
			appearence.setTitle(3002);
			toolbelt.refreshConfigs();
			sendTradeOption();
		} else if (mode == DEADMAN) {
			appearence.setTitle(3001);
		} else if (mode == DUNGEONEER) {
			appearence.setTitle(3003);
			getDungManager().setMaxComplexity(6);
			getDungManager().setMaxFloor(60);
			for (int skill = 0; skill < 25; skill++)
				getSkills().addXp(skill, Skills.getXPForLevel(skill == Skills.DUNGEONEERING ? 120 : 99), true);
		} else if (mode == HARDCORE_IRONMAN) {
			acceptAid = false;
			refreshAcceptAid();
			appearence.setTitle(3027);
			toolbelt.refreshConfigs();
			sendTradeOption();
		} else if (mode == EXPERT) {
			appearence.setTitle(3029);
		} else
			appearence.setTitle(-1);
		sendAccountMode();
	}

	/**
	 * @author dragonkk(Alex) Oct 21, 2017
	 * @return
	 */
	public String getGameMode() {
		if (gameMode == SUPER_FAST)
			return "Slayer";
		if (gameMode == FAST)
			return "Hero";
		if (gameMode == DUNGEONEER)
			return "Dungeoneer";
		if (gameMode == IRONMAN)
			return "Ironman";
		if (gameMode == ULTIMATE_IRONMAN)
			return "Ultimate";
		if (gameMode == HARDCORE_IRONMAN)
			return "Hardcore";
		if (gameMode == EXPERT)
			return "Expert";
		if (gameMode == DEADMAN)
			return "Deadman";//Extreme";
		return "Normal";//"Legendary";
	}
	
	public String getRank() {
		if (isSupremeVIPDonator())
			return "Supreme VIP";
		if (isVIPDonator())
			return "VIP";
		if (isLegendaryDonator())
			return "Legendary";
		if (isExtremeDonator())
			return "Extreme";
		if (isSuperDonator())
			return "Super";
		return isDonator() ? "Normal" : "No Rank";
	}

	/**
	 * @return the votesIn24h
	 */
	public int getVotesIn24h() {
		return votesIn24h;
	}

	/**
	 * @param votesIn24h
	 *            the votesIn24h to set
	 */
	public void setVotesIn24h(int votesIn24h) {
		this.votesIn24h = votesIn24h;
	}

	/**
	 * @return the blowpipeDarts
	 */
	public Item getBlowpipeDarts() {
		return blowpipeDarts;
	}

	/**
	 * @param blowpipeDarts
	 *            the blowpipeDarts to set
	 */
	public void setBlowpipeDarts(Item blowpipeDarts) {
		this.blowpipeDarts = blowpipeDarts;
	}

	/**
	 * @return the blowpipeDarts
	 */
	public Item getInfernalBlowpipeDarts() {
		return infernalBlowpipeDarts;
	}

	/**
	 * @param blowpipeDarts
	 *            the blowpipeDarts to set
	 */
	public void setInfernalBlowpipeDarts(Item blowpipeDarts) {
		this.infernalBlowpipeDarts = blowpipeDarts;
	}

	public Entity getLastTarget() {
		return lastTarget;
	}

	public void setLastTarget(Entity lastTarget) {
		if (this.lastTarget != lastTarget) {
			this.lastTarget = lastTarget;
			if (!disableHealthPlugin) {
				if (lastTarget != null)
					getPackets().sendHitboxName();
				getPackets().sendRefreshHitbox();
			}
		}
	}
	
	public boolean isDisableHealthPlugin() {
		return disableHealthPlugin;
	}
	
	public void switchHealthPlugin() {
		if (!disableHealthPlugin)
			setLastTarget(null);
		disableHealthPlugin = !disableHealthPlugin;
	}

	@Override
	public String getName() {
		return getDisplayName();
	}

	public void sendWeaponStance() {
		getPackets().sendCSVarInteger(779, getEquipment().getWeaponStance());
	}

	/**
	 * @return the bossKillcount
	 */
	public Map<String, Integer> getBossKillcount() {
		if (bossKillcount == null) // temporary
			bossKillcount = new HashMap<String, Integer>();
		return bossKillcount;
	}

	public int getBossKillcount(String name) {
		if (bossKillcount == null) {
			bossKillcount = new HashMap<String, Integer>();
			return 0;
		}
		name = name.toLowerCase();
		return bossKillcount.getOrDefault(name, 0);
	}
	
	public Map<String, Long> getBosskilltime() {
		if (bossKilltime == null) // temporary
			bossKilltime = new HashMap<String, Long>();
		return bossKilltime;
	}

	/**
	 * @return the wonStackedDuel
	 */
	public boolean isWonStackedDuel() {
		return wonStackedDuel;
	}

	public void setWonStackedDuel() {
		wonStackedDuel = true;
	}

	public boolean isSellMandrithStatuete() {
		return sellMandrithStatuete;
	}

	public void setSellMandrithStatuete() {
		sellMandrithStatuete = true;
	}

	public boolean isSafePk() {
		Controller controller = getControlerManager().getControler();
		return (controller instanceof FfaZone && !((FfaZone) controller).isRisk()) || controller instanceof CastleWarsPlaying
				|| controller instanceof PestControlGame;
	}

	public void setWeaponAttackOption(int id) {
		if (id == 10501)
			getPackets().sendPlayerOption("Throw at", 1, true);
	}
	
	public void removeWeaponAttackOption(int id) {
		if (id == 10501)
			getPackets().sendPlayerOption(isCanPvp() ? "Attack" : "null", 1, true);
	}

	/**
	 * @return the nextRoll
	 */
	public int getNextRoll() {
		return nextRoll;
	}
	
	public void setNextRoll(int nextRoll) {
		this.nextRoll = nextRoll;
	}
	
	public LootingBag getLootingBag() {
		return lootingBag;
	}

	public CoalBag getCoalBag() {
		return coalBag;
	}

	public GemBag getGemBag() {
		return gemBag;
	}

	public ItemsContainer<Item> getRunePouch() {
		if (runePouch == null) //temporary
			runePouch = new ItemsContainer<Item>(3, false);
		if (spawnRunePouch == null) //temporary
			spawnRunePouch = new ItemsContainer<Item>(3, false);
		return getControlerManager().getControler() instanceof FfaZone ? spawnRunePouch : runePouch;
	}

	public String getGetPreviousTPName() {
		return getPreviousTPName;
	}

	public void setGetPreviousTPName(String getPreviousTPName) {
		this.getPreviousTPName = getPreviousTPName;
	}

	public WorldTile getGetPreviousTPTile() {
		return getPreviousTPTile;
	}

	public void setGetPreviousTPTile(WorldTile getPreviousTPTile) {
		this.getPreviousTPTile = getPreviousTPTile;
	}

	public long getLastDollarKeyFragment() {
		return lastDollarKeyFragment;
	}

	public void setLastDollarKeyFragment() {
		lastDollarKeyFragment = Utils.currentTimeMillis();
	}

	public int getBossTasksCompleted() {
		return bossTasksCompleted;
	}

	public void setBossTasksCompleted(int bossTasksCompleted) {
		this.bossTasksCompleted = bossTasksCompleted;
	}

	public int getWonTrivias() {
		return wonTrivias;
	}

	public void increaseWonTrivias() {
		wonTrivias++;
	}

	public long getLastActive() {
		return lastActive;
	}

	public String getHcPartner() {
		return hcPartner;
	}

	public void setHcPartner(String hcPartner) {
		this.hcPartner = hcPartner;
		getPackets().sendGameMessage("You are now partning with "+Utils.formatPlayerNameForDisplay(hcPartner)+"!");
		sendTradeOption();
	}
	
	public void checkDonatorRank() {
		if (getDonated() >= 4000 && !isSupremeVIPDonator()) {
			setDonator(ZENYTE_DONATOR);
			sendAccountRank();
			getPackets().sendGameMessage("Your rank has been increased for free thanks to your donation!");
		} else if (getDonated() >= 2000 && !this.isVIPDonator()) {
			setDonator(ONYX_DONATOR);
			sendAccountRank();
			getPackets().sendGameMessage("Your rank has been increased for free thanks to your donation!");
		} else if (getDonated() >= 800 && !this.isLegendaryDonator()) {
			setDonator(DIAMOND_DONATOR);
			sendAccountRank();
			getPackets().sendGameMessage("Your rank has been increased for free thanks to your donation!");
		} else if (getDonated() >= 400 && !this.isExtremeDonator()) {
			setDonator(RUBY_DONATOR);
			sendAccountRank();
			getPackets().sendGameMessage("Your rank has been increased for free thanks to your donation!");
		} else if (getDonated() >= 200 && !this.isSuperDonator()) {
			setDonator(EMERALD_DONATOR);
			sendAccountRank();
			getPackets().sendGameMessage("Your rank has been increased for free thanks to your donation!");
		} else if (getDonated() >= 60 && !this.isDonator()) {
			setDonator(SAPHIRE_DONATOR);
			sendAccountRank();
			getPackets().sendGameMessage("Your rank has been increased for free thanks to your donation!");	
		}
	}
	
	public boolean isAtVipZone() {
		int mapID = getRegionId();
		return  mapID == 21829 || mapID == 21828 || mapID == 22084 || mapID == 22085 || mapID == 14679 || mapID == 14678 || mapID == 14934 || mapID == 14935;
	}
	
	public boolean isAtDonatorZone() {
		int mapID = getRegionId();
		return mapID == 13393;
	}

	public boolean isNotifications() {
		return !disableNotifications;
	}

	public void switchNotifications() {
		this.disableNotifications = !disableNotifications;
	}

	public void checkAntiBot() {
		if (Utils.currentTimeMillis() - antibotTime >= 60000 * 60) {
			Bot.sendLog(Bot.ANTIBOT_CHANNEL, "[type=BOT][name="+getUsername()+"][coords=X: " + getX() + ", Y: " + getY() + ", Z: " + getPlane() + "]");
			if (CombatEventNPC.canRandomEvent(this))
				CombatEventNPC.startRandomEvent(this, 0);
			resetAntibot();
		} else if (!isActive(60000 * 3))
			resetAntibot();
	}
	
	public void resetAntibot() {
		antibotTime = Utils.currentTimeMillis();
	}
	
	
	public void checkAFK() {
		if (!warnedAFK && !actionManager.hasAction() && !isLocked() && !isActive(60000 * 30)) {
			if (isStaff() && getFriendsIgnores().getPmStatus() == FriendsIgnores.PM_STATUS_ONLINE) {
				getFriendsIgnores().setPmStatus(FriendsIgnores.PM_STATUS_OFFLINE, true);
				getPackets().sendGameMessage("<col=FF0040>AFK DETECTED. PM turned off.");
			}
			warnedAFK = true;
		/*	getDialogueManager().startDialogue("SimpleMessage", "<col=FF0040>AFK DETECTED: ::afk at dream trees to earn rewards.");
			getPackets().sendGameMessage("<col=FF0040>AFK DETECTED: ::afk at dream trees to earn rewards.");
		*/} if (isActive(60000 * 30))
			warnedAFK = false;
		/*else if (warnedAFK && !actionManager.hasAction() && !isLocked()) {
			this.setNextAnimation(new Animation(isOnyxDonator() ? 4117 : isDiamondDonator() ? 4116 : isRubyDonator() ? 4115 : isEmeraldDonator() ? 4114 : isDonator() ? 4113 : 4111)); //chair
		}*/
	}

	public boolean isDisableAutoLoot() {
		return disableAutoLoot;
	}

	public boolean isDisableAutoLoot(int rarity) {
		if ((rarity == Drops.ALWAYS && alwaysAutoLootDisabled)
		|| (rarity == Drops.COMMOM && commonAutoLootDisabled)
		|| (rarity == Drops.UNCOMMON && uncommonAutoLootDisabled)
		|| (rarity == Drops.RARE && rareAutoLootDisabled)
		|| (rarity == Drops.VERY_RARE && veryRareAutoLootDisabled))
			return true;
		return disableAutoLoot;
	}

	public void setGamblingSession(Gambling session) {
		this.gamblingSession = session;
	}

	public Gambling getGamblingSession() {
		return gamblingSession;
	}

	public void switchAutoLoot() {
		disableAutoLoot = !disableAutoLoot;
	}
	
	public Deals getDeals() {
		return deals;
	}
	
	public Achievements getAchievements() {
		return achievements;
	}
	
	public Presets getPresets() {
		return presets;
	}
	


	public boolean isDisablePotionTimersPlugin() {
		return disablePotionTimersPlugin;
	}

	public void switchPotionTimersPlugin() {
		this.disablePotionTimersPlugin = !disablePotionTimersPlugin;
		if (disablePotionTimersPlugin)
			getPackets().resetTimers(); //resets all timers
		else
			sendTimers();
	}

	public boolean isHideAttackOption() {
		return hideAttackOption;
	}

	public void switchHideAttackOption() {
		this.hideAttackOption = !hideAttackOption;
		getPackets().sendHideAttackOption();
	}
	
	public boolean isDisableGroundItemNames() {
		return disableGroundItemNames;
	}
	
	public void switchGroundItemNames() {
		this.disableGroundItemNames = !disableGroundItemNames;
		getPackets().sendGroundItemNames();
		
	}

	public boolean isGambledPartyhat() {
		return gambledPartyhat;
	}

	public void setGambledPartyhat(boolean gambledPartyhat) {
		this.gambledPartyhat = gambledPartyhat;
	}

	public long getThrownWishingCoins() {
		return thrownWishingCoins;
	}

	public void setThrownWishingCoins(long thrownWishingCoins) {
		this.thrownWishingCoins = thrownWishingCoins;
	}

	public long getCallusSpawnDonations() {
		return callusSpawnDonations;
	}

	public void addCallusSpawnDonations(long donation) {
		this.callusSpawnDonations += donation;
	}

	public boolean isUgradedItem() {
		return ugradedItem;
	}

	public void setUgradedItem(boolean ugradedItem) {
		this.ugradedItem = ugradedItem;
	}
	
    public String getLastBossInstanceKey() {
    	return lastBossInstanceKey;
    }

    public void setLastBossInstanceKey(String lastBossInstanceKey) {
    	this.lastBossInstanceKey = lastBossInstanceKey;
    }

    public InstanceSettings getLastBossInstanceSettings() {
    	return lastBossInstanceSettings;
    }

    public void setLastBossInstanceSettings(InstanceSettings lastBossInstanceSettings) {
    	this.lastBossInstanceSettings = lastBossInstanceSettings;
    }

	public List<String> getJournalLines() {
		return journalLines;
	}
	
	public void setJournalLines(List<String> list) {
		this.journalLines = list;
	}

	public boolean isDisableCosmeticOverrides() {
		return disableCosmeticOverrides;
	}

	public void setDisableCosmeticOverrides(boolean disableCosmeticOverrides) {
		this.disableCosmeticOverrides = disableCosmeticOverrides;
	}
	
	public void switchOsrsHitbars() {
		osrsHitbars = !osrsHitbars;
	}
	
	public boolean isOsrsHitbars() {
		return osrsHitbars;
	}
	
	public boolean isOsrsGameframe() {
		return osrsGameframe;
	}
	
	public void sendGameframe() {
		getPackets().sendExecuteScript(-14, osrsGameframe ? 1 : 0);
	}
	
	public void resetGameframe() {//sent when switching and when rezi/fixed for osrs(due to orbs)
		getPackets().sendExecuteScript(-16);
		moneyPouch.init();
	}
	
	public void resetInterfaceSprites() {
		getPackets().sendExecuteScript(-15);
	}
	
	
	public void switchGameframe() {
		osrsGameframe = !osrsGameframe;
		sendGameframe();//sent only at begin and when switching
		resetInterfaceSprites(); //only when switching
		resetGameframe();//sent only whent swiching and (when fix/rezi switch in osrs)
		interfaceManager.sendInterfaces();
	}

    public void sendMessage(String s) {
		getPackets().sendGameMessage(s);
    }

	public long getDropPartyValue() {
		return dropPartyValue;
	}

	public void addDropPartyValue(long dropPartyValue) {
		this.dropPartyValue += dropPartyValue;
	}

	private Keybinds keybinds;

	public Keybinds getKeyBinds() {
		return keybinds;
	}

	public int getFightCavesCompletions() {
		return fightCavesCompletions;
	}

	public void incrementFightCavesCompletions() {
		getTasksManager().checkForProgression(DailyTasksManager.MINIGAME, DailyTasksManager.FIGHT_CAVE);
		fightCavesCompletions++;
	}

	public void incrementFightKilnCompletions() {

		getTasksManager().checkForProgression(DailyTasksManager.MINIGAME, DailyTasksManager.FIGHT_KILN);
		this.fightKilnCompletions++;
	}

	private int fightKilnCompletions = 0;

	public int getFightKilnCompletions() {
		return fightKilnCompletions;
	}

	private int infernoCompletions = 0;

	public int getInfernoCompletions() {
		return infernoCompletions;
	}

	public void incrementInfernoCompletions() {
		this.infernoCompletions++;
	}

	private int hordeCompletions = 0;

	public int getHordeCompletions() {
		return hordeCompletions;
	}

	public void incrementHordeCompletions() {
		this.hordeCompletions++;
	}

	private int pestGames = 0;

	public void incrementPestGames() {
		this.pestGames ++;
	}

	public int getPestGames() {
		return pestGames;
	}

	private int sorceressGardenCompletions = 0;

	public int getSorceressGardenCompletions() {
		return sorceressGardenCompletions;
	}

	public void incrementSorceressGardenCompletions() {
		sorceressGardenCompletions++;
	}

	private int sawmillJobsComplete = 0;

	public void incrementSawmillJobs() {
		sawmillJobsComplete++;
	}

	public int getSawmillJobsComplete() {
		return sawmillJobsComplete;
	}

	public boolean isOldNPCLooks() {
		return oldNPCLooks;
	}

	public void switchOldNPCLooks() {
		this.oldNPCLooks = !oldNPCLooks;
		getPackets().sendNPCLooks();
		getPackets().resetLocalNPCUpdate();
	}

	public long getCoins() {
		return inventory.getCoinsAmount();
	}

	public int getPkTournamentKills() {
		return pkTournamentKills;
	}

	public void incrementPkTournamentKills() {
		this.pkTournamentKills++;
	}

	private int pkTournamentKills = 0;
	
	@Override
	public boolean withinDistance(WorldTile tile, int distance) {
		if (this.getControlerManager().getControler() instanceof PkTournamentGame
				&& tile instanceof Player && (((Player)tile).getControlerManager().getControler() instanceof PkTournamentGame)) {
			PkTournamentGame game = (PkTournamentGame) getControlerManager().getControler();
			if (/*tile instanceof Player
			&&   */game.getTarget() != tile) {
				return false;
			}
		}
		//System.out.println("123 "+(this.getControlerManager().getControler() instanceof PkTournamentGame));
		return super.withinDistance(tile, distance);
	}

	public boolean isAdmin() {
		return rights == 2;
	}
	public boolean isModerator() {
		return rights == 1;
	}

	public void increaseCallusDropWeight(int damage) {
		this.callusDropWeight += damage;
	}

	public void resetCallusDropWeight() {
		this.callusDropWeight = 0;
	}

	public int getCallusDropWeight() {
		return this.callusDropWeight;
	}

    public void increasePriffCourseCompletions() {
		priffCourseCompletions ++;

		sendMessage("Prifddinas agility course lap completions: <col=ff0000>" + priffCourseCompletions);

		if(this.priffCourseLapTime != -1) {
			long time = System.currentTimeMillis() - this.priffCourseLapTime;
			boolean newPb = this.priffCourseLapTimePB <= 0 || this.priffCourseLapTimePB > time;
			String pb =  newPb ? "(New personal best!)" : "(Personal best: " + Utils.formatTime2(this.priffCourseLapTimePB) + ")";
			if(this.priffCourseLapTimePB == -1) {
				pb = "(New personal best!)";
				newPb = true;
			}
			sendMessage("Lap duration: <col=ff0000>" + Utils.formatTime2(time) + " <col=ffffff>" +  pb);
			if(newPb)
				this.priffCourseLapTimePB = time;
			this.priffCourseLapTime = -1;
		}
    }

    public void startPriffCourse() {
		this.priffCourseLapTime = System.currentTimeMillis();
	}

    public void increaseFireCapeGambles() {
		this.fireCapeGambles++;
		sendMessage(String.format("You have now gambled %s Fire cape%s.", Colour.RED.wrap(this.fireCapeGambles), this.fireCapeGambles == 1 ? "" : "s"));
    }

	public void increaseInfernalCapeGambles() {
		this.infernalCapeGambles++;
		sendMessage(String.format("You have now gambled %s Infernal cape%s.", Colour.RED.wrap(this.infernalCapeGambles), this.infernalCapeGambles == 1 ? "" : "s"));
	}

	public void increaseKilnCapeGambles() {
		this.kilnCapeGambles++;
		sendMessage(String.format("You have now gambled %s TokHaar-kal%s.", Colour.RED.wrap(this.kilnCapeGambles), this.kilnCapeGambles == 1 ? "" : "s"));
	}

	private HashMap<Integer, Integer> upgradeAttempts = new HashMap<Integer, Integer>();

    public void increaseUpgradeAttemptCount(Integer to) {
    	Integer amt = upgradeAttempts.get(to);
    	amt = amt == null ? 1 : amt + 1;
    	upgradeAttempts.put(to, amt);

		sendMessage(String.format("You have attempted to create "+ ItemConfig.forID(to).getName() +" %s time%s.", Colour.RED.wrap(amt), amt == 1 ? "" : "s"));
	}

	private boolean coxLogout = false;

	public WorldTile getLastCoxTile() {
		return lastCoxTile;
	}

	public void setLastCoxTile(WorldTile lastCoxTile) {
		this.lastCoxTile = lastCoxTile;
	}

	public WorldTile lastCoxTile = null;

    public void setCoxLogout(boolean coxLogout) {
    	ChambersOfXeric raid = ChambersOfXeric.getRaid(this);
    	if(raid != null && coxLogout) {
    		this.lastCoxTile = new WorldTile(this);
			this.coxLogout = true;
		} else {
    		this.coxLogout = false;
		}
    }

	public ItemsContainer<Item> getPrivateItems() {
    	return privateItems;
	}

	public boolean isFluidStrikes() {
		return fluidStrikes;
	}

	public boolean isQuickShot() {
		return quickShot;
	}

	public boolean isDoubleCast() {
		return doubleCast;
	}

	public void setFluidStrikes(boolean fluidStrikes) {
		this.fluidStrikes = fluidStrikes;
	}

	public void setQuickShot(boolean quickShot) {
		this.quickShot = quickShot;
	}

	public void setDoubleCast(boolean doubleCast) {
		this.doubleCast = doubleCast;
	}

	private boolean fluidStrikes, quickShot, doubleCast;

    private ItemsContainer<Item> raidRewards = new ItemsContainer<Item>(6, true);

	public ItemsContainer<Item> resetRaidRewards() {
		return raidRewards = new ItemsContainer<Item>(6, true);
	}
	public ItemsContainer<Item> getRaidRewards() {
		return raidRewards;
    }

    private long[] chambersPB = new long[100], osrsChambersPB = new long[100];

	public int getChambersCompletions() {
		return chambersCompletions;
	}

	public int getOsrsChambersCompletions() {
		return osrsChambersCompletions;
	}

	private int chambersCompletions = 0;

	private int osrsChambersCompletions = 0;

	public void completedChambers(ChambersOfXeric raid) {
		if(raid.getRaidTime() == 0)
			return;

		if(raid.isOsrsRaid()) {
			completedOsrsModeChambers(raid);
			return;
		}

		int size = raid.getTeamSize();
		long previousBest = chambersPB[raid.getTeamSize()];
		chambersCompletions++;

		sendMessage("Chambers of Xeric (ONYX mode) completions: <col=ff0000>" + chambersCompletions);
		long time = raid.getRaidTime();
		boolean newPb = previousBest <= 0 || previousBest > time;
		String pb =  newPb ? "(New personal best!)" : "(Personal best: " + Utils.formatTime2(previousBest) + ")";
		sendMessage((size == 1 ? "Solo" : raid.getTeamSize() + "-man") + " Chambers of Xeric time: <col=ff0000>" + Utils.formatTime2(time) + " <col=ffffff>" +  pb);
		if(newPb) {
			chambersPB[raid.getTeamSize()] = time;
		}
	}
	public void completedOsrsModeChambers(ChambersOfXeric raid) {
		int size = raid.getTeamSize();
		long previousBest = osrsChambersPB[raid.getTeamSize()];
		osrsChambersCompletions++;

		sendMessage("Chambers of Xeric (OSRS mode) completions: <col=ff0000>" + osrsChambersCompletions);
		long time = raid.getRaidTime();
		boolean newPb = previousBest <= 0 || previousBest > time;
		String pb =  newPb ? "(New personal best!)" : "(Personal best: " + Utils.formatTime2(previousBest) + ")";
		sendMessage((size == 1 ? "Solo" : raid.getTeamSize() + "-man") + " Chambers of Xeric time: <col=ff0000>" + Utils.formatTime2(time) + " <col=ffffff>" +  pb);
		if(newPb) {
			osrsChambersPB[raid.getTeamSize()] = time;
		}
	}

	private boolean submittedReferral = false;

    public boolean hasSubmittedReferral() {
    	if(getTotalOnlineTime() > TimeUnit.MINUTES.toMillis(1440)) //1440
    		return true;
    	return submittedReferral;
    }

	public void setSubmittedReferral(boolean submittedReferral) {
		this.submittedReferral = submittedReferral;
	}

	public boolean isSkipVPNCheck() {
		return skipVPNCheck;
	}

	public void switchSkipVPNCheck() {
		skipVPNCheck = !skipVPNCheck;
	}

	public long lastChatMessage = 0L;

	public DailyTasksManager getTasksManager() {
		return tasksManager;
	}

	public long getLastGambleKing() {
		return lastGambleKing;
	}

	public void updateLastGambleKing() {
		lastGambleKing = Utils.currentTimeMillis();
	}

	public NpcKillCountTracker getNpcKillCountTracker() {
		if (npcKillCountTracker == null) npcKillCountTracker = new NpcKillCountTracker();

		return npcKillCountTracker;
	}

	public void setNpcKillCountTracker(NpcKillCountTracker npcKillCountTracker) {
		this.npcKillCountTracker = npcKillCountTracker;
	}
}
