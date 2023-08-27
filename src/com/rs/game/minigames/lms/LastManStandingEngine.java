package com.rs.game.minigames.lms;

import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Simplex
 * created on 2021-02-01
 *
 * Engine for the minigame Last Man Standing.
 */
public class LastManStandingEngine extends WorldTask implements Serializable {

    private transient List<WorldTile> finalArea = null;
    private transient int radius = 12;

    private static final long serialVersionUID = -5376539416426103608L;

    private void drawFinalAreaBounds() {
        if(Utils.currentWorldCycle() % 10 != 0)
            return;

        WorldTile fTile = game.getFinalAreaTile();
        if(finalArea == null) {
            finalArea = fTile.area(radius, wt -> wt.distance(fTile) == radius && Utils.rollDie(3));
        }

        finalArea/*.stream().filter(we -> we.distance(fTile) == 3)*/.forEach(worldTile -> {
            World.sendGraphics(2688, worldTile);
        });
    }

    @Override
    public void run() {
        //System.out.println("PROCESSING " + (game.isStructuredGame() ? "public" : "private") + " : " + game.getState());
        //if(game.getLobbyClock().remaining() > 60000)
        //    game.getLobbyClock().delayMS(20000);

        switch (game.getState()) {
            case LOBBY:
                processLobby();
                break;
            case RUNNING:
                drawFinalAreaBounds();
                fog();
                processGame();
                break;
            case FINISHED:
                // after the game finishes, stop the engine task if this was not a structured instance.
                if(!game.isStructuredGame()) {
                    stop();
                    LastManStanding.destroyPrivateInstance();
                    return;
                }

                processIntermission();
                break;
        }
    }

    private boolean calledFog = false;

    private void fog() {
        if(Utils.currentWorldCycle() % 4 != 0)
            return;

        if(game.getGameClock().remaining() < TimeUnit.MINUTES.toMillis(11)) {
            if(!calledFog) {
                calledFog = true;
                game.getPlayersInGame().stream().forEach(player -> {
                    player.sendMessage("<col=ff0000>Poisonous fog is beginning to move in!");
                    player.sendMessage("<col=ff0000>Move toward the Debtor's Hideout to avoid being poisoned by the fog!");
                });
            }
        }

        if(game.getGameClock().remaining() < TimeUnit.MINUTES.toMillis(10)) {
            game.getPlayersInGame().stream().filter(
                    player -> player.distance(game.getFinalAreaTile()) > radius)
                        .forEach(player -> {
                            player.sendMessage("<col=ff0000>The poisonous fog damages you!");
                            player.applyHit(null, Utils.random(20, 50), Hit.HitLook.POISON_DAMAGE);
                        });
        }
    }

    /**
     * Game which is being managed.
     */
    private transient LastManStanding game;

    /**
     * Construct a management class to oversee the game.
     */
    public LastManStandingEngine(LastManStanding game) {
        this.game = game;
    }

    /**
     * Between games, initiate lobby when force started or timer has elapsed.
     */
    private void processIntermission() {
        if(game.getIntermissionClock().finished()) {
            LastManStanding.clearPrivateGames();
            game.startStructuredGame(LastManStanding.DEFAULT_ENTRANCE_FEE);
        } else {
            // System.out.println("INTERMISSION " + Utils.formatTime(game.getIntermissionClock().remaining()));
        }
    }

    private transient int lastSecondCalled = -1;

    /**
     * Game running
     */
    private void processGame() {
        // process game countdown
        if(!game.getGameStartClock().finished()) {
            int s = game.getGameClock().remaining() > 0 ? game.getGameStartClock().remaining() / 1000 : 0;
            if(s <= 5 && s != lastSecondCalled) {
                lastSecondCalled = s;
                game.getPlayersInGame().forEach(player -> {
                    player.forceTalk(s + (s != 0 ? ".." : ""));
                    if(s != 0)
                        player.lock();
                    else
                        player.unlock();
                });
            }
            return;
        }

        // process stop condition
        if(game.getPlayersInGame().size() <= 1 || game.getGameClock().finished()) {
            // game over
            game.endGame();
        }
    }

    /**
     * Game initiated, waiting for players to join.
     * Start game when timer runs out or when 20 players have joined.
     */
    private void processLobby() {
        if(game.getLobbyClock().finished()) {
            game.startGame();
        } else {
            // System.out.println("LOBBY" + Utils.formatTime(game.getLobbyClock().remaining()));
        }
    }

}
