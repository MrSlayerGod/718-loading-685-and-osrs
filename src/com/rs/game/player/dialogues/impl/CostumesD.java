package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.Costumes;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class CostumesD extends Dialogue {

	private int npcId;
	//private Costumes costume;
	
	@Override
	public void start() {
		npcId = (int) parameters[0];
	//	sendNPCDialogue(npcId, DRUNK, "Hello, would you like to buy one of my costumes?");
		sendNPCDialogue(npcId, DRUNK, "Hello, would you like to reset your keepsaked items?");
	/*	player.getEquipment().setCostume(null);
		player.getEquipment().setCostumeColor(0);*/
	}

	int option = 0;
	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(Dialogue.DEFAULT_OPTIONS_TITLE, "Reset keepsaked items.", "Nevermind.");
		}  else {
			if (componentId == OPTION_1) {
				player.getEquipment().getKeepsakeItems().reset();
				player.getAppearence().generateAppearenceData();
			}
			end();
		}
	/*	if (true == true) {
			end();
			return;
		}
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(Dialogue.DEFAULT_OPTIONS_TITLE, "Sure, show me them.", "I want to dye my costume.", "Reset my costume already.");
		}else if (stage == 0) {
			if(componentId == OPTION_2) {
				if(!player.isDonator()) {
					sendNPCDialogue(npcId, NORMAL, "Sorry, this is a donator feature.");
					stage = -2;
					return;
				}
				end();
				SkillCapeCustomizer.costumeColorCustomize(player);
			} else if(componentId == OPTION_3) {
				stage = -2;
				sendNPCDialogue(npcId, NORMAL, "Ok ok...");
				player.getEquipment().setCostume(null);
				player.getEquipment().setCostumeColor(0);
			}else{
				stage = 1;
				showOption();
			}
		}else if (stage == 1) {
			if(componentId == OPTION_5) {
				option = (option+3) % (Costumes.values().length-1);
				showOption();
			}else if(componentId == OPTION_4) {
				option = (option-3) % (Costumes.values().length-1);
				if(option < 0)
					option = 0;
				showOption();
			}else {
				int o = (option+(componentId == OPTION_1 ? 0 : componentId == OPTION_2 ? 1 : 2)) % (Costumes.values().length-1);
				costume = Costumes.values()[o];
				stage = 2;
				sendOptionsDialogue("Are you sure you want to buy "+ Utils.formatPlayerNameForDisplay(costume.name())+"?", "Yes.", "No.");
			}
		}else if(stage == 2) {
			if(componentId == OPTION_1) {
				if(player.getInventory().getCoinsAmount() < (player.isOnyxDonator() ? 50000 : 100000)) {
					player.getPackets().sendGameMessage("You do not have enough coins.");
					end();
					return;
				}
				player.getInventory().removeItemMoneyPouch(new Item(995, player.isOnyxDonator() ? 50000 : 100000));
				player.getEquipment().setCostume(costume);
				sendNPCDialogue(npcId, DRUNK, "You look better now hehe.");
				stage = -2;
			}else{
				sendNPCDialogue(npcId, DRUNK, "Maybe another time.");
				stage = -2;
			}
		}else  {
			end();
		}
		*/
	}
	
	public void showOption() {
		String[] options = new String[5];
		for(int i = 0; i < 3; i++) {
			int op = (option+i) % (Costumes.values().length-1);
			options[i] = Utils.formatPlayerNameForDisplay(Costumes.values()[op].name())+" ("+(player.isVIPDonator() ? 50 : 100)+"k)";
		}
		options[options.length-2] = "Before.";
		options[options.length-1] = "Next.";
		sendOptionsDialogue(Dialogue.DEFAULT_OPTIONS_TITLE, options);
	}

	@Override
	public void finish() {

	}
}
