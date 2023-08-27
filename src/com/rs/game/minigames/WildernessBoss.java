/**
 * 
 */
package com.rs.game.minigames;

import java.util.TimerTask;

import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.NewPlayerController;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Apr 20, 2018
 */
public class WildernessBoss {
	
	private static NPC boss;
	private static Locations lastLocation;
	private static long nextSpawnTime;
	
	public static enum Locations {
		WILDERNESS_FROZEN_WASTE_PLATEAU(new WorldTile(2979, 3908, 0)),
		WILDERNESS_SCORPION_PIT(new WorldTile(3233, 3926, 0)), 
		WILDERNESS_NORTH_EAST_VOLCANO(new WorldTile(3349, 3927, 0)),
		WILDERNESS_SOUTH_VOLCANO(new WorldTile(3131, 3725, 0)),
		WILDERNESS_BLACK_CHINS(new WorldTile(3136, 3818, 0)),
		WILDERNESS_MAGE_ARENA(new WorldTile(3101, 3931, 0)),
		WILDERNESS_RUINS(new WorldTile(3220, 3730, 0)),
		WILDERNESS_LAVA_MAZE(new WorldTile(3010, 3844, 0)),
		WIlDERNESS_DEMONIC_RUINS(new WorldTile(3278, 3873, 0)),
		WILDERNESS_REVENANT_CAVES(new WorldTile(3087, 10127, 0));
		
		;
		
		private WorldTile tile; 
		
		private Locations(WorldTile tile) {
			this.tile = tile;
		}
	}
	
	
	
	//every 2-12h
	
	public static final void init() {
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
			player.getPackets().sendGameMessage("<img=7><col=D80000><img=2>Galvek has appeared in "+Utils.formatPlayerNameForDisplay(lastLocation.name())+"!");
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
					lastLocation = Locations.values()[Utils.random(Locations.values().length)];
					World.sendNews("<img=2><col=cc33ff>Galvek just appeared in "+Utils.formatPlayerNameForDisplay(lastLocation.name())+"!", 1);
					
					for (Player player : World.getPlayers()) {
						if (!player.hasStarted() || player.hasFinished())
							continue;
						player.getInterfaceManager().sendNotification("WARNING", "Galvek just appeared in "+Utils.formatPlayerNameForDisplay(lastLocation.name())+"!");
					}
					boss = World.spawnNPC(28097, lastLocation.tile, -1, true, true);
					setSpawnTask();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			
		}, timeLeft); //every 2-12h 
	}
	
}
