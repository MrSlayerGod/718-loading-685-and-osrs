package com.rs.game.minigames.lms;

/**
 * @author Simplex
 * created on 2021-02-01
 */
public enum LastManStandingState {
    /**
     * Players may enter while the lobby clock is counting down.
     */
    LOBBY,

    /**
     * The game is running, players are moved to the game field.
     */
    RUNNING,

    /**
     * All players have died except one, he should be moved out.
     */
    FINISHED
}
