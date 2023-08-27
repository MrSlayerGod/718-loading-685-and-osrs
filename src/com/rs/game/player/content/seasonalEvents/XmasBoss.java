/**
 * 
 */
package com.rs.game.player.content.seasonalEvents;

import java.util.TimerTask;

import com.rs.Settings;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.holiday.EvilSanta;
import com.rs.game.player.Player;
import com.rs.game.player.content.box.ChristmasBox;
import com.rs.game.player.controllers.NewPlayerController;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex) - Snowman
 * @author Simplex - Evil Santa
 */
public class XmasBoss {
	
	//private static NPC boss;
	private static NPC boss;
	private static long nextSpawnTime;
	public static final boolean ENABLED = false;
	
	public static final void init() {

		EvilSanta.init();
		ChristmasBox.init();

		if (ENABLED) {
			setSpawnTaskEvilSanta();
		}
	}
	
	public static boolean isBossAlive() {
		if (boss instanceof EvilSanta)
			return boss != null && !((EvilSanta)boss).isFightOver();
		return boss != null && !boss.hasFinished() && !boss.isDead();
	}
/*
	public static boolean isBossAlive() {
		return boss != null && !boss.isFightOver();
	}*/
	
	public static long getNextSpawnTime() {
		return nextSpawnTime-Utils.currentTimeMillis();
	}
	
	public static void login(Player player) {
		if (boss != null && !boss.hasFinished() && !(player.getControlerManager().getControler() instanceof NewPlayerController))
			if (snowman)
				player.getPackets().sendGameMessage("<img=7><col=D80000><img=2>Evil Snowman has appeared! Type ::xmasevent to get there!");
			else
				player.getPackets().sendGameMessage("<img=7><col=D80000><img=2>Evil Santa has appeared! Type ::xmasevent to get there!");
	}
	private static void setSpawnTaskEvilSanta() {
		long timeLeft = boss == null ? !Settings.HOSTED ? 0 :  Utils.random(60000 * 5) : Utils.random(3600000 * 2, 3600000 * 4);
		nextSpawnTime = Utils.currentTimeMillis() + timeLeft;
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					spawnBoss();
					setSpawnTaskEvilSanta();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}

		}, timeLeft); //every 6-18h
	}

	private static boolean snowman;
	private static void spawnBoss() {
		if (boss != null && !boss.hasFinished()) {
			WorldTasksManager.schedule(() -> {
				// allow boss 1 tick for boss to despawn
				spawnBoss();
			}, 2);
			boss.finish();
			return;
		}
		snowman = !snowman;
		if (snowman)
			World.sendNews("<img=2><col=cc33ff>Evil Snowman just appeared! Type ::xmasevent to get there!", 1);
		else
			World.sendNews("<img=2><col=cc33ff>Santa has appeared! Type ::xmasevent to collect your gift!", 1);
		for (Player player : World.getPlayers()) {
			if (!player.hasStarted() || player.hasFinished())
				continue;
			player.getInterfaceManager().sendNotification("WARNING", "Evil Santa just appeared!");
		}
		if (snowman)
			boss = World.spawnNPC(16032, new WorldTile(2727, 5734, 0), -1, true, true);
		else
			boss = (EvilSanta) World.spawnNPC(1552, new WorldTile(2728, 5735, 0), -1, true, true);

	}


	private static void setSpawnTaskSNOWMAN() {
		long timeLeft = boss == null ? !Settings.HOSTED ? 0 :  Utils.random(60000 * 5) : Utils.random(3600000 * 2, 3600000 * 4);
		nextSpawnTime = Utils.currentTimeMillis() + timeLeft;
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if (boss != null && !boss.hasFinished()) 
						boss.finish();
					World.sendNews("<img=2><col=cc33ff>Evil Snowman just appeared! Type ::xmasevent to get there!", 1);
					for (Player player : World.getPlayers()) {
						if (!player.hasStarted() || player.hasFinished())
							continue;
						player.getInterfaceManager().sendNotification("WARNING", "Evil Snowman just appeared!");
					}
					//boss = World.spawnNPC(16032, new WorldTile(2727, 5734, 0), -1, true, true);
					setSpawnTaskSNOWMAN();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			
		}, timeLeft); //every 6-18h 
	}

	public static void respawn() {
		spawnBoss();
	}

    public static EvilSanta getSanta() {
		return (EvilSanta) boss;
    }
}
