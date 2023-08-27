package com.rs.game.player.content.track;

import java.util.Date;
import java.util.TimerTask;

import com.rs.discord.Bot;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.utils.Logger;

public class TrackPC {

	@SuppressWarnings("deprecation")
	public static void setTask() {
		Date date = new Date();
		int timeMSLeft = (60 - date.getMinutes()) * 60000;
		
		
		 GameExecutorManager.fastExecutor.schedule(new TimerTask() {

	            @Override
	            public void run() {
	                try {
	                	Bot.sendLog(Bot.TRACK_PC, "[track=TRACK][count="+World.getPlayerCount()+"][wild="+World.getPlayersOnWildernessCount()+"]");
	                } catch (Throwable e) {
	                    Logger.handle(e);
	                }
	            }

	        }, timeMSLeft, 60000 * 60); //every 4h
		
	}
}
