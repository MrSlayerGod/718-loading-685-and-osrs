package com.rs.game.player.dialogues.impl;

import com.rs.game.minigames.pest.Lander;
import com.rs.game.minigames.pest.PestControl;
import com.rs.game.player.Player;
import com.rs.game.player.dialogues.Dialogue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PestControlEnterD extends Dialogue {

    private Lander lander;
    @Override
    public void start() {
        lander = Lander.getLanders()[(int) parameters[0]];
        if (player.getSkills().getCombatLevelWithSummoning() < lander.getLanderRequierment().getRequirement()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a combat level of " + lander.getLanderRequierment().getRequirement() + " or more to enter in boat.");
            return;
        } else if (player.getPet() != null || player.getFamiliar() != null) {
            player.getPackets().sendGameMessage("You can't take a follower into the lander, there isn't enough room!");
            end();
            return;
        }
        this.sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Enter Solo Mode", "Enter Group Mode", "Cancel");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            player.stopAll();
            final List<Player> playerList = new LinkedList<Player>();
            playerList.add(player);
            new PestControl(playerList, PestControl.PestData.valueOf(lander.getLanderRequierment().name()), true).create();
        } else if (componentId == OPTION_2) {
            lander.enterLander(player);
        }
        end();
    }

    @Override
    public void finish() {

    }
}
