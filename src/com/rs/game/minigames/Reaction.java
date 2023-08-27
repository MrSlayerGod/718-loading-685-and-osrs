/**
 * 
 */
package com.rs.game.minigames;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import com.rs.cache.loaders.ItemConfig;
import com.rs.executor.GameExecutorManager;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author dragonkk(Alex)
 * Jun 19, 2018
 */
public class Reaction {

	//make it so rewards dont repeat
	private static final int[] REWARDS = {6654, 6655, 6656, 6184, 6185, 6186, 6187, 6188, 6180, 6181, 6182, 3057, 3058, 3059, 3060, 3061, 7592,7593,7594,7595,7596};

	private static String text;
	private static long expireTime;
	private static final List<String> players = new LinkedList<String>();

	private static double int1 = 0, int2 = 0, int3 = 0;
	private static String operation1 = "", operation2 = "";
	private static String MULT = "multiplied by", DIV = "divided by", ADD = "plus", SUB = "minus";
	
	public static void init() {
		if (true)
			return;
		setReactionTask();
	}
	
	public static void start() {
		expireTime = Utils.currentTimeMillis() + (1000 * 60 * 5);
		players.clear();

		operation1 = Utils.rollPercent(50) ? ADD : SUB;
		operation2 = Utils.rollPercent(50) ? MULT : DIV;
		int1 = 1 + Utils.random(4);
		if(int1 %2 != 0) int1++;
		int2 = 4 + Utils.random(2);
		if(int2 %2 != 0) int2++;
		int3 = 1 + Utils.random(10);
		if(int3 %2 != 0) int3++;

		if(operation2.equals(DIV)) {
			int1 = 20 + Utils.random(10);
			if(int1 %2 != 0) int1++;
			int2 = 2;
			if(int2 %2 != 0) int2++;
		}

		double solve = operation2.equals(MULT) ? int1 * int2 : int1 / int2;

		if(operation1.equals(ADD))
			solve += int3;
		else solve -= int3;

		//text = generateString();
		text = "" + (int) solve;

		String quiz = "[Pop quiz] What is " + int1 + " " + operation2 + " " + int2 + " " + operation1 + " " + int3 + "? ::quiz #";
		World.sendNews(quiz, 1);
		for (Player player : World.getPlayers()) {
			if (!player.hasStarted() || player.hasFinished())
				continue;
			player.getInterfaceManager().sendNotification("EVENT", quiz);
		}
	}
	
	private static String generateString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	
	private static void setReactionTask() {
		GameExecutorManager.fastExecutor.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					start();
					setReactionTask();
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
			
		},  Utils.random(60000 * 15, 60000 * 105)); //every random 1hour
	}
	
	private static boolean expired() {
		return expireTime < Utils.currentTimeMillis();
	}
	
	public static void check(Player player, String message) {
		if (text == null || !text.equalsIgnoreCase(message)) {
			player.getPackets().sendGameMessage("Wrong answer!");
			return;
		}
		if (players.contains(player.getUsername())) {
			player.getPackets().sendGameMessage("You already claimed this reward.");
			return;
		}
		if (players.size() >= 3) {
			player.getPackets().sendGameMessage("Too many people already claimed this reward. Try to be faster the next time.");
			return;
		}
		if (expired()) {
			player.getPackets().sendGameMessage("Too late! this reaction already expired.");
			return;
		}
		players.add(player.getUsername());
		player.setWonReaction();
		int id = getReward(player);
		player.getBank().addItem(id, 1, false);
		player.getPackets().sendGameMessage("You have received " + ItemConfig.forID(id).getName() + " from reaction!");
		player.getPackets().sendGameMessage("Your rewards were added to bank.");
		World.sendNews(player, player.getDisplayName() + " won reaction!", 1);
	}
	

	private static int getReward(Player player) {
		/*List<Integer> pieces = new ArrayList<Integer>();
		for (int i : REWARDS)
			if (!player.containsItem(i))
				pieces.add(i);
		if (pieces.isEmpty())
			return 23713;
		return pieces.get(Utils.random(pieces.size()));*/
		return 23713;
	}
}
