package com.rs.game.player.controllers.partyroom;

import com.rs.executor.GameExecutorManager;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.ItemSetsKeyGenerator;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.rs.game.TemporaryAtributtes.Key.PARTY_ROOM_CHEST_DEPOSIT;

public class PartyRoom {

    private static final int MIN_ITEMS_TO_START_DROPPING = 10;
    public static double CHANCE_OF_EMPTY_BALLOON = 3; // 1 in 4

    public static final int PARTY_CHEST_CLOSED = 26193, PARTY_CHEST_OPEN = 2418, PARTY_LEVER = 26194;

    public static final int PARTY_CHEST_INTERFACE = 647;
    public static final int CHEST_INV_INTERFACE = 648;

    public static final int BALLOON_PARTY_GP = 1_000_000, KNIGHTLY_DANCE_GP = 500_000;

    private static ItemsContainer<Item> itemsQueued = new ItemsContainer<Item>(215, false);
    private static ItemsContainer<Item> itemsDropping = new ItemsContainer<Item>(215, false);

    public static final int CHEST_INV_KEY = ItemSetsKeyGenerator.generateKey(),
            QUEUE_KEY = ItemSetsKeyGenerator.generateKey(),
            DROPPING_KEY = ItemSetsKeyGenerator.generateKey(),
            TO_DEPO_KEY = ItemSetsKeyGenerator.generateKey();

    public static int TO_DROP_CONTAINER = 23,
            DROPPING_CONTAINER = 24,
            TO_DEPO_CONTAINER = 25;


    // list of players viewing chest
    public static CopyOnWriteArrayList<Player> chestUpdateSubscribers = new CopyOnWriteArrayList<Player>();

    // active balloons
    public static Map<WorldObject, PartyBalloon> balloons = Collections.synchronizedMap(new LinkedHashMap<WorldObject, PartyBalloon>());

    public static final Object lock = new Object();
    private static BalloonParty gameTask = null;

    /*
     * add prize into balloon object, remove from prize list
     * add balloon at valid tile and add to map
     */
    public static boolean addBalloon() {
        List<Item> prizes = Arrays.stream(itemsDropping.getItems()).filter(Objects::nonNull).collect(Collectors.toList());

        if (prizes.size() == 0) {
            // all prizes dropped
            return false;
        }

        int prizeSlot = Utils.random(prizes.size() - 1);
        Item prizeStack = prizes.get(prizeSlot);

        // give 15-60% of the stack
        int amount = 1;

        if (prizeStack.getAmount() > 1) {
            // gradually increase reward sizes as items get dropped
            // items stacks > 10m take 20+ waves to deplete without
            double waveMod = (double) gameTask.waves / 80;
            double yield = Math.min(1.0, Utils.random(0.15 + waveMod, 0.60 + waveMod));
            amount = (int) Math.floor((double) prizeStack.getAmount() * yield);
            if (amount == 0)
                amount = 1;
        }

        if(prizeStack.getAmount() > 1000 && prizeStack.getAmount() < 100000
                && GrandExchange.getPrice(prizeStack.getId()) * prizeStack.getAmount() < prizeStack.getAmount() * 2) {
            // if the stack is worth less than 2 coins each
            // and there is more and 1000-100k, drop it all
            amount = prizeStack.getAmount();
        }

        Item prize = new Item(prizeStack.getId(), amount);

        if (Utils.random(0, CHANCE_OF_EMPTY_BALLOON) == 1) {
            // empty balloon
            prize = null;
        } else {
            itemsDropping.remove(prizeSlot, prize);
            itemsDropping.shift();
            updateChestForSubscribers();
        }

        // System.out.println("Stack:  " + prizeStack);
        // System.out.println("Prize:  " + prize);
        // System.out.println();

        balloons = Collections.synchronizedMap(balloons);

        WorldTile tile = getValidTile();

        if (tile == null) {
            Logger.log("PartyRoom.class", "Could not find a valid world tile: balloons=" + balloons.size());
            return true;
        }

        PartyBalloon.PartyBalloonType balloonType = PartyBalloon.PartyBalloonType.getRandom();

        WorldObject object = new WorldObject(balloonType.getBalloonId(), 10, Utils.random(4),
                tile.getX(), tile.getY(), 0);

        balloons.put(object, new PartyBalloon(prize));

        World.spawnObjectTemporary(object, 30 * 1000, true, false, (() -> {
            PartyBalloon balloon = PartyRoom.balloons.get(object);
            if(balloon != null && balloon.prize != null) {
                // spawn items to public if balloon despawns
                World.addGroundItem(balloon.prize, object, null, false, 30);
            }
        }));


        return true;
    }

