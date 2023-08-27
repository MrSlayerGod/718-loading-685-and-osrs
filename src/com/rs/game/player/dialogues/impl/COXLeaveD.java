package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.dialogues.Dialogue;

public class COXLeaveD extends Dialogue {
    @Override
    public void start() {
        sendOptionsDialogue("You will not be able to re-enter if you leave.",
                "Leave.", "Stay.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        player.stopAll();
        if (componentId == OPTION_1) {
            ChambersOfXeric raid = ChambersOfXeric.getRaid(player);
            if(raid != null) {
                raid.remove(player, ChambersOfXeric.LEAVE);
            }
        }
    }

    @Override
    public void finish() {

    }

}