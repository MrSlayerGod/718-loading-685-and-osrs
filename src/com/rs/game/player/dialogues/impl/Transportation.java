package com.rs.game.player.dialogues.impl;

import java.util.Arrays;

import com.rs.game.item.Item;
import com.rs.game.player.content.ItemTransportation;
import com.rs.game.player.dialogues.Dialogue;

public class Transportation extends Dialogue {

	int page = 0;
	
	@Override
	public void start() {
		String[] locations = (String[]) parameters[0];
		if (locations.length > 5) {
			String[] options = Arrays.copyOf(locations, 5);
			options[4] = "Next";
			sendOptionsDialogue("Where would you like to teleport to", options);
			return;
		}
		sendOptionsDialogue("Where would you like to teleport to", locations);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		String[] locations = (String[]) parameters[0];
		if (locations.length > 5) {
			if (componentId == OPTION_5 && page == 0) {
				String[] options = new String[locations.length - 3];
				for (int i = 0; i < locations.length - 4; i++)
					options[i] = locations[4+i];
				options[options.length-1] = "Back";
				sendOptionsDialogue("Where would you like to teleport to", options);
				page = 1;
				return;
			} else if (page == 1) {
				int backOption = locations.length - 4;
				int option =  componentId == OPTION_1 ? 0 : componentId - 12;
				if (backOption == option) {
					String[] options = Arrays.copyOf(locations, 5);
					options[4] = "Next";
					sendOptionsDialogue("Where would you like to teleport to", options);
					page = 0;
					return;
				}
				ItemTransportation.sendTeleport(player, (Item) parameters[1], option + 4, (boolean) parameters[3], (locations[locations.length - 1].equals("Nowhere") ? locations.length - 1 : locations.length), (boolean) parameters[2]);
				end();
				return;
			}
		}
		
		
		ItemTransportation.sendTeleport(player, (Item) parameters[1], componentId == OPTION_1 ? 0 : componentId - 12, (boolean) parameters[3], (locations[locations.length - 1].equals("Nowhere") ? locations.length - 1 : locations.length), (boolean) parameters[2]);
		end();
	}

	@Override
	public void finish() {
	}
}
