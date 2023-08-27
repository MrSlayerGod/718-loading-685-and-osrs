package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Arham 4 on 2/7/2016.
 * <s>Stage 1: me against computer</s>
 * Stage 2: multiplayer
 * <p>
 * Current stage: 2
 * Improved by Dragonkk
 */
public class Gambling {


    /**
     * Is there any completely blank item?
     */
    private static final int BLANK_ITEM = 50000;
    public static final int INTERFACE = 9;

    private static final int PLAYER_SCORE_COMPONENT = 29, PLAYER_STATUS_COMPONENT = 30, OPPONENT_SCORE_COMPONENT = 31,
            OPPONENT_STATUS_COMPONENT = 32, DRAW_BUTTON_COMPONENT = 5, HOLD_BUTTON_COMPONENT = 6;

    /**
     * Why does RS do so many random numbers? Is there a better way to do this?
     */
    private static final int[] PLAYER_COMPONENTS = {7, 20, 23, 22, 26, 24, 27, 25, 21, 28};
    private static final int[] OPPONENT_COMPONENTS = {9, 17, 10, 15, 14, 16, 13, 12, 11, 18};

    //@formatter:off
    private Player player, opponent;
    private int playerScore, opponentScore;
    /**
     * Should I use a getter too? It seems unconventional to me to use getStatus() rather than status. It just doesn't
     * feel right.
     */
    private Status status;
    private int gambleAmount;
    //@formatter:on

    /**
     * This is too index-based, as the index of this determines the components.
     */
    private ArrayList<Rune> playerRunes, opponentRunes;
    /**
     * Easier to loop general stuff.
     * Heard from hc747 that I should learn to synchronize. Also, I should use null checks!
     */
    private Player[] players;

    public Gambling(Player player, Player opponent, int gambleAmount) {
        this.player = player;
        this.opponent = opponent;
        this.gambleAmount = gambleAmount;
        this.playerRunes = new ArrayList<>();
        this.opponentRunes = new ArrayList<>();
        this.players = new Player[]{player, opponent};
    }

    public static void start(Player player, Player target, int amount) {
        if (player.getInventory().getCoinsAmount() < amount) {
            player.sendMessage(player.getName() + " doesn't have enough money!");
            target.sendMessage(player.getName() + " doesn't have enough money!");
            return;
        }
        if (player.getInventory().getCoinsAmount() < amount) {
            player.sendMessage(player.getName() + " doesn't have enough money!");
            target.sendMessage(player.getName() + " doesn't have enough money!");
            return;
        }
        Gambling session = new Gambling(player, target, amount);
        player.setGamblingSession(session);
        target.setGamblingSession(session);
        session.initialize();
        player.setCantWalk(true);
        target.setCantWalk(true);
    }



