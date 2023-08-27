package com.rs.game.player.dialogues.impl;

import com.rs.game.player.actions.JakindoFiremaking;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogues.Dialogue;

public class JakindoFireD extends Dialogue {



    @Override
    public void start() {
    	SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.SELECT, "How many roots do you want to add to the fire?", 28, new int[] {21350}, null, true);
    }

    @Override
    public void run(int interfaceId, int componentId) {
	int quantity = SkillsDialogue.getQuantity(player);
	player.getActionManager().setAction(new JakindoFiremaking(quantity));
	end();
    }

    @Override
    public void finish() {

    }

}
