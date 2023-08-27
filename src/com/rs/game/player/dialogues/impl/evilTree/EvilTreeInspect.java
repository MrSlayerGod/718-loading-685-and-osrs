package com.rs.game.player.dialogues.impl.evilTree;

import com.rs.game.minigames.EvilTrees;
import com.rs.game.minigames.EvilTrees.TreeConfig;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

public class EvilTreeInspect extends Dialogue {


	@Override
	public void start() {
		TreeConfig config = EvilTrees.getConfig();
		if (config == null) {
			end();
			return;
		}
		stage = 0;
		sendDialogue("This is an "+Utils.formatPlayerNameForDisplay(config.name())+". A Woodcutting / Firemaking level of at least "+config.getLevel()+" is required to interact with this tree and the surrounding roots.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			sendDialogue("The tree currently has less than "+EvilTrees.getHealthPerc()+"% of its health remaining. Your help has earned you "+EvilTrees.getRewardPerc(player)+"% of your available rewards.");
			stage = -2;
			break;
		default:
			end();
			break;
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
