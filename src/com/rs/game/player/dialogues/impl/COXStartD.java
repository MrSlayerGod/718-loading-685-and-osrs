package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.dialogues.Dialogue;

public class COXStartD extends Dialogue {
    @Override
    public void start() {
        sendOptionsDialogue("No-one may join the party after the raid begins.",
                "Begin the raid.", "Don't begin the raid yet.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        player.stopAll();
        if (componentId == OPTION_1) {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                raid.start(player);
            }
        }
    }

    @Override
    public void finish() {

    }

}