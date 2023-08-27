package com.rs.game.minigames.lms;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.MapInstance;
import com.rs.game.minigames.pktournament.PkTournamentType;
import com.rs.game.player.Player;
import com.rs.game.player.content.Drinkables;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.net.decoders.handlers.ObjectHandler;
import com.rs.utils.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Simplex
 * created on 2021-02-01
 */
public class LastManStanding implements Serializable {

    private static final long serialVersionUID = -560352519997725245L;

    public static final int DEFAULT_ENTRANCE_FEE = 10_000_000;

    /**
     * Minimum players required to start a game
     */
    public static final int MIN_PLAYERS_TO_BEGIN = 4;

    /**
     * Time between structured games
     */
    private static final long TIME_BETWEEN_GAMES = TimeUnit.HOURS.toMillis(6);

    /**
     * Player temp attribute keys
     */
    private static final Object LMS_KILLS_KEY = "LMS_KILLS";

    /**
     * Blood money rewarded
     */
    private static final int BLOOD_MONEY_PER_KILL = 2500, BLOOD_MONEY_PARTICIPATION = 2500, BLOOD_MONEY_WIN = 10000;

    /**
     * Main game instance singleton.
     */
    private static LastManStanding structuredInstance = null;

    /**
     * Privately hosted player instance.
     */
    private static LastManStanding privateInstance = null;

    /**
     * NPCS
     */
    public static final int LISA_NPC = 27316;

    /**
     * OBJECTS
     */
    public static final int EXIT_LOBBY_OBJECT = 11177;

    /**
     * ITEMS
     */
    public static final int BLOODIER_KEY = 50608;
    public static final int BLOODY_KEY = 50526;
    public static final int SURVIVAL_TOKEN = 50527;

    /**
     * TILES
     */
    public static final WorldTile LOBBY = new WorldTile(3087, 3477, 0), OUTSIDE = new WorldTile(3087, 3476, 0);

    /**
     * The fee to enter, this will decide the pot - if there is one.
     */
    public static int entranceFee = 0;

    /**
     * Each player that joins adds entranceFee - 15% to the pot.
     */
    public static long rewardPot = 0;

    /**
     * Only one game should run at a time; therefore instance may be static.
     */
    private transient MapInstance mapInstance;

    /**
     * Time for players to join the game
     */
    private Stopwatch lobbyClock = new Stopwatch();

    /**
     * Maximum time a game can run
     */
    private Stopwatch gameClock = new Stopwatch();

    /**
     * Game countdown start clock
     */
    private Stopwatch gameStartClock = new Stopwatch();

    /**
     * Time between games
     */
    private Stopwatch intermissionClock = new Stopwatch();

    /**
     * Tile which final area is built around
     */
    private WorldTile finalAreaTile = null;

    /**
     * Tiles players can spawn on
     */
    private LinkedList<WorldTile> spawnAreaTiles = new LinkedList<>();

    /**
     * Current game state
     */
    private LastManStandingState state = LastManStandingState.FINISHED;

    /**
     * Players in a game or in lobby
     */
    private transient List<Player> players;

    /**
     * Manages this instance.
     */
    private LastManStandingEngine engine;

    /**
     * Singleton instance. (For structured games)
     */
    public static LastManStanding getSingleton() {
        return structuredInstance;
    }

    /**
     * Default constructor
     */
    public LastManStanding(LastManStandingState state) {
        initGame();
        players = new ArrayList<>();
        engine = new LastManStandingEngine(this);
        WorldTasksManager.schedule(engine, 0, 0);
        this.state = state;
    }

    /**
     * Static content initializers.
     */
    public static void init() {
        LastManStandingChest.init();

        // talk to lisa (enter minigame)
        NPCHandler.register(27316, 1, (player, npc) -> {
            player.stopAll();
            player.getDialogueManager().startDialogue(new EnterLastManStandingD());
        });

        /*NPCHandler.register(27316, 2, (player, npc) -> {
            player.stopAll();
            ShopsHandler.openShop(player, 502);
        });*/

        ObjectHandler.register(129092, 1, (player, obj) -> {
            player.lock(1);
            player.stopAll();
            if(player.getY() > obj.getY()) {
                player.setNextWorldTile(obj.getX(), obj.getY() - 1);
            } else {
                player.setNextWorldTile(obj.getX(), obj.getY() + 1);
            }
        });
        ObjectHandler.register(112352, 1, (player, obj) -> {
            if(player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof LastManStandingLobby) {
                player.stopAll();
                ((LastManStandingLobby) player.getControlerManager().getControler()).leave();
                player.lock(2);
            }
        });

        // exit lobby portal
        ObjectHandler.register(EXIT_LOBBY_OBJECT, 1, (player, obj) -> {
            if(isInLobby.test(player)) {
                ((LastManStandingLobby) player.getControlerManager().getControler()).leave();
            }
        });

        // start singleton
        WorldTasksManager.schedule(() -> {
            // after the server finishes loading, create structured instance
            structuredInstance = new LastManStanding(LastManStandingState.FINISHED);
            // start structured game 30 minutes after launch
            structuredInstance.intermissionClock.delayMS(10500); //TimeUnit.MINUTES.toMillis(Settings.DEBUG ? 1 : 30)
        }, 10);
    }

