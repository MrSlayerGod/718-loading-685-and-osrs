package com.rs.game.player.dialogues.impl;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogues.Dialogue;

public class KBDArtifact extends Dialogue {

	@Override
	public void start() {
		if (player.hasEnteredKBD()) {
			end();
			BossInstanceHandler.enterInstance(player, Boss.King_Black_Dragon);
		} else
			player.getInterfaceManager().sendInterface(1361);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.stopAll();
		end();
		if (componentId == 13) {
			player.setEnteredKBD();
			player.setNextAnimation(new Animation(827));
			player.getPackets().sendGameMessage("You activate the artefact...", true);
			BossInstanceHandler.enterInstance(player, Boss.King_Black_Dragon);
			//Magic.pushLeverTeleport(player, new WorldTile(2273, 4681, 0), 827, "You activate the artefact...", "and teleport into the lair of the King Black Dragon!");
		}
	}

	@Override
	public void finish() {

	}

}
