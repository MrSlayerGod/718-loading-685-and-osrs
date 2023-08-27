package com.rs.game.minigames.pktournament;

import com.rs.Settings;
import com.rs.cache.loaders.NPCConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.box.MinigameBox;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.pktournament.PkTournamentGame;
import com.rs.game.player.controllers.pktournament.PkTournamentLobby;
import com.rs.game.player.controllers.pktournament.PkTournamentSpectating;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

enum State {
    LOBBY, PLAYING, SPECTATING
}

/**
 * @author Simplex
 * @since Jun 2020
 */
public class PkTournament {

    public static final PkTournamentType[] ENABLED_TYPES = {
            // assuming 7 types
            // 42.8% chance dharoks
            PkTournamentType.Dharoks,
            PkTournamentType.Dharoks,
            PkTournamentType.Dharoks,

            // 14.2% everything else
            PkTournamentType.Hybrid_Main_Mystics,
            PkTournamentType.Max_Strength,
            PkTournamentType.Pure,
            PkTournamentType.Pure_NH,
    };

    private static final WorldTile LOBBY_TOP = new WorldTile(5982, 9843, 0);
    private static final WorldTile LOBBY = new WorldTile(5982, 9803, 0);
    private static final WorldTile GAME_TILE_1 = new WorldTile(5977, 9817, 0);
    private static final WorldTile GAME_TILE_2 = new WorldTile(5990, 9830, 0);
    private static final WorldTile OUTSIDE = new WorldTile(3087, 3486, 0);
    private static final WorldTile[] SPEC_TILES = {
            new WorldTile(5983, 9835, 0),
            new WorldTile(5984, 9812, 0),
            new WorldTile(5995, 9823, 0),
            new WorldTile(5972, 9823, 0),
    };
    public static int NPC_ID = 3050;
    public static CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();
    public static int round = 0;
    public static boolean roundStarted = false;
    public static boolean isRunning = false, canJoin = false, intermission = false;
    public static long intermissionMS = 0L, joinMS = 0L;
    public static PkTournamentType type;
    public static int tournamentPlayerCount = -1;
    // these may be set by talking to the tournament host
    // they will not be added as they may not be tangible rewards
    // example: 100m OSRS GP
    public static String grandPrize = null, runnerUpPrize = null, runnerUp2Prize = null;
    public static Item defaultGrandPrize = new Item(995, 15_000_000),
            defaultRunnerUpPrize = new Item(995, 10_000_000),
            defaultRunnerUp2Prize = new Item(995, 7_500_000);
    public static boolean fun = false;
    public static long triggerTime, triggerTime2, triggerTime3, triggerTime4;// triggerTime3, triggerTime4;
    public static int tournamentID = Utils.random(Integer.MAX_VALUE);
    public static PkTournamentType forceType = null;
    private static String lastSeededPlayer = null;
    private static int reseedAttempts = 0;

    static {
        init();
    }

    public static void handleRemoval(Player player) {
        if (getState(player) == State.PLAYING) {
            Player target = ((PkTournamentGame) player.getControlerManager().getControler()).getTarget();
            if (target != null) {
                target.sendMessage(String.format("%s has forfeited the match.", target.getName()));
                endBracket(target, player, true);
            }
        }
        if (player.tournamentResetRequired() || getLobbyCon(player) != null || getGameCon(player) != null) 
			PkTournamentType.removeSetup(player);
		
        player.getControlerManager().removeControlerWithoutCheck();
        players.remove(player);
        player.setRequiresTournamentReset(false);
        player.setCantTrade(false);
        player.setCanPvp(false);
        player.setLastTarget(null);
        player.getInterfaceManager().removeOverlay(false);
        player.useStairs(-1, OUTSIDE, 0, 2);
    }

    private static int getPlacement() {
        return getIngamePlayers().size() + getLobbyPlayers().size();
    }

