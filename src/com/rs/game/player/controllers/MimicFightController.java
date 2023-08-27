package com.rs.game.player.controllers;

import com.rs.game.map.MapInstance;
import com.rs.game.npc.others.Mimic;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Simplex
 * @since Sep 13, 2020
 */
public class MimicFightController extends Controller {
    private Mimic boss;
    private MapInstance map;
    private boolean init = false;
    @Override
    public void start() {
        load();
    }

    private void load() {
        player.lock(3);
        final long time = FadingScreen.fade(player);
        player.stopAll();
        map = new MapInstance(449, 1185);
        map.load(() -> {
            boss = new Mimic(this);

            FadingScreen.unfade(player, time, () -> {
                player.setNextWorldTile(map.getTile(Mimic.TELE_TILE));
                player.setForceMultiArea(true);
                WorldTasksManager.schedule(() -> init = true, 2);
            });
        });
    }

    @Override
    public void process() {
        if(init && boss != null && !player.withinDistance(boss, 32)) {
            boss.finish();
            getMap().destroy(null);
            removeControler();
        }
    }

    public boolean isRunning() {
        return map != null && map.getStage() == MapInstance.Stages.RUNNING;
    }

    public MapInstance getMap() {
        return map;
    }
}
