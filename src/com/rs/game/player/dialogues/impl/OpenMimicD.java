package com.rs.game.player.dialogues.impl;

import com.rs.game.item.Item;
import com.rs.game.npc.others.Mimic;
import com.rs.game.player.dialogues.Dialogue;

public class OpenMimicD extends Dialogue {

    int slot = -1;

    @Override
    public void start() {
        slot = (int) parameters[0];
        sendOptionsDialogue("<col=ff0000>Casket will vanish after opening!</col><br>", "Start Mimic fight", "Cancel");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (slot > -1 && componentId == OPTION_1) {
            if(player.getInventory().getItems().get(slot).getId() == Mimic.MIMIC_CASKET) {
                Mimic.openCasket(player);
                player.getInventory().deleteItem(slot, new Item(Mimic.MIMIC_CASKET));
            }
        }
        end();
    }

    @Override
    public void finish() {

    }

}