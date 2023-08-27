package com.rs.game.player.dialogues.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.Slayer;
import com.rs.game.player.content.Slayer.SlayerMaster;
import com.rs.game.player.dialogues.Dialogue;

public class QuickTaskD extends Dialogue {

	private SlayerMaster newMaster;
	@Override
	public void start() {
		newMaster = (SlayerMaster) parameters[0];
		int npcId = newMaster.getNPCId();

		Slayer.SlayerTask currentTask = player.getSlayerManager().getCurrentTask();
		SlayerMaster currentMaster = player.getSlayerManager().getCurrentMaster();

		if(newMaster == SlayerMaster.TURAEL && currentMaster != SlayerMaster.TURAEL) {
			stage = 10;
			downgradeTask(npcId);
			return;
		}

		if (currentTask != null && player.getSlayerManager().getCount() > 0) {
			sendNPCDialogue(npcId, NORMAL, "You're still hunting " + currentTask.getName() + "; come back when you've finished your task.");
			return;
		}
		if (player.getSkills().getCombatLevelWithSummoning() < newMaster.getRequiredCombatLevel())
			sendNPCDialogue(npcId, 9827, "Your too weak overall, come back when you've become stronger.");
		else if (player.getSkills().getLevel(Skills.SLAYER) < newMaster.getRequiredSlayerLevel()) {
			sendNPCDialogue(npcId, 9827, "Your Slayer level is too low to take on my challenges, come back when you have a level of at least " + newMaster.getRequiredSlayerLevel() + " slayer.");
		} else if (newMaster == SlayerMaster.KRYSTILIA) {
			this.sendOptionsDialogue("<col=D80000>Warning: This task can only be completed in the wilderness. Do you want to continue?", "Yes.", "No.");
			stage = 1;
		} else {
			setTask();
		}
	}

	public void downgradeTask(int componentId) {
		if(newMaster == null) {
			// error from console
			System.out.println("Error new master is null");
			return;
		}

		switch(stage) {
			case 10:
				sendNPCDialogue(newMaster.getNPCId(), NORMAL, "You're hunting " + (player.getSlayerManager().getCurrentTask() == null ? "nothing" : player.getSlayerManager().getCurrentTask().getName() + "s;"),
						"Would you like an easier task? Your task streak will be reset.");
				stage = 11;
				break;
			case 11:
				sendOptionsDialogue("Reset task and streak?", "YES", "NO");
				stage = 12;
				break;
			case 12:
				if(componentId == Dialogue.OPTION_1)
					setTask();
				else end();
				stage = 13;
				break;
			case 13:
				end();
				break;
		}
	}
	
	public void setTask() {
		if (newMaster == SlayerMaster.TURAEL && player.getSlayerManager().getCurrentTask() != null)
			player.getSlayerManager().skipCurrentTask(true);
		player.getSlayerManager().setCurrentTask(true, newMaster);
		sendNPCDialogue(newMaster.getNPCId(), 9827, "Your new assignment is: " + player.getSlayerManager().getCurrentTask().getName() + "; only " + player.getSlayerManager().getCount() + " more to go.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(stage > 10)
			downgradeTask(componentId);
		else if (stage == 1 && componentId == Dialogue.OPTION_1)
			setTask();
		else
			end();
	}

	@Override
	public void finish() {

	}
}
