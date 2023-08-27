package com.rs.game.player.dialogues.impl;

import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.dialogues.Dialogue;

public class EasterBunnyJrD extends Dialogue {

	@Override
	public void start() {
		if (player.getQuestManager().getQuestStage(Quests.EASTER_2014) == 0) {
			stage = 1;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			
		}
	}

	@Override
	public void finish() {
		
	}
}