    /**
     * This will abruptly cancel any private games which have not started yet and cancel the game
     * to make way for an official instance.
     */
    public static void clearPrivateGames() {
        if(privateInstance != null) {
            if(privateInstance.getState() == LastManStandingState.LOBBY) {
                privateInstance.endGame();
                privateInstance.getPlayersInLobby().stream().forEach(player -> {
                    ((LastManStandingLobby) player.getControlerManager().getControler()).leave();
                    player.sendMessage("<col=ff0000>An official game of Last Man Standing is starting, private game closing.");
                });

                privateInstance = null;
            }
        }
    }


    /**
     * Creates a private instance.
     */
    private static void startPrivateInstance() {
        // set static private instance, in future we can map these
        privateInstance = new LastManStanding(LastManStandingState.LOBBY);

        // start lobby clock, processed in LastManStandingManager
        privateInstance.lobbyClock.delayMS((int) TimeUnit.MINUTES.toMillis(10));
    }

    public static void destroyPrivateInstance() {
        privateInstance = null;
    }

    public static void forceStart(int entryFee) {
        entranceFee = entryFee;
        getSingleton().startStructuredGame(entryFee);
    }

    public static LastManStanding getPrivateGame() {
        return privateInstance;
    }

    /**
     * Move from finished -> lobby
     * Wait for players to join and then start game.
     */
    private void initGame() {
        // init map instance
        mapInstance = new MapInstance(424, 720);

        // load map instance
        mapInstance.load(() -> { /* empty */});
    }

    /**
     * Inistal player count
     */
    private int playerCount = 0;

    /**
     * Move from lobby -> running
     */
    public void startGame() {
        if(getPlayersInLobby().size() < MIN_PLAYERS_TO_BEGIN) {
            lobbyClock.delayMS(TimeUnit.MINUTES.toMillis(10));
            getPlayersInLobby().forEach(player -> {
                player.sendMessage("Not enough players to start the game! Players required: " + MIN_PLAYERS_TO_BEGIN);
            });
            return;
        }

        // Set the center tile of the final area rectangular area
        finalAreaTile = mapInstance.getTile(12, 43);

        /* List<WorldTile> tiles = mapInstance.getTile(28, 15).area(6);
        Collections.shuffle(tiles);
        spawnAreaTiles.addAll(tiles);*/

        // start lobby clock, processed in LastManStandingManager
        gameClock.delayMS((int) TimeUnit.MINUTES.toMillis(20));

        // initial game clock
        gameStartClock.delayMS(10000);

        // switch state to game mode
        state = LastManStandingState.RUNNING;

        // move all players into the game
        getPlayersInLobby().forEach(this::enterGame);

        // set total players at beginning of game
        playerCount = getPlayersInGame().size();
    }

    /**
     * Move from running -> finished
     */
    public void endGame() {
        state = LastManStandingState.FINISHED;

        if(isStructuredGame()) {
            intermissionClock.delayMS(TIME_BETWEEN_GAMES);
            entranceFee = 0;
        }

        // players left before end of game
        if(players.size() == 0)
            return;

        Player winner = players.get(0);

        while(players.size() > 0)
            leave(players.get(0));

        rewardWinner(winner);
    }

