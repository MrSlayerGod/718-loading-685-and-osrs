package com.rs.game.player.content.raids.cox;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.FloorItem;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.MapBuilder;
import com.rs.game.map.MapInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.cox.COXBoss;
import com.rs.game.npc.cox.impl.GreatOlm;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FriendsChat;
import com.rs.game.player.content.PlayerExamine;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.game.player.content.raids.cox.chamber.Chamber;
import com.rs.game.player.content.raids.cox.chamber.impl.*;
import com.rs.game.player.controllers.ChambersOfXericController;
import com.rs.game.player.controllers.partyroom.PartyRoom;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.ButtonHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.Colour;
import com.rs.utils.ItemSetsKeyGenerator;
import com.rs.utils.Stopwatch;
import com.rs.utils.Utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChambersOfXeric extends MapInstance {

    public static boolean ENABLED = true;

    public static final WorldTile OUTSIDE = new WorldTile(1233, 3572, 0);
    public static final int LEAVE = 0, TELEPORT = 1, LOGOUT = 2;
    public static Map<String, ChambersOfXeric> raidingParties = Collections.synchronizedMap(new HashMap<>());
    private final List<Player> team;
    private final FriendsChat fc;
    private int partyPoints = 0;
    private final Map<String, Integer> pointMap = Collections.synchronizedMap(new HashMap<>());
	private final ArrayList<Chamber> chambers = new ArrayList<>();
    private final ArrayList<COXBoss> chambersNPCs = new ArrayList<>();
	public WorldTile checkpointTile;
	private int size;
	private long startTime = -1;
	private long endTime = -1;
	private double combatPointsFactor = 1.0;

    // HARDCODED CHAMBERS
    private final EntranceChamber entranceChamber = new EntranceChamber(3, 1, 3, this);
    private final IceDemonChamber iceDemonChamber = new IceDemonChamber(2, 2, 3, this);
    private final ShamanChamber shamanChamber = new ShamanChamber(3, 2, 3, this);
    private final TektonChamber tektonChamber = new TektonChamber(2, 1, 3, this);
    private final ScavengerChamber scavsChamberUpper = new ScavengerChamber(1, 2, 3, this);
    private final ResourceChamber resourceChamberUpper = new ResourceChamber(4, 1, 3, this);
    private final ScavengerChamber scavsChamberMid = new ScavengerChamber(1, 1, 2, this);
    private final ThievingChamber thievingChamber = new ThievingChamber(2, 1, 2, this);
    private final VanguardChamber vanguardChamber = new VanguardChamber(3, 1, 2, this);
    private final TightropeChamber tightropeChamber = new TightropeChamber(3, 2, 2, this);
    private final ResourceChamber resourceChamberMid = new ResourceChamber(2, 2, 2, this);
    private final VespulaChamber vespulaChamber = new VespulaChamber(1, 2, 2, this);
    private final MuttadileChamber muttadileChamber = new MuttadileChamber(2, 1, 1, this);
    private final MysticsChamber mysticsChamber = new MysticsChamber(1, 1, 1, this);
    private final VasaChamber vasaChamber = new VasaChamber(2, 2, 1, this);
    private final ResourceChamber resourceChamberLower = new ResourceChamber(3, 1, 1, this);
    private final GreatOlmChamber greatOlmChamber = new GreatOlmChamber(0, 0, 0, this);
    private final CrabsChamber crabsChamber = new CrabsChamber(1, 1, 3, this);
    private final GuardianChamber guardianChamber = new GuardianChamber(3, 2, 1, this);
	private final ScavengerChamber scavsChamberLower = new ScavengerChamber(1, 2, 1, this);
	private final UnnamedChamber unknownChamber = new UnnamedChamber(-1,-1,-1,this) {@Override public void onRaidStart() {}};
    private int scavsKilled = 0;
    private static final int SCAV_CAP = 6;

    public static boolean isCoxItem(int id) {
        return coxItems.contains(id);
    }

    public static void reloadRegion(Player player) {
        ChambersOfXeric raid = getRaid(player);
        if(raid == null) {
            player.sendMessage("You are not in a raid.");
        } else {
            player.lock(3);
            //WorldTile position = player.clone();
            player.getControlerManager().removeControlerWithoutCheck();
            player.useStairs(-1, OUTSIDE, 0, 0);
            WorldTasksManager.schedule(() -> {
                raid.add(player, true);
            }, 2);
        }
    }

    public ThievingChamber getThievingChamber() {
        return thievingChamber;
    }

    public TightropeChamber getTightropeChamber() {
        return tightropeChamber;
    }

    public void sharedDeposit(Player player, int item, int amt) {
        if(!getCoxItems().contains(item)) {
            player.sendMessage("You cannot store that item in the shared storage.");
            return;
        }
        amt = Math.min(player.getInventory().getAmountOf(item), amt);
        if(amt == 0)
            return;
        synchronized (sharedItems) {
            int chest = 0;
            for (Item i : sharedItems.getItems()) {
                if (i != null)
                    chest += i.getAmount();
            }

            if (chest == maximumSharedCapacity) {
                player.sendMessage("The public storage is full, remove some items before trying to add more.");
                return;
            }

            int deposit = amt;

            if (amt + chest > maximumSharedCapacity) {
                deposit = maximumSharedCapacity - chest;
                player.sendMessage("The public storage is now full, remove some items before trying to add more.");
            }

            Item i = new Item(item, deposit);
            player.getInventory().deleteItem(i);
            sharedItems.add(i);
            sharedStorageUpdateSubscribers.forEach(this::refreshSharedInterface);
        }
    }

    public boolean privateDeposit(Player player, int item, int amt, boolean refresh) {
        if(getRaid(player) == null) {
            player.sendMessage("<col=ff0000>You may only withdraw items here.");
            return false;
        }
        int inInv = player.getInventory().getAmountOf(item);
        amt = Math.min(inInv, amt);
        if(inInv == 0)
            return false;
        if(inInv < amt) {
            amt = inInv;
        }

        int chest = 0;
        for(Item i : player.getPrivateItems().getItems()) {
            if(i != null)
                chest++;
        }

        if(new Item(item).getDefinitions().isStackable()) {
            if(chest == maximumPrivateCapacity && !player.getPrivateItems().containsOne(new Item(item))) {
                player.sendMessage("Your storage is full, remove some items before trying to add more.");
                return false;
            }
        } else {
            if(chest == maximumPrivateCapacity) {
                player.sendMessage("Your storage is full, remove some items before trying to add more.");
                return false;
            }
        }

        int deposit = amt;

        if(player.getInventory().requiredSlots(new Item(item, amt)) + chest > maximumPrivateCapacity) {
            deposit = maximumPrivateCapacity - chest;
            player.sendMessage("Your storage is now full, remove some items before trying to add more.");
        }

        Item i = new Item(item, deposit);
        player.getInventory().getItems().remove(i);
        player.getPrivateItems().add(i);

        if(refresh) {
            player.getInventory().refresh();
            refreshPrivateInterface(player);
        }

        return true;
    }

    public static void privateWithdraw(Player player, int itemId, int amt) {
        int inChest =  player.getPrivateItems().getNumberOf(itemId);
        amt = Math.min(amt, inChest);
        if(amt == 0) {
            return;
        }

        Item toDeposit = new Item(itemId, amt);
        Item depositRemainder = player.getInventory().add(toDeposit);

        // not all items fit into inventory
        if(depositRemainder != null) {
            if(depositRemainder.getAmount() == toDeposit.getAmount()) {
                // inventory full
                return;
            }
            player.sendMessage("You do not have enough space in your inventory.");
            // remove only the items that were moved
            Item itemsRemoved = new Item(toDeposit.getId(), toDeposit.getAmount() - depositRemainder.getAmount());
            player.getPrivateItems().remove(itemsRemoved);
        } else {
            player.getPrivateItems().remove(toDeposit);
        }

        player.getPrivateItems().shift();
        refreshPrivateInterface(player);
    }

    public void sharedWithdraw(Player player, int itemId, int amt) {
        if(player.isIronman() || player.isUltimateIronman() || player.isHCIronman()) {
            player.sendMessage("Ironmen cannot withdraw items from the shared chest.");
            return;
        }
        synchronized (sharedItems) {
            int inChest =  sharedItems.getNumberOf(itemId);
            amt = Math.min(amt, inChest);
            if(amt == 0) {
                return;
            }

            Item toDeposit = new Item(itemId, amt);
            Item depositRemainder = player.getInventory().add(toDeposit);

            // not all items fit into inventory
            if(depositRemainder != null) {
                if(depositRemainder.getAmount() == toDeposit.getAmount()) {
                    // inventory full
                    return;
                }
                player.sendMessage("You do not have enough space in your inventory.");
                // remove only the items that were moved
                Item itemsRemoved = new Item(toDeposit.getId(), toDeposit.getAmount() - depositRemainder.getAmount());
                sharedItems.remove(itemsRemoved);
            } else {
                sharedItems.remove(toDeposit);
            }

            sharedItems.shift();
            sharedStorageUpdateSubscribers.forEach(this::refreshSharedInterface);
        }
    }

    public long getRaidTime() {
	    if(startTime == -1)
	        return 0;
	    if(endTime != -1)
	        return endTime - startTime;

	    return Utils.currentTimeMillis() - startTime;
    }

    public String formatRaidTime() {
	    return Utils.formatTimeCox(getRaidTime());
    }

    /**
     * Creator is first player in instance, no specifically the leader
     */
    public ChambersOfXeric(Player creator, FriendsChat chat) {
        super(0, 0, 3, 2);
        fc = chat;
		creator.lock();
		creator.stopAll();
		team = new CopyOnWriteArrayList<Player>();
		load(() -> {
			getChambers().addAll(Arrays.asList(entranceChamber, iceDemonChamber, shamanChamber,
					tektonChamber, scavsChamberUpper, resourceChamberUpper, scavsChamberMid, thievingChamber,
					vanguardChamber, tightropeChamber, resourceChamberMid, vespulaChamber, muttadileChamber,
					mysticsChamber, vasaChamber, resourceChamberLower, crabsChamber, guardianChamber,
					scavsChamberLower, greatOlmChamber));

			// wait for map to be built
            checkpoint = Checkpoints.WAITING;
            checkpointTile = getTile(111, 35, 3);

            // init chambers
            getChambers().forEach(chamber -> {
                WorldTasksManager.schedule(() ->
                    chamber.onRaidStart(), 2);
            });

            add(creator, false);

            // broadcast start message
            for (Player player : chat.getLocalMembers()) {
                player.getPackets().sendGameMessage("<col=ff66cc>" + (player == creator ? "Inviting party" : "Your party has entered the dungeons! Come and join them now."));
            }
		});
	}

    public static void enter(Player player) {
        if (!ENABLED) {
            player.sendMessage("You hear strange noises from below.");
            return;
        }

        if(!player.getPrivateItems().isEmpty()) {
            // remove all chambers items - need to do it here in case player glitches out of cox with items in private
            for(int i = 0; i < player.getPrivateItems().getItems().length; i++) {
                Item item = player.getPrivateItems().get(i);
                if(item == null)
                    continue;
                if(isCoxItem(item.getId())) {
                    player.getPrivateItems().set(i, null);
                }
            }
            if(!player.getPrivateItems().isEmpty()) {
                player.getPrivateItems().shift();
                player.sendMessage("<col=ff0000>You must remove all items from your private storage before entering the raid!");
                openPrivateStorage(player, true);
            }
            return;
        }

        if (player.getFamiliar() != null || Summoning.hasPouch(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't want your friends to be eaten. You are not allowed to take familiars onto raids.");
            return;
        }


        FriendsChat chat = player.getCurrentFriendsChat();
        if (chat == null) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need to be part of a friend chat in order to enter.");
            player.getInterfaceManager().openGameTab(10);
            return;
        }

        ChambersOfXeric raid = findInstance(chat);

        if (raid == null) {
            // create raid
            raid = new ChambersOfXeric(player, chat);
            raidingParties.put(chat.getChannel(), raid);
        } else {

            // join raid
            if (raid.hasStarted()) {
                if(raid.getTeam().size() != 0 && !raid.getFc().toLowerCase().equals(player.getUsername())) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "This raid has already started.");
                } else {
                    raid.remove(player, 0);
                    raidingParties.remove(raid);
                    raid.destroy(false);
                    player.getDialogueManager().startDialogue("SimpleMessage", "The previous raid party has disbanded.");
                }
                return;
            }
            if(raid.bannedPlayerList.contains(player.getUsername())) {
                player.sendMessage("You have been kicked from this raid, you may not re-join until the next raid.");
                return;
            }

            raid.add(player, false);
        }
    }

    public static ChambersOfXeric findInstance(String channel) {
        return raidingParties.get(channel);
    }

    public static ChambersOfXeric findInstance(FriendsChat chat) {
        return raidingParties.get(chat.getChannel());
    }

    public static ChambersOfXeric getRaid(Player player) {
        if (player.getControlerManager().getControler() == null || !(player.getControlerManager().getControler() instanceof ChambersOfXericController))
            return null;
        return ((ChambersOfXericController) player.getControlerManager().getControler()).getRaid();
    }

    public static void enterOlmRoom(Player player) {
        ChambersOfXeric raid = getRaid(player);
        if(raid != null) {
            player.lock(3);
            boolean run = player.isRunning();
            player.setRun(false);
            player.addWalkSteps(player.getX(), player.getY() + 2, 2, false);
            WorldTasksManager.schedule(() -> player.setRun(run), 3);
            raid.getGreatOlmChamber().setActivated(true);
        }
    }

    private static final Integer[] COX_ITEMS = {
         51036, 51037, 51038, 51039, 51040, 51041, 51042, ChambersHerblore.EMPTY_GOURD_VIAL, ChambersHerblore.WATER_FILLED_GOURD_VIAL
    };

    public static final int SWITCH_MODE_BTN = 21, START_RAID_BTN = 17, RAID_INFO_BTN = 16,  SORT_BY_NAME_BTN = 1020, SORT_BY_CB_BTN = 1021, SORT_BY_TOTAL_BTN = 1022;

    public void processLookupClick(Player p, int i) {
        if(i > -1 && i < partyInfo.size()) {
            Player lookup = World.getPlayerByDisplayName(partyInfo.get(i).name);
            if(lookup == null) {
                p.sendMessage("Could not find " + partyInfo.get(i).name + "!");
                return;
            }
            p.sendMessage("Showing info for " + p.getDisplayName() + " ...");
            PlayerExamine.examine(p, lookup);
            int x = p.getChambersCompletions();
            p.sendMessage(lookup.getDisplayName() + " has completed Chambers of Xeric mode " + x + " time" + (x == 1 ? "" : "s") + ".");
           /* x = p.getOsrsChambersCompletions();
            p.sendMessage(lookup.getDisplayName() + " has completed Chambers of Xeric OSRS mode " + x + " time" + (x == 1 ? "" : "s") + ".");*/
        }
    }

    private ArrayList<String> bannedPlayerList = new ArrayList<>();

    public void processKickClick(Player p, int i) {
        if(hasStarted()) {
            p.sendMessage("You cannot kick players once the raid has started.");
            return;
        }
        if(i > -1 && i < partyInfo.size()) {
            Player lookup = World.getPlayerByDisplayName(partyInfo.get(i).name);
            if(lookup == null) {
                p.sendMessage("Could not find " + partyInfo.get(i).name + "!");
                return;
            }
            if(lookup == p) {
                p.sendMessage("You cannot kick yourself.");
                return;
            }

            bannedPlayerList.add(lookup.getUsername());
            p.sendMessage("Kicking " + p.getDisplayName() + " from the raid...");
            remove(lookup, 3);
            WorldTasksManager.schedule(()
                    -> lookup.getDialogueManager().startDialogue("SimpleMessage", "You have been kicked from the raiding party."));
        }
    }

    public static boolean addOsrsBlacklistItem(int id) {
        if(id < 1)
            return false;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/item/coxOsrsBlacklist.txt", true))) {
            bw.write("" + id);
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        bannedOsrsRaidItems.clear();
        loadOsrsBlacklist();
        return true;
    }

    public static int getBannedCount() {
        return bannedOsrsRaidItems.size();
    }

    private static ArrayList<Integer> bannedOsrsRaidItems = new ArrayList<>();

    public static void loadOsrsBlacklist() {
        File f = new File("data/item/coxOsrsBlacklist.txt");
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String data;
            int line = 0;
            while((data = br.readLine()) != null) {
                data = data.trim();
                if(data.length() == 0)
                    continue;
                if(data.startsWith("//"))
                    continue;
                line++;
                try {
                    bannedOsrsRaidItems.add(Integer.parseInt(data));
                } catch(Exception e) { System.out.println("Error on line " + line + " of coxOsrsBlacklist.txt");}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        getCoxItems().addAll(Arrays.asList(COX_ITEMS));

        for(int i = 50853; i <= 50996; i++) {
            getCoxItems().add(i);
        }

        // click start raid button on side panel
        ButtonHandler.register(375, 3, 1, (player, slot1, slot2, action) -> {
            if (ChambersOfXeric.getRaid(player) != null) {
                if (checkFC(player) != null && player.getCurrentFriendsChat().isOwner(player)) {
                    player.getDialogueManager().startDialogue("COXStartD");
                } else {
                    player.sendMessage("Only the FC owner " + player.getCurrentFriendsChat().getChannel() + " can start the raid.");
                }
            } else {
                player.sendMessage("No raid found.");
            }
        });

        IceDemonChamber.init();
        VespulaChamber.init();
        MuttadileChamber.init();
        CrabsChamber.init();
        ScavengerChamber.init();
        GreatOlm.init();
        ShamanChamber.init();
        TektonChamber.init();
        ChambersFarming.init();
        ChambersHerblore.init();
        ThievingChamber.init();
        TightropeChamber.init();

        for(int i = 0; i < 198; i++) {
            int c = 25 + (i * 5);
            final int I = i;
            // kick
            ButtonHandler.register(RAIDS_PARTY, c, 1, ((player, slot1, slot2, action) -> {
                ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
                if(raid != null) {
                    if(raid.fc != null && raid.fc.isOwner(player)) {
                        raid.processKickClick(player, I);
                    } else {
                        player.sendMessage("Only the FC owner can kick players from the raid.");
                    }
                }
            }));
            // lookup
            ButtonHandler.register(RAIDS_PARTY, c, 2, ((player, slot1, slot2, action) -> {
                ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
                if(raid != null) {
                    raid.processLookupClick(player, I);
                }
            }));
        }

        ButtonHandler.register(RAIDS_PARTY, RAID_INFO_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                raid.raidInformation(player);
            }
        }));

        ButtonHandler.register(RAIDS_PARTY, SORT_BY_CB_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                player.sendMessage("Bloop!");
            }
        }));

        ButtonHandler.register(RAIDS_PARTY, SORT_BY_NAME_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                player.sendMessage("Bloop!");
            }
        }));
        ButtonHandler.register(RAIDS_PARTY, SORT_BY_TOTAL_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                player.sendMessage("Bloop!");
            }
        }));
        ButtonHandler.register(RAIDS_PARTY, SWITCH_MODE_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                raid.switchMode(player);
            }
        }));


        ButtonHandler.register(RAIDS_PARTY, START_RAID_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                player.getDialogueManager().startDialogue("COXStartD");
            }
        }));


        ButtonHandler.register(PRIVATE_STORAGE_INTERFACE, PRIVATE_SWITCH_INTERFACE_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                player.closeInterfaces();
                raid.openSharedStorage(player);
            }
        }));

        ButtonHandler.register(SHARED_STORAGE_INTERFACE, SHARED_SWITCH_INTERFACE_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                player.closeInterfaces();
                raid.openPrivateStorage(player, false);
            }
        }));

        ButtonHandler.register(PRIVATE_STORAGE_INTERFACE, PRIVATE_STORAGE_WITHDRAW_ALL_BTN, 1, ((player, slot1, slot2, action) -> {
            //ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            //if(raid != null) {
                withdrawAllPrivateStorage(player);
            //}
        }));

        ButtonHandler.register(PRIVATE_STORAGE_INTERFACE, PRIVATE_STORAGE_DEPOSIT_ALL_BTN, 1, ((player, slot1, slot2, action) -> {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                raid.depositAllPrivateStorage(player);
            }
        }));

        ObjectHandler.register(130066, 1, (player, obj) -> {
            ChambersOfXeric raid = getRaid(player);
            if(raid == null)
                return;
            player.setRunEnergy(100);
            player.sendMessage("You feel replenished.");
        });

        ObjectHandler.register(new int[] {129770, 129779, 129780}, 1, (player, obj) -> {
            ChambersOfXeric raid = getRaid(player);
            if(raid == null)
                return;
            raid.openSharedStorage(player);
        });

        ObjectHandler.register(new int[] {129770, 129779, 129780, 130107}, 2, (player, obj) -> {
            ChambersOfXeric raid = getRaid(player);
            if(raid == null)
                return;
            openPrivateStorage(player, false);
        });

        ObjectHandler.register(130107, 1, (player, obj) -> {
            openPrivateStorage(player, false);
        });

        ObjectHandler.register(129879, 1, (player, obj) -> {
            if(getRaid(player) == null)
                return;
            // "pass"
            if (player.getY() > obj.getY()) {
                player.sendMessage("You cannot pass the barrier from this side.");
                return;
            }
            player.getDialogueManager().startDialogue("EnterOlmD", obj);
        });

        ObjectHandler.register(129776, 1, (player, obj) -> {
            // "read"
            player.sendMessage("<col=ff981f><shad=0><img=6> Join a Friends Chat channel and enter the chambers to begin a raid.");
            player.getInterfaceManager().openGameTab(10);
        });

        ObjectHandler.register(132544, 1, (player, obj) -> {
            // "read"
            player.getDialogueManager().startDialogue("TopCoxD", -1, false);
        });

        ObjectHandler.register(129879, 2, (player, obj) -> {
            // "quick-pass"
            if (player.getY() > obj.getY()) {
                player.sendMessage("You cannot pass the barrier from this side.");
                return;
            }
            ChambersOfXeric.enterOlmRoom(player);
        });

        /*ObjectHandler.register(129777, 1, (player, obj) -> {
            // cox entrance
            ChambersOfXeric.enter(player);
        });*/
    }

    private static void withdrawAllPrivateStorage(Player player) {
        refreshPrivateInterface(player);
        player.getInventory().refresh();
        for(int i = 0; i < player.getPrivateItems().getItems().length; i++) {
            Item item = player.getPrivateItems().get(i);
            if (item != null) {
                if (player.getInventory().getFreeSlots() >= player.getInventory().requiredSlots(item)) {
                    player.getInventory().add(item);
                    player.getPrivateItems().set(i, null);
                } else {
                    player.sendMessage("You do not have enough space in your inventory.");
                    player.getPrivateItems().shift();
                    refreshPrivateInterface(player);
                    return;
                }
            }
        }
        refreshPrivateInterface(player);
    }

    private void depositAllPrivateStorage(Player player) {
        int amt = 0;
        for(Item item : player.getPrivateItems().getItems()) {
            if(item != null)
                amt ++;
        }

        if(amt >= maximumPrivateCapacity) {
            player.sendMessage("Your private storage is full, remove some items before trying to add more.");
            return;
        }

        for(int i = 0; i < player.getInventory().getItems().getItems().length; i++) {
            Item item = player.getInventory().getItems().get(i);
            if (item != null) {
                privateDeposit(player, item.getId(), item.getAmount(), false);
            }
        }
        refreshPrivateInterface(player);
        player.getInventory().refresh();
    }
    public static FriendsChat checkFC(Player player) {
        FriendsChat fc = player.getCurrentFriendsChat();
        if (fc == null) {
            if (getRaid(player) != null)
                getRaid(player).remove(player, LEAVE);
        }
        return fc;
    }

    public void addPoints(Player player, int add) {
        addPoints(player.getUsername(), add);
    }

    public void addPoints(String name, int add) {
        if(raidMode == RaidMode.NORMAL) {
            add = (int) ((double) add * 0.75);
        }
        name = name.toLowerCase();
        pointMap.put(name, pointMap.getOrDefault(name, 0) + add);
        partyPoints += add;
    }

    public List<Player> getTeam() {
        return team;
    }

    public List<Player> getTargets(NPC npc) {
        int mapID = npc.getRegionId();
        List<Player> targets = new LinkedList<Player>();
        for (Player player : team) {
            if (!player.hasFinished() && !player.isDead() && !player.isLocked() && player.getRegionId() == mapID)
                targets.add(player);
        }
        return targets;
    }

    public void setHPBar(COXBoss boss) {
    }


    public CrabsChamber getCrabsChamber() {
        return crabsChamber;
    }

    public IceDemonChamber getIceDemonChamber() {
        return iceDemonChamber;
    }

    public VespulaChamber getVespulaChamber() {
        return vespulaChamber;
    }

    public GreatOlmChamber getGreatOlmChamber() {
        return greatOlmChamber;
    }

    public TektonChamber getTektonChamber() {
        return tektonChamber;
    }

    public VanguardChamber getVanguardChamber() {
        return vanguardChamber;
    }

    public ShamanChamber getShamanChamber() {
        return shamanChamber;
    }

    public VasaChamber getVasaChamber() {
        return vasaChamber;
    }

    public MuttadileChamber getMuttadileChamber() {
        return muttadileChamber;
    }

    public MysticsChamber getMysticsChamber() {
        return mysticsChamber;
    }

    public boolean isOsrsRaid() {
        return raidMode == RaidMode.HARD;
    }

    private ArrayList<FloorItem> trackedFloorItems = new ArrayList<>();

    public void trackFloorItem(FloorItem addCoxFloorItem) {
        trackedFloorItems.add(addCoxFloorItem);
    }

    public boolean isOsrsMode() {
        return raidMode == RaidMode.HARD;
    }

    public void killedScav() {
        scavsKilled++;
    }

    private boolean finished = false;
    public boolean hasFinished() {
        return finished;
    }

    public String getCheckpoint() {
        return checkpoint.name();
    }

    public void stockSharedStorage(Player player) {
        buildStorageUnit(player, HouseConstants.HObject.LARGE_STORAGE_UNIT.getId());
        sharedItems.clear();
        getTeam().stream().forEach(p -> {
            p.sendMessage(Colour.RAID_PURPLE.wrap("* Large storage chest was provided & stocked with supplies *"));
        });
        int n = getTeamSize() * 1000;
        int i = 0;
        sharedItems.getItems()[i++] = new Item(50984, n); // xeric's aids
        sharedItems.getItems()[i++] = new Item(50996,  n); // overloads
        sharedItems.getItems()[i++] = new Item(50960, n); // revite
        sharedItems.getItems()[i++] = new Item(50964, n); // enhance
        sharedItems.getItems()[i++] = new Item(50799, n); // kindling
    }

    class PartyInfo {
        String name;

        public PartyInfo(String name, int combat, int total) {
            this.name = name;
            this.combat = combat;
            this.total = total;
        }

        int combat, total;

        @Override
        public String toString() {
            return "PartyInfo{" +
                    "name='" + name + '\'' +
                    ", combat=" + combat +
                    ", total=" + total +
                    '}';
        }
    }

    private enum RaidMode {
        HARD(Colour.RAID_PURPLE), NORMAL(Colour.GREEN);

        RaidMode(Colour format) {
            this.format = format;
        }

        public String format() {
            return "<shad=0>" + format.wrap(name());
        }

        private Colour format;
    }

    private ArrayList<PartyInfo> partyInfo = new ArrayList<>();
    private RaidMode raidMode = RaidMode.NORMAL;

    private static int RAIDS_PARTY = 3078;

    private enum Checkpoints {
        WAITING(110, 35, 3, "Waiting for your leader to<br>begin the raid..."),
        UPPER_LEVEL(110, 35, 3, "Your team is currently exploring<br>the upper level."),
        MID_LEVEL(144, 49, 3,"Your team is currently exploring<br>the mid level."),
        LOWER_LEVEL(143, 80, 2, "Your team is currently exploring<br>the lower level."),
        GET_OUT(145, 47, 1, "Your team must get out..");

        int x, y, z;
        String status;

        Checkpoints(int x, int y, int z, String status) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    private Checkpoints checkpoint = Checkpoints.WAITING;

    public boolean checkpoint(Player player) {
        Checkpoints newPoint = Checkpoints.WAITING;

        if(player.getPlane() == 3) {
            newPoint = Checkpoints.MID_LEVEL;
        } else if(player.getPlane() == 2) {
            newPoint = Checkpoints.LOWER_LEVEL;
        } else if(player.getPlane() == 1) {
            newPoint = Checkpoints.GET_OUT;
        }

        if(newPoint.ordinal() <= checkpoint.ordinal())
            return false;

        checkpoint = newPoint;

        checkpointTile = getTile(checkpoint.x, checkpoint.y, checkpoint.z);


        //player.sendMessage("Your team has passed a checkpoint, you feel an evil presence growing closer.");

        updateRaidPartyInterface();
        return true;
    }

    public void updateRaidPartyInterface() {
        partyInfo.clear();

        //String col;
        for(String s : getPointMap().keySet()) {
            Player p = World.getPlayer(s);
            if(p == null) {
                partyInfo.add(new PartyInfo("<col=808080>" + s, 0, 0));
            } else {
                partyInfo.add(new PartyInfo(p.getDisplayName(), p.getSkills().getCombatLevel(), p.getSkills().getTotalLevel()));
            }
        }

        int c = 25;
        PartyInfo info;

        for(int i = 0; i < 198; i++) {
            if(i < partyInfo.size()) {
                info = partyInfo.get(i);
            } else {
                info = null;
            }
            for(Player player : getTeam()) {
                player.getPackets().sendHideIComponent(RAIDS_PARTY, c + (i * 5), info == null);
                player.getPackets().sendIComponentText(RAIDS_PARTY, c + (i * 5) + 1, info == null ? "" : info.name);
                player.getPackets().sendIComponentText(RAIDS_PARTY, c + (i * 5) + 2, info == null ? "" : "" + info.combat);
                player.getPackets().sendIComponentText(RAIDS_PARTY, c + (i * 5) + 3, info == null ? "" : "" + info.total);
            }
        }

        for(Player player : getTeam()) {
            player.getPackets().sendIComponentText(RAIDS_PARTY, 15, "Party size: <col=ffffff>" + getTeam().size());
            player.getPackets().sendIComponentText(RAIDS_PARTY, 16, "Raid mode: " + raidMode.format());

            if(!fc.isOwner(player) || hasStarted()) {
                player.getPackets().sendHideIComponent(RAIDS_PARTY, 17, true);
                player.getPackets().sendHideIComponent(RAIDS_PARTY, 21, true);
                player.getPackets().sendIComponentText(RAIDS_PARTY, 24, raidMode.format());

                player.getPackets().sendHideIComponent(RAIDS_PARTY, 1023, false);
                player.getPackets().sendIComponentText(RAIDS_PARTY, 1023,
                        checkpoint.getStatus());
            } else {
                player.getPackets().sendHideIComponent(RAIDS_PARTY, 17, false);
                player.getPackets().sendHideIComponent(RAIDS_PARTY, 21, false);
                player.getPackets().sendHideIComponent(RAIDS_PARTY, 1023, true);
                player.getPackets().sendIComponentText(RAIDS_PARTY, 24, "Hard");
            }
        }
    }

    public void add(Player player, boolean login, WorldTile tile) {
        player.lock(2);
        WorldTasksManager.schedule(()-> player.setForceMultiArea(true));
        //player.setLargeSceneView(true);
        if(!login) {
            player.getPrayer().setPrayerBook(false);
            player.reset();
        }
        if(login && player.getLastCoxTile() != null) {
            tile = player.getLastCoxTile();
            player.setLastCoxTile(null);
        }
        player.getControlerManager().startControler("ChamberOfXericController", tile == null ? getTile(110, 35, 3) : tile);

        if(!team.contains(player)) {
            team.add(player);
        }

        playMusic(player, 3);
        if(!getPointMap().containsKey(player.getUsername().toLowerCase()))
            getPointMap().put(player.getUsername().toLowerCase(), 0);
        updateRaidPartyInterface();
    }
    public void add(Player player, boolean login) {
        if(!login && player.getChambersCompletions() + player.getOsrsChambersCompletions() < 1) {
            player.sendMessage("<u=ffffff>Chambers Warning:");
            player.sendMessage("There is a known FPS drop halfway through raids - while we are working on a solution, you may " + Colour.DARK_GREEN.wrap("SAFELY RE-LOG") + " to fix the issue at any point in the raid.");
        }

        add(player, login, checkpointTile);
    }

    public void logoutPlayer(Player player) {
        player.setCoxLogout(true);
    }

    public static ChambersOfXeric findRaid(Player player) {
        for(ChambersOfXeric raid : raidingParties.values()) {
            if(raid != null && raid.getPointMap() != null) {
                if(raid.getPointMap().containsKey(player.getUsername().toLowerCase()))
                    return raid;
            }
        }
        return null;
    }

    public static boolean loginPlayer(Player player) {
        player.sendMessage("<col=ff00ff><shad=0>Attempting to re-join raiding party.. ");
        player.setCoxLogout(false);
        ChambersOfXeric findRaid;

        findRaid = ChambersOfXeric.findInstance(
                player.getCurrentFriendsChat() != null ? player.getCurrentFriendsChat().getChannel()
                : player.getLastFriendsChat() != null ? player.getLastFriendsChat().toLowerCase() : null);

        if(findRaid == null) {
            // sometimes last friendchat can be null on login (issue on beta)
            // second attempt, shouldn't happen
            findRaid = findRaid(player);
        }

        if (findRaid == null) {
            player.sendMessage("<col=ff0000>Your raid party has disbanded. ");
            player.reset();
            player.setNextWorldTile(OUTSIDE.clone());
        } else {
            ChambersOfXeric raid = findRaid;
            if(raid.getPointMap().keySet().stream().noneMatch(name->name.equalsIgnoreCase(player.getUsername()))) {
                return false; // not in this raid
            }
            WorldTasksManager.schedule(() -> {
                player.lock(2);
                raid.add(player, true);
            }, 1);
        }
        return true;
    }
    //0 - leave, 1 - teleport, 2 - logout 3 - kick
    public void remove(Player player, int type) {
        team.remove(player);
        player.setForceMultiArea(false);
        if(type == LEAVE) {
            if(!hasStarted())
                getPointMap().remove(player.getUsername().toLowerCase());
            dropCoxItems(player);
            player.reset();
            /*if(!player.getPrivateItems().isEmpty()) {
                player.sendMessage("<col=ff0000>You left some items in your private storage, you will need to collect these before starting another raid.");
            }*/
        } else if(type == LOGOUT) {
            logoutPlayer(player);
        } else if(!hasStarted()) {
            pointMap.remove(player.getUsername().toLowerCase());
        }

        WorldTasksManager.schedule(() -> updateRaidPartyInterface());

        player.getInterfaceManager().removeOverlay(false);
        player.getControlerManager().removeControlerWithoutCheck();

        if (type == LOGOUT)
            player.setLocation(OUTSIDE);
        else {
            if (type == LEAVE || type == 3) {
                player.useStairs(-1, OUTSIDE, 0, 1);
                WorldTasksManager.schedule(()
                        -> player.getDialogueManager().startDialogue("SimpleMessage", "You are no longer in a raiding party."));
            }
            player.getInterfaceManager().sendQuestTab();
        }

        if (team.isEmpty() && type != LOGOUT) {
            // left with door / left cc
            destroy(true);
        }
    }

    private void destroy(boolean forceDestroy) {
        if(endTime == -1 && !forceDestroy) {
            //incomplete raid
            WorldTasksManager.schedule(new WorldTask() {
                int tick = 50;
                @Override
                public void run() {
                    if(getTeam().size() != 0)
                        stop();
                    if(tick-- < 1) {
                        destroy(false);
                        stop();
                    }
                }
            });
            // leave some time for a solo player to relog
            return;
        }

        pointMap.clear();
        team.clear();
        finished = true;
        endTime = System.currentTimeMillis();

        for(FloorItem i : trackedFloorItems) {
            if(i != null)
                World.removeGroundItem(i);
        }

        sharedItems.clear();

        for(WorldTile tile : storageChests) {
            WorldObject storage = World.getObjectWithType(getTile(tile.getX(), tile.getY(), tile.getPlane()), 10);
            if(storage != null) {
                storage.remove();
            }
        }

        chambersNPCs.forEach(NPC::finish);
        raidingParties.remove(fc.getChannel());
        destroy(() -> {
            for(Player player : getTeam()) {
                if(player.getControlerManager().getControler() != null)
                    remove(player, LEAVE);
            }
        });
    }

    @Override
    protected void buildMap() {
        //surronding by 4x4 black
        for (int plane = 0; plane < 4; plane++)
            for (int x = 0; x < 24; x++)
                for (int y = 0; y < 16; y++)
                    MapBuilder.copyChunk(744, 1176, 0, getInstancePos()[0] + x, getInstancePos()[1] + y, plane, 0);
        //upper
        setRoom(new WorldTile(2064, 972, 3), 2, 0, 3, 0); //lobby
        setRoom(new WorldTile(2032, 972, 3), 1, 0, 3, 0); //tekton (boss 1)
        setRoom(new WorldTile(2000, 972, 3), 0, 0, 3, 0); //Jewelled Crabs
        setRoom(new WorldTile(2000, 1000, 3), 0, 1, 3, 0); //Scanvager beast 1
        setRoom(new WorldTile(2032, 1000, 3), 1, 1, 3, 0); //ice demon (boss2)
        setRoom(new WorldTile(2064, 1000, 3), 2, 1, 3, 0); //lizard man
        setRoom(new WorldTile(2096, 1000, 3), 3, 1, 3, 0); //resource room 1
        setRoom(new WorldTile(2096, 972, 3), 3, 0, 3, 0); //bank room + hole 1
        //mid
        setRoom(new WorldTile(2096, 972, 2), 3, 0, 2, 0); //stairs 1
        setRoom(new WorldTile(2064, 972, 2), 2, 0, 2, 0); //vanguard (boss 3)
        setRoom(new WorldTile(2032, 972, 2), 1, 0, 2, 0); //thieving room
        setRoom(new WorldTile(2000, 972, 2), 0, 0, 2, 0); //Scavanger beasts 2
        setRoom(new WorldTile(2000, 1000, 2), 0, 1, 2, 0); //vespula (boss 4)
        setRoom(new WorldTile(2032, 1000, 2), 1, 1, 2, 0); //resource room 2
        setRoom(new WorldTile(2064, 1000, 2), 2, 1, 2, 0); //tightrope room
        setRoom(new WorldTile(2096, 1000, 2), 3, 1, 2, 0); //bank room + hole 2
        //low
        setRoom(new WorldTile(2096, 1000, 1), 3, 1, 1, 0); //stairs 2
        setRoom(new WorldTile(2064, 1000, 1), 2, 1, 1, 0); //guardians
        setRoom(new WorldTile(2032, 1000, 1), 1, 1, 1, 0);  //vasa nistrio (boss 5)
        setRoom(new WorldTile(2000, 1000, 1), 0, 1, 1, 0); //Scavanger beasts 3
        setRoom(new WorldTile(2000, 972, 1), 0, 0, 1, 0); //skeletal mytstics
        setRoom(new WorldTile(2032, 972, 1), 1, 0, 1, 0);  //muttadiles
        setRoom(new WorldTile(2064, 972, 1), 2, 0, 1, 0); //resource room 3
        setRoom(new WorldTile(2096, 972, 1), 3, 0, 1, 0); //bank room + hole 3
        //olm
        MapBuilder.copyMap(744, 1176, getInstancePos()[0] + 12, getInstancePos()[1], 8, new int[]{0}, new int[]{0}); //keeping map under door
    }

    private void setRoom(WorldTile tile, int x, int y, int level, int rotation) {
        MapBuilder.copyMap((tile.getX() / 32) * 4, (tile.getY() / 32) * 4, getInstancePos()[0] + (x * 4) + 4, getInstancePos()[1] + (y * 4) + 4, 4, 4, rotation, new int[]{tile.getPlane()}, new int[]{level});
    }

    public void switchMode(Player player) {
        if (true == true) {
            player.getPackets().sendGameMessage("Hard mode is currently disabled!");
            return;
        }
        FriendsChat fc = checkFC(player);
        if (fc != null) {
            if (player.getCurrentFriendsChat().isOwner(player)) {
                String s, s2;

                if(raidMode == RaidMode.HARD) {
                    raidMode = RaidMode.NORMAL;
                    s = "<u=0>Warning</u><br>" + player.getDisplayName() + " has switched the raid mode to: " + raidMode.format() + "!" +
                            "<br>All items are permitted!";
                } else {
                    raidMode = RaidMode.HARD;
                    s = "<u=0>Warning</u><br>" + player.getDisplayName() + " has switched the raid mode to: " + raidMode.format() + "!" +
                            "<br>Any items not found in OSRS must be banked before the raid can start.";
                }
                player.getPackets().sendIComponentText(RAIDS_PARTY, 24, (raidMode == RaidMode.HARD ? RaidMode.NORMAL.name() : RaidMode.HARD.name()));

                getTeam().stream().forEach(p -> {
                    p.getDialogueManager().startDialogue("SimpleNPCMessage", 2253, s);
                    p.getPackets().sendIComponentText(RAIDS_PARTY, 16, "Raid mode: " + raidMode.format());
                });
            } else {
                player.sendMessage("Only the FC owner may change the raid mode.");
            }
        }
    }

    public void raidInformation(Player player) {
        String info;

        if(raidMode == RaidMode.NORMAL) {
            info = "<u=0>Matrix Mode" +
                    "<br>All items & equipment allowed";// +
                    //"<br>points are slower than OSRS by 25%" +
                  //  "<br>No chance of Meta dust or 3 unique rewards";
        } else {
            info = "<u=0>OSRS Mode" +
                    "<br>Only OSRS items & equipment allowed" +
                    "<br>Points are equal to OSRS" +
                    "<br>Chance of Meta dust and 3 unique rewards added!";
        }


        player.getDialogueManager().startDialogue("SimpleMessage", info);
    }

    public static boolean checkBlacklist(int id) {
        return bannedOsrsRaidItems.contains(id);
    }

    private Stopwatch startChecksCooldown = null;

    public static String getBannedItemsString(ArrayList<Item> items) {
        StringBuilder sb = new StringBuilder();
        items.forEach(item -> sb.append(item.getName() + ", "));
        if(sb.toString().length() > 0)
            sb.replace(sb.lastIndexOf(","), sb.length(), "");
        return sb.toString();
    }

    public void start(Player owner) {
        /**
         * Has to check all items / equipment aren't on blacklist
         */
        if(startChecksCooldown == null || startChecksCooldown.finished()) {
            (startChecksCooldown = new Stopwatch()).delayMS(3000);
        } else {
            owner.sendMessage("You must wait before trying to start the raid again.");
            return;
        }
        FriendsChat fc = checkFC(owner);
        if (fc != null) {
            if (fc.isOwner(owner)) {
                String s;
                ArrayList<Item> bannedItems;
                if(raidMode == RaidMode.HARD) {
                    boolean exit = false;
                    for(Player p : getTeam()) {
                        bannedItems = new ArrayList<>();
                        for(Item item : p.getEquipment().getItems().getItems()) {
                            if(item == null) continue;
                            if(checkBlacklist(item.getId()))
                                bannedItems.add(item);
                        }
                        for(Item item : p.getInventory().getItems().getItems()) {
                            if(item == null) continue;
                            if(checkBlacklist(item.getId()))
                                bannedItems.add(item);
                        }
                        s = getBannedItemsString(bannedItems);
                        if(bannedItems.size() > 0) {
                            p.sendMessage("You have banned items: " + s);
                            p.sendMessage("You must bank these items before starting an OSRS raid.");
                            if(owner != p)
                                owner.sendMessage(p.getDisplayName() + " has banned items: " + s);
                            exit = true;
                        }
                    }

                    if(exit) {
                        return;
                    }
                }

                int maxCb = 3;

                for(Player p : getTeam())
                    if(p.getSkills().getCombatLevel() > maxCb)
                        maxCb = p.getSkills().getCombatLevel();

                if (maxCb < 90)
                    combatPointsFactor = 0.6;
                else if (maxCb < 115)
                    combatPointsFactor = 0.8;

                checkpoint = Checkpoints.UPPER_LEVEL;

                size = (int) team.stream().distinct().count();
                startTime = Utils.currentTimeMillis();
                yell("<col=ff66cc>The raid has begun!");

                updateRaidPartyInterface();

                for(COXBoss npc : chambersNPCs) {
                    if(!npc.isInit()) {
                        npc.scale(size);
                    }
                }
            } else {
                owner.sendMessage("Only the FC owner may start the raid.");
            }
        }
    }

    public void yell(String message) {
        for (Player player : team)
            player.getPackets().sendGameMessage(message);
    }

    public boolean hasStarted() {
        return size > 0;
    }

    public void playMusic(Player player, int level) {
        String music = level == 3 ? "Upper Depths" : level == 0 ? "Fire in the Deep" : "Lower Depths";
        player.getMusicsManager().playOSRSMusic(music);
    }

    public int getTeamSize() {
        return size;
    }

    /**
     * Finds the map tile base for the SW corner of the chamber
     * ex. 2, 2, 3
     */
    public WorldTile getMapTileBaseWorldTile(int x, int y, int z) {
        return new WorldTile(getInstancePos()[0] * 8 + (32 * x), getInstancePos()[1] * 8 + (32 * y), z);
    }

    public WorldTile getChamberTile(WorldTile tile) {
        WorldTile instancePos = getInstanceTile(tile);
        return new WorldTile(instancePos.getX() / 32, instancePos.getY() / 32, tile.getPlane());
    }

    public void moveToChamber(Player player, WorldTile chamberTile) {
        player.sendMessage("Moving to chamber: <col=ffff00>" + chamberTile);
        player.setNextWorldTile(
                getMapTileBaseWorldTile(chamberTile.getX(), chamberTile.getY(), chamberTile.getPlane())
                        .transform(16, 16, 0));
    }

    public ArrayList<Chamber> getChambers() {
        return chambers;
    }

    public Chamber getCurrentChamber(Player player) {
        if(player.getPlane() == 0 && getInstanceTile(player).getY() > 33)
            return getGreatOlmChamber();
        WorldTile tile = getChamberTile(player);
        for (Chamber chamber : chambers)
            if (chamber.getBaseTile() != null && chamber.getBaseTile().matches(tile))
                return chamber;
        return unknownChamber;
    }

    public int getTotalWCLevel() {
        int sum = 0;
        for(Player p : getTeam())
            if(p != null)
                sum += p.getSkills().getLevelForXp(Skills.WOODCUTTING);
        return sum;
    }

    public static void addDamagePoints(Player player, COXBoss target, int damage) {
        if(damage == 0) {
            return;
        }

        double reward = damage;


        ChambersOfXeric raid = getRaid(player);
        if(raid == null) {
            return;
        }

        if(/*target.isRewardNoPoints() || */ScavengerChamber.isScav(target.getId()) && raid.scavsKilled >= SCAV_CAP) {
            return;
        }

        reward *= 0.1;

        if (target.getId() == 27548 || target.getId() == 27549) { // scavengers
            reward *= 0.25;
        }

        reward *= raid.combatPointsFactor;
        if(raid.combatPointsFactor != 1.0) {
            if(Settings.DEBUG) {
                player.sendMessage(" -"+((1-(raid.combatPointsFactor) * 100))+"% ("+-(reward*raid.combatPointsFactor)+") low combat raid modifier");
            }
        }

        if(!raid.isOsrsRaid()) {

            reward *= 0.75;
        }

        reward *= 5;

        raid.addPoints(player, (int) reward);
    }

    public void writeRaidOverlayInfo(Player player) {
        player.getPackets().sendIComponentText(3076, 4, player.getDisplayName());
        player.getPackets().sendIComponentText(3076, 6, "" + Utils.getFormattedNumber(getPartyPoints()));
        player.getPackets().sendIComponentText(3076, 7, "" + Utils.getFormattedNumber(getPoints(player)));
        player.getPackets().sendIComponentText(3076, 8, "" + formatRaidTime());
        /*return "<br><br><br><col=ff9933>Total: <col=ffffff>"+Utils.getFormattedNumber(partyPoints)
                + "<br><col=ff9933>"+player.getName()+": <col=ffffff>"+Utils.getFormattedNumber(getPoints(player))
                + "<br><col=ff9933>Time: <col=ffffff>" + formatRaidTime()
                + "<br>" + getCurrentChamber(player).getDebug();*/
    }

    public Integer getPoints(Player player) {
        return pointMap.getOrDefault(player.getUsername().toLowerCase(), 0);
    }

    private static final WorldTile[] storageChests = {
            new WorldTile(76, 81, 3),
            new WorldTile(144, 81, 3),
            new WorldTile(136, 47, 3),
            new WorldTile(76, 52, 2),
            new WorldTile(87, 79, 2),
            new WorldTile(144, 72, 2),
            new WorldTile(140, 51, 1),
            new WorldTile(112, 42, 1),
    };

    public void buildStorageUnit(Player player, int id) {
        if(id == HouseConstants.HObject.LARGE_STORAGE_UNIT.getId()) {
            maximumSharedCapacity = 1000; // * 2 until we solve the issue with items randomly deleting
            maximumPrivateCapacity = 90;
        } else if(id == HouseConstants.HObject.MEDIUM_STORAGE_UNIT.getId()) {
            maximumSharedCapacity = 500;
            maximumPrivateCapacity = 60;
        } else {
            maximumSharedCapacity = 250;
            maximumPrivateCapacity = 30;
        }
        player.getTemporaryAttributtes().remove("COX_STORAGE_UNIT");
        for(WorldTile tile : storageChests) {
            WorldObject storage = World.getObjectWithType(getTile(tile.getX(), tile.getY(), tile.getPlane()), 10);
            if(storage != null) {
                storage.updateId(id);
            }
        }
    }

    private ItemsContainer<Item> sharedItems = new ItemsContainer<Item>(215, true);

    public static final int PRIVATE_STORAGE_INTERFACE = 3065;
    public static final int SHARED_STORAGE_INTERFACE = 3064;

    public static final int PRIV_INV_KEY = ItemSetsKeyGenerator.generateKey(),
            PUB_INV_KEY = ItemSetsKeyGenerator.generateKey(),
            PRIVATE_STORAGE_KEY = ItemSetsKeyGenerator.generateKey(),
            SHARED_STORAGE_KEY = ItemSetsKeyGenerator.generateKey();

    private static final int SHARED_STORED_ITEMS_TEXT = 18, SHARED_CAPACITY_TEXT = 20, SHARED_ITEM_CONTAINER = 22, SHARED_SWITCH_INTERFACE_BTN = 24;

    private static final int PRIVATE_STORAGE_ITEM_CONTAINER = 45, PRIVATE_STORED_ITEMS_TEXT = 18, PRIVATE_CAPACITY_TEXT = 20, PRIVATE_STORAGE_WITHDRAW_ALL_BTN = 30,
            PRIVATE_STORAGE_DEPOSIT_ALL_BTN = 37, PRIVATE_SWITCH_INTERFACE_BTN = 25;


    // list of players viewing chest
    public CopyOnWriteArrayList<Player> sharedStorageUpdateSubscribers = new CopyOnWriteArrayList<Player>();

    private int maximumSharedCapacity = 250;
    private int maximumPrivateCapacity = 30;

    public void openSharedStorage(Player player) {
        sendSharedOptions(player);
        sharedStorageUpdateSubscribers.add(player);
        if(!player.isHCIronman() && !player.isUltimateIronman() && !player.isIronman()) {
            player.getPackets().sendIComponentText(SHARED_STORAGE_INTERFACE, 21,
                    "<col=ffffff>Items donated here may be retrieved by <col=ff0000>other</col> party-members," +
                            "<br>or <col=ff0000>may be lost</col> if the party dissolves.");
        }
        player.getInterfaceManager().sendInterface(SHARED_STORAGE_INTERFACE);
        player.getInterfaceManager().sendInventoryInterface(PartyRoom.CHEST_INV_INTERFACE);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                sharedStorageUpdateSubscribers.remove(player);
            }
        });
    }

    public static void openPrivateStorage(Player player, boolean outsideCOX) {
        if(!outsideCOX && getRaid(player) == null)
            return;
        player.getInterfaceManager().sendInterface(PRIVATE_STORAGE_INTERFACE);
        player.getInterfaceManager().sendInventoryInterface(PartyRoom.CHEST_INV_INTERFACE);
        sendPrivatedOptions(player);
    }

    private void sendSharedOptions(final Player player) {
        player.getPackets().sendUnlockIComponentOptionSlots(SHARED_STORAGE_INTERFACE, SHARED_ITEM_CONTAINER, 0, 720, 0, 1, 2, 3, 4);
        player.getPackets().sendInterSetItemsOptionsScript(SHARED_STORAGE_INTERFACE, SHARED_ITEM_CONTAINER, SHARED_STORAGE_KEY, 10, 64, "Withdraw", "Withdraw-5", "Withdraw-10", "Withdraw-All", "Withdraw-X");
        player.getPackets().sendInterSetItemsOptionsScript(PartyRoom.CHEST_INV_INTERFACE, 0, PUB_INV_KEY, 4, 7, "Deposit", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X");
        player.getPackets().sendUnlockIComponentOptionSlots(PartyRoom.CHEST_INV_INTERFACE, 0, 0, 27, 0, 1, 2, 3, 4);
        refreshSharedInterface(player);
    }

    private static void sendPrivatedOptions(final Player player) {
        player.getPackets().sendUnlockIComponentOptionSlots(PRIVATE_STORAGE_INTERFACE, PRIVATE_STORAGE_ITEM_CONTAINER, 0, 156, 0, 1, 2, 3, 4);
        player.getPackets().sendUnlockIComponentOptionSlots(PartyRoom.CHEST_INV_INTERFACE, 0, 0, 27, 0, 1, 2, 3, 4);
        player.getPackets().sendInterSetItemsOptionsScript(PRIVATE_STORAGE_INTERFACE, PRIVATE_STORAGE_ITEM_CONTAINER, PRIVATE_STORAGE_KEY, 8, 20, "Withdraw", "Withdraw-5", "Withdraw-10", "Withdraw-All", "Withdraw-X");
        player.getPackets().sendInterSetItemsOptionsScript(PartyRoom.CHEST_INV_INTERFACE, 0, PRIV_INV_KEY, 4, 7, "Deposit", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X");
        refreshPrivateInterface(player);
    }

    public void refreshSharedInterface(Player player) {
        player.getPackets().sendItems(SHARED_STORAGE_KEY, sharedItems);
        player.getPackets().sendItems(PUB_INV_KEY, player.getInventory().getItems());
        int amt = 0;
        for(Item item : sharedItems.getItems()) {
            if(item != null)
                amt ++;
        }
        player.getPackets().sendIComponentText(SHARED_STORAGE_INTERFACE, SHARED_CAPACITY_TEXT, "" + maximumSharedCapacity);
        player.getPackets().sendIComponentText(SHARED_STORAGE_INTERFACE, SHARED_STORED_ITEMS_TEXT, "" + amt);
    }

    public static void refreshPrivateInterface(Player player) {
        player.getPackets().sendItems(PRIVATE_STORAGE_KEY, player.getPrivateItems());
        player.getPackets().sendItems(PRIV_INV_KEY, player.getInventory().getItems());
        int amt = 0;
        for(Item item : player.getPrivateItems().getItems()) {
            if(item != null)
                amt ++;
        }
        if(ChambersOfXeric.getRaid(player) != null) {
            player.getPackets().sendIComponentText(PRIVATE_STORAGE_INTERFACE, PRIVATE_CAPACITY_TEXT, "" + ChambersOfXeric.getRaid(player).maximumPrivateCapacity);
            player.getPackets().sendIComponentText(PRIVATE_STORAGE_INTERFACE, PRIVATE_STORED_ITEMS_TEXT, "" + amt);
        }
    }

    public ItemsContainer<Item> getSharedItems() {
        return sharedItems;
    }

    public int getPartyPoints() {
        return partyPoints;
    }

    public void raidCompleted() {
        endTime = Utils.currentTimeMillis();
        finished = true;
        for(Player player : getTeam()) {
            player.reset();
        }
    }

    public void addNPC(COXBoss coxBoss) {
        chambersNPCs.add(coxBoss);
    }

    public static ArrayList<Integer> getCoxItems() {
        return coxItems;
    }

    private static ArrayList<Integer> coxItems = new ArrayList<>();

    public void handleDeath(Player player, boolean removePoints) {
        dropCoxItems(player);
        if(removePoints) {
            int pointsLost = (int) (pointMap.getOrDefault(player.getUsername().toLowerCase(), 0) * 0.4);
            if (pointsLost > 0)
                addPoints(player.getUsername(), -pointsLost);
        }
    }

    private void dropCoxItems(Player player) {
        for (int i = 0; i < 28; i ++) {
            Item item = player.getInventory().getItems().get(i);
            if (item != null && coxItems.contains(item.getId())) {
                FloorItem fi = World.addGroundItem(new Item(item.getId(), item.getAmount()), player.clone(), player, false, 0);
                player.getInventory().getItems().set(i, null);
                trackFloorItem(fi);
            }
            player.getInventory().refresh();
        }
    }

    public Map<String, Integer> getPointMap() {
        return pointMap;
    }

    public String getFc() {
        if(fc == null) return "Unknown Raiders";
        return fc.getChannel();
    }
}
