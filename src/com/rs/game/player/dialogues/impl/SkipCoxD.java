package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.raids.cox.ChambersOfXeric;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.game.tasks.WorldTasksManager;

public class SkipCoxD extends Dialogue {

    private ChambersOfXeric raid;

    @Override
    public void start() {
        raid = (ChambersOfXeric) parameters[0];
        sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Continue Next Floor (Full Raid)", "Skip to Final Boss");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            if(raid.checkpoint(player) && player.getOsrsChambersCompletions() + player.getChambersCompletions() < 3) {
                WorldTasksManager.schedule(() -> {
                    player.getDialogueManager().startDialogue("SimpleMessage", "<col=ff0000>If you are having graphics issues - please re-log. Your position is saved and these issues will be fixed.");
                });
            }
            //if(!raid.checkpoint(player)) {
            player.useStairs(827, raid.getTile(144, 47, 2), 1, 2);
            raid.playMusic(player, 2);
            //}
        } else {
            player.useStairs(827, raid.getTile(128, 25, 0), 1, 2);
            raid.playMusic(player, 0);
            raid.checkpoint(player);
            WorldTasksManager.schedule(() -> {
                player.getDialogueManager().startDialogue("SimpleMessage", "<col=ff0000>If you are having graphics issues - please re-log. Your position is saved and these issues will be fixed.");
            });
        }
    }

    @Override
    public void finish() {

    }
}
