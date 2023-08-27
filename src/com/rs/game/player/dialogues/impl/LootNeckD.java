package com.rs.game.player.dialogues.impl;

import com.rs.game.player.dialogues.Dialogue;

public class LootNeckD extends Dialogue {
    @Override
    public void start() {
       // sendSelection();
        stage = 0;
        this.sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Auto Loot Function: "+(player.isDisableAutoLoot() ? "Disabled" : "Enabled")+")", "Switch Loot Settings");
    }


    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == 0) {
            if (componentId == OPTION_1) {
                player.switchAutoLoot();
                start();
            } else {
                stage = 1;
                sendSelection();
            }

        } else if (stage == 1) {
            if (componentId == OPTION_1)
                player.switchAlwaysAutoLoot();
            else if (componentId == OPTION_2)
                player.switchCommonAutoLoot();
            else if (componentId == OPTION_3)
                player.switchUncommonAutoLoot();
            else if (componentId == OPTION_4)
                player.switchRareAutoLoot();
            else
                player.switchVeryRareLoot();
            sendSelection();
        }
    }

    public void sendSelection() {

        this.sendOptionsDialogue(DEFAULT_OPTIONS_TITLE, "Always: (" + (player.isAlwaysAutoLootDisabled() ? "Disabled" : "Enabled")+")",
                "Common: (" + (player.isCommonAutoLootDisabled() ? "Disabled" : "Enabled")+")",
                "Uncommon: (" + (player.isUncommonAutoLootDisabled() ? "Disabled" : "Enabled")+")",
                "Rare: (" + (player.isRareAutoLootDisabled() ? "Disabled" : "Enabled")+")",
                "Very Rare: (" + (player.isVeryRareAutoLootDisabled() ? "Disabled" : "Enabled")+")");
    }

    @Override
    public void finish() {

    }
}
