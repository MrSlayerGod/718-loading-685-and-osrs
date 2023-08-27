package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

/**
 * @author Simplex
 * @since Dec 19, 2020
 */

public class NPCMessage extends Dialogue {

    private int npcId;
    private int animation;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        animation = (Integer) parameters[1];
        String[] messages = new String[parameters.length - 2];
        for (int i = 0; i < messages.length; i++)
            messages[i] = (String) parameters[i + 2];
        sendNPCDialogue(npcId, animation, messages);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        end();
    }

    @Override
    public void finish() {

    }

}
