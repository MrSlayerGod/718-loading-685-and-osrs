package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

/**
 * @author Simplex
 * @since May 13, 2020
 */
public class TheCollector extends Dialogue {

    private int npcId;
    public static final int COLLECTION_LOG = 52711;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        if(npcId == -1) {
            if(player.containsItem(COLLECTION_LOG)) {
                sendItemDialogue(COLLECTION_LOG, "You've got a collection log laying around somewhere.");
            } else {
                sendItemDialogue(COLLECTION_LOG, "The collector hands you a collection log.");
                player.getInventory().addItem(COLLECTION_LOG, 1);
            }
        } else {
            sendNPCDialogue(npcId, NORMAL, "It's beautiful, isn't it?");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(npcId == -1 || stage == 2 && player.containsItem(COLLECTION_LOG)) {
            end();
            return;
        }

        stage++;

        if (stage == 0)
            sendPlayerDialogue(NORMAL, "What is?");
        else if (stage == 1)
            sendNPCDialogue(npcId, NORMAL, "Everything! The wonders in the museum collected from all corners of the land.");
        else if (stage == 2)
            sendPlayerDialogue(NORMAL, "I guess you're right.");
        else if (stage == 3)
            sendNPCDialogue(npcId, NORMAL, "Matter of fact, I consider myself quite the collector. I keep a record of just about everything I find!");
        else if(stage == 4)
            if(player.getInventory().getFreeSlots() == 0) {
                sendNPCDialogue(npcId, NORMAL, "Free up some inventory space and I'll give you a collection log!");
                end();
            } else {
                sendNPCDialogue(npcId, NORMAL, "Would you like one? You could keep track of your own collections.");
            }
        else if(stage == 5)
            sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Yes, I would like one.", "No.");
        else if(stage == 6)
            if (componentId == OPTION_2) {
                end();
            } else {
                sendPlayerDialogue(NORMAL, "Yes, I'll take one.");
            }
        else if(stage == 7) {
            sendItemDialogue(COLLECTION_LOG, "The collector hands you a collection log.");
            player.getInventory().addItem(COLLECTION_LOG, 1);
        } else if(stage == 8)
            sendNPCDialogue(npcId, NORMAL, "There! Now you'll be able to see the true beauty of everything collect on your adventures.");
         else if(stage == 9)
            sendPlayerDialogue(NORMAL, "Thanks.");
         else if(stage == 10) {
            end();
        }
    }

    @Override
    public void finish() {

    }
}