    /**
     * 1 player wins the entire pot - 15%
     * @param winner
     */
    private void rewardWinner(Player winner) {
        if(rewardPot > 0 && isStructuredGame()) {
            World.sendNews(winner, "<col="+ Colour.ORANGE_RED.hex+"><shad=ff7200>"+winner.getName()+"<shad=0><col="+ Colour.ORANGE_RED.hex+"> has won Last Man Standing! (", World.GAME_NEWS);
            rewardPot = 0;

            // remove
            long plat = ((long) ((double) rewardPot * 0.85));
            Item coin = new Item(43204, (int) (plat / 1000));//
            Item bm = new Item(43307, BLOOD_MONEY_WIN);
            winner.getInventory().add(coin); // 12.5% tax
            winner.getInventory().add(bm);
            winner.sendMessage(Colour.RAID_PURPLE.wrap("You are victorious! You have won " + Utils.getFormattedNumber(coin.getAmount()) + " gp and " + Utils.getFormattedNumber(bm.getAmount()) + " blood money!"));
        } else {
            winner.sendMessage(Colour.RAID_PURPLE.wrap("You are victorious! There was no reward pot."));
        }
    }

    /**
     * Collect a list of players in LMS lobby.
     */
    public List<Player> getPlayersInLobby() {
        return players.stream().filter(isInLobby).collect(Collectors.toList());
    }

    /**
     * Collect a list of players in LMS game.
     */
    public List<Player> getPlayersInGame() {
        return players.stream().filter(isInGame).collect(Collectors.toList());
    }

    /**
     * In lobby predicate
     */
    public static Predicate<Player> isInLobby = player -> player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof LastManStandingLobby;

    /**
     * In game predicate
     */
    public Predicate<Player> isInGame = player -> player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof LastManStandingGame;

    /**
     * Joining LMS from Lisa dialogue.
     */
    public static void enterLobby(Player player) {
        if(structuredInstance == null) {
            player.sendMessage("Try again in a few seconds.");
            return;
        }

        if(structuredInstance.getState() == LastManStandingState.LOBBY) {
            structuredInstance.join(player);
            player.sendMessage("You have joined the LMS Game!");
        } else {
            if(privateInstance == null) {
                startPrivateInstance();
            }

            if(privateInstance.mapInstance.getStage() != MapInstance.Stages.RUNNING) {
                player.lock();
                WorldTasksManager.schedule(() -> {
                    // wait for map to be built
                    if(privateInstance != null && privateInstance.mapInstance.getStage() == MapInstance.Stages.RUNNING) {
                        privateInstance.join(player);
                    } else {
                        player.sendMessage("The private game of last man standing has been destroyed!");
                    }
                    player.unlock();
                },2);
            } else {
                privateInstance.join(player);
            }

            if(structuredInstance.getState() == LastManStandingState.FINISHED) {
                player.sendMessage("A public game will begin in " + Utils.longFormat(structuredInstance.intermissionClock.remaining()) + ".");
            }
        }
    }

    /**
     * Add a player to the LMS lobby.
     * @param player
     */
    private void join(Player player) {
        synchronized (players) {
            if(mapInstance.getStage() != MapInstance.Stages.RUNNING) {
                // shouldn't happen
                player.sendMessage("Last man standing is not ready yet.");
                return;
            }

            if(entranceFee != 0) {
                if(player.getMoneyPouch().getCoinsAmount() < entranceFee) {
                    player.sendMessage("You do not have enough coin to enter the <col=ff0000>high risk</col> last man standing game!");
                    player.sendMessage("Entrance fee: <col=ff0000>" + Utils.getFormattedNumber(entranceFee) + ".");
                    return;
                } else {
                    rewardPot += entranceFee;
                    player.getMoneyPouch().sendDynamicInteraction(entranceFee, true);
                }
            }

            if(privateInstance == this) {
                player.sendMessage("You have entered a "+ Colour.YELLOW.wrap("private")+" game of Last Man Standing.");
            }

            if(!players.contains(player))
                players.add(player);

            PkTournamentType.LMS.setup(player);
            player.setCantTrade(true);
            player.getControlerManager().startControler(new LastManStandingLobby(), this);
            player.setRequiresTournamentReset(true);
            player.setNextWorldTile(LOBBY);
        }
    }

    /**
     * Remove player from last man standing.
     */
    public void leave(Player player) {

        synchronized (players) {
            players.remove(player);
            PkTournamentType.LMS.removeSetup(player);
            player.setNextWorldTile(OUTSIDE);
            player.reset();
            player.setCanPvp(false);
            player.setCantTrade(false);
            player.getControlerManager().removeControlerWithoutCheck();
            player.getInterfaceManager().removeOverlay(false);
            player.setRequiresTournamentReset(false);
            player.getTemporaryAttributtes().remove(LMS_KILLS_KEY);
            int kills = (int) player.getTemporaryAttributtes().getOrDefault(LMS_KILLS_KEY, 0);
            if(kills > 0) {
                player.getInventory().add(new Item(43307, BLOOD_MONEY_PER_KILL * kills));
            }
            if(isStructuredGame() && getState() == LastManStandingState.LOBBY && rewardPot >= entranceFee) { //failsafes
                player.getInventory().addItemMoneyPouch(new Item(995, entranceFee));
                rewardPot -= entranceFee;
            }
        }
    }

