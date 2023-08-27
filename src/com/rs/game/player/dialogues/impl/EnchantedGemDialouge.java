package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class EnchantedGemDialouge extends Dialogue {

	private int npcId;

	@Override
	public void start() {
		npcId = (int) this.parameters[0];
		sendNPCDialogue(npcId, 9827, "'Ello and what are you after then?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			if (player.isDonator())
				sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "How many monsters do I have left?", "Give me a tip.", "View slayer kill log.", "Give me a new task.", player.isExtremeDonator() ? "Teleport to task." : "Nothing, Nevermind.");
			else
				sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "How many monsters do I have left?", "Give me a tip.", "View slayer kill log.", "Nothing, Nevermind.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				player.getSlayerManager().checkKillsLeft();
				end();
			} else if (componentId == OPTION_2) {
				stage = 1;
				if (player.getSlayerManager().getCurrentTask() == null) {
					sendNPCDialogue(npcId, 9827, "You currently don't have a task.");
					return;
				}
				String[] tipDialouges = player.getSlayerManager().getCurrentTask().getTips();
				if (tipDialouges != null && tipDialouges.length != 0) {
					String chosenDialouge = tipDialouges[Utils.random(tipDialouges.length)];
					if (chosenDialouge == null || chosenDialouge.equals(""))
						sendNPCDialogue(npcId, 9827, "I don't have any tips for you currently.");
					else
						sendNPCDialogue(npcId, 9827, chosenDialouge);
				} else
					sendNPCDialogue(npcId, 9827, "I don't have any tips for you currently.");
			} else if (componentId == OPTION_3) {
				end();
				NPCKillLog.sendSlayerLog(player);
			} else  if (componentId == OPTION_4 && player.isDonator()) {
				SlayerMaster master = player.getSlayerManager().getCurrentMaster();
				if (player.getSlayerManager().getCurrentTask() != null && player.getSlayerManager().getCount() > 0) {
					sendNPCDialogue(npcId, NORMAL, "You're still hunting " + player.getSlayerManager().getCurrentTask().getName() + "; come back when you've finished your task.");
				}else
				/*if (player.getSlayerManager().getCurrentMaster() == master && master != SlayerMaster.TURAEL && player.getSlayerManager().getCurrentTask() != null) {
					sendNPCDialogue(npcId, NORMAL, "You're still hunting " + player.getSlayerManager().getCurrentTask().getName() + "; come back when you've finished your task.");
				} else*/ if (player.getSkills().getCombatLevelWithSummoning() < master.getRequiredCombatLevel())
					sendNPCDialogue(npcId, 9827, "Your too weak overall, come back when you've become stronger.");
				else if (player.getSkills().getLevel(Skills.SLAYER) < master.getRequiredSlayerLevel()) {
					sendNPCDialogue(npcId, 9827, "Your Slayer level is too low to take on my challenges, come back when you have a level of at least " + master.getRequiredSlayerLevel() + " slayer.");
				} else {
					if (master == SlayerMaster.TURAEL && player.getSlayerManager().getCurrentTask() != null)
						player.getSlayerManager().skipCurrentTask(true);
					player.getSlayerManager().setCurrentTask(true, master);
					sendNPCDialogue(npcId, 9827, "Your new assignment is: " + player.getSlayerManager().getCurrentTask().getName() + "; only " + player.getSlayerManager().getCount() + " more to go.");
				}
				stage = 1;
			} else if (player.isExtremeDonator()) {
				stage = 1;
				if (player.getSlayerManager().getCurrentTask() == null) {
					sendNPCDialogue(npcId, 9827, "You currently don't have a task.");
					return;
				}
				if (player.getSlayerManager().getCurrentTask().getTile() == null) {
					sendNPCDialogue(npcId, 9827, "You can not teleport to this slayer task.");
					return;
				}
				Magic.sendCommandTeleportSpell(player, player.getSlayerManager().getCurrentTask().getTile());
			} else
				
				end();
		} else if (stage == 1) {
			end();
		}
	}

	@Override
	public void finish() {

	}
}
