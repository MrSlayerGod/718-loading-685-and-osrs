package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;
import com.rs.net.decoders.handlers.InventoryOptionsHandler;
import com.rs.utils.Utils;

public class HighValueWarning extends Dialogue {
    int slot = 0;
    @Override
    public void start() {
        slot = (int) parameters[0];
        sendOptionsDialogue("High value item", "Yes, drop item (worth " + Utils.getFormattedNumber((long) parameters[1]) + ")", "Cancel");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        player.stopAll();
        if (componentId == OPTION_1) {
            InventoryOptionsHandler.dropItem(player, slot, player.getInventory().getItem(slot));
        }
    }

    @Override
    public void finish() {

    }

}