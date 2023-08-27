package com.rs.game.player.content;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.npc.Drop;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class BrimstoneChest {
	
	private final static Drop REWARDS[] =
	{
	        new Drop(995, 500000, 1500000),
	        new Drop(1618, 25, 35),
	        new Drop(1620, 25, 35),
	        new Drop(454, 300, 500),
	        new Drop(445, 100, 200),
	        new Drop(11237, 50, 200),
	        new Drop(441, 350, 500),
	        new Drop(1164, 2, 4),
	        new Drop(1128, 1, 2),
	        new Drop(1080, 1, 2),
	        new Drop(360, 100, 350),
	        new Drop(378, 100, 350),
	        new Drop(372, 100, 300),
	        new Drop(7945, 100, 300),
	        new Drop(384, 100, 250),
	        new Drop(396, 80, 200),
	        new Drop(390, 80, 160),
	        new Drop(452, 10, 15),
	        new Drop(2354, 300, 500),
	        new Drop(1514, 120, 160),
	        new Drop(11232, 40, 160),
	        new Drop(5289, 2, 4),
	        new Drop(5316, 2, 3),
	        new Drop(5304, 3, 5),
	        new Drop(5300, 3, 5),
	        new Drop(5295, 3, 5),
	        new Drop(7937, 3000, 6000),
	        new Drop(4089, 1, 1),
	        new Drop(4091, 1, 1),
	        new Drop(4093, 1, 1),
	        new Drop(4095, 1, 1),
	        new Drop(4097, 1, 1),
	        new Drop(52731, 1, 1)
		//// Missing celastrus, Dragonfruit tree, and Redwood tree seeds.
	};
		


	public static void openChest(final Player player) {
		if (!player.getInventory().containsItem(53083, 1)) {
			player.getPackets().sendGameMessage("This chest is securely locked shut. You need a brimstone key to open it!");
			return;
		}
		player.getInventory().deleteItem(53083, 1);
		player.getVarsManager().sendVar(134662, 1);
		player.setNextAnimation(new Animation(536));
		player.lock(2);
		player.getPackets().sendGameMessage("You unlock the chest with your key.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
			//	WorldObject openedChest = new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
			//	World.spawnObjectTemporary(openedChest, 600, false, true);
				player.getVarsManager().sendVar(134662, 0);
				player.getPackets().sendGameMessage("You find some treasure in the chest!");
				Drop reward = REWARDS[Utils.random(REWARDS.length)];
				player.getInventory().addItemDrop(reward.getItemId(), reward.getMinAmount() + Utils.random(reward.getExtraAmount() + 1));
			}
		}, 0);
	}
}
