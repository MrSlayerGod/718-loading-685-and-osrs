package com.rs.game.minigames;

import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.NewPlayerController;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.TimerTask;

public class VoteWorldBoss {


    private static NPC boss;
    private static long nextSpawnTime;

    public static final void init() {
        setSpawnTask();
    }

    public static boolean isBossAlive() {
        return boss != null && !boss.hasFinished() && !boss.isDead();
    }

    public static long getNextSpawnTime() {
        return nextSpawnTime- Utils.currentTimeMillis();
    }

    public static void login(Player player) {
        if (boss != null && !boss.hasFinished() && !(player.getControlerManager().getControler() instanceof NewPlayerController))
            player.getPackets().sendGameMessage("<img=7><col=D80000><img=2>Giant mimic has appeared! Type ::voteboss");
    }

    private static void setSpawnTask() {
        long timeLeft = boss == null ? Utils.random(60000 * 5, 3600000 * 12) : Utils.random(3600000 * 2, 3600000 * 12);
        nextSpawnTime = Utils.currentTimeMillis() + timeLeft;
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (boss != null && !boss.hasFinished())
                        boss.finish();
                    World.sendNews("<img=2><col=cc33ff>Giant mimic has appeared! Type ::voteboss!", 1);

                    for (Player player : World.getPlayers()) {
                        if (!player.hasStarted() || player.hasFinished())
                            continue;
                        player.getInterfaceManager().sendNotification("WARNING", "Giant mimic has appeared! Type ::voteboss!");
                    }
                    boss = World.spawnNPC(21230, new WorldTile(1595, 4511, 0), -1, true, true);
                    setSpawnTask();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }

        }, timeLeft); //every 2-12h
    }
}
