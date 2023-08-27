package com.rs.game.player.content.dungeoneering.daily;

import java.util.Calendar;
import java.util.Random;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class DailyDungeon {
	
	public static long[] SEEDS = new long[5];
	public static int[] FLOORS = new int[5];
	
	private static long last = 0;
	private static Calendar calendar1 = Calendar.getInstance();
	private static Calendar calendar2 = Calendar.getInstance();
	private static Random random = new Random(0L);


	
	
	public static synchronized void setCompleted(Player player, int id) {
		player.setDungChallengeTime(id, last);
	}
	
	
	public static synchronized boolean hasCompleted(Player player, int id) {
		calendar1.setTimeInMillis(player.getDungChallengeTime(id));
		calendar2.setTimeInMillis(last);
		
		return (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
				calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
				calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH));
	}
	
	
	
	public static synchronized void checkForUpdates() {
		calendar1.setTimeInMillis(Utils.currentTimeMillis());
		calendar2.setTimeInMillis(last);
		
		if (calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR) ||
			calendar1.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH) ||
			calendar1.get(Calendar.DAY_OF_MONTH) != calendar2.get(Calendar.DAY_OF_MONTH)) {
			
			last = calendar1.getTimeInMillis();
			random.setSeed(calendar1.get(Calendar.YEAR) * (calendar1.get(Calendar.MONTH)+1) * (calendar1.get(Calendar.DAY_OF_MONTH)+1));
			
			for (int i = 0; i < 5; i++) {
				SEEDS[i] = 1000000+random.nextInt(1000000);
				FLOORS[i] = random.nextInt(60)+1;
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
}
