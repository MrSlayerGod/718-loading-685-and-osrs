/**
 *
 */
package com.rs.game.minigames;

import java.util.TimerTask;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.worldboss.CallusFrostborne;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.Summoning;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.controllers.NewPlayerController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Apr 20, 2018
 */
public class WorldBosses {

    public static NPC getBoss() {
        return boss;
    }

    private static NPC boss;
    private static long nextSpawnTime;

    public static final void init() {
        setSpawnTask();
    }

    public static boolean isBossAlive() {
        return boss != null && !boss.hasFinished() && !boss.isDead();
    }

    public static long getNextSpawnTime() {
        return nextSpawnTime - Utils.currentTimeMillis();
    }

    public static boolean[] disableBoss = new boolean[WorldBoss.values().length];

    public static void eventTeleport(Player player, WorldBoss b) {
        if(disableBoss[b.ordinal()]) {
            player.sendMessage(b.name() + " is disabled. please try again later.");
            return;
        }

        /*if(b == WorldBosses.WorldBoss.Callus) {
            if(!Magic.canTeleport(player, b.eventTile)) 
                return;
        	if (!player.getControlerManager().processMagicTeleport(b.eventTile))
				return;
            if (player.getFamiliar() != null || Summoning.hasPouch(player)) {
                player.getDialogueManager().startDialogue("SimpleMessage",
                        "You don't want your friends to be frozen by Callus, maybe dismiss them.");
                return;
            }
            WorldTasksManager.schedule(new WorldTask() {
                int cycle = 0;

                @Override
                public void run() {
                    switch (cycle ++) {
                        case 0:
                            player.anim(2417);
                            player.gfx(364);
                            player.sendMessage("A snow storm begin to form around you..");
                            break;
                        case 3:
                            player.anim(6939);
                            player.sendMessage("Callus' icy wind engulfs you.");
                            break;
                        case 4:
                            WorldTile tile = new WorldTile(b.eventTile.getX() + Utils.random(1),
                                    b.eventTile.getY() + Utils.random(3), 0);
                            player.setNextWorldTile(tile);
                            stop();
                            break;
                    }
                }
            }, 0, 1);
        } else {*/
            Magic.sendCommandTeleportSpell(player, b.eventTile);
       // }
    }

    public enum WorldBoss {
        Onyx("<img=2><col=cc33ff>Matrix boss just appeared! Type ::worldboss to get there!",
                15186, new WorldTile(2973, 2016, 0), new WorldTile(2975, 2000, 0)),
        Lucien("<img=2><col=cc33ff>Lucien boss just appeared! Type ::worldboss to get there!",
                14256, new WorldTile(1890, 4517, 2), new WorldTile(1891, 4522, 2));
      /*  Callus("<img=2><col=cc33ff>Callus the Frostborne has resurrected! Type ::worldboss to get there!",
                21212, new WorldTile(2399, 4069, 0), new WorldTile(2399, 4035, 0));*/

        String message;
        int id;

        WorldBoss(String message, int id, WorldTile worldTile, WorldTile eventTile) {
            this.id = id;
            this.message = message;
            this.worldTile = worldTile;
            this.eventTile = eventTile;
        }

        WorldTile worldTile, eventTile;

        public NPC spawn() {
            /*if(this == Callus) {
                for(Player player : World.getPlayers()) {
                    if(player.getRegionId() == 9535) {
                        player.sendMessage("<col=00ffff><shad=0>You are whisked from Callus' icy domain. Return at your peril.");
                        player.setNextWorldTile(3086, 3499);
                    }
                }
            }*/
            return World.spawnNPC(nextWorldBoss.id, nextWorldBoss.worldTile, -1, true, true);
        }
    }

    public static void login(Player player) {
        if (boss != null && !boss.hasFinished() && !(player.getControlerManager().getControler() instanceof NewPlayerController))
            player.getPackets().sendGameMessage(currentWorldBoss.message);
    }

    public static WorldBoss nextWorldBoss = WorldBoss.Onyx;
    public static WorldBoss currentWorldBoss = null;

    private static WorldBoss nextBoss() {
        WorldBoss[] bosses = WorldBoss.values();
        int len = bosses.length;
        int idx = nextWorldBoss.ordinal();
        int next = len-1 == idx ? 0 : idx+1;
        return  WorldBoss.values()[next];
    }

    public static void forceNext(int id) {
        if(boss != null) {
            if (boss instanceof CallusFrostborne) {
                ((CallusFrostborne) boss).endFight();
            }
            boss.finish();
        }
        //nextWorldBoss = WorldBoss.values()[id];
        spawnNextBoss();
    }

    public static final int SPAWN_TIMER_MS = 3_600_000 * 4;

    private static void setSpawnTask() {
        long timeLeft = (boss == null) ? !Settings.HOSTED ? 0 : SPAWN_TIMER_MS: SPAWN_TIMER_MS;

        timeLeft/=2; //halve the time, 2 bosses now

        nextSpawnTime = Utils.currentTimeMillis() + timeLeft;
        GameExecutorManager.fastExecutor.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    spawnNextBoss();
                    nextWorldBoss = nextBoss();
                    setSpawnTask();
                } catch (Throwable e) {
                    Logger.handle(e);
                }
            }

        }, timeLeft); //every 4h
    }

    private static void spawnNextBoss() {
        if (boss != null && !boss.hasFinished()) {
            boss.finish();
            WorldTasksManager.schedule(() -> spawnNextBoss());
            return;
        }
        World.sendNews(nextWorldBoss.message, 1);
        for (Player player : World.getPlayers()) {
            if (!player.hasStarted() || player.hasFinished())
                continue;
            player.getInterfaceManager().sendNotification("WARNING", nextWorldBoss.message);
        }
        currentWorldBoss = WorldBoss.values()[nextWorldBoss.ordinal()];
        boss = currentWorldBoss.spawn();
    }

}
