package com.rs.game.player.dialogues.impl;

import com.rs.game.player.controllers.Inferno;
import com.rs.game.player.dialogues.Dialogue;

public class InfernoEnter extends Dialogue {

    @Override
    public void start() {
    	sendOptionsDialogue("Enable Test Mode (No rewards, wave 66+ only)?", "Yes.", "No.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
    	end();
    	Inferno.enter(player, componentId == OPTION_1);
    }

    @Override
    public void finish() {

    }

}