    //static ArrayList<WorldTile> VALID_TILES = new ArrayList<WorldTile>();

    // few tiles (i.e. doorway) are outside of party room building
    static List<WorldTile> EXCEPTION_TILES = Arrays.asList(new WorldTile(3045, 3371, 0), new WorldTile(3046, 3371, 0));

    static int PARTY_ROOM_SW_X = 3036, PARTY_ROOM_SW_Y = 3371, X_MAX = 18, Y_MAX = 14;

   /* static {
        for(int x = PARTY_ROOM_SW_X; x < PARTY_ROOM_SW_X + X_MAX; x++) {
            for (int y = PARTY_ROOM_SW_Y; y < PARTY_ROOM_SW_Y + Y_MAX; y++) {
                WorldTile tile = new WorldTile(x, y, 0);
                if (World.isTileFree(tile, 1) && !EXCEPTION_TILES.stream().anyMatch(e->e.matches(tile))) {
                    VALID_TILES.add(tile);
                }
            }
        }
    }*/

    static Predicate<WorldTile> occupiedTest =
            testTile -> balloons.keySet().stream().filter(Objects::nonNull)
                    .anyMatch(obj -> obj.getX() == testTile.getX() && obj.getY() == testTile.getY());

    private static WorldTile getValidTile() {
    	 List<WorldTile> validTiles2 = new LinkedList<WorldTile>();
    	 for(int x = PARTY_ROOM_SW_X; x < PARTY_ROOM_SW_X + X_MAX; x++) {
             for (int y = PARTY_ROOM_SW_Y; y < PARTY_ROOM_SW_Y + Y_MAX; y++) {
                 WorldTile tile = new WorldTile(x, y, 0);
                 if (World.isTileFree(tile, 1) && !EXCEPTION_TILES.stream().anyMatch(e->e.matches(tile))) {
                	 validTiles2.add(tile);
                 }
             }
         }
    	
    	
        List<WorldTile> validTiles = validTiles2.stream().filter(t -> !occupiedTest.test(t)).collect(Collectors.toList());
        if(validTiles.size() == 0) {
            return null;
        }

        return validTiles.get(Utils.random(validTiles.size()));
    }

