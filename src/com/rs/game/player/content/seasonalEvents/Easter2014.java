package com.rs.game.player.content.seasonalEvents;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.cutscenes.actions.CutsceneAction;

public class Easter2014 {
	
	private static final NPC EASTER_BUNNY_JR;
	private static final int[] REWARDS = {2185, 3, 7927, 1};
	
	static {
		EASTER_BUNNY_JR = new NPC(7410, new WorldTile(3086, 3495, 0), -1, false);
		EASTER_BUNNY_JR.setName("East-a Bunny JR");
	}
	
	public static class EasterCutScene extends Cutscene {

		@Override
		public boolean hiddenMinimap() {
			return false;
		}

		@Override
		public CutsceneAction[] getActions(Player player) {
			return null;
		}
	}
}
