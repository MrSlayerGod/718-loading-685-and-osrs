package com.rs.game.player.dialogues.impl;

import com.rs.discord.Bot;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class DegradeItemOption extends Dialogue {

	int slotId;
	Item item;

	@Override
	public void start() {
		slotId = (Integer) parameters[0];
		item = (Item) parameters[1];
		player.getInterfaceManager().sendChatBoxInterface(1183);
		player.getPackets().sendIComponentText(1183, 7, item.getName());
		player.getPackets().sendIComponentText(1183, 12, "This item will degrade upon drop.");
		player.getPackets().sendItemOnIComponent(1183, 13, item.getId(), 1);
		player.getPackets().sendIComponentText(1183, 22, "Are you sure you want to drop this object?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (interfaceId == 1183 && componentId == 9) {
			player.getInventory().deleteItem(slotId, item);
			
			if (item.getId() == 42926 || item.getId() == 42931) {
				int scales = player.getCharges().getCharges(item.getId(), true) / 10;
				if (scales > 0)
					player.getInventory().addItem(42934, scales);
			}
			
			if (player.getCharges().degradeCompletly(item))
				return;
			if (player.getControlerManager().getControler() instanceof Wilderness && ItemConstants.isTradeable(item))
				World.addGroundItem(item, new WorldTile(player), player, false, -1);
			else
				World.addGroundItem(item, new WorldTile(player), player, true, 60);
			Logger.globalLog(player.getUsername(), player.getSession().getIP(),
					new String(" has dropped item [ id: " + item.getId() + ", amount: " + item.getAmount() + " ]."));
			player.getPackets().sendSound(2739, 0, 1);
			Bot.sendLog(Bot.PICKUP_DROP_CHANNEL, "[type=DROP][name="+player.getUsername()+"][item="+item.getName()+"("+item.getId()+")x"+Utils.getFormattedNumber(item.getAmount())+"]");
		}
		end();
	}

	@Override
	public void finish() {

	}

}
