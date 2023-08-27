package com.rs.game.player.content;

import com.rs.discord.Bot;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.Calendar;

public class GamblerKing {
	
	public static boolean DISABLE = false;
	
	public static void gamble(NPC npc, Player player, Item item) {
		if (DISABLE) {
			player.getPackets().sendGameMessage("This content is currently disabled.");
			return;
		}
		Calendar previous = Calendar.getInstance();
		previous.setTimeInMillis(player.getLastGambleKing());
		Calendar current = Calendar.getInstance();
		if (previous.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "I spent all my mind power on my last gamble. Let's do it again tomorrow!");
			return;
		}
		player.faceEntity(npc);
		npc.faceEntity(player);
		final FriendsChat chat = player.getCurrentFriendsChat();
		if (chat == null) {
			player.getPackets().sendGameMessage("You need to be in a friends chat to use this option.");
			return;
		}
		if (!ItemConstants.isTradeable(item) || (item.getDefinitions().isStackable() && item.getId() != 995)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "You can not gamble this item against me.");
			return;
		}
		if (item.getId() == 995 && item.getAmount() < 5000000) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Your so cheap. I won't gamble under 5m coins.");
			return;
		}
		if (item.getId() == 995 && item.getAmount() > 1000000000) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "This is too much. I wont gamble over 1b coins.");
			return;
		}
		player.updateLastGambleKing();
		double chance = item.getId() != 995 ? 0.4 : item.getAmount() >= 500000000 ? 0.1 : item.getAmount() >= 250000000 ? 0.2 : item.getAmount() >= 100000000 ? 0.3 : item.getAmount() >= 10000000 ? 0.4 : 0.45; 
		boolean win = Math.random() <= chance;
		
		int rollKing = Utils.random(100) + 1; //rolls between 2 and 99
		if (win && rollKing == 100)
			rollKing = 99;
		else if (!win && rollKing == 1)
			rollKing = 2;
		int rollPlayer = win ? Utils.random(rollKing, 101) : Utils.random(1, rollKing);
		int rollKingF = rollKing; 
		player.lock();
		player.getInventory().deleteItem(item);
		npc.setNextAnimation(new Animation(11900));
		npc.setNextGraphics(new Graphics(2075));
		player.setNextAnimation(new Animation(11900));
		player.setNextGraphics(new Graphics(2075));
		player.getPackets().sendGameMessage("Rolling...", true);
		
		chat.sendLocalMessage(player, "Friends Chat channel-mate <col=db3535>" + player.getDisplayName() + "</col> gambling <col=db3535>"+item.getName()+" x "+Utils.getFormattedNumber(item.getAmount())+"</col> agaisnt the <col=db3535>Gambling King...");
		
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				chat.sendLocalMessage(player, "Friends Chat channel-mate <col=db3535>Dicing King</col> rolled <col=db3535>" + rollKingF + "</col> on the percentile die.");
				chat.sendLocalMessage(player, "Friends Chat channel-mate <col=db3535>" + player.getDisplayName() + "</col> rolled <col=db3535>" + rollPlayer + "</col> on the percentile die.");
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.unlock();
						if (win) {
							player.getPackets().sendGameMessage("<col=00FF00>You win! You are a legend!");
							player.getInventory().addItemDrop(item.getId(), item.getAmount() * 2);
							npc.setNextForceTalk(new ForceTalk("Omg! I hate you."));
						} else {
							player.getPackets().sendGameMessage("Better luck next time.");
							npc.setNextForceTalk(new ForceTalk("See you next time!"));
						}
						if ((item.getId() >= 1038 && item.getId() <= 1048) || item.getId() == 41862 || item.getId() == 25643)
							player.setGambledPartyhat(true);
					}
				}, 1);
			}
		}, 1);
	}
	
}
