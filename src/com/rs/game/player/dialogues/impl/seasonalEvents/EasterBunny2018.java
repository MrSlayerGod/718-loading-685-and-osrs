package com.rs.game.player.dialogues.impl.seasonalEvents;

import com.rs.cache.loaders.ItemConfig;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class EasterBunny2018 extends Dialogue {

	private int npcId = 13651;

	@Override
	public void start() {
		sendNPCDialogue(npcId, NORMAL, "Hey! I need eggs, eggs please! Bring me some and I will reward you.");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 0) {
			if (!player.getInventory().containsItem(1961, 1)) {
				sendPlayerDialogue(NORMAL, "Sorry, I don't have any easter eggs at the moment.");
				stage = -2;
			} else {
				stage = 1;
				sendPlayerDialogue(NORMAL, "Sure! Enjoy your easter weed, urgh eggs, bunny!");
			}
		} else if (stage == 1) {
			stage = 2;
			player.getInventory().deleteItem(1961, 1);
			int itemID = selectReward(player);
			if (itemID != 23713)
				World.sendNews(player, player.getDisplayName() + " has received " + ItemConfig.forID(itemID).getName() + " from easter bunny!", 1);
			else if (Utils.random(3) != 0)
				itemID = 1973;
			sendNPCDialogue(npcId, NORMAL, "Ah I feel so much better, thanks!!! Oh right please take these "+ItemConfig.forID(itemID).getName().toLowerCase()+" as a reward. Come back if you have more eggs.");
			player.getInventory().addItem(itemID, 1);
		} else if (stage == -2) {
			stage = -3;
			sendNPCDialogue(npcId, NORMAL, "You should be able to find some while playing the game. Please help me!");
		} else
			end();
	}
	
	private static final int[] REWARDS = {11019, 11020, 11021, 11022};

	private static int selectReward(Player player) {
		if (Utils.random(200) == 0 && !player.containsItem(1037))
			return 1037;
		if (Utils.random(100) == 0 && !player.containsItem(7927))
			return 7927;
		if (Utils.random(3) == 0) {
			for (int i : REWARDS) {
				if (!player.containsItem(i)) {
					return i;
				}
			}
		}
		return 23713;
		
	}
	@Override
	public void finish() {

	}
}
