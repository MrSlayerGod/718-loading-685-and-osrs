package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.NPCKillLog;
import com.rs.game.player.controllers.ZulrahShrine;
import com.rs.game.player.dialogues.Dialogue;

public class ZulrahEnter extends Dialogue {

    @Override
    public void start() {
    	sendOptionsDialogue("Return to Zulrah's shrine?", "Yes (Normal Mode)", "Yes (Hard Mode)", "No.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
    	end();
    	if (componentId == OPTION_1) 
			ZulrahShrine.enterZulrahShrine(player, false);
        else if (componentId == OPTION_2) {
            if (NPCKillLog.getKilled(player, "Zulrah") < 100 && !player.isYoutuber() && player.getRights() != 2) {
                player.getPackets().sendGameMessage("You need to kill 100 Zulrah's (Normal Mode) in order to enable hard mode!");
                return;
            }
            ZulrahShrine.enterZulrahShrine(player, true);
        }
    }

    @Override
    public void finish() {

    }

}