    private static void reward(Player player, int placement) {
        placement += 1;

        //if(round != 0) {
        if (!fun) {
            int bloodMoney = 2500 * (round + 1);
            player.getBank().addItem(43307, bloodMoney, true);
            player.sendMessage(String.format("<col=ff0000>Pk Tournament: %d Blood money has been added to your bank for staying alive %d round%s", bloodMoney, round, round <= 1 ? "" : "s"));

            Item item = new Item(MinigameBox.ID, (round + 1));
            player.getPackets().sendGameMessage("<col=ff0000>Pk Tournament: " + item.getName() + " x" + item.getAmount() + " has been added to your bank.");
            player.getBank().addItem(item.getId(), item.getAmount(), false);
        }

        String prize, title;
        final String dialogue;

        if (placement < 4) {
            switch (placement) {
                case 1:
                    title = "grand champion";
                    prize = grandPrize != null ? grandPrize
                            : Utils.getFormattedNumber(defaultGrandPrize.getAmount()) + " x " + defaultGrandPrize.getName();

                    if (fun) prize = "No prize (fun tournament)";
                    else if (grandPrize == null) {
                        player.getBank().addItem(defaultGrandPrize.getId(), defaultGrandPrize.getAmount(), false);
                        player.sendMessage("<img=7><shad=000><col=D80000>1st place prize: <col=ff0000>" + prize + " <col=D80000>was added to your bank!");
                    }
                    break;
                case 2:
                    title = "first runner up";
                    prize = runnerUpPrize != null ? runnerUpPrize
                            : Utils.getFormattedNumber(defaultRunnerUpPrize.getAmount()) + " x " + defaultRunnerUpPrize.getName();

                    if (fun) prize = "No prize (fun tournament)";
                    else if (runnerUpPrize == null) {
                        player.getBank().addItem(defaultRunnerUpPrize.getId(), defaultRunnerUpPrize.getAmount(), false);
                        player.sendMessage("<img=7><shad=000><col=D80000>2nd place prize: <col=ff0000>" + prize + " <col=D80000>was added to your bank!");
                    }
                    break;
                case 3:
                    title = "second runner up";
                    prize = runnerUp2Prize != null ? runnerUp2Prize
                            : Utils.getFormattedNumber(defaultRunnerUp2Prize.getAmount()) + " x " + defaultRunnerUp2Prize.getName();

                    if (fun) prize = "No prize (fun tournament)";
                    else if (runnerUp2Prize == null) {
                        player.getBank().addItem(defaultRunnerUp2Prize.getId(), defaultRunnerUp2Prize.getAmount(), false);
                        player.sendMessage("<img=7><shad=000><col=D80000>3rd place prize: <col=ff0000>" + prize + " <col=D80000>was added to your bank!");
                    }
                    break;
                default:
                    prize = title = "error";
                    break;
            }

            dialogue = title.toUpperCase() + "<br>" + getType().getFormattedName().toUpperCase() + " PK TOURNAMENT" +
                    "<br>You've won: <col=ff0000>" + prize + "</col>!";
        } else {
            dialogue = String.format("<col=ff0000>Pk Tournament:</col> You placed %s out of %d contestants.", ordinal(placement), tournamentPlayerCount);
        }

        if (placement <= 3)
            World.sendNews(dialogue.replace("You've", player.getName()).replace("<br>", "!! "), 1);


        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getDialogueManager().startDialogue("SimpleMessage",
                        dialogue);
                stop();
            }
        }, 1);
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }

    public static void endBracket(Player winningPlr, Player losingPlr, boolean giveup) {
        if (!(winningPlr.getControlerManager().getControler() instanceof PkTournamentGame)) {
            //winning player lost already. died after.
            winningPlr.reset();
            return;
        }
        //restart fight if both die same time or when one is dying
        if (losingPlr != null && losingPlr.isDead() && winningPlr.isDead()) {
            losingPlr.reset();
            winningPlr.reset();
            winningPlr.sendMessage("Restarting fight since both players died!");
            losingPlr.sendMessage("Restarting fight since both players died!");
            return;
        }
        if (winningPlr.getControlerManager().getControler() instanceof PkTournamentGame &&
                losingPlr != null && winningPlr.getControlerManager().getControler() instanceof PkTournamentGame && !giveup
                && !losingPlr.isDead()) {
            return; //restarted fight
        }

        if (losingPlr == null)
            winningPlr.sendMessage("Your opponent has dropped out of the tournament. You will be automatically seeded to the next round.");
        else if (/*losingPlr == null*/giveup) {
            winningPlr.sendMessage("Your opponent has dropped out of the tournament. You will be automatically seeded to the next round.");
            losingPlr.sendMessage("You have dropped out of the tournament. You won't receive a reward.");

            losingPlr.getHintIconsManager().removeUnsavedHintIcon();
            losingPlr.useStairs(-1, new WorldTile(SPEC_TILES[Utils.random(SPEC_TILES.length)], 2, true), 0, 2);
            losingPlr.reset();
            getType().removeSetup(losingPlr);
            switchController(losingPlr, "PkTournamentSpectating");
        } else {
            losingPlr.getHintIconsManager().removeUnsavedHintIcon();
            winningPlr.sendMessage("<col=ff0000>PK Tournament:</col> You have eliminated <col=ff0000>" + losingPlr.getName() + "</col> and won round <col=ff0000>" + (round + 1) + "</col>!");
            losingPlr.sendMessage("<col=ff0000>PK Tournament:</col> " +
                    "<col=ff0000>You have been eliminated in round " + (round + 1) + " by <col=ff0000>" + winningPlr.getName() + "!");
            losingPlr.useStairs(-1, new WorldTile(SPEC_TILES[Utils.random(SPEC_TILES.length)], 2, true), 0, 2);
            losingPlr.reset();
            getType().removeSetup(losingPlr);
            switchController(losingPlr, "PkTournamentSpectating");
            reward(losingPlr, getPlacement());
        }

        winningPlr.incrementPkTournamentKills();
        getType().removeSetup(winningPlr);
        getType().setup(winningPlr);
        winningPlr.reset();
        winningPlr.getHintIconsManager().removeUnsavedHintIcon();
        winningPlr.useStairs(-1, new WorldTile(LOBBY_TOP, 2, true), 0, 2);
        switchController(winningPlr, "PkTournamentLobby");
    }

    private static boolean checkRoundOver() {
        if (getIngamePlayers().size() < 1) {
            for (Player p : players) {
                if (p != null) {
                    p.sendMessage("<col=ff0000>PK Tournament:</col> Round " + (round + 1) + " has completed. " + (getLobbyPlayers().size() >= 2 ? "Round " + (round + 2) + " will commence in 10 seconds." : "The tournament has concluded!"));
                }
            }

            round++;
            return true;
        }

        return false;
    }

    public static boolean inMinigame(Player player) {
        return getState(player) == null;
    }

    public static State getState(Player player) {
        Controller c = player.getControlerManager().getControler();
        return c instanceof PkTournamentLobby ? State.LOBBY : c instanceof PkTournamentGame ? State.PLAYING : c instanceof PkTournamentSpectating ? State.SPECTATING : null;
    }

    public static PkTournamentType getType() {
        return type;
    }

    public static void join(Player player) {
        handleAdd(player);
        player.useStairs(-1, Utils.getFreeTile(LOBBY, 3), 0, 2);
        PkTournament.switchController(player, "PkTournamentLobby");
        getType().setup(player);
    }

    public static void handleAdd(Player player) {
        if (!players.contains(player))
            players.add(player);
    }

    public static void init() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 6);
        cal1.set(Calendar.MINUTE, 0);

        triggerTime = cal1.getTime().getTime();

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 18);
        cal2.set(Calendar.MINUTE, 0);

        triggerTime2 = cal2.getTime().getTime();

        Calendar cal3 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 12);
        cal2.set(Calendar.MINUTE, 0);

        triggerTime3 = cal2.getTime().getTime();

        Calendar cal4 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 24);
        cal2.set(Calendar.MINUTE, 0);

        triggerTime4 = cal2.getTime().getTime();

        if (Utils.currentTimeMillis() > triggerTime) {
            // already passed trigger time
            triggerTime += TimeUnit.DAYS.toMillis(1);
        }
        if (Utils.currentTimeMillis() > triggerTime2) {
            // already passed 2nd trigger time
            triggerTime2 += TimeUnit.DAYS.toMillis(1);
        }
        if (Utils.currentTimeMillis() > triggerTime3) {
            // already passed 2nd trigger time
            triggerTime3 += TimeUnit.DAYS.toMillis(1);
        }
        if (Utils.currentTimeMillis() > triggerTime4) {
            // already passed 2nd trigger time
            triggerTime4 += TimeUnit.DAYS.toMillis(1);
        }

        // tournament daily scheduler
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                // triggers for tournament start
                if (Utils.currentTimeMillis() > triggerTime) {
                    initTournament(10, TimeUnit.MINUTES);
                    triggerTime += TimeUnit.DAYS.toMillis(1);
                }
                if (Utils.currentTimeMillis() > triggerTime2) {
                    initTournament(10, TimeUnit.MINUTES);
                    triggerTime2 += TimeUnit.DAYS.toMillis(1);
                }
                if (Utils.currentTimeMillis() > triggerTime3) {
                    initTournament(10, TimeUnit.MINUTES);
                    triggerTime3 += TimeUnit.DAYS.toMillis(1);
                }
                if (Utils.currentTimeMillis() > triggerTime4) {
                    initTournament(10, TimeUnit.MINUTES);
                    triggerTime4 += TimeUnit.DAYS.toMillis(1);
                }
            }
        }, 0, 1000);
    }

    public static void initTournament(int units, TimeUnit unit) {
        tournamentID = Utils.random(Integer.MAX_VALUE);
        if (forceType == null) {
            type = Utils.get(ENABLED_TYPES);
        } else {
            type = PkTournamentType.values()[forceType.ordinal()];
            forceType = null;
        }

        if (type == null) {
            // shouldn't happen
            System.err.println("Couldn't select random tournament type, defaulting to Dharok's");
            type = PkTournamentType.Dharoks;
        }

      //  World.sendNews("A " + (fun ? "FUN " : "") + "" + type.getFormattedName() + " PK Tournament will begin in " + units + " " + unit.name().toLowerCase() + "!", 1);
        //World.sendNews("Speak to " + NPCConfig.forID(PkTournament.NPC_ID).getName() + " in Edgeville to join!", 1);

        final String specialPrizeBroadcast;

        if (!fun && (grandPrize != null || runnerUpPrize != null || runnerUp2Prize != null)) {
            specialPrizeBroadcast = "PRIZES: 1st: <col=ff0000>" +
                    (grandPrize == null ? defaultGrandPrize.getName() : grandPrize) +
                    " <col=ff8c38>2nd: <col=ff0000>" +
                    (runnerUpPrize == null ? defaultRunnerUpPrize.getName() : runnerUpPrize) +
                    "<col=ff8c38> 3rd: <col=ff0000>" +
                    (runnerUp2Prize == null ? defaultRunnerUp2Prize.getName() : runnerUp2Prize) +
                    "<col=ff8c38>";

            World.sendNews(specialPrizeBroadcast, 1);
        } else
            specialPrizeBroadcast = null;

        canJoin = true;

        joinMS = unit.toMillis(units) + Utils.currentTimeMillis();
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {
            int i = units;
            final int tid = tournamentID;

            @Override
            public void run() {
                if (tournamentID != tid) {
                    this.cancel();
                    // tourny was cancelled
                    return;
                }
                if (i == 1 || i == 2 || i == 5) {
                    World.sendNews("A " + (fun ? "FUN " : "") + "" + type.getFormattedName() + " PK Tournament will begin in " + i + " minutes!", 1);
                    World.sendNews("Speak to " + NPCConfig.forID(PkTournament.NPC_ID).getName() + " in Edgeville to join!", 1);
                    if (specialPrizeBroadcast != null) {
                        World.sendNews(specialPrizeBroadcast, 1);
                    }
                }
                i--;

                if (i == -1) {
                    this.cancel();
                    canJoin = false;
                    tournamentPlayerCount = getIngamePlayers().size() + getLobbyPlayers().size();
                    startTournament();
                }
            }
        }, 0, 60000);
    }

    public static void startTournament() {
        if (isRunning)
            return;
        isRunning = true;
        World.sendNews("A " + (fun ? "FUN " : "") + "" + type.getFormattedName() + " PK Tournament has begun!", 1);

        if (getLobbyPlayers().size() < 3 && !Settings.DEBUG) {
            getLobbyPlayers().stream().filter(Objects::nonNull).forEach(PkTournament::handleRemoval);
            World.sendNews("The PK Tournament was cancelled! Less than 3 players joined.", 1);
            isRunning = false;
            canJoin = false;
            intermission = false;
            round = 0;
            return;
        }

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                List<Player> lobby = PkTournament.getLobbyPlayers();
                List<Player> ingame = PkTournament.getIngamePlayers();

                if (roundStarted) {
                    if (ingame.size() >= 1) {
                        // ensure the player's target didn't glitch out of the tourny
                        // shouldn't happen
                        for (Player player : ingame) {
                            Player target = Objects.requireNonNull(getGameCon(player)).getTarget();
                            if (target == null || getGameCon(target) == null) {
                                endBracket(player, null, true);
                                System.err.println("Error: " + player.getName() + "'s opponent glitched out of the fight.");
                            }
                        }
                    }

                    if (checkRoundOver()) {
                        if (getLobbyPlayers().size() > 1) {
                            // schedule next round
                            intermission = true;
                            intermissionMS = Utils.currentTimeMillis() + 10000;
                            roundStarted = false;

                            GameExecutorManager.slowExecutor.schedule(() -> {
                                intermission = false;
                            }, 10, TimeUnit.SECONDS);
                        } else {
                            // if runner up disconnects in the intermission
                            // this may be called
                            decideWinner();
                            roundStarted = false;
                            isRunning = false;
                            fun = false;
                            stop();
                        }
                    }
                } else {
                    // round over, or first round is starting
                    if (!intermission && lobby.size() > 1) {
                        decideBrackets();
                    } else if (lobby.size() == 1) {
                        isRunning = false;
                        decideWinner();
                        stop();
                    }
                }

                if (players.size() == 0) {
                    // tournament over
                    isRunning = false;
                    round = 0;
                    fun = false;
                    stop();
                }
            }
        }, 0, 0);
    }

    private static PkTournamentGame getGameCon(Player player) {
        if (!(player.getControlerManager().getControler() instanceof PkTournamentGame))
            return null;
        return (PkTournamentGame) player.getControlerManager().getControler();
    }

    private static PkTournamentLobby getLobbyCon(Player player) {
        if (!(player.getControlerManager().getControler() instanceof PkTournamentLobby))
            return null;
        return (PkTournamentLobby) player.getControlerManager().getControler();
    }

    private static void decideWinner() {
        Optional<Player> finalContenstant = getLobbyPlayers().stream().findFirst();
        Player winner = finalContenstant.orElse(null);

        if (winner != null) {
            if (round == 0) {
                // other participants quit
                winner.sendMessage("<col=ff0000>PK Tournament:</col> You won the tournament uncontested, therefore you are not rewarded.");
            } else {
                winner.sendMessage("<col=ff0000>PK Tournament:</col> You have won the <col=ff0000>" + type.getFormattedName() + " tournament</col>! Congratulations!");
                reward(winner, 0);
            }
            winner.useStairs(-1, new WorldTile(OUTSIDE, 5), 0, 2);
            winner.getInterfaceManager().removeOverlay(false);
        } else {
            System.err.println("Fatal error: PK Tournament concluded with no winner.");
        }


        players.stream().filter(Objects::nonNull).forEach(PkTournament::handleRemoval);
        players.clear();

        for (Player player : World.getPlayers()) {
            if (player.getControlerManager().getControler() instanceof PkTournamentSpectating)
                player.getControlerManager().forceStop();
        }
    }

    private static List<Player> getLobbyPlayers() {
        return players.stream().filter(Objects::nonNull)
                .filter(player -> getState(player) == State.LOBBY)
                .collect(Collectors.toList());
    }

    private static List<Player> getIngamePlayers() {
        return players.stream().filter(Objects::nonNull)
                .filter(player -> getState(player) == State.PLAYING)
                .collect(Collectors.toList());
    }

    private static void decideBrackets() {
        roundStarted = true;

        List<Player> toPair = getLobbyPlayers();
        Collections.shuffle(toPair);

        if (toPair.size() % 2 != 0) {
            // if the lobby size is odd (a player will be seeded) ensure
            // the player to be seeded is not the last seeded player
            if (toPair.get(toPair.size() - 1).getUsername().equals(lastSeededPlayer)) {
                if (reseedAttempts++ < 10) {
                    decideBrackets();
                    return;
                }
            }
            reseedAttempts = 0;
        }

        for (int i = 0; i < toPair.size() - 1; i += 2) {
            Player p1 = toPair.get(i);
            Player p2 = toPair.get(i + 1);
            p1.sendMessage("<col=ff0000>PK Tournament:</col> You have been paired against <col=ff0000>" + p2.getName() + "</col> for round " + (round + 1) + "!");
            p2.sendMessage("<col=ff0000>PK Tournament:</col> You have been paired against <col=ff0000>" + p1.getName() + "</col> for round " + (round + 1) + "!");

            p1.stopAll();
            p2.stopAll();

            if (type.disablePrayers) {
                p1.getPrayer().closeProtectionPrayers();
                p2.getPrayer().closeProtectionPrayers();
            }


            switchController(p1, "PkTournamentGame", p2);
            switchController(p2, "PkTournamentGame", p1);

            //MapInstance map = new MapInstance(472, 376);
            //map.load(() -> {
            WorldTile tile1 = new WorldTile(GAME_TILE_1.getX() + Utils.random(GAME_TILE_2.getX() - GAME_TILE_1.getX()), GAME_TILE_1.getY() + Utils.random(GAME_TILE_2.getY() - GAME_TILE_1.getY()), 0);
            WorldTile tile2 = new WorldTile(GAME_TILE_1.getX() + Utils.random(GAME_TILE_2.getX() - GAME_TILE_1.getX()), GAME_TILE_1.getY() + Utils.random(GAME_TILE_2.getY() - GAME_TILE_1.getY()), 0);

            p1.useStairs(-1, tile1, 0, 0, null);
            p1.getHintIconsManager().addHintIcon(p2, 1, -1, false);
            p2.useStairs(-1, tile2.transform(1, 0, 0), 0, 0, null);
            p2.getHintIconsManager().addHintIcon(p1, 1, -1, false);

            WorldTasksManager.schedule(new WorldTask() {
                int countdown = 5;

                @Override
                public void run() {

                    if (!(p1.getControlerManager().getControler() instanceof PkTournamentGame)
                            || !(p2.getControlerManager().getControler() instanceof PkTournamentGame)) {
                        stop();
                        return; //end already lols
                    }

                    if (countdown == 0) {
                        p1.forceTalk("Fight!");
                        p2.forceTalk("Fight!");
                        p2.setCanPvp(true);
                        p1.setCanPvp(true);
                        stop();
                    } else {
                        if (countdown == 5) {
                            p1.setNextFaceWorldTile(p2);
                            p2.setNextFaceWorldTile(p1);
                        }


                        p1.forceTalk("" + countdown + "...");
                        p2.forceTalk("" + countdown + "...");
                    }
                    countdown--;
                }
            }, 0, 2);

        }
        toPair = players.stream().filter(Objects::nonNull).filter(player -> getState(player) == State.LOBBY).collect(Collectors.toList());

        if (toPair.size() > 0) {
            // should be only 1 player if the remaining list size is odd
            for (Player player : toPair) {
                if (player != null) {
                    player.sendMessage("You have been seeded to the next round.");
                    lastSeededPlayer = player.getUsername();
                }
            }
        }
    }

    public static void switchController(Player player, String controller, Object... params) {
        player.getControlerManager().removeControlerWithoutCheck();
        player.getControlerManager().startControler(controller, params);
        player.setCanPvp(false);//controller.equals("PkTournamentGame"));
        player.setLastTarget(null);
        player.setCantTrade(true);//!controller.equals("PkTournamentSpectating"));
    }

    public static void switchController(Player player, String controller) {
        switchController(player, controller, new Object[0]);
    }

    public static void resetPlayer(Player player) {
        PkTournamentType.removeSetup(player);
    }

    public static void enterSpectate(Player player) {
        handleAdd(player);
        player.useStairs(-1, SPEC_TILES[Utils.random(SPEC_TILES.length)], 0, 2);
        PkTournament.switchController(player, "PkTournamentSpectating");
    }

    public static String getQuestTabString() {
        if (canJoin)
            return "<col=00ff00>Join Now!";
        if (isRunning)
            return "<col=ffff00>In Round " + (round + 1) + "!";


        long time  = Math.min(Math.min(Math.min(triggerTime, triggerTime2), triggerTime3), triggerTime4) - Utils.currentTimeMillis();
        //long time = Math.min(triggerTime, triggerTime2) - Utils.currentTimeMillis();
        return "<col=ffff00>" + Utils.formatTime(time);
    }

}