    public static void openPartyChest(final Player player) {
        player.getTemporaryAttributtes().put(PARTY_ROOM_CHEST_DEPOSIT, new ItemsContainer<Item>(10, false));
        player.getInterfaceManager().sendInterface(PARTY_CHEST_INTERFACE);
        player.getInterfaceManager().sendInventoryInterface(CHEST_INV_INTERFACE);
        sendOptions(player);
        chestUpdateSubscribers.add(player);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                ItemsContainer<Item> depositing = getDepositContainer(player);

                if (depositing != null) {
					for (Item item : depositing.getItems())
						if(item != null)
							player.getInventory().addItem(item);

					depositing.clear();
				}
                player.getTemporaryAttributtes().remove(PARTY_ROOM_CHEST_DEPOSIT);
                chestUpdateSubscribers.remove(player);
            }
        });
    }

    public static ItemsContainer<Item> getDepositContainer(Player player) {
        Object inv = player.getTemporaryAttributtes().get(PARTY_ROOM_CHEST_DEPOSIT);
        if (inv == null)
            return null;
        else return (ItemsContainer<Item>) inv;
    }

    private static void sendOptions(final Player player) {
		player.getPackets().sendUnlockIComponentOptionSlots(PARTY_CHEST_INTERFACE, TO_DROP_CONTAINER, 0, 214, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(PARTY_CHEST_INTERFACE, DROPPING_CONTAINER, 0, 214, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(PARTY_CHEST_INTERFACE, TO_DEPO_CONTAINER, 0, 9, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(CHEST_INV_INTERFACE, 0, 0, 27, 0, 1, 2, 3, 4);

        player.getPackets().sendInterSetItemsOptionsScript(PARTY_CHEST_INTERFACE, TO_DROP_CONTAINER, QUEUE_KEY, 5, 43, "Examine");
        player.getPackets().sendInterSetItemsOptionsScript(PARTY_CHEST_INTERFACE, TO_DEPO_CONTAINER, TO_DEPO_KEY, 10, 1, "Withdraw");
        player.getPackets().sendInterSetItemsOptionsScript(PARTY_CHEST_INTERFACE, DROPPING_CONTAINER, DROPPING_KEY, 5, 43, "Examine");
		player.getPackets().sendInterSetItemsOptionsScript(CHEST_INV_INTERFACE, 0, CHEST_INV_KEY, 4, 7, "Deposit", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X");
        refreshChestInterface(player);
    }

    public static void refreshChestInterface(Player player) {
        player.getPackets().sendItems(CHEST_INV_KEY, player.getInventory().getItems());
        player.getPackets().sendItems(QUEUE_KEY, itemsQueued);
        player.getPackets().sendItems(DROPPING_KEY, itemsDropping);
        player.getPackets().sendItems(TO_DEPO_KEY, getDepositContainer(player));
    }

    public static long getTotalCoins() {
        long price = 0;
        for (Item item : itemsQueued.getItems()) {
            if (item == null)
                continue;
            price += item.getAmount() * (item.getId() == 995 ? 1 : GrandExchange.getPrice(item.getId()));
        }
        return price;
    }

    public static void purchase(final Player player, boolean balloons) {
        if (balloons) {
            if (player.getInventory().getCoinsAmount() >= BALLOON_PARTY_GP) {
                if(itemsQueued.getSize() >= MIN_ITEMS_TO_START_DROPPING) {
                    startParty(player);
                } else {
                    player.sendMessage("There must be at least " + MIN_ITEMS_TO_START_DROPPING + " in the chest to start Balloon bonanza!");
                }
            } else {
                player.sendMessage("Balloon Bonanza costs "+Utils.getFormattedNumber(BALLOON_PARTY_GP)+" coins.");
            }
        } else {
            if(dancing) {
                player.sendMessage("The Knightly Dance is already on!");
                return;
            }
            if (player.getInventory().getCoinsAmount() >=  KNIGHTLY_DANCE_GP) {
            	player.lock(2);
            	player.anim(2140);
                startDancingKnights();
                player.getInventory().removeItemMoneyPouch(new Item(995, KNIGHTLY_DANCE_GP));
            } else {
                player.sendMessage("Knightly Dance costs "+Utils.getFormattedNumber(KNIGHTLY_DANCE_GP)+" coins.");
            }
        }
    }

    public static boolean dancing = false;

    public static void startDancingKnights() {
        dancing = true;

        final List<NPC> npcs = new ArrayList<NPC>();
        for (int i = 0; i < 6; i++) {
            NPC npc = new NPC(660, new WorldTile(3043 + i, 3378, 0), 0, false);
            npcs.add(npc);
        }

        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            int cycle = 0;
            @Override
            public void run() {
                switch (cycle) {
                    case 3:
                        npcs.get(3).setNextForceTalk(new ForceTalk("We're Knights of the Party Room"));
                        break;
                    case 6:
                        npcs.get(3).setNextForceTalk(new ForceTalk("We dance round and round like a loon"));
                        break;
                    case 8:
                        npcs.get(3).setNextForceTalk(new ForceTalk("Quite often we like to sing"));
                        break;
                    case 11:
                        npcs.get(3).setNextForceTalk(new ForceTalk("Unfortunately we make a din"));
                        break;
                    case 13:
                        npcs.get(3).setNextForceTalk(new ForceTalk("We're Knights of the Party Room"));
                        break;
                    case 16:
                        npcs.get(3).setNextForceTalk(new ForceTalk("Do you like our helmet plumes?"));
                        break;
                    case 18:
                        npcs.get(3).setNextForceTalk(new ForceTalk("Everyone's happy now we can move"));
                        break;
                    case 20:
                        npcs.get(3).setNextForceTalk(new ForceTalk("Like a party animal in the groove"));
                        break;
                }

                if(cycle > 24) {
                    dancing = false;
                    npcs.stream().forEach(NPC::finish);
                    npcs.clear();
                    this.cancel();
                }
                cycle++;
            }
        }, 0, 600);

    }

    public static void acceptDepositItems(Player player) {
        ItemsContainer<Item> depo = getDepositContainer(player);
        if(depo == null)
            return;

        if(depo.getFreeSlots() == depo.getSize()) {
            player.sendMessage("You must add some items first!");
            return;
        }
        List<Item> queueValidItems = Arrays.stream(itemsQueued.getItems()).filter(Objects::nonNull).collect(Collectors.toList());
        List<Item> depoValidItems = Arrays.stream(depo.getItems()).filter(Objects::nonNull).collect(Collectors.toList());
        if(queueValidItems.size() + depoValidItems.size() > 214) {
            player.sendMessage("The item drop queue is full!");
            return;
        }

        // check if items being added will cause an overflow in the queue
        for (Item item : depoValidItems) {
            if (Utils.intOverflow(item.getAmount(), itemsQueued.getNumberOf(item))) {
                player.sendMessage("The drop party chest cannot hold that many " + item.getName() + ".");
                return;
            }
        }

        player.sendMessage("Your items have been added to the party chest!");

        depoValidItems.stream().filter(Objects::nonNull)
                .forEach(item -> {
                    itemsQueued.add(item);
                    player.addDropPartyValue(item.getAmount() * (item.getId() == 995 ? 1 : GrandExchange.getPrice(item.getId())));
                });
        updateChestForSubscribers();
        player.getTemporaryAttributtes().put(PARTY_ROOM_CHEST_DEPOSIT, new ItemsContainer<Item>(10, false));
        refreshChestInterface(player);
    }

    public static void externalAdd(Item item) {
        itemsQueued.add(item);
        updateChestForSubscribers();
    }

    private static void updateChestForSubscribers() {
        chestUpdateSubscribers.stream().forEach(PartyRoom::refreshChestInterface);
    }

    public static int canDeposit(Player player, Item i) {
        ItemsContainer<Item> depo = getDepositContainer(player);
        if(i.getDefinitions().isStackable() && (depo.freeSlots() > 0 || depo.containsOne(i)))
            return i.getAmount();
        else {
            return depo.getFreeSlots();
        }

    }

    public static void withdrawItem(Player player, int slotId) {
        ItemsContainer<Item> depo = getDepositContainer(player);
        if(depo == null)
            return;

        Item withdrawItem = depo.get(slotId);
        if(withdrawItem == null)
            return;

        // default withdraw all
        int remove = depo.getNumberOf(withdrawItem);

        player.getInventory().addItem(withdrawItem.getId(), remove);
        depo.removeAll(withdrawItem);
        depo.shift();
        refreshChestInterface(player);
    }

    public static void deposit(Player player, int itemId, int amount) {
    	if (!ItemConstants.isTradeable(new Item(itemId))) {
			player.getPackets().sendGameMessage("You can't add untradeable items to the party chest!");
			return;
    	}
        Item item = new Item(itemId, Math.min(amount, player.getInventory().getAmountOf(itemId)));
        amount = Math.min(canDeposit(player, item), item.getAmount());
        if(amount == 0) {
            player.sendMessage("You must remove an item in order to add anything else.");
            return;
        }
        getDepositContainer(player).add(new Item(item.getId(), amount));
        player.getInventory().deleteItem(item.getId(), amount);
        refreshChestInterface(player);
    }

    public static void openChest(Player player, WorldObject object) {
        player.lock(2);
        WorldObject chest = new WorldObject(PARTY_CHEST_OPEN, object.getType(), object.getRotation(),
                object.getX(), object.getY(), object.getPlane());
        World.spawnObject(chest);
        player.sendMessage("You open the chest.");
        player.anim(536);
    }

    public static void closeChest(Player player, WorldObject object) {
        player.lock(2);
        WorldObject chest = new WorldObject(PARTY_CHEST_CLOSED, object.getType(), object.getRotation(),
                object.getX(), object.getY(), object.getPlane());
        World.spawnObject(chest);

        player.sendMessage("You close the chest.");
        player.anim(536);
    }

    private static void startParty(Player player) {
        if(gameTask != null && gameTask.isRunning) {
            player.sendMessage("There is already a balloon party running!");
            return;
        }

        if(Arrays.stream(itemsQueued.getItems()).filter(Objects::nonNull).count() < 10) {
            player.sendMessage("There must be at least 10 items in the chest to start Balloon bonanza.");
            return;
        }

        if(balloons.keySet().stream().filter(Objects::nonNull).filter(obj -> !World.isTileFree(obj.getPlane(), obj.getX(), obj.getY(), 1)).count() > 0) {
            player.sendMessage("There are still balloons on the ground!");
            return;
        } else {
            balloons.clear();
        }
        player.lock(2);
    	player.anim(2140);
        if(gameTask == null) {
            gameTask = new BalloonParty();
            GameExecutorManager.fastExecutor.schedule(gameTask, 1, 1000);
        }

        player.getInventory().removeItemMoneyPouch(new Item(995, BALLOON_PARTY_GP));
        gameTask.isRunning = true;
        gameTask.setValue();
        gameTask.setCountdown();
    }


    private static class BalloonParty extends TimerTask {

        private static int MIN_DROPS_PER_CYCLE = 4;
        private static int MAX_DROPS_PER_CYCLE = 10;

        boolean isRunning = true;
        long value = 0;
        int countdown = 0;

        public void setValue() {
            value = getTotalCoins();
        }

        public void setCountdown() {
            countdown = value > 50_000_000 ? 300 : value > 20_000_000 ? 120 : 60; // 5min, 2min, 1min
        }

        public void stop() {
            isRunning = false;
        }

        private void call() {

            // people can add items until the countdown is done
            value = PartyRoom.getTotalCoins();
            String val = value / 1_000_000 + "M";
            int mins = countdown / 60;

            for(NPC n : World.getNPCs()) {
                // don't call under 10m
                if(value > 10000000 && countdown % 5 == 0) {
                    if (n.getName().toLowerCase().contains("banker")
                    		|| n.getName().toLowerCase().contains("guard")
                    		|| n.getName().toLowerCase().contains("town")) {
                        n.setNextForceTalk(new ForceTalk("A drop party worth " + val + " is starting in " + (mins <= 1 ? "less than a minute" : mins + " minutes") + "!"));
                    }
                }
                if(n.getId() == 659) 
                    n.setNextForceTalk(new ForceTalk(countdown+""));
            }
        }

        int nextWave = 0;
        int waves = 0;

        @Override
        public void run() {
            try {
                synchronized (lock) {
                    if(!isRunning) return;

                    if(--countdown > 0) {
                        call();
                        return;
                    } else if(countdown == 0) {
                        flipQueue();
                        nextWave = 0;
                    }

                    if(nextWave-- > 0)
                        return;

                    nextWave = 2; // every 2 seconds
                    waves++;

                    int toDrop = Utils.random(MIN_DROPS_PER_CYCLE, MAX_DROPS_PER_CYCLE);

                    for(int i = 0; i < toDrop && isRunning; i++) {
                        if(!PartyRoom.addBalloon())
                            stop();
                    }
                }
            } catch (Throwable e) {
                Logger.handle(e);
            }
        }

        private void flipQueue() {
            // start the drop
            itemsDropping.addAll(itemsQueued.getItems());
            itemsQueued.clear();
            updateChestForSubscribers();
            if (value >= 10000000)
            	World.sendNews("A drop party has started worth "+ Utils.getFormattedNumber(value) + " in the Falador party room!", 0);
        }

    }
}
