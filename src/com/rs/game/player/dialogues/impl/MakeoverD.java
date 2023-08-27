package com.rs.game.player.dialogues.impl;

import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.dialogues.Dialogue;

public class MakeoverD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("What would you like to do?",
                "Change gender / skin color",
                "Change haircut",
                "Change clothing",
                "Change shoes",
                "Cancel");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        player.sendMessage("c "  +componentId);
        switch(componentId) {
            case OPTION_1:
                PlayerLook.openMageMakeOver(player);
                end();
                break;
            case OPTION_2:
                end();
                PlayerLook.openHairdresserSalon(player);
                break;
            case OPTION_3:
                end();
                PlayerLook.openThessaliasMakeOver(player);
                break;
            case OPTION_4:
                end();
                PlayerLook.openYrsaShop(player);
                break;
        }
    }

    @Override
    public void finish() {

    }

}