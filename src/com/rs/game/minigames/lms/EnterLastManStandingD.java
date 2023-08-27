package com.rs.game.minigames.lms;

import com.rs.Settings;
import com.rs.game.player.dialogues.Dialogue;
import com.rs.utils.Utils;

/**
 * @author Simplex
 * created on 2021-02-01
 */
public class EnterLastManStandingD extends Dialogue {
    String fee;

    @Override
    public void start() {
        if(Settings.LMS_DISABLED) {
            sendNPCDialogue(LastManStanding.LISA_NPC, SAD, "An admin has disabled Last Man Standing.");
            return;
        }

        if(player.getInventory().getItems().getFreeSlots() != player.getInventory().getItemsContainerSize()
                || player.getEquipment().getItems().getFreeSlots() != player.getEquipment().getItems().getSize()
                || player.getFamiliar() != null || player.getPet() != null) {
            sendNPCDialogue(LastManStanding.LISA_NPC, NORMAL, "You must bank all items, equipment and pets. There is a deposit box to the West.");
            stage = -1;
            return;
        }

        fee = Utils.getFormattedNumber(LastManStanding.entranceFee);
        if(LastManStanding.entranceFee != 0) {
            sendNPCDialogue(LastManStanding.LISA_NPC, HAPPY, "Hello, would you like to join Last Man Standing?", "The entrance fee is <col=ff0000>" + fee + " coins.");
        } else {
            sendNPCDialogue(LastManStanding.LISA_NPC, HAPPY, "Hello, would you like to join Last Man Standing?", "There is no fee to enter.");
        }
        stage = 1;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 1:
                String title = LastManStanding.entranceFee != 0 ? "Pay " + fee + " to join?" : "Join for free?";
                sendOptionsDialogue(title, "Join", "Nevermind");
                stage = 2;
                break;
            case 2:
                player.stopAll();
                if(componentId == OPTION_1) {
                    LastManStanding.enterLobby(player);
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}
