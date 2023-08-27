package com.rs.game.player.dialogues.impl;

import com.rs.game.player.controllers.FightCaves;
import com.rs.game.player.dialogues.Dialogue;

public class FightCavesEnter extends Dialogue {

    @Override
    public void start() {
    	sendOptionsDialogue("Enable Test Mode (No rewards, wave 62+ only)?", "Yes.", "No.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
    	end();
    	FightCaves.enter(player, componentId == OPTION_1);
    }

    @Override
    public void finish() {

    }

}
