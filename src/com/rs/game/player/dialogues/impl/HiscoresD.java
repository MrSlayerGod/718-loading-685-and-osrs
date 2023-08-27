package com.rs.game.player.dialogues.impl;

import com.rs.Settings;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.BossKillsScore;
import com.rs.utils.BossTimerScore;
import com.rs.utils.DTRank;
import com.rs.utils.PkRank;
import com.rs.utils.TopDung;

public class HiscoresD extends Dialogue {

	private int page;
	
	@Override
	public void start() {
		if (page == 0)
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Top Donators", "Top Voters", "PVP", "Fastest Boss Kills", "More");
		else
			sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Boss Kills", "Dominion Tower", "Dungeoneering", "Skilling", "More");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		//end();
		if (page == 0) {
			if (componentId == OPTION_1) {
				end();
				player.getDialogueManager().startDialogue("DonatorHiscoresD");
			} else if (componentId == OPTION_2) {
				end();
				player.getDialogueManager().startDialogue("VoterHiscoresD");
			} else if (componentId == OPTION_3) {
				end();
				PkRank.showRanks(player);
			} else if (componentId == OPTION_4) {
				end();
				BossTimerScore.show(player);
			} else if (componentId == OPTION_5) {
				page = 1;
				start();
			}
		} else {
			if (componentId == OPTION_1) {
				end();
				BossKillsScore.show(player);
			} else if (componentId == OPTION_2) {
				end();
				DTRank.showRanks(player);
			} else if (componentId == OPTION_3) {
				end();
				TopDung.showRanks(player);
			} else if (componentId == OPTION_4) {
				end();
				player.getPackets().sendOpenURL(Settings.HIGHSCORES_LINK);
			} else if (componentId == OPTION_5) {
				page = 0;
				start();
			}
		}
		
	}

	@Override
	public void finish() {
		
	}
}