    public void enterGame(Player player) {
        player.lock();
        player.reset();
        player.sendMessage("The game is starting!");
        final long time = FadingScreen.fade(player);
        FadingScreen.unfade(player, time, () -> {
            if(player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof LastManStandingLobby) {
                player.setCanPvp(true);
                player.getControlerManager().removeControlerWithoutCheck();
                player.getControlerManager().startControler(new LastManStandingGame(), this);
                //player.setNextWorldTile(spawnAreaTiles.poll());
                WorldTile t = Utils.get(mapInstance.getTile(20, 15).area(12));
                if(Settings.DEBUG) {
                    player.sendMessage("Debug: moving to world tile: " + t);
                }
                player.setNextWorldTile(t);
                player.setForceMultiArea(false);
            }
            player.unlock();
        });
    }

    public LastManStandingState getState() {
        return state;
    }

    public Stopwatch getLobbyClock() {
        return lobbyClock;
    }

    public Stopwatch getGameClock() {
        return gameClock;
    }

    public Stopwatch getGameStartClock() {
        return gameStartClock;
    }

    public Stopwatch getIntermissionClock() {
        return intermissionClock;
    }

    public void eliminate(Player player) {
        leave(player);

        if(isStructuredGame()) {
            player.getInventory().add(new Item(43307, BLOOD_MONEY_PARTICIPATION));
        }
    }

    public void startStructuredGame(int fee) {
        state = LastManStandingState.LOBBY;

        // start lobby clock, processed in LastManStandingManager
        lobbyClock.delayMS((int) TimeUnit.MINUTES.toMillis(10));

        entranceFee = fee;

        World.sendNews("A High Risk<col=ffffff> <img=9> Last Man Standing <col=ffff00> game will begin in 10 minutes!", 3);
        World.sendNews("Type <col=ffffff>::lms<col=ffff00> to get there! (Entrance fee: <col=ffffff>" + Utils.getFormattedNumber(entranceFee) + " <col=ffff00>coins)", 3);
    }

    public boolean isStructuredGame() {
        return structuredInstance == this;
    }

    private static Drinkables.Drink[] RESTOCK = {
        Drinkables.Drink.SUPER_RESTORE_POTION,

        Drinkables.Drink.SARADOMIN_BREW_POTION,

        Drinkables.Drink.SUPER_RESTORE_POTION,

        Drinkables.Drink.RANGING_POTION
    };

    public void rewardKill(Player killer) {
        Item key = new Item(getPlayersInGame().size() < 6 ? BLOODIER_KEY : BLOODY_KEY);
        killer.reset();
        killer.getInventory().add(key);
        killer.addLmsKill();
        killer.sendMessage(Colour.RED.wrap("You find a " + key.getName() + "!"));
        killer.getTemporaryAttributtes().put(LMS_KILLS_KEY, (int) killer.getTemporaryAttributtes().getOrDefault(LMS_KILLS_KEY, 0) + 1);
        killer.setAttackedByDelay(15000);
        if(killer.getAttackedBy() != null)
            killer.getAttackedBy().asPlayer().resetCombat();
        killer.resetCombat();
        int sharks = 0;
        for(int i = 0; i < 28; i++) {
            Item item = killer.getInventory().getItems().getItems()[i];
            final Item ITEM = item;
            if (item != null) {
                Optional<Drinkables.Drink> drink = Arrays.stream(RESTOCK).filter(d -> d.contains(ITEM.getId())).findAny();
                if(drink.isPresent()) {
                    killer.getInventory().getItems().set(i, new Item(drink.get().getIdForDoses(drink.get().getMaxDoses())));
                }
                if(item.getId() == 385) {
                    sharks ++;
                }
            } else {
                if(sharks ++ < 12)
                    killer.getInventory().getItems().set(i, new Item(385));
            }
        }

        killer.getInventory().refresh();

    }

    public WorldTile getTile(int i, int i1) {
        return mapInstance.getTile(i, i1);
    }

    public WorldTile getFinalAreaTile() {
        return finalAreaTile;
    }

}