    public void initialize() {
        build();
        setStatus(Utils.randomFrom(Status.PLAYER_TURN, Status.OPPONENT_TURN));
        updateStatus();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void handleButtons(Player player, int buttonId) {
        if (status == Status.PLAYER_TURN && player == this.getPlayer() || status == Status.OPPONENT_TURN && player == this.getOpponent()) {
            switch (buttonId) {
                case DRAW_BUTTON_COMPONENT:
                    draw();
                    break;
                case HOLD_BUTTON_COMPONENT:
                    if (isHoldButtonVisible(player)) {
                        hold();
                    }
                    break;
            }
        }
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    /**
     * If somebody has higher than the other player, then they can hold, but if its lower, then they can't.
     */
    private boolean isHoldButtonVisible(Player player) {
        return player == this.getPlayer() && getPlayerScore() >= getOpponentScore() || player == this.getOpponent() && getOpponentScore() >= getPlayerScore();
    }

    private void hold() {
        setStatus(getOtherStatus());
        updateStatus();
    }

    /**
     * This looks a little weird, but I need it to check for death runes before the victory check, because maybe
     * the last rune drawn is a death rune and that could cost the game if they are submitted as the winner
     * due to having more points, regardless of them drawing a death rune.
     * <p>
     * Don't know how to reduce this repetition.
     */
    private void draw() {
        Rune rune = Rune.getRandomRune(playerRunes.size() >= 5 && opponentRunes.size() >= 5); // prevent death rune for first 5 turns
        if (status == Status.PLAYER_TURN) {
            incrementPlayerScore(rune.value);
            playerRunes.add(rune);
        } else if (status == Status.OPPONENT_TURN) {
            incrementOpponentScore(rune.value);
            opponentRunes.add(rune);
        }
        updateScore();
        if (rune == Rune.DEATH) {
            finish();
            return;
        }
        if (isPossibilityForVictory()) {
            setWinnerStatus();
            finish();
            return;
        }
        if (status == Status.PLAYER_TURN) {
            setStatus(Status.OPPONENT_TURN);
            updateHoldButton(player);
        } else if (status == Status.OPPONENT_TURN) {
            setStatus(Status.PLAYER_TURN);
            updateHoldButton(opponent);
        }
        updateStatus();
    }

    private boolean isPossibilityForVictory() {
        return opponentRunes.size() == OPPONENT_COMPONENTS.length || playerRunes.size() == PLAYER_COMPONENTS.length;
    }

    private void setWinnerStatus() {
        if (playerScore > opponentScore) {
            setStatus(Status.PLAYER_WIN);
        } else if (playerScore < opponentScore) {
            setStatus(Status.OPPONENT_WIN);
        } else {
            setStatus(Status.DRAW);
        }
    }

    private void finish() {
        setWinner();
        updateStatus();
        sendEndText();
        rewardWinner();
        destroy();
        player.setGamblingSession(null);
    }

    private void setWinner() {
        switch (status) {
            case PLAYER_TURN:
                setStatus(Status.OPPONENT_WIN);
                break;
            case OPPONENT_TURN:
                setStatus(Status.PLAYER_WIN);
                break;
        }
    }

    private void rewardWinner() {
        switch (status) {
            case PLAYER_WIN:
                player.getInventory().addItemMoneyPouch(new Item(995, gambleAmount));
                opponent.getInventory().removeItemMoneyPouch(new Item(995, gambleAmount));
                player.getPackets().sendGameMessage("<col=ff0000>You won the gamble!</col>");
                opponent.getPackets().sendGameMessage("<col=ff0000>Oh no! You lost the gamble!</col>");
                break;
            case OPPONENT_WIN:
                opponent.getInventory().addItemMoneyPouch(new Item(995, gambleAmount));
                player.getInventory().removeItemMoneyPouch(new Item(995, gambleAmount));
                player.getPackets().sendGameMessage("<col=ff0000>Oh no! You lost the gamble!</col>");
                opponent.getPackets().sendGameMessage("<col=ff0000>You won the gamble!</col>");
                break;
            case DRAW:
                player.getPackets().sendGameMessage("<col=ff0000>The gambling match has resulted in a draw.</col>");
                opponent.getPackets().sendGameMessage("<col=ff0000>The gambling match has resulted in a draw.</col>");
                break;
        }
    }

    public void destroy() {
        for (Player player : players) {
            player.setGamblingSession(null);
            player.setCantWalk(false);
            player.closeInterfaces();
        }
    }

    public void end(Player abrupter) {
        for (Player player : players)
            player.getPackets().sendGameMessage("<col=ff0000>The gambling match has been abruptly ended!</col>");
        destroy();
        setQuitterAsLoser(abrupter);
        rewardWinner();
    }

    private void setQuitterAsLoser(Player abrupter) {
        if (opponent == abrupter) {
            setStatus(Status.PLAYER_WIN);
        } else if (player == abrupter) {
            setStatus(Status.OPPONENT_WIN);
        }
    }

    private void sendEndText() {
        for (Player player : players) {
            player.getPackets().sendIComponentText(INTERFACE, DRAW_BUTTON_COMPONENT, "");
            player.getPackets().sendIComponentText(INTERFACE, HOLD_BUTTON_COMPONENT, "");
        }
        if (status == Status.OPPONENT_WIN) {
            player.getPackets().sendIComponentText(INTERFACE, PLAYER_SCORE_COMPONENT, "DEATH");
            opponent.getPackets().sendIComponentText(INTERFACE, OPPONENT_SCORE_COMPONENT, "DEATH");
        } else if (status == Status.PLAYER_WIN) {
            player.getPackets().sendIComponentText(INTERFACE, OPPONENT_SCORE_COMPONENT, "DEATH");
            opponent.getPackets().sendIComponentText(INTERFACE, PLAYER_SCORE_COMPONENT, "DEATH");
        } else if (status == Status.DRAW) {
            for (Player player : players) {
                player.getPackets().sendIComponentText(INTERFACE, PLAYER_SCORE_COMPONENT, "DRAW");
                player.getPackets().sendIComponentText(INTERFACE, OPPONENT_SCORE_COMPONENT, "DRAW");
            }
        }
    }

    private void updateHoldButton(Player player) {
        if (isHoldButtonVisible(player)) {
            player.getPackets().sendIComponentText(INTERFACE, HOLD_BUTTON_COMPONENT, "Hold");
        } else {
            player.getPackets().sendIComponentText(INTERFACE, HOLD_BUTTON_COMPONENT, "");
        }
    }

    private void build() {
        for (Player player : players) {

            player.setCloseInterfacesEvent(new Runnable() {
                @Override
                public void run() {
                    if (player.getGamblingSession() != Gambling.this)
                        return;
                    end(player);
                }
            });


            for (int i = 7; i <= 28; i++) {
                if (i == 8 || i == 19) {
                    continue;
                }
                player.getPackets().sendItemOnIComponent(INTERFACE, i, BLANK_ITEM, 0);
            }
            player.getPackets().sendIComponentText(INTERFACE, 2, "Gambling");
            player.getPackets().sendIComponentText(INTERFACE, 3, "Your Score");
            player.getPackets().sendIComponentText(INTERFACE, PLAYER_SCORE_COMPONENT, "");
            player.getPackets().sendIComponentText(INTERFACE, PLAYER_STATUS_COMPONENT, "");
            player.getPackets().sendIComponentText(INTERFACE, OPPONENT_SCORE_COMPONENT, "");
            player.getPackets().sendIComponentText(INTERFACE, OPPONENT_STATUS_COMPONENT, "");
            player.getPackets().sendHideIComponent(INTERFACE, 33, true);
            player.getInterfaceManager().sendInterface(INTERFACE);
        }
        player.getPackets().sendIComponentText(INTERFACE, 4, opponent.getDisplayName() + "'s Score");
        opponent.getPackets().sendIComponentText(INTERFACE, 4, player.getDisplayName() + "'s Score");
    }

    private void updateScore() {
        if (status == Status.PLAYER_TURN) {
            for (int i = 0; i < playerRunes.size(); i++) {
                player.getPackets().sendItemOnIComponent(INTERFACE, PLAYER_COMPONENTS[i], playerRunes.get(i).id, 1);
                opponent.getPackets().sendItemOnIComponent(INTERFACE, OPPONENT_COMPONENTS[i], playerRunes.get(i).id, 1);
            }
            player.getPackets().sendIComponentText(INTERFACE, PLAYER_SCORE_COMPONENT, String.valueOf(getPlayerScore()));
            opponent.getPackets().sendIComponentText(INTERFACE, OPPONENT_SCORE_COMPONENT, String.valueOf(getPlayerScore()));
        } else if (status == Status.OPPONENT_TURN) {
            for (int i = 0; i < opponentRunes.size(); i++) {
                player.getPackets().sendItemOnIComponent(INTERFACE, OPPONENT_COMPONENTS[i], opponentRunes.get(i).id, 1);
                opponent.getPackets().sendItemOnIComponent(INTERFACE, PLAYER_COMPONENTS[i], opponentRunes.get(i).id, 1);
            }
            player.getPackets().sendIComponentText(INTERFACE, OPPONENT_SCORE_COMPONENT, String.valueOf(getOpponentScore()));
            opponent.getPackets().sendIComponentText(INTERFACE, PLAYER_SCORE_COMPONENT, String.valueOf(getOpponentScore()));
        }
    }

    private void updateStatus() {
        player.getPackets().sendIComponentText(INTERFACE, PLAYER_STATUS_COMPONENT, status.playerStatus);
        player.getPackets().sendIComponentText(INTERFACE, OPPONENT_STATUS_COMPONENT, status.opponentStatus);
        opponent.getPackets().sendIComponentText(INTERFACE, PLAYER_STATUS_COMPONENT, getOtherStatus().playerStatus);
        opponent.getPackets().sendIComponentText(INTERFACE, OPPONENT_STATUS_COMPONENT, getOtherStatus().opponentStatus);
    }

    /**
     * To keep this only or make a setter too? Don't really see a reason to tbh.
     */
    private void incrementPlayerScore(int increment) {
        playerScore = playerScore + increment;
    }

    /**
     * To keep this only or make a setter too? Don't really see a reason to tbh.
     */
    private void incrementOpponentScore(int increment) {
        opponentScore = opponentScore + increment;
    }

    /**
     * Is using the Magic class for the IDs too repetitive?
     */
    private enum Rune {
        AIR(Magic.AIR_RUNE, 1),
        MIND(Magic.MIND_RUNE, 2),
        WATER(Magic.WATER_RUNE, 3),
        EARTH(Magic.EARTH_RUNE, 4),
        FIRE(Magic.FIRE_RUNE, 5),
        BODY(Magic.BODY_RUNE, 6),
        COSMIC(Magic.COSMIC_RUNE, 7),
        CHAOS(Magic.CHAOS_RUNE, 8),
        NATURE(Magic.NATURE_RUNE, 9),
        DEATH(Magic.DEATH_RUNE, -1);

        private static final List<Rune> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = VALUES.size();

        //@formatter:off
        private int id;
        private int value;
        //@formatter:on

        Rune(int id, int value) {
            this.id = id;
            this.value = value;
        }

        public static Rune getRandomRune(boolean includeDeath) {
            Rune rune = VALUES.get(Utils.random(SIZE));
            if (rune == DEATH && !includeDeath) {
                return getRandomRune(includeDeath);
            }
            return rune;
        }
    }



    public enum Status {
        PLAYER_TURN("Your turn", ""),
        OPPONENT_TURN("", "Thinking..."),
        PLAYER_WIN("You win!", ""),
        OPPONENT_WIN("You lose!", ""),
        DRAW("Draw!", "Draw!");

        //@formatter:off
        String playerStatus;
        String opponentStatus;
        //@formatter:on

        Status(String playerStatus, String opponentStatus) {
            this.playerStatus = playerStatus;
            this.opponentStatus = opponentStatus;
        }
    }

    /**
     * For the opponent.
     */
    public Status getOtherStatus() {
        if (status == Status.PLAYER_TURN) {
            return Status.OPPONENT_TURN;
        } else if (status == Status.PLAYER_WIN) {
            return Status.OPPONENT_WIN;
        } else if (status == Status.OPPONENT_TURN) {
            return Status.PLAYER_TURN;
        } else if (status == Status.OPPONENT_WIN) {
            return Status.PLAYER_WIN;
        }
        return Status.DRAW;
    }
}
