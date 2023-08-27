package com.rs.game.player.dialogues.impl;

import com.rs.game.item.Item;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.content.pet.LuckyPets;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.controllers.Inferno;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Colour;

import static com.rs.game.player.content.pet.LuckyPets.*;

public class ExchangeCapeD extends Dialogue {

	private SlayerMaster newMaster;

	private static final LuckyPet[] PETS = {
			LuckyPet.TZREK_JAD,
			LuckyPet.JAL_NIB_REK,
			LuckyPet.SHRIMPY,
	};

	private static final int[] CAPE_ID = {
			6570, Inferno.INFERNAL_CAPE, FightKiln.TOKHAAR_KAL
	};

	private boolean[] hasCape;

	private int npc;

	@Override
	public void start() {
		npc = (int) parameters[0];
		sendNPCDialogue(npc, NORMAL, "" +
						"I can exchange your Fire cape, Infernal cape or Tzhaar-kal for a chance at the respective pet.<br>",
				"Which cape would you like to gamble?");
		hasCape = new boolean[CAPE_ID.length];
		for(int i = 0; i < CAPE_ID.length; i++)
			hasCape[i] = has(CAPE_ID[i]);
		stage = 1;
	}

	private boolean has(int id) {
		return player.getInventory().containsItem(id, 1);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(stage == 1) {
			sendOptionsDialogue("Select a cape",
					!hasCape[0] ? Colour.STRIKE.wrap("Fire cape") : "Fire cape",
					!hasCape[1] ? Colour.STRIKE.wrap("Infernal cape") : "Infernal cape",
					!hasCape[2] ? Colour.STRIKE.wrap("Tokhaar-kal") : "Tokhaar-kal");
			stage = 2;
		} else if(stage == 2) {
			int index = componentId == OPTION_1 ? 0 : componentId == OPTION_2 ? 1 : componentId == OPTION_3 ? 2 : -1;
			if(index == -1) {
				end();
				return;
			}
			if(!hasCape[index]) {
				sendNPCDialogue(npc, NORMAL, "" +
								"You must have the cape in your inventory, JalYt.");
				stage = 3;
				return;
			}

			if(hasLuckyPet(player, PETS[index])) {
				sendNPCDialogue(npc, NORMAL, "" +
						"You already have " + new Item(PETS[index].getPet().getBabyItemId()).getName() + ", JalYt.");
				stage = 3;
				return;
			}

			player.getInventory().deleteItem(CAPE_ID[index], 1);
			boolean pet = false;

			if(componentId == OPTION_1) {
				// gamble fire cape
				pet = checkPet(player, LuckyPet.TZREK_JAD, "Fight Caves");
				player.increaseFireCapeGambles();
			} else if(componentId == OPTION_2) {
				pet = checkPet(player, LuckyPet.JAL_NIB_REK, "The Inferno");
				player.increaseInfernalCapeGambles();
			} else if(componentId == OPTION_3) {
				pet = checkPet(player, LuckyPet.SHRIMPY, "Fight Kiln");
				player.increaseKilnCapeGambles();
			}

			if(pet) {
				sendNPCDialogue(npc, NORMAL, "" +
						"You lucky, JalYt.");
			} else {
				sendNPCDialogue(npc, NORMAL, "" +
						"You not lucky. Maybe next time, JalYt.");
			}
			stage = 3;
		} else {
			end();
		}
	}

	@Override
	public void finish() {

	}
}
