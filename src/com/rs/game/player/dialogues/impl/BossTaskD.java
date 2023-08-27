package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class BossTaskD extends Dialogue {

	@Override
	public void start() {
		SlayerMaster master = (SlayerMaster) parameters[0];
		int npcId = master.getNPCId();
		npcId = 27690;//tzhaar npc
		if (player.getSlayerManager().getBossTask() != null && player.getSlayerManager().getBossTaskRemaining() > 0) {
			
			int remainingSkip = (player.getDonator()+1) - (player.getSlayerManager().getLastBossSkip()+1000*60*60*24 < Utils.currentTimeMillis() ? 0 : player.getSlayerManager().getBossSkipCount());
			if (remainingSkip > 0) {
				stage = 1;
				sendOptionsDialogue(Dialogue.DEFAULT_OPTIONS_TITLE, "Skip boss task (Remaining "+remainingSkip+")", "Cancel");
			}	else
				sendNPCDialogue(npcId, NORMAL, "You're still hunting " + Utils.formatPlayerNameForDisplay(player.getSlayerManager().getBossTask()) + "; come back when you've finished your task.");
			return;
		}
		if (player.getSkills().getCombatLevelWithSummoning() < master.getRequiredCombatLevel())
			sendNPCDialogue(npcId, 9827, "Your too weak overall, come back when you've become stronger.");
		else if (player.getSkills().getLevel(Skills.SLAYER) < master.getRequiredSlayerLevel()) 
			sendNPCDialogue(npcId, 9827, "Your Slayer level is too low to take on my challenges, come back when you have a level of at least " + master.getRequiredSlayerLevel() + " slayer.");
		else {
			player.getSlayerManager().setBossTask();
			sendNPCDialogue(npcId, 9827, "Your new boss task assignment is: " + Utils.formatPlayerNameForDisplay(player.getSlayerManager().getBossTask()) + "; only " + player.getSlayerManager().getBossTaskRemaining() + " more to go.");
		}
	}
	
	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1 && componentId == OPTION_1) {
			SlayerMaster master = (SlayerMaster) parameters[0];
			int npcId = master.getNPCId();
			npcId = 27690;
			if (player.getSkills().getCombatLevelWithSummoning() < master.getRequiredCombatLevel())
				sendNPCDialogue(npcId, 9827, "Your too weak overall, come back when you've become stronger.");
			else if (player.getSkills().getLevel(Skills.SLAYER) < master.getRequiredSlayerLevel()) 
				sendNPCDialogue(npcId, 9827, "Your Slayer level is too low to take on my challenges, come back when you have a level of at least " + master.getRequiredSlayerLevel() + " slayer.");
			else {
				boolean hardTaskSkip = player.getSlayerManager().isHardBossTask();
				player.getSlayerManager().setBossTask();
				sendNPCDialogue(npcId, 9827, "Your new boss task assignment is: " + Utils.formatPlayerNameForDisplay(player.getSlayerManager().getBossTask()) + "; only " + player.getSlayerManager().getBossTaskRemaining() + " more to go.");
				if (hardTaskSkip)
					return;
				if (player.getSlayerManager().getLastBossSkip()+1000*60*60*24 < Utils.currentTimeMillis()) {
					player.getSlayerManager().setLastBossSkip(Utils.currentTimeMillis());
					player.getSlayerManager().setBossSkipCount(1);
				} else
					player.getSlayerManager().setBossSkipCount(player.getSlayerManager().getBossSkipCount()+1);
			}
		} else
			end();
	}

	@Override
	public void finish() {

	}
}
