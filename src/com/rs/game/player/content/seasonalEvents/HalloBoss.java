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
import com.rs.game.player.Player;
import com.rs.game.player.controllers.NewPlayerController;
import com.rs.net.decoders.handlers.NPCHandler;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Apr 20, 2018
 */
public class HalloBoss {
	
	private static NPC boss;
	private static long nextSpawnTime;
	public static final boolean ENABLED = false;

	public static final void init() {
		// hween pet dialog
		NPCHandler.register(21944, 1, ((player, npc) -> {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 14385,
					"You may have captured me as your pet, but... soon enough... you'll be mine! Aaahahaha!");
		}));
		if (!ENABLED)
			return;
		setSpawnTask();
	}
	
	public static boolean isBossAlive() {
		return boss != null && !boss.hasFinished() && !boss.isDead();
	}
	
	public static long getNextSpawnTime() {
		return nextSpawnTime-Utils.currentTimeMillis();
	}
	
	public static void login(Player player) {
		if (boss != null && !boss.hasFinished() && !(player.getControlerManager().getControler() instanceof NewPlayerController))
			player.getPackets().sendGameMessage("<img=7><col=D80000><img=2>Grim Reaper has appeared! Type ::hweenevent to get there!");
	}
	
	private static void setSpawnTask() {
		long timeLeft = boss == null ?
				!Settings.HOSTED ? 0 : Utils.random(60000 * 5) : Utils.random(3600000 * 2, 3600000 * 4);
		nextSpawnTime = Utils.currentTimeMillis() + timeLeft;
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if (boss != null && !boss.hasFinished()) 
						boss.finish();
					World.sendNews("<img=2><col=cc33ff>Grim Reaper just appeared! Type ::halloevent to get there!", 1);
					for (Player player : World.getPlayers()) {
						if (!player.hasStarted() || player.hasFinished())
							continue;
						player.getInterfaceManager().sendNotification("WARNING", "Grim Reaper just appeared!");
					}
					boss = World.spawnNPC(16031, new WorldTile(4094, 5236, 0), -1, true, true);
					setSpawnTask();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			
		}, timeLeft); //every 6-18h 
	}

    public static boolean forceSpawn() {
		if(isBossAlive())
			return false;
		if (boss != null && !boss.hasFinished())
			boss.finish();
		boss = World.spawnNPC(16031, new WorldTile(4094, 5236, 0), -1, true, true);
    	return true;
	}
}
