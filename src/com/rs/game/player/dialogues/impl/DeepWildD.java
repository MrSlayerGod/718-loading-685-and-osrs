package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.teleportation.Teleport;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogues.Dialogue;

public class DeepWildD extends Dialogue {

    Teleport teleport = null;

    @Override
    public void start() {
        teleport = (Teleport) parameters[0];
        sendOptionsDialogue("Wilderness:<col=ff0000>" + teleport.name, "Teleport into the wilderness", "Cancel");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            Magic.sendCommandTeleportSpell(player, teleport.tile);
        } else {
            player.stopAll();
        }
    }

    @Override
    public void finish() {

    }

